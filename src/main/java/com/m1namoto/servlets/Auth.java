package com.m1namoto.servlets;

import java.io.File;
import java.io.IOException;
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
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.classifier.ClassificationResult;
import com.m1namoto.classifier.Classifier;
import com.m1namoto.classifier.DynamicsInstance;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.Feature;
import com.m1namoto.domain.HoldFeature;
import com.m1namoto.domain.ReleasePressFeature;
import com.m1namoto.domain.Session;
import com.m1namoto.domain.User;
import com.m1namoto.etc.AuthRequest;
import com.m1namoto.service.EventsService;
import com.m1namoto.service.FeaturesService;
import com.m1namoto.service.SessionsService;
import com.m1namoto.service.UsersService;
import com.m1namoto.utils.PropertiesService;
import com.m1namoto.utils.ReleasePressPair;

/**
 * Servlet implementation class Auth
 */
@WebServlet("/auth")
public class Auth extends HttpServlet {
    private final static Logger logger = Logger.getLogger(Auth.class);

    private static final int TRUSTED_AUTHENTICATION_LIMIT = 5;
    
    private static final String DYNAMICS_NOT_PASSED = "Keystroke dynamics was not passed";
    private static final String WRONG_PASSWORD = "Wrong password";
    private static final String CAN_NOT_FIND_USER = "Can not find user";
    private static final String EMPTY_LOGIN_OR_PASSWORD = "Login or password is empty";

    private static final long serialVersionUID = 1L;
    
    private static final String REQ_LOGIN_PARAM = "login";
    private static final String REQ_PASSWORD_PARAM = "password";
    private static final String REQ_STAT_PARAM = "stat";
    
    private static final double CLASS_PREDICTION_THRESHOLD = 0.8;

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

    private ClassificationResult getPredictedClass(List<Session> sessions, User userToCheck) {
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
        User user = null;
        for (Session session : sessions) {
            //logger.debug(session);
            sessionHoldFeatures.addAll(session.getHoldFeaturesFromEvents());
            sessionReleasePressFeatures.addAll(session.getReleasePressFeaturesFromEvents());
        }

        List<Double> featureValues = new ArrayList<Double>();

        logger.debug("Session Hold Features");
        logger.debug(sessionHoldFeatures);

        logger.debug("Session Release-Press Features");
        logger.debug(sessionReleasePressFeatures);

        Map<Integer, List<HoldFeature>> holdFeaturesPerCode = FeaturesService.getHoldFeaturesPerCode(sessionHoldFeatures);
        logger.debug("Hold Features Per Code:");
        logger.debug(holdFeaturesPerCode);
        char[] passwordCharacters = userToCheck.getPassword().toCharArray();  
        for (char c : passwordCharacters) {
            List<HoldFeature> featuresByCode = holdFeaturesPerCode.get((int)c);
            
            logger.debug("Features by code: " + featuresByCode);
            
            featureValues.add(featuresByCode.get(0).getValue());
        }
        
        Map<ReleasePressPair, List<ReleasePressFeature>> releasePressFeaturesPerCode = FeaturesService.getReleasePressFeaturesPerCode(sessionReleasePressFeatures);
        logger.debug("Release-Press Features Per Code:");
        logger.debug(releasePressFeaturesPerCode);
        for (int i = 1; i < passwordCharacters.length; i++) {
            int releaseCode = passwordCharacters[i-1],
                pressCode = passwordCharacters[i];
            ReleasePressPair codePair = new ReleasePressPair(releaseCode, pressCode);
            List<ReleasePressFeature> featuresByCode = releasePressFeaturesPerCode.get(codePair);
            
            logger.debug("Release Press Features by code: " + featuresByCode);
            
            featureValues.add(featuresByCode.get(0).getValue());
        }

        double meanKeyPressTimeSum = 0;
        for (Session session : sessions) {
            List<Event> events = session.getEvents();
            meanKeyPressTimeSum += FeaturesService.getMeanKeyPressTime(events);
        }
        double meanKeyPressTime = sessions.size() == 0 ? 0 : meanKeyPressTimeSum / sessions.size();
        featureValues.add(meanKeyPressTime);
        
        logger.info("Sample to check: " + featureValues);
        
        DynamicsInstance instance = new DynamicsInstance(featureValues);
        try {
            predictedClass = classifier.getClassForInstance(instance);
        } catch (Exception e) {
            logger.error("Can not get class for instance", e);
        }

        return predictedClass;
    }

    private boolean isExpectedClass(List<Session> sessions, User expectedUser, double threshold) {
        ClassificationResult classificationResult = getPredictedClass(sessions, expectedUser);
        
        boolean isExpectedUserId = classificationResult.getPredictedClass() == expectedUser.getId();
        boolean isThresholdAccepted = classificationResult.getProbability() >= threshold;
        if (!isExpectedUserId) {
            return false;
        }
        
        if (!isThresholdAccepted) {
            logger.info("Predicted probability is lower than can be accepted " + threshold);
            return false;
        }

        return true;
    }

    private void saveSessionEvents(List<Session> statSessions) {
        logger.debug("Save session events");
        logger.debug(statSessions);
        for (Session session : statSessions) {
            SessionsService.save(session);
            
            List<Event> events = session.getEvents();
            if (events.size() == 0) {
                continue;
            }
            for (Event event : events) {
                EventsService.save(event);
            }
            for (Feature feature : session.getFeaturesFromEvents()) {
                logger.debug("Save feature: " + feature);
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
    
    private void saveRequest(HttpServletRequest request) throws IOException {
        String login = request.getParameter(REQ_LOGIN_PARAM);
        String password = request.getParameter(REQ_PASSWORD_PARAM);
        String stat = request.getParameter(REQ_STAT_PARAM);
        boolean isStolen = Boolean.parseBoolean(request.getParameter("isStolen"));

        AuthRequest authReq = new AuthRequest(login, password, stat);

        String json = new Gson().toJson(authReq);
        String savedReqPath = PropertiesService.getPropertyValue("saved_auth_requests_path");

        File reqDir = new File(savedReqPath);
        if (!reqDir.exists()) {
            reqDir.mkdirs();
        }

        File loginDir = new File(savedReqPath + "/" + login);
        if (!loginDir.exists()) {
            loginDir.mkdirs();
        }
        
        File ownershipDir = new File(loginDir.getAbsolutePath() + 
                (isStolen ? "/stolen" : "/own"));
        if (!ownershipDir.exists()) {
            ownershipDir.mkdirs();
        }

        File reqFile = new File(ownershipDir.getAbsolutePath() +
                "/req-" + new Date().getTime());
        FileUtils.writeStringToFile(reqFile, json);
        
    }
    
    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String login = request.getParameter(REQ_LOGIN_PARAM);
        String password = request.getParameter(REQ_PASSWORD_PARAM);
        boolean isStolen = Boolean.parseBoolean(request.getParameter("isStolen"));
        String stat = request.getParameter(REQ_STAT_PARAM);

        String isTest = request.getParameter("test");
        String threshold = request.getParameter("threshold");
        
        String learningRate = PropertiesService.getPropertyValue("learning_rate");
        
        boolean saveRequest = Boolean.valueOf(PropertiesService.getPropertyValue("save_requests"));
        boolean updateTemplate = Boolean.valueOf(PropertiesService.getPropertyValue("update_template"));

        if (saveRequest) {
            saveRequest(request);
        }
        
        logger.info("Authentication");
        logger.info("Login: " + login);
        logger.info("Password: " + password);
        
        if (login == null || login.isEmpty() || password == null  || password.isEmpty()) {
            logger.info("Authentication failed: " + EMPTY_LOGIN_OR_PASSWORD);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, EMPTY_LOGIN_OR_PASSWORD);
            return;
        }

        User user = UsersService.findByLogin(login);
        if (user == null) {
            logger.info("Authentication failed: " + CAN_NOT_FIND_USER);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, CAN_NOT_FIND_USER);
            return;
        }

        if (!user.getPassword().equals(password)) {
            logger.info("Authentication failed: " + WRONG_PASSWORD);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, WRONG_PASSWORD);
            return;
        }

        if (user.getUserType() == User.USER_TYPE_ADMIN) {
            logger.info("Authentication passed: " + "Adminitstrator");
            HttpSession session = request.getSession();
            response.sendRedirect("/");
            return;
        }

        if (stat == null) {
            logger.info("Authentication failed: " + DYNAMICS_NOT_PASSED);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, DYNAMICS_NOT_PASSED);
            return;
        }

        Type type = new TypeToken<Map<String, List<Event>>>(){}.getType();
        Map<String, List<Event>> sessionsMap = new Gson().fromJson(stat, type);
        List<Session> statSessions = prepareSessions(sessionsMap, user);

        int authenticatedCnt = user.getAuthenticatedCnt();
        
        if (!isStolen && authenticatedCnt < (((learningRate == null) || learningRate.isEmpty()) ?
                TRUSTED_AUTHENTICATION_LIMIT : Integer.parseInt(learningRate))) {
            logger.info("Authentication has successfuly passed");
            if (updateTemplate) {
                saveSessionEvents(statSessions);
                user.setAuthenticatedCnt(user.getAuthenticatedCnt() + 1);
                UsersService.save(user);
            }
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        // TODO: Refactor
        boolean isExpectedClass = isExpectedClass(statSessions, user,
                Boolean.parseBoolean(isTest) ? Double.parseDouble(threshold) : CLASS_PREDICTION_THRESHOLD);
        if (isExpectedClass) {
            logger.info("Authentication has successfuly passed");
            if (!isStolen && updateTemplate) {
                saveSessionEvents(statSessions);
                user.setAuthenticatedCnt(user.getAuthenticatedCnt() + 1);
                UsersService.save(user);
            }
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        logger.info("Authentication failed: " + "keystroke dynamics does not match");
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

}
