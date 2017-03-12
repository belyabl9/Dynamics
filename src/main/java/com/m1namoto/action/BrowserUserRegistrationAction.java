package com.m1namoto.action;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.Feature;
import com.m1namoto.domain.Session;
import com.m1namoto.domain.User;
import com.m1namoto.etc.RegRequest;
import com.m1namoto.page.PageData;
import com.m1namoto.service.EventsService;
import com.m1namoto.service.FeaturesService;
import com.m1namoto.service.SessionsService;
import com.m1namoto.service.UsersService;
import com.m1namoto.utils.PropertiesService;
import com.m1namoto.utils.Utils;

public class BrowserUserRegistrationAction extends Action {

	private final static Logger logger = Logger.getLogger(BrowserUserRegistrationAction.class);
    
	private static final String REQ_NAME_PARAM = "name";
	private static final String REQ_SURNAME_PARAM = "surname";
    private static final String REQ_LOGIN_PARAM = "login";
    private static final String REQ_PASSWORD_PARAM = "password";
    private static final String REQ_IS_STOLEN_PARAM = "isStolen";
    private static final String REQ_STAT_PARAM = "stat";
    
    private static final String REQ_SAVE = "saveRequest";
	
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
    
    private void saveRequest() throws IOException {
        String name = requestParameters.get(REQ_NAME_PARAM)[0];
        String surname = requestParameters.get(REQ_SURNAME_PARAM)[0];
        String login = requestParameters.get(REQ_LOGIN_PARAM)[0];
        String password = requestParameters.get(REQ_PASSWORD_PARAM)[0];
        String stat = requestParameters.get(REQ_STAT_PARAM)[0];
        RegRequest regReq = new RegRequest(name, surname, login, password, stat);

        String json = new Gson().toJson(regReq);
        //String savedReqPath = PropertiesService.getPropertyValue("saved_reg_requests_path") + "/" + password.length();
        String savedReqPath = System.getenv("OPENSHIFT_DATA_DIR")
                + PropertiesService.getDynamicPropertyValue("saved_reg_requests_path") + "/" + password.length();

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
    
	private User createUser() {
        String name = requestParameters.get(REQ_NAME_PARAM)[0];
        String surname = requestParameters.get(REQ_SURNAME_PARAM)[0];
        String login = requestParameters.get(REQ_LOGIN_PARAM)[0];
        String password = requestParameters.get(REQ_PASSWORD_PARAM)[0];
        
        User user = new User();
        user.setName(name + " " + surname);
        user.setLogin(login);
        user.setPassword(password);
        user.setUserType(User.USER_TYPE_REGULAR);
        
        return user;
	}

	@Override
	protected ActionResult execute() throws Exception {
		PageData pageData = new PageData();
		
		boolean saveRequest = Boolean.valueOf(PropertiesService.getDynamicPropertyValue("save_requests"));
        if (saveRequest) {
            saveRequest();
        }
	    
	    try {
            Utils.checkMandatoryParams(
                    requestParameters, new String[] { "name", "surname", "login", "password", "stat" });
        } catch (Exception e) {
            logger.error(e);
            pageData.setError(true);
            return createAjaxResult(pageData);
        }

        if (UsersService.findByLogin(requestParameters.get(REQ_LOGIN_PARAM)[0]) != null) {
            logger.info("User with such login already exists");
            pageData.setError(true);
            return createAjaxResult(pageData);
        }

        User user = createUser();
        user = UsersService.save(user);
        if (user.getId() == 0) {
            logger.error("User was not created");
            pageData.setError(true);
            return createAjaxResult(pageData);
        }
        
        String stat = requestParameters.get(REQ_STAT_PARAM)[0];
        Type type = new TypeToken<Map<String, List<Event>>>(){}.getType();
        Map<String, List<Event>> sessionsMap = new Gson().fromJson(stat, type);
        List<Session> statSessions = prepareSessions(sessionsMap, user);
        saveSessionEvents(statSessions);
        
        user.setAuthenticatedCnt(user.getAuthenticatedCnt() + 1);
        UsersService.save(user);
        
        return createAjaxResult(null);
	}
	
}
