package com.m1namoto.action;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.m1namoto.domain.*;
import com.m1namoto.service.*;
import com.m1namoto.service.FeatureExtractorService;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.etc.RegRequest;
import com.m1namoto.page.PageData;
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

    private static final String[] MANDATORY_PARAMS = new String[] { "name", "surname", "login", "password", "stat" };
    private static final String REG_REQ_PATH_NOT_SPECIFIED = "Path for saving registration requests has to be specified.";

    private static final FeatureService FEATURE_SERVICE = FeatureService.getInstance();

    private void saveSessionFeatures(@NotNull List<Event> events, @NotNull User user) throws Exception {
        logger.debug("Save session events");
        logger.debug(events);
        if (events.isEmpty()) {
            throw new RuntimeException("Event list must contain at least on element.");
        }

        Session session = new Session("GENERATED", user);
        session = SessionService.save(session);

        List<HoldFeature> holdFeatures = FeatureExtractorService.getInstance().getHoldFeatures(events, user);
        List<ReleasePressFeature> releasePressFeatures = FeatureExtractorService.getInstance().getReleasePressFeatures(events, user);
        List<Feature> features = new ArrayList<>();
        features.addAll(holdFeatures);
        features.addAll(releasePressFeatures);

        for (Feature feature : features) {
            logger.debug("Save feature: " + feature);
            feature.setSession(session);
            FEATURE_SERVICE.save(feature);
        }
        if (!features.isEmpty()) {
            FEATURE_SERVICE.invalidateFeatureCache();
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
        Optional<String> savedRegReqPathOpt = PropertiesService.getDynamicPropertyValue("saved_reg_requests_path");
        if (!savedRegReqPathOpt.isPresent()) {
            throw new RuntimeException(REG_REQ_PATH_NOT_SPECIFIED);
        }

        String savedReqPath = System.getenv("OPENSHIFT_DATA_DIR") + savedRegReqPathOpt.get() + "/" + password.length();

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
        user.setPassword(context.getPasswordSha1());
        user.setUserType(User.Type.REGULAR);

        return user;
	}

	@Override
	protected ActionResult execute() throws Exception {
		PageData pageData = new PageData();
		
		boolean saveRequest = Boolean.valueOf(PropertiesService.getDynamicPropertyValue("save_requests").get());
        if (saveRequest) {
            saveRequest();
        }
	    
	    try {
            Utils.checkMandatoryParams(requestParameters, MANDATORY_PARAMS);
        } catch (Exception e) {
            logger.debug(e);
            pageData.setError(true);
            return createAjaxResult(pageData);
        }

        RegistrationContext context = RegistrationContext.fromRequestParameters(requestParameters);
        if (UserService.getInstance().findByLogin(context.getLogin()).isPresent()) {
            logger.info(USER_ALREADY_EXISTS);
            pageData.setError(true);
            return createAjaxResult(pageData);
        }

        User user = createUser(context);
        user = UserService.getInstance().save(user);
        if (user.getId() == 0) {
            logger.error(USER_WAS_NOT_CREATED);
            pageData.setError(true);
            return createAjaxResult(pageData);
        }
        
        Type type = new TypeToken<List<Event>>(){}.getType();
        List<Event> events = new Gson().fromJson(context.getStat(), type);
        saveSessionFeatures(events, user);
        
        return createAjaxResult(pageData);
	}

	private static class RegistrationContext {
        private final String name;
        private final String surname;
        private final String login;
        private final String password;
        private final String passwordSha1;
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
            this.passwordSha1 = PasswordService.getInstance().makeHash(password);
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

        public String getPasswordSha1() {
            return passwordSha1;
        }

        public String getStat() {
            return stat;
        }
    }
	
}
