package com.m1namoto.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.m1namoto.features.FeatureExtractor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.classifier.ClassificationResult;
import com.m1namoto.classifier.Classifier;
import com.m1namoto.classifier.DynamicsInstance;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.Feature;
import com.m1namoto.domain.HoldFeature;
import com.m1namoto.domain.ReleasePressFeature;
import com.m1namoto.domain.ReleasePressPair;
import com.m1namoto.domain.Session;
import com.m1namoto.domain.User;
import com.m1namoto.etc.AuthRequest;
import com.m1namoto.service.FeatureService;
import com.m1namoto.service.SessionService;
import com.m1namoto.service.UserService;
import com.m1namoto.utils.PropertiesService;

/**
 * Servlet implementation class Auth
 */
@WebServlet("/auth")
public class Auth extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final static Logger logger = Logger.getLogger(Auth.class);

    private static final String REQ_FILE_PREFIX = "req-";
    private static final String OWN_DIR_PREFIX = "own";
    private static final String STOLEN_DIR_PREFIX = "stolen";

    private static final String OPENSHIFT_DATA_DIR_VAR = "OPENSHIFT_DATA_DIR";

    private static final String SAVED_AUTH_REQUESTS_PATH_PROP = "saved_auth_requests_path";


    private static final String DYNAMICS_NOT_PASSED = "Keystroke dynamics was not passed";
    private static final String WRONG_PASSWORD = "Wrong password";
    private static final String CAN_NOT_FIND_USER = "Can not find user";
    private static final String EMPTY_LOGIN_OR_PASSWORD = "Login or password is empty";

    private static final String REQ_LOGIN_PARAM = "login";
    private static final String REQ_PASSWORD_PARAM = "password";
    private static final String REQ_STAT_PARAM = "stat";
    
    private static final String  REQ_SAVE_REQUESTS_PARAM = "save_requests";
    private static final String  REQ_UPDATE_TEMPLATE_PARAM = "update_template";
    private static final String  REQ_THRESHOLD_PARAM = "threshold";
    private static final String  REQ_LEARNING_RATE_PARAM = "learning_rate";
    private static final String  REQ_IS_STOLEN_PARAM = "isStolen";

    private static final String RESP_SUCCESS_PARAM = "success";
    private static final String RESP_THRESHOLD_PARAM = "threshold";
    
    private static final int TRUSTED_AUTHENTICATION_LIMIT = 5;
    private static final double CLASS_PREDICTION_THRESHOLD = 0.8;

    private static final String AUTHENTICATION_FAILED = "Authentication failed";
    private static final String AUTHENTICATION_PASSED = "Authentication passed";
    
    private static final String DYNAMICS_DOES_NOT_MATCH = "Keystroke dynamics does not match";
    
    private static final String SUCCESSFUL_AUTH_MSG = "Authentication has successfully passed";
    private static final String CAN_NOT_CREATE_CLASSIFIER = "Can not create classifier";
    private static final String CAN_NOT_GET_CLASS_FOR_INSTANCE = "Can not get class for instance";

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Auth() {
        super();
    }

    private ClassificationResult getPredictedThreshold(@NotNull List<Event> events,
                                                       @NotNull User userToCheck) throws Exception {
        logger.info("Predict classes for passed sessions");
        Classifier classifier = makeClassifier(userToCheck);

        List<HoldFeature> sessionHoldFeatures = FeatureExtractor.getInstance().getHoldFeatures(events, userToCheck);
        List<ReleasePressFeature> sessionReleasePressFeatures = FeatureExtractor.getInstance().getReleasePressFeatures(events, userToCheck);

        List<Double> featureValues = new ArrayList<>();
        Map<Integer, List<HoldFeature>> holdFeaturesPerCode = FeatureService.extractHoldFeaturesPerCode(sessionHoldFeatures);
        char[] passwordCharacters = userToCheck.getPassword().toCharArray();  
        for (char c : passwordCharacters) {
            List<HoldFeature> featuresByCode = holdFeaturesPerCode.get((int)c);

            if (CollectionUtils.isNotEmpty(featuresByCode)) {
            	featureValues.add(featuresByCode.get(0).getValue());
            } else {
            	featureValues.add(null);
            }
        }

        Map<ReleasePressPair, List<ReleasePressFeature>> releasePressFeaturesPerCode = FeatureService.extractReleasePressFeaturesPerCode(sessionReleasePressFeatures);
        for (int i = 1; i < passwordCharacters.length; i++) {
            int releaseCode = passwordCharacters[i-1],
                pressCode = passwordCharacters[i];
            ReleasePressPair codePair = new ReleasePressPair(releaseCode, pressCode);
            List<ReleasePressFeature> featuresByCode = releasePressFeaturesPerCode.get(codePair);
            
            if (CollectionUtils.isNotEmpty(featuresByCode)) {
            	featureValues.add(featuresByCode.get(0).getValue());
            } else {
            	featureValues.add(null);
            }
        }

        double meanKeyPressTime = FeatureExtractor.getInstance().getMeanKeyPressTime(events);
        featureValues.add(meanKeyPressTime);
        
        DynamicsInstance instance = new DynamicsInstance(featureValues);
        try {
            return classifier.getClassForInstance(instance);
        } catch (Exception e) {
            logger.error(CAN_NOT_GET_CLASS_FOR_INSTANCE, e);
            throw new RuntimeException(CAN_NOT_GET_CLASS_FOR_INSTANCE, e);
        }
    }

    @NotNull
    private Classifier makeClassifier(@NotNull User userToCheck) {
        Classifier classifier;
        try {
            classifier = new Classifier(userToCheck);
        } catch (Exception e) {
            logger.error(CAN_NOT_CREATE_CLASSIFIER, e);
            throw new RuntimeException(CAN_NOT_CREATE_CLASSIFIER, e);
        }
        return classifier;
    }

    private boolean isThresholdAccepted(ClassificationResult classificationResult, double threshold) {
        boolean isThresholdAccepted = classificationResult.getProbability() >= threshold;
        if (!isThresholdAccepted) {
            logger.debug("Predicted probability is lower than can be accepted " + threshold);
        }
        return isThresholdAccepted;
    }

    /**
     * Saves a session with its features
     */
    private Session saveSession(@NotNull List<Event> events, @NotNull User user) {
        Session session = new Session("GENERATED", user);
        session = SessionService.save(session);

        List<HoldFeature> holdFeatures = FeatureExtractor.getInstance().getHoldFeatures(events, user);
        List<ReleasePressFeature> releasePressFeatures = FeatureExtractor.getInstance().getReleasePressFeatures(events, user);

        List<Feature> features = new ArrayList<>();
        features.addAll(holdFeatures);
        features.addAll(releasePressFeatures);
        for (Feature feature : features) {
            feature.setSession(session);
            FeatureService.save(feature);
        }

        return session;
    }

    /**
     * Saves an authentication request to a file in .json format if corresponding property is set in he configuration file
     */
    private void saveRequest(@NotNull AuthContext context) throws IOException {
        AuthRequest authReq = new AuthRequest(context.getLogin(), context.getPassword(), context.getStat());

        String json = new Gson().toJson(authReq);
        String savedReqPath = PropertiesService.getInstance().getStaticPropertyValue(SAVED_AUTH_REQUESTS_PATH_PROP) + "/" + context.getPassword().length();
        
        /* Should be used if app is deployed to OpenShift
           String savedReqPath = System.getenv(OPENSHIFT_DATA_DIR_VAR)
               + PropertiesService.getStaticPropertyValue(SAVED_AUTH_REQUESTS_PATH_PROP) + "/" + password.length();
        */

        String dirPath = String.join("/", new String[] { savedReqPath, context.getLogin(), (context.isStolen() ? STOLEN_DIR_PREFIX : OWN_DIR_PREFIX) });
        File ownershipDir = new File(dirPath);
        if (!ownershipDir.exists()) {
            ownershipDir.mkdirs();
        }

        File reqFile = new File(ownershipDir, REQ_FILE_PREFIX + new Date().getTime());
        FileUtils.writeStringToFile(reqFile, json);
    }

    private String getResponseMessage(@NotNull String status, @NotNull String msg) {
        return String.format("%s: %s", status, msg);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	response.setContentType("application/json");
    	response.setCharacterEncoding("utf8");
    	PrintWriter out = response.getWriter();

        authenticate(request, response, out);
    }

    private void authenticate(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException, ServletException {
        AuthContext context = new AuthContext(request);

        if (context.isSaveRequest()) {
            saveRequest(context);
        }

        logger.info(String.format("Authentication (%s, %s)", context.getLogin(), context.getPassword()));

        JSONObject responseObj = new JSONObject();

        if (Strings.isNullOrEmpty(context.getLogin()) || Strings.isNullOrEmpty(context.getPassword())) {
            logger.info(getResponseMessage(AUTHENTICATION_FAILED, EMPTY_LOGIN_OR_PASSWORD));
            responseObj.put(RESP_SUCCESS_PARAM, false);
            out.print(responseObj);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, EMPTY_LOGIN_OR_PASSWORD);
            return;
        }

        Optional<User> userOpt = UserService.findByLogin(context.getLogin());
        if (!userOpt.isPresent()) {
            processUnknownUser(response, out, responseObj);
            return;
        }
        User user = userOpt.get();
        context.setUser(user);

        if (!user.getPassword().equals(context.getPassword())) {
            processWrongPassword(response, out, responseObj);
            return;
        }

        if (user.getUserType() == User.Type.ADMIN) {
            processAdminAccess(request, response);
            return;
        }

        if (context.getStat() == null) {
            processStatAbsence(response, out, responseObj);
            return;
        }

        // TODO: Consider using only one session per request
        Type type = new TypeToken<List<Event>>(){}.getType();
        List<Event> events = new Gson().fromJson(context.getStat(), type);
        context.setSessionEvents(events);

        // First <learningRate> authentication attempts are considered genuine
        boolean trustedAuthenticationsExpired = user.getAuthenticatedCnt() < context.getLearningRate();
        if (!trustedAuthenticationsExpired) {
            processTrustedAuth(response, out, responseObj, context);
            return;
        }

        ClassificationResult classificationResult;
        try {
            classificationResult = getPredictedThreshold(events, user);
        } catch (Exception e) {
            logger.error(e);
            throw new ServletException(e);
        }

        boolean isExpectedClass = isThresholdAccepted(classificationResult, context.getThreshold());
        if (isExpectedClass) {
            processExpectedClass(response, out, responseObj, context, classificationResult);
            return;
        }

        responseObj.put(RESP_SUCCESS_PARAM, false);
        responseObj.put(RESP_THRESHOLD_PARAM, classificationResult.getProbability());
        out.print(responseObj);
        logger.info(getResponseMessage(AUTHENTICATION_FAILED, DYNAMICS_DOES_NOT_MATCH));
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    private void processExpectedClass(HttpServletResponse response, PrintWriter out, JSONObject responseObj, AuthContext context, ClassificationResult classificationResult) throws ServletException {
        logger.debug(SUCCESSFUL_AUTH_MSG);
        if (!context.isStolen()) {
            saveSession(context.getSessionEvents(), context.getUser());
        }
        responseObj.put(RESP_SUCCESS_PARAM, true);
        responseObj.put(RESP_THRESHOLD_PARAM, classificationResult.getProbability());
        out.print(responseObj);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void processTrustedAuth(HttpServletResponse response, PrintWriter out, JSONObject responseObj, AuthContext context) throws ServletException {
        logger.info(SUCCESSFUL_AUTH_MSG);
        saveSession(context.getSessionEvents(), context.getUser());
        responseObj.put(RESP_SUCCESS_PARAM, true);
        out.print(responseObj);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void processStatAbsence(HttpServletResponse response, PrintWriter out, JSONObject responseObj) {
        logger.info(getResponseMessage(AUTHENTICATION_FAILED, DYNAMICS_NOT_PASSED));
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        responseObj.put(RESP_SUCCESS_PARAM, false);
        out.print(responseObj);
    }

    private void processAdminAccess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info(getResponseMessage(AUTHENTICATION_PASSED, "Administrator"));
        request.getSession();
        response.sendRedirect("/");
    }

    private void processWrongPassword(HttpServletResponse response, PrintWriter out, JSONObject responseObj) {
        logger.info(getResponseMessage(AUTHENTICATION_FAILED, WRONG_PASSWORD));
        responseObj.put(RESP_SUCCESS_PARAM, false);
        out.print(responseObj);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    private void processUnknownUser(HttpServletResponse response, PrintWriter out, JSONObject responseObj) {
        logger.info(getResponseMessage(AUTHENTICATION_FAILED, CAN_NOT_FIND_USER));
        responseObj.put(RESP_SUCCESS_PARAM, false);
        out.print(responseObj);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    class AuthContext {
        private final String login;
        private final String password;
        private final String stat;
        private final boolean stolen;
        private final boolean saveRequest;
        private final boolean updateTemplate;
        private final int learningRate;
        private final double threshold;

        private User user;
        private List<Event> sessionEvents;

        public AuthContext(@NotNull HttpServletRequest req) {
            login    = req.getParameter(REQ_LOGIN_PARAM);
            password = req.getParameter(REQ_PASSWORD_PARAM);
            stat     = req.getParameter(REQ_STAT_PARAM);

            // used for test purposes only
            stolen = Boolean.parseBoolean(req.getParameter(REQ_IS_STOLEN_PARAM));

            saveRequest = Boolean.valueOf(PropertiesService.getInstance().getDynamicPropertyValue(REQ_SAVE_REQUESTS_PARAM));
            updateTemplate = Boolean.valueOf(PropertiesService.getInstance().getDynamicPropertyValue(REQ_UPDATE_TEMPLATE_PARAM));

            String thresholdStr = PropertiesService.getInstance().getDynamicPropertyValue(REQ_THRESHOLD_PARAM);
            threshold = Strings.isNullOrEmpty(thresholdStr) ? CLASS_PREDICTION_THRESHOLD : Double.valueOf(thresholdStr);

            String learningRateStr = PropertiesService.getInstance().getDynamicPropertyValue(REQ_LEARNING_RATE_PARAM);
            learningRate = Strings.isNullOrEmpty(learningRateStr) ? TRUSTED_AUTHENTICATION_LIMIT : Integer.valueOf(learningRateStr);
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }

        public String getStat() {
            return stat;
        }

        public boolean isStolen() {
            return stolen;
        }

        public boolean isSaveRequest() {
            return saveRequest;
        }

        public boolean isUpdateTemplate() {
            return updateTemplate;
        }

        public int getLearningRate() {
            return learningRate;
        }

        public double getThreshold() {
            return threshold;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public List<Event> getSessionEvents() {
            return sessionEvents;
        }

        public void setSessionEvents(List<Event> sessionEvents) {
            this.sessionEvents = sessionEvents;
        }
    }

}
