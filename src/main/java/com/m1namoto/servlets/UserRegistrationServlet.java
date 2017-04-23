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

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.Feature;
import com.m1namoto.domain.Session;
import com.m1namoto.domain.User;
import com.m1namoto.etc.RegRequest;
import com.m1namoto.service.EventsService;
import com.m1namoto.service.FeaturesService;
import com.m1namoto.service.SessionsService;
import com.m1namoto.service.UsersService;
import com.m1namoto.utils.PropertiesService;
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
    private static final String REQ_IS_STOLEN_PARAM = "isStolen";
    private static final String REQ_STAT_PARAM = "stat";
    
    private static final String REQ_SAVE = "saveRequest";
	
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
	
    private void saveSessionEvents(List<Session> statSessions) throws Exception {
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
    
    private void saveRequest(HttpServletRequest request) throws IOException {
        String name = request.getParameter(REQ_NAME_PARAM);
        String surname = request.getParameter(REQ_SURNAME_PARAM);
        String login = request.getParameter(REQ_LOGIN_PARAM);
        String password = request.getParameter(REQ_PASSWORD_PARAM);
        String stat = request.getParameter(REQ_STAT_PARAM);
        RegRequest regReq = new RegRequest(name, surname, login, password, stat);

        String json = new Gson().toJson(regReq);
        //String savedReqPath = PropertiesService.getPropertyValue("saved_reg_requests_path") + "/" + password.length();
        String savedReqPath = System.getenv("OPENSHIFT_DATA_DIR")
                + PropertiesService.getInstance().getDynamicPropertyValue("saved_reg_requests_path") + "/" + password.length();

        File reqDir = new File(savedReqPath);
        if (!reqDir.exists()) {
            reqDir.mkdirs();
        }
        File loginDir = new File(savedReqPath + "/" + login);
        if (!loginDir.exists()) {
            loginDir.mkdirs();
        }
        File reqFile = new File(loginDir + "/req-" + new Date().getTime());
        FileUtils.writeStringToFile(reqFile, json);
    }
    
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
	    boolean saveRequest = Boolean.valueOf(PropertiesService.getInstance().getDynamicPropertyValue("save_requests"));
        if (saveRequest) {
            saveRequest(request);
        }
	    
	    try {
            Utils.checkMandatoryParams(
                    request.getParameterMap(), new String[] { "name", "surname", "login", "password", "stat" });
        } catch (Exception e) {
            logger.error(e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (UsersService.findByLogin(request.getParameter(REQ_LOGIN_PARAM)) != null) {
            logger.info("User with such login already exists");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        User user = createUser(request);
        user = UsersService.save(user);
        if (user.getId() == 0) {
            logger.error("User was not created");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        String stat = request.getParameter(REQ_STAT_PARAM);
        Type type = new TypeToken<Map<String, List<Event>>>(){}.getType();
        Map<String, List<Event>> sessionsMap = new Gson().fromJson(stat, type);
        List<Session> statSessions = prepareSessions(sessionsMap, user);

        try {
            saveSessionEvents(statSessions);
        } catch (Exception e) {
            logger.error(e);
            throw new ServletException();
        }
        
        user.setAuthenticatedCnt(user.getAuthenticatedCnt() + 1);
        UsersService.save(user);
        
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
        user.setUserType(User.Type.REGULAR);
        
        return user;
	}

}
