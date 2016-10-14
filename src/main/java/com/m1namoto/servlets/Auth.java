package com.m1namoto.servlets;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.Feature;
import com.m1namoto.domain.HoldFeature;
import com.m1namoto.domain.ReleasePressFeature;
import com.m1namoto.domain.Session;
import com.m1namoto.domain.User;
import com.m1namoto.service.Classifier;
import com.m1namoto.service.DynamicsInstance;
import com.m1namoto.service.Events;
import com.m1namoto.service.Features;
import com.m1namoto.service.Users;
import com.m1namoto.utils.ReleasePressPair;

/**
 * Servlet implementation class Auth
 */
@WebServlet("/auth")
public class Auth extends HttpServlet {
    final static Logger logger = Logger.getLogger(Auth.class);

    private static final int TRUSTED_AUTHENTICATION_LIMIT = 5;
    
    private static final String DYNAMICS_NOT_PASSED = "Keystroke dynamics was not passed";
    private static final String WRONG_PASSWORD = "Wrong password";
    private static final String CAN_NOT_FIND_USER = "Can not find user";
    private static final String EMPTY_LOGIN_OR_PASSWORD = "Login or password is empty";

    private static final long serialVersionUID = 1L;
    
    private static final String REQ_LOGIN_PARAM = "login";
    private static final String REQ_PASSWORD_PARAM = "password";
    private static final String REQ_STAT_PARAM = "stat";
    
    private static final double ALLOWED_CLASS_PROBABILITY = 80;

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

    private List<Integer> getPredictedClasses(List<Session> sessions, String password) {
        logger.info("Predict classes for passed sessions");
        List<Integer> predictedClasses = new ArrayList<Integer>();
        Classifier classifier = null;
        try {
            classifier = new Classifier(password);
        } catch (Exception e) {
            logger.error("Can not create neural network", e);
        }

        List<HoldFeature> sessionHoldFeatures = new ArrayList<HoldFeature>();
        List<ReleasePressFeature> sessionReleasePressFeatures = new ArrayList<ReleasePressFeature>();
        User user = null;
        for (Session session : sessions) {
            logger.debug(session);
            sessionHoldFeatures.addAll(session.getHoldFeatures());
            sessionReleasePressFeatures.addAll(session.getReleasePressFeatures());
        }

        List<Double> featureValues = new ArrayList<Double>();

        logger.debug("Session Hold Features");
        logger.debug(sessionHoldFeatures);

        logger.debug("Session Release-Press Features");
        logger.debug(sessionReleasePressFeatures);

        Map<Integer, List<HoldFeature>> holdFeaturesPerCode = Features.getUserHoldFeaturesPerCode(sessionHoldFeatures);
        logger.debug("Hold Features Per Code:");
        logger.debug(holdFeaturesPerCode);
        char[] passwordCharacters = password.toCharArray();  
        for (char c : passwordCharacters) {
            List<HoldFeature> featuresByCode = holdFeaturesPerCode.get((int)c);
            
            logger.debug("Features by code: " + featuresByCode);
            
            featureValues.add(featuresByCode.get(0).getValue());
        }
        
        Map<ReleasePressPair, List<ReleasePressFeature>> releasePressFeaturesPerCode = Features.getUserReleasePressFeaturesPerCode(sessionReleasePressFeatures);
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
        
        DynamicsInstance instance = new DynamicsInstance(featureValues);
        try {
            predictedClasses.add(classifier.getClassForInstance(instance));
        } catch (Exception e) {
            logger.error("Can not get class for instance", e);
        }

        return predictedClasses;
    }

    private boolean isExpectedClass(List<Session> sessions, User expectedUser, String password) {
        List<Integer> predictedClasses = getPredictedClasses(sessions, password);
        logger.debug("Predict class for passed keystroke dynamics");
        logger.debug(predictedClasses);

        int expectedClassOccurences = 0;
        long expectedUserId = expectedUser.getId();
        for (Integer classVal : predictedClasses) {
            if (expectedUserId == classVal) {
                expectedClassOccurences++;
            }
        }

        double probPercentage = (expectedClassOccurences * 100) / predictedClasses.size();
        logger.debug("Probability that dynamics refers to expected user: " + probPercentage + "%");

        return probPercentage > ALLOWED_CLASS_PROBABILITY;
    }

    private void saveSessionEvents(List<Session> statSessions) {
        logger.debug("Save session events");
        logger.debug(statSessions);
        for (Session session : statSessions) {
            List<Event> events = session.getEvents();
            if (events.size() == 0) {
                continue;
            }
            for (Event event : events) {
                Events.save(event);
            }
            for (Feature feature : session.getFeatures()) {
                logger.debug("Save feature: " + feature);
                Features.save(feature);
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
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String login = request.getParameter(REQ_LOGIN_PARAM);
        String password = request.getParameter(REQ_PASSWORD_PARAM);
        String stat = request.getParameter(REQ_STAT_PARAM);

        logger.info("Authentication");

        if (logger.isDebugEnabled()) {
            logger.debug("Login: " + login);
            logger.debug("Password: " + password);
            //logger.debug("Statistics: " + stat);
        }
        
        if (login == null || login.isEmpty() || password == null  || password.isEmpty()) {
            logger.info("Authentication failed: " + EMPTY_LOGIN_OR_PASSWORD);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, EMPTY_LOGIN_OR_PASSWORD);
            return;
        }

        User user = Users.findByLogin(login);
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

        if (authenticatedCnt < TRUSTED_AUTHENTICATION_LIMIT || isExpectedClass(statSessions, user, password)) {
            logger.info("Authentication has successfuly passed");
            saveSessionEvents(statSessions);
            user.setAuthenticatedCnt(user.getAuthenticatedCnt() + 1);
            Users.save(user);
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        logger.info("Authentication failed: " + "keystroke dynamics does not match");
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

}
