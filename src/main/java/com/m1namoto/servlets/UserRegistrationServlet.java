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

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.Feature;
import com.m1namoto.domain.Session;
import com.m1namoto.domain.User;
import com.m1namoto.service.Events;
import com.m1namoto.service.Features;
import com.m1namoto.service.Users;
import com.m1namoto.utils.Utils;

/**
 * Servlet implementation class UserRegistrationServlet
 */
@WebServlet("/reg")
public class UserRegistrationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final static Logger logger = Logger.getLogger(UserRegistrationServlet.class);
    
	private static final String REQ_NAME_PARAM = "name";
	private static final String REQ_SURNAME_PARAM = "surname";
    private static final String REQ_LOGIN_PARAM = "login";
    private static final String REQ_PASSWORD_PARAM = "password";
    private static final String REQ_STAT_PARAM = "stat";
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserRegistrationServlet() {
        super();
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
    
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        try {
            Utils.checkMandatoryParams(
                    request.getParameterMap(), new String[] { "name", "surname", "login", "password", "stat" });
        } catch (Exception e) {
            logger.error(e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (Users.findByLogin(request.getParameter(REQ_LOGIN_PARAM)) != null) {
            logger.info("User with such login already exists");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        User user = createUser(request);
        user = Users.save(user);
        if (user.getId() == 0) {
            logger.error("User was not created");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        String stat = request.getParameter(REQ_STAT_PARAM);
        Type type = new TypeToken<Map<String, List<Event>>>(){}.getType();
        Map<String, List<Event>> sessionsMap = new Gson().fromJson(stat, type);
        List<Session> statSessions = prepareSessions(sessionsMap, user);
        saveSessionEvents(statSessions);
        
        user.setAuthenticatedCnt(user.getAuthenticatedCnt() + 1);
        Users.save(user);
        
        response.setStatus(HttpServletResponse.SC_OK);
	}
	
	private User createUser(HttpServletRequest request) {
        String name = request.getParameter(REQ_NAME_PARAM);
        String surname = request.getParameter(REQ_SURNAME_PARAM);
        String login = request.getParameter(REQ_LOGIN_PARAM);
        String password = request.getParameter(REQ_PASSWORD_PARAM);
        
        User user = new User();
        user.setName(name + " " + surname);
        user.setLogin(login);
        user.setPassword(password);
        user.setUserType(User.USER_TYPE_REGULAR);
        
        return user;
	}

}
