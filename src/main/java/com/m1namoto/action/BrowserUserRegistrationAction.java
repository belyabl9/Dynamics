package com.m1namoto.action;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.m1namoto.domain.*;
import com.m1namoto.features.FeatureExtractor;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.etc.RegRequest;
import com.m1namoto.page.PageData;
import com.m1namoto.service.FeatureService;
import com.m1namoto.service.SessionService;
import com.m1namoto.service.UserService;
import com.m1namoto.utils.PropertiesService;
import com.m1namoto.utils.Utils;
import org.jetbrains.annotations.NotNull;

public class BrowserUserRegistrationAction extends Action {

    private final static Logger logger = Logger.getLogger(BrowserUserRegistrationAction.class);
    
	private static final String REQ_NAME_PARAM = "name";
	private static final String REQ_SURNAME_PARAM = "surname";
    private static final String REQ_LOGIN_PARAM = "login";
    private static final String REQ_PASSWORD_PARAM = "password";
    private static final String REQ_IS_STOLEN_PARAM = "isStolen";
    private static final String REQ_STAT_PARAM = "stat";
    
    private static final String REQ_SAVE = "saveRequest";
    private static final String USER_WAS_NOT_CREATED = "User was not created";
    private static final String USER_ALREADY_EXISTS = "User with such login already exists";
    
    private void saveSessionEvents(@NotNull List<Event> events, @NotNull User user) throws Exception {
        logger.debug("Save session events");
        logger.debug(events);
        if (events.isEmpty()) {
            throw new RuntimeException("Event list must contain at least on element.");
        }

        Session session = new Session("GENERATED", user);
        session = SessionService.save(session);

        List<HoldFeature> holdFeatures = FeatureExtractor.getInstance().getHoldFeatures(events, user);
        List<ReleasePressFeature> releasePressFeatures = FeatureExtractor.getInstance().getReleasePressFeatures(events, user);
        List<Feature> features = new ArrayList<>();
        features.addAll(holdFeatures);
        features.addAll(releasePressFeatures);

        for (Feature feature : features) {
            logger.debug("Save feature: " + feature);
            feature.setSession(session);
            FeatureService.save(feature);
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

    @NotNull
	private User createUser(@NotNull RegistrationContext context) {
        User user = new User();
        user.setName(context.getName() + " " + context.getSurname());
        user.setLogin(context.getLogin());
        user.setPassword(context.getPassword());
        user.setUserType(User.Type.REGULAR);

        return user;
	}

	@Override
	protected ActionResult execute() throws Exception {
		PageData pageData = new PageData();
		
		boolean saveRequest = Boolean.valueOf(PropertiesService.getInstance().getDynamicPropertyValue("save_requests"));
        if (saveRequest) {
            saveRequest();
        }
	    
	    try {
            Utils.checkMandatoryParams(requestParameters, new String[] { "name", "surname", "login", "password", "stat" });
        } catch (Exception e) {
            logger.debug(e);
            pageData.setError(true);
            return createAjaxResult(pageData);
        }

        RegistrationContext context = RegistrationContext.fromRequestParameters(requestParameters);
        if (UserService.findByLogin(context.getLogin()).isPresent()) {
            logger.info(USER_ALREADY_EXISTS);
            pageData.setError(true);
            return createAjaxResult(pageData);
        }

        User user = createUser(context);
        user = UserService.save(user);
        if (user.getId() == 0) {
            logger.error(USER_WAS_NOT_CREATED);
            pageData.setError(true);
            return createAjaxResult(pageData);
        }
        
        Type type = new TypeToken<List<Event>>(){}.getType();
        List<Event> events = new Gson().fromJson(context.getStat(), type);
        saveSessionEvents(events, user);
        
        user.setAuthenticatedCnt(user.getAuthenticatedCnt() + 1);
        UserService.save(user);
        
        return createAjaxResult(pageData);
	}

	private static class RegistrationContext {
        private final String name;
        private final String surname;
        private final String login;
        private final String password;
        private final String stat;

        public RegistrationContext(@NotNull String name,
                                   @NotNull String surname,
                                   @NotNull String login,
                                   @NotNull String password,
                                   @NotNull String stat) {
            this.name = name;
            this.surname = surname;
            this.login = login;
            this.password = password;
            this.stat = stat;
        }

        public static RegistrationContext fromRequestParameters(Map<String, String[]> requestParameters) {
            return new RegistrationContext(
                    requestParameters.get(REQ_NAME_PARAM)[0],
                    requestParameters.get(REQ_SURNAME_PARAM)[0],
                    requestParameters.get(REQ_LOGIN_PARAM)[0],
                    requestParameters.get(REQ_PASSWORD_PARAM)[0],
                    requestParameters.get(REQ_STAT_PARAM)[0]
            );
        }

        public String getName() {
            return name;
        }

        public String getSurname() {
            return surname;
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
    }
	
}
