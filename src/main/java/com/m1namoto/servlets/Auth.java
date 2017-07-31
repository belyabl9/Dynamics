package com.m1namoto.servlets;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.classifier.ClassificationResult;
import com.m1namoto.classifier.Classifier;
import com.m1namoto.classifier.Configuration;
import com.m1namoto.classifier.DynamicsInstance;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.Feature;
import com.m1namoto.domain.Session;
import com.m1namoto.domain.User;
import com.m1namoto.etc.AuthRequest;
import com.m1namoto.exception.NotEnoughCollectedStatException;
import com.m1namoto.service.FeatureExtractorService;
import com.m1namoto.service.*;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet implementation for passing authentication procedure using keystroke dynamics as an additional factor
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

    private static final FeatureService FEATURE_SERVICE = FeatureService.getInstance();

    private static class RequestParam {
        static final String LOGIN = "login";
        static final String PASSWORD = "password";
        static final String STAT = "stat";
        static final String SAVE_REQUESTS = "save_requests";
        static final String UPDATE_TEMPLATE = "update_template";
        static final String THRESHOLD = "threshold";
        static final String LEARNING_RATE = "learning_rate";
        static final String IS_STOLEN = "isStolen";
    }

    private static class ResponseParam {
        static final String SUCCESS = "success";
        static final String THRESHOLD = "threshold";
    }

    /**
     * Specifies the number of first authentications for which keystroke dynamics is not checked
     */
    private static final int TRUSTED_AUTHENTICATION_LIMIT = 5;

    /**
     * Specifies how similar predicted class has to be in range [0-1]. May be overridden
     */
    private static final double CLASS_PREDICTION_THRESHOLD = 0.8;


    private static class Message {
        static final String AUTHENTICATION_FAILED = "Authentication has failed";
        static final String AUTHENTICATION_PASSED = "Authentication has successfully passed";
        static final String DYNAMICS_DOES_NOT_MATCH = "Keystroke dynamics does not match";
        static final String CAN_NOT_CREATE_CLASSIFIER = "Can not create classifier";
        static final String CAN_NOT_CLASSIFY_INSTANCE = "Can not get class for instance";
        static final String SAVED_AUTH_REQ_PATH_NOT_SPECIFIED = "Path for saving authentication requests is not specified.";
    }


    private static final Gson GSON = new Gson();

    private static final FeatureExtractorService FEATURE_EXTRACTOR = FeatureExtractorService.getInstance();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Auth() {
        super();
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

    /**
     * Simplified authentication algorithm:
     * 1. Check credentials
     * 2. Check if this is an authentication for administrator access
     * 3. Check if account is new and limit of first trusted authentications is not reached
     * 4. Check that sent keystroke dynamics matches to stored biometric template for this user
     */
    private void authenticate(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException, ServletException {
        AuthContext context = new AuthContext(request);
        if (context.isSaveRequest()) {
            saveRequest(context);
        }

        logger.info(String.format("Authentication (%s, %s)", context.getLogin(), context.getPassword()));
        JSONObject responseObj = new JSONObject();
        if (Strings.isNullOrEmpty(context.getLogin()) || Strings.isNullOrEmpty(context.getPassword())) {
            logger.info(getResponseMessage(Message.AUTHENTICATION_FAILED, EMPTY_LOGIN_OR_PASSWORD));
            responseObj.put(ResponseParam.SUCCESS, false);
            out.print(responseObj);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, EMPTY_LOGIN_OR_PASSWORD);
            return;
        }

        Optional<User> userOpt = UserService.getInstance().findByLogin(context.getLogin());
        if (!userOpt.isPresent()) {
            processUnknownUser(response, out, responseObj);
            return;
        }
        User user = userOpt.get();
        context.setUser(user);

        if (!user.getPassword().equals(PasswordService.getInstance().makeHash(context.getPassword()))) {
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

        Type type = new TypeToken<List<Event>>(){}.getType();
        List<Event> events = GSON.fromJson(context.getStat(), type);
        context.setSessionEvents(events);

        // First <learningRate> authentication attempts are considered genuine
        boolean trustedAuthenticationsExpired = user.getAuthenticatedCnt() < context.getLearningRate();
        if (!trustedAuthenticationsExpired) {
            processTrustedAuth(response, out, responseObj, context);
            return;
        }

        // From this moment we need plain password, not a hash that we store in DB
        user.setPassword(context.getPassword());

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

        responseObj.put(ResponseParam.SUCCESS, false);
        responseObj.put(ResponseParam.THRESHOLD, classificationResult.getProbability());
        out.print(responseObj);
        logger.info(getResponseMessage(Message.AUTHENTICATION_FAILED, Message.DYNAMICS_DOES_NOT_MATCH));
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    private ClassificationResult getPredictedThreshold(@NotNull List<Event> events,
                                                       @NotNull User authUser) throws Exception {
        logger.info("Predict classes for passed keystroke dynamics.");
        Classifier classifier;
        try {
            classifier = makeClassifier(authUser);
        } catch (NotEnoughCollectedStatException e) {
            return new ClassificationResult(1d);
        }

        DynamicsInstance instance = new DynamicsInstance(FEATURE_EXTRACTOR.getFeatureValues(events, authUser));
        try {
            return classifier.getClassForInstance(instance, authUser.getId());
        } catch (Exception e) {
            logger.error(Message.CAN_NOT_CLASSIFY_INSTANCE, e);
            throw new RuntimeException(Message.CAN_NOT_CLASSIFY_INSTANCE, e);
        }
    }

    @NotNull
    private Classifier makeClassifier(@NotNull User user) throws NotEnoughCollectedStatException {
        Classifier classifier;
        try {
            // TODO cache configuration or/and classifier
            Configuration configuration = ConfigurationService.getInstance().create(user);
            classifier = new Classifier(configuration);
        } catch (NotEnoughCollectedStatException e) {
            throw e;
        } catch (Exception e) {
            logger.error(Message.CAN_NOT_CREATE_CLASSIFIER, e);
            throw new RuntimeException(Message.CAN_NOT_CREATE_CLASSIFIER, e);
        }
        return classifier;
    }

    private boolean isThresholdAccepted(@NotNull ClassificationResult classificationResult, double threshold) {
        boolean isThresholdAccepted = classificationResult.getProbability() >= threshold;
        if (!isThresholdAccepted) {
            logger.debug("Predicted probability is lower than can be accepted " + threshold);
        }
        return isThresholdAccepted;
    }

    /**
     * Saves a session with its features
     */
    @NotNull
    private Session saveSession(@NotNull List<Event> events, @NotNull User user) {
        Session session = new Session("GENERATED", user);
        session = SessionService.save(session);

        List<Feature> features = new ArrayList<>();
        features.addAll(FEATURE_EXTRACTOR.getHoldFeatures(events, user));
        features.addAll(FEATURE_EXTRACTOR.getReleasePressFeatures(events, user));
        for (Feature feature : features) {
            feature.setSession(session);
            FEATURE_SERVICE.save(feature);
        }
        if (!features.isEmpty()) {
            FEATURE_SERVICE.invalidateFeatureCache();
        }

        return session;
    }

    /**
     * Saves an authentication request to a file in .json format if corresponding property is set in he configuration file
     */
    private void saveRequest(@NotNull AuthContext context) throws IOException {
        AuthRequest authReq = new AuthRequest(context.getLogin(), context.getPassword(), context.getStat());

        String json = GSON.toJson(authReq);
        Optional<String> savedAuthReqPathOpt = PropertiesService.getStaticPropertyValue(SAVED_AUTH_REQUESTS_PATH_PROP);
        if (!savedAuthReqPathOpt.isPresent()) {
            throw new RuntimeException(Message.SAVED_AUTH_REQ_PATH_NOT_SPECIFIED);
        }
        String savedReqPath = savedAuthReqPathOpt.get() + "/" + context.getPassword().length();

        /* Should be used if app is deployed to OpenShift
           String savedReqPath = System.getenv(OPENSHIFT_DATA_DIR_VAR)
               + PropertiesService.getStaticPropertyValue(SAVED_AUTH_REQUESTS_PATH_PROP) + "/" + password.length();
        */

        String dirPath = String.join("/", new String[] { savedReqPath, context.getLogin(), (context.isStolen() ? STOLEN_DIR_PREFIX : OWN_DIR_PREFIX) });
        File ownershipDir = new File(dirPath);
        if (!ownershipDir.exists()) {
            ownershipDir.mkdirs();
        }

        File reqFile = new File(ownershipDir, REQ_FILE_PREFIX + LocalDateTime.now());
        FileUtils.writeStringToFile(reqFile, json);
    }

    private String getResponseMessage(@NotNull String status, @NotNull String msg) {
        return String.format("%s: %s", status, msg);
    }

    private void processExpectedClass(HttpServletResponse response, PrintWriter out, JSONObject responseObj, AuthContext context, ClassificationResult classificationResult) throws ServletException {
        logger.debug(Message.AUTHENTICATION_PASSED);
        if (!context.isStolen()) {
            saveSession(context.getSessionEvents(), context.getUser());
        }
        responseObj.put(ResponseParam.SUCCESS, true);
        responseObj.put(ResponseParam.THRESHOLD, classificationResult.getProbability());
        out.print(responseObj);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void processTrustedAuth(HttpServletResponse response, PrintWriter out, JSONObject responseObj, AuthContext context) throws ServletException {
        logger.info(Message.AUTHENTICATION_PASSED);
        saveSession(context.getSessionEvents(), context.getUser());
        responseObj.put(ResponseParam.SUCCESS, true);
        out.print(responseObj);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void processStatAbsence(HttpServletResponse response, PrintWriter out, JSONObject responseObj) {
        logger.info(getResponseMessage(Message.AUTHENTICATION_FAILED, DYNAMICS_NOT_PASSED));
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        responseObj.put(ResponseParam.SUCCESS, false);
        out.print(responseObj);
    }

    private void processAdminAccess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info(getResponseMessage(Message.AUTHENTICATION_PASSED, "Administrator"));
        request.getSession();
        response.sendRedirect("/");
    }

    private void processWrongPassword(HttpServletResponse response, PrintWriter out, JSONObject responseObj) {
        logger.info(getResponseMessage(Message.AUTHENTICATION_FAILED, WRONG_PASSWORD));
        responseObj.put(ResponseParam.SUCCESS, false);
        out.print(responseObj);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    private void processUnknownUser(HttpServletResponse response, PrintWriter out, JSONObject responseObj) {
        logger.info(getResponseMessage(Message.AUTHENTICATION_FAILED, CAN_NOT_FIND_USER));
        responseObj.put(ResponseParam.SUCCESS, false);
        out.print(responseObj);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    private static class AuthContext {
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
            login    = req.getParameter(RequestParam.LOGIN);
            password = req.getParameter(RequestParam.PASSWORD);
            stat     = req.getParameter(RequestParam.STAT);

            // used for test purposes only
            stolen = Boolean.parseBoolean(req.getParameter(RequestParam.IS_STOLEN));

            // TODO "Dynamic - Static - Default" strategy should be used
            saveRequest = Boolean.valueOf(PropertiesService.getDynamicPropertyValue(RequestParam.SAVE_REQUESTS).get());
            updateTemplate = Boolean.valueOf(PropertiesService.getDynamicPropertyValue(RequestParam.UPDATE_TEMPLATE).get());

            String thresholdStr = PropertiesService.getDynamicPropertyValue(RequestParam.THRESHOLD).get();
            threshold = Strings.isNullOrEmpty(thresholdStr) ? CLASS_PREDICTION_THRESHOLD : Double.valueOf(thresholdStr);

            String learningRateStr = PropertiesService.getDynamicPropertyValue(RequestParam.LEARNING_RATE).get();
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
