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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
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
import com.m1namoto.domain.XFeature;
import com.m1namoto.domain.YFeature;
import com.m1namoto.etc.AuthRequest;
import com.m1namoto.service.EventsService;
import com.m1namoto.service.FeaturesService;
import com.m1namoto.service.SessionsService;
import com.m1namoto.service.UsersService;
import com.m1namoto.utils.PropertiesService;

/**
 * Servlet implementation class Auth
 */
@WebServlet("/auth")
public class Auth extends HttpServlet {
    private static final String REQ_FILE_PREFIX = "req-";

    private static final String OWN_DIR_PREFIX = "own";

    private static final String STOLEN_DIR_PREFIX = "stolen";

    private static final String OPENSHIFT_DATA_DIR_VAR = "OPENSHIFT_DATA_DIR";

    private static final String SAVED_AUTH_REQUESTS_PATH_PROP = "saved_auth_requests_path";

    private final static Logger logger = Logger.getLogger(Auth.class);

    private static final long serialVersionUID = 1L;

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
    
    private static final String SUCCESSFUL_AUTH_MSG = "Authentication has successfuly passed";

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Auth() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().append("Served at: ").append(request.getContextPath());
    }

    private ClassificationResult getPredictedThreshold(List<Session> sessions, User userToCheck) throws Exception {
        logger.info("Predict classes for passed sessions");
        ClassificationResult predictedClass = null;
        Classifier classifier = null;
        try {
            classifier = new Classifier(userToCheck);
        } catch (Exception e) {
            logger.error("Can not create classifier", e);
        }

        List<HoldFeature> sessionHoldFeatures = new ArrayList<HoldFeature>();
        List<ReleasePressFeature> sessionReleasePressFeatures = new ArrayList<ReleasePressFeature>();
        List<XFeature> sessionXFeatures = new ArrayList<XFeature>();
        List<YFeature> sessionYFeatures = new ArrayList<YFeature>();

        for (Session session : sessions) {
            sessionHoldFeatures.addAll(session.getHoldFeaturesFromEvents());
            sessionReleasePressFeatures.addAll(session.getReleasePressFeaturesFromEvents());
            if (FeaturesService.includeMobileFeatures()) {
                sessionXFeatures.addAll(session.getXFeaturesFromEvents());
                sessionYFeatures.addAll(session.getYFeaturesFromEvents());
            }
        }

        List<Double> featureValues = new ArrayList<Double>();

        Map<Integer, List<HoldFeature>> holdFeaturesPerCode = FeaturesService.getHoldFeaturesPerCode(sessionHoldFeatures);
        char[] passwordCharacters = userToCheck.getPassword().toCharArray();  
        for (char c : passwordCharacters) {
            List<HoldFeature> featuresByCode = holdFeaturesPerCode.get((int)c);

            if (featuresByCode != null && featuresByCode.size() > 0) {
            	featureValues.add(featuresByCode.get(0).getValue());
            } else {
            	featureValues.add(null);
            }
        }

        Map<ReleasePressPair, List<ReleasePressFeature>> releasePressFeaturesPerCode = FeaturesService.getReleasePressFeaturesPerCode(sessionReleasePressFeatures);
        for (int i = 1; i < passwordCharacters.length; i++) {
            int releaseCode = passwordCharacters[i-1],
                pressCode = passwordCharacters[i];
            ReleasePressPair codePair = new ReleasePressPair(releaseCode, pressCode);
            List<ReleasePressFeature> featuresByCode = releasePressFeaturesPerCode.get(codePair);
            
            if (featuresByCode != null && featuresByCode.size() > 0) {
            	featureValues.add(featuresByCode.get(0).getValue());
            } else {
            	featureValues.add(null);
            }
        }

        if (FeaturesService.includeMobileFeatures()) {
            Map<Integer, List<XFeature>> xFeaturesPerCode = FeaturesService.getXFeaturesPerCode(sessionXFeatures);
            for (char c : passwordCharacters) {
                List<XFeature> featuresByCode = xFeaturesPerCode.get((int)c);
                featureValues.add(featuresByCode.get(0).getValue());
            }
            
            Map<Integer, List<YFeature>> yFeaturesPerCode = FeaturesService.getYFeaturesPerCode(sessionYFeatures);
            for (char c : passwordCharacters) {
                List<YFeature> featuresByCode = yFeaturesPerCode.get((int)c);
                featureValues.add(featuresByCode.get(0).getValue());
            }
        }

        double meanKeyPressTimeSum = 0;
        for (Session session : sessions) {
            List<Event> events = session.getEvents();
            meanKeyPressTimeSum += FeaturesService.getMeanKeyPressTime(events);
        }
        double meanKeyPressTime = sessions.size() == 0 ? 0 : meanKeyPressTimeSum / sessions.size();
        featureValues.add(meanKeyPressTime);

        DynamicsInstance instance = new DynamicsInstance(featureValues);
        try {
            predictedClass = classifier.getClassForInstance(instance);
        } catch (Exception e) {
            logger.error("Can not get class for instance", e);
        }

        return predictedClass;
    }

    private boolean isThresholdAccepted(ClassificationResult classificationResult, double threshold) {
        boolean isThresholdAccepted = classificationResult.getProbability() >= threshold;
        
        if (!isThresholdAccepted) {
            logger.info("Predicted probability is lower than can be accepted " + threshold);
            return false;
        }

        return true;
    }

    /**
     * Saves sessions with their events and features
     * @param sessions
     * @throws Exception
     */
    private void saveSessions(List<Session> sessions) throws Exception {
        for (Session session : sessions) {
            SessionsService.save(session);
            
            List<Event> events = session.getEvents();
            if (events.isEmpty()) {
                continue;
            }
            for (Event event : events) {
                EventsService.save(event);
            }
            for (Feature feature : session.getFeaturesFromEvents()) {
                feature.setSession(session);
                FeaturesService.save(feature);
            }
        }
    }

    private List<Session> prepareSessions(Map<String, List<Event>> sessionsMap, User user) {
        List<Session> statSessions = new ArrayList<Session>();
        for (String uuid : sessionsMap.keySet()) {
            List<Event> events = sessionsMap.get(uuid);
            for (Event event : events) {
                event.setUser(user);
                event.setSession(uuid);
            }
            statSessions.add(new Session(uuid, events, user));
        }

        return statSessions;
    }

    /**
     * Saves an authentication request to a file in .json format
     * if corresponding property is set in the configuration file
     * @param request
     * @throws IOException
     */
    private void saveRequest(HttpServletRequest request) throws IOException {
        String login = request.getParameter(REQ_LOGIN_PARAM);
        String password = request.getParameter(REQ_PASSWORD_PARAM);
        String stat = request.getParameter(REQ_STAT_PARAM);
        boolean isStolen = Boolean.parseBoolean(request.getParameter("isStolen"));

        AuthRequest authReq = new AuthRequest(login, password, stat);

        String json = new Gson().toJson(authReq);
        String savedReqPath = PropertiesService.getStaticPropertyValue(SAVED_AUTH_REQUESTS_PATH_PROP) + "/" + password.length();
        
        /* Should be used if app is deployed to OpenShift
           String savedReqPath = System.getenv(OPENSHIFT_DATA_DIR_VAR)
               + PropertiesService.getStaticPropertyValue(SAVED_AUTH_REQUESTS_PATH_PROP) + "/" + password.length();
        */

        String dirPath = String.join("/", new String[] { savedReqPath, login, (isStolen ? STOLEN_DIR_PREFIX : OWN_DIR_PREFIX) });
        File ownershipDir = new File(dirPath);
        if (!ownershipDir.exists()) {
            ownershipDir.mkdirs();
        }

        File reqFile = new File(ownershipDir, REQ_FILE_PREFIX + new Date().getTime());
        FileUtils.writeStringToFile(reqFile, json);
    }

    private String getResponseMessage(String status, String msg) {
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

        String login = request.getParameter(REQ_LOGIN_PARAM);
        String password = request.getParameter(REQ_PASSWORD_PARAM);
        String stat = request.getParameter(REQ_STAT_PARAM);

        boolean isStolen = Boolean.parseBoolean(request.getParameter(REQ_IS_STOLEN_PARAM));
        boolean saveRequest = Boolean.valueOf(PropertiesService.getDynamicPropertyValue(REQ_SAVE_REQUESTS_PARAM));
        boolean updateTemplate = Boolean.valueOf(PropertiesService.getDynamicPropertyValue(REQ_UPDATE_TEMPLATE_PARAM));

        Double threshold = Double.valueOf(PropertiesService.getDynamicPropertyValue(REQ_THRESHOLD_PARAM));
        if (threshold == null) {
            threshold = CLASS_PREDICTION_THRESHOLD;
        }

        Integer learningRate = Integer.valueOf(PropertiesService.getDynamicPropertyValue(REQ_LEARNING_RATE_PARAM));
        if (learningRate == null) {
            learningRate = TRUSTED_AUTHENTICATION_LIMIT;
        }
        
        if (saveRequest) {
            saveRequest(request);
        }

        logger.info(String.format("Authentication (%s, %s)", login, password));
        
        JSONObject responseObj = new JSONObject();
        
        if (StringUtils.isBlank(login) || StringUtils.isBlank(password)) {
            logger.info(getResponseMessage(AUTHENTICATION_FAILED, EMPTY_LOGIN_OR_PASSWORD));
            responseObj.put(RESP_SUCCESS_PARAM, false);
            out.print(responseObj);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, EMPTY_LOGIN_OR_PASSWORD);
            return;
        }

        User user = UsersService.findByLogin(login);
        if (user == null) {
            logger.info(getResponseMessage(AUTHENTICATION_FAILED, CAN_NOT_FIND_USER));
            responseObj.put(RESP_SUCCESS_PARAM, false);
            out.print(responseObj);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (!user.getPassword().equals(password)) {
            logger.info(getResponseMessage(AUTHENTICATION_FAILED, WRONG_PASSWORD));
            responseObj.put(RESP_SUCCESS_PARAM, false);
            out.print(responseObj);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (user.getUserType() == User.USER_TYPE_ADMIN) {
            logger.info(getResponseMessage(AUTHENTICATION_PASSED, "Administrator"));
            request.getSession();
            response.sendRedirect("/");
            return;
        }

        if (stat == null) {
            logger.info(getResponseMessage(AUTHENTICATION_FAILED, DYNAMICS_NOT_PASSED));
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            responseObj.put(RESP_SUCCESS_PARAM, false);
            out.print(responseObj);
            return;
        }

        // TODO: Consider using only one session per request
        Type type = new TypeToken<Map<String, List<Event>>>(){}.getType();
        Map<String, List<Event>> sessionsMap = new Gson().fromJson(stat, type);
        List<Session> statSessions = prepareSessions(sessionsMap, user);

        int authenticatedCnt = user.getAuthenticatedCnt();

        // First <learningRate> authentication attempts are considered genuine
        if (!isStolen && authenticatedCnt < learningRate) {
            logger.info(SUCCESSFUL_AUTH_MSG);
            if (updateTemplate) {
                try {
                    saveSessions(statSessions);
                } catch (Exception e) {
                    logger.error(e);
                    throw new ServletException();
                }
                user.setAuthenticatedCnt(user.getAuthenticatedCnt() + 1);
                UsersService.save(user);
            }
            responseObj.put(RESP_SUCCESS_PARAM, true);
            out.print(responseObj);
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        ClassificationResult classificationResult;
        try {
            classificationResult = getPredictedThreshold(statSessions, user);
        } catch (Exception e) {
            logger.error(e);
            throw new ServletException();
        }

        boolean isExpectedClass = isThresholdAccepted(classificationResult, threshold);
        if (isExpectedClass) {
            logger.info(SUCCESSFUL_AUTH_MSG);
            if (!isStolen && updateTemplate) {
                try {
                    saveSessions(statSessions);
                } catch (Exception e) {
                    logger.error(e);
                    throw new ServletException();
                }
                user.setAuthenticatedCnt(user.getAuthenticatedCnt() + 1);
                UsersService.save(user);
            }
            responseObj.put(RESP_SUCCESS_PARAM, true);
            responseObj.put(RESP_THRESHOLD_PARAM, classificationResult.getProbability());
            out.print(responseObj);
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        responseObj.put(RESP_SUCCESS_PARAM, false);
        responseObj.put(RESP_THRESHOLD_PARAM, classificationResult.getProbability());
        out.print(responseObj);
        logger.info(getResponseMessage(AUTHENTICATION_FAILED, DYNAMICS_DOES_NOT_MATCH));
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

}
