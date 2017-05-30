package com.m1namoto.servlets.user;

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

import com.google.common.collect.ImmutableList;
import com.m1namoto.domain.*;
import com.m1namoto.features.FeatureExtractor;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.etc.RegRequest;
import com.m1namoto.service.FeatureService;
import com.m1namoto.service.SessionService;
import com.m1namoto.service.UserService;
import com.m1namoto.utils.PropertiesService;
import com.m1namoto.utils.Utils;
import org.jetbrains.annotations.NotNull;

@WebServlet("/reg")
public class UserRegistrationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final static Logger logger = Logger.getLogger(UserRegistrationServlet.class);

    static class RequestParam {
        private static final String NAME = "name";
        private static final String SURNAME = "surname";
        private static final String LOGIN = "login";
        private static final String PASSWORD = "password";
        private static final String STAT = "stat";
    }

    static class Message {
        private static final String LOGIN_ALREADY_EXISTS = "User with such login already exists";
        private static final String USER_WAS_NOT_CREATED = "User was not created";
    }

    private static List<String> mandatoryParams = ImmutableList.of("name", "surname", "login", "password", "stat");

    private static Gson gson = new Gson();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserRegistrationServlet() {
        super();
    }

    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    Utils.checkMandatoryParams(request.getParameterMap(), mandatoryParams);

        RegistrationContext regContext = makeContext(request);
        boolean saveRequest = Boolean.valueOf(PropertiesService.getInstance().getDynamicPropertyValue("save_requests"));
        if (saveRequest) {
            saveRequest(regContext);
        }

        if (UserService.findByLogin(regContext.getLogin()) != null) {
            logger.info(Message.LOGIN_ALREADY_EXISTS);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        User user = makeUser(regContext);
        user = UserService.save(user);
        if (user.getId() == 0) {
            logger.error(Message.USER_WAS_NOT_CREATED);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        Type type = new TypeToken<Map<String, List<Event>>>(){}.getType();
        List<Event> events = new Gson().fromJson(regContext.getStat(), type);
        saveSessionEvents(events, user);

        UserService.save(user);
        
        response.setStatus(HttpServletResponse.SC_OK);
	}

    private void saveSessionEvents(@NotNull List<Event> events, @NotNull User user) {
        if (events.isEmpty()) {
            throw new IllegalArgumentException("Event list must contain at least one element.");
        }
        Session session = SessionService.save(new Session("GENERATED", user));

        List<HoldFeature> holdFeatures = FeatureExtractor.getInstance().getHoldFeatures(events, user);
        List<ReleasePressFeature> releasePressFeatures = FeatureExtractor.getInstance().getReleasePressFeatures(events, user);
        List<Feature> features = new ArrayList<>();
        features.addAll(holdFeatures);
        features.addAll(releasePressFeatures);

        for (Feature feature : features) {
            feature.setSession(session);
            FeatureService.save(feature);
        }
    }

    private void saveRequest(@NotNull RegistrationContext regContext) throws IOException {
        RegRequest regReq = makeRegRequest(regContext);

        String json = gson.toJson(regReq);
        String savedReqPath = getSavedRequestsPath(regContext.getPassword());

        File reqDir = new File(savedReqPath);
        if (!reqDir.exists()) {
            reqDir.mkdirs();
        }
        File loginDir = new File(savedReqPath + "/" + regContext.getLogin());
        if (!loginDir.exists()) {
            loginDir.mkdirs();
        }
        File reqFile = new File(loginDir + "/req-" + new Date().getTime());
        FileUtils.writeStringToFile(reqFile, json);
    }

    @NotNull
    private String getSavedRequestsPath(String password) {
	    String common = PropertiesService.getInstance().getDynamicPropertyValue("saved_reg_requests_path") + "/" + password.length();
        if (Utils.isOpenShift()) {
            return System.getenv("OPENSHIFT_DATA_DIR") + common;
        }
        return common;
    }

	@NotNull
	private User makeUser(@NotNull RegistrationContext context) {
        User user = new User();
        user.setName(context.getName() + " " + context.getSurname());
        user.setLogin(context.getLogin());
        user.setPassword(context.getPassword());
        user.setUserType(User.Type.REGULAR);

        return user;
	}

	private RegRequest makeRegRequest(@NotNull RegistrationContext context) {
        return new RegRequest(
                context.getName(),
                context.getSurname(),
                context.getLogin(),
                context.getPassword(),
                context.getStat()
        );
    }

    @NotNull
    private RegistrationContext makeContext(@NotNull HttpServletRequest request) {
	    return new RegistrationContext(
                request.getParameter(RequestParam.NAME),
                request.getParameter(RequestParam.SURNAME),
                request.getParameter(RequestParam.LOGIN),
                request.getParameter(RequestParam.PASSWORD),
                request.getParameter(RequestParam.STAT)
        );
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
