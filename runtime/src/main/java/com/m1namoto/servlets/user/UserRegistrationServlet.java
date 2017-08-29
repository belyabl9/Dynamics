package com.m1namoto.servlets.user;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.domain.*;
import com.m1namoto.entity.FeatureType;
import com.m1namoto.etc.RegRequest;
import com.m1namoto.service.*;
import com.m1namoto.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Server for registration of users.
 */
@WebServlet("/reg")
public class UserRegistrationServlet extends HttpServlet {
    private final static Logger logger = Logger.getLogger(UserRegistrationServlet.class);
    private static final long serialVersionUID = 1L;

    private static final String REG_REQ_PATH_NOT_SPECIFIED = "Path for saving registration requests is not specified.";

    private static final FeatureService FEATURE_SERVICE = FeatureService.getInstance();

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

    private static final List<String> MANDATORY_PARAMS = ImmutableList.of("name", "surname", "login", "password", "stat");

    private static final Gson GSON = new Gson();

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
	    Utils.checkMandatoryParams(request.getParameterMap(), MANDATORY_PARAMS);

        RegistrationContext regContext = makeContext(request);
        boolean saveRequest = Boolean.valueOf(PropertiesService.getInstance().getDynamicPropertyValue("save_requests").or("false"));
        if (saveRequest) {
            saveRequest(regContext);
        }

        if (UserService.getInstance().findByLogin(regContext.getLogin()).isPresent()) {
            logger.info(Message.LOGIN_ALREADY_EXISTS);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        User user = makeUser(regContext);
        user = UserService.getInstance().save(user);
        if (user.getId() == 0) {
            logger.error(Message.USER_WAS_NOT_CREATED);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Type type = new TypeToken<InputStatistics>(){}.getType();
        InputStatistics statistics = new Gson().fromJson(regContext.getStat(), type);
        saveSessionEvents(regContext, statistics, user);

        response.setStatus(HttpServletResponse.SC_OK);
	}

    private void saveSessionEvents(@NotNull RegistrationContext context, @NotNull InputStatistics statistics, @NotNull User user) {
        if (statistics.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password event list must contain at least one element.");
        }
        Session session = SessionService.save(new Session(user));

        List<Event> events = new ArrayList<>(statistics.getPassword());
        events.addAll(statistics.getAdditional());

        List<Feature> features = new ArrayList<>();
        Set<FeatureType> featureTypes = FeatureSelectionService.getInstance().getFeatureTypes();
        for (FeatureType featureType : featureTypes) {
            switch (featureType) {
                case HOLD:
                    features.addAll(FeatureExtractorService.getInstance().getHoldFeatures(events, user));
                    break;
                case RELEASE_PRESS:
                    features.addAll(FeatureExtractorService.getInstance().getReleasePressFeatures(events, user));

                    // todo tmp
                    String hash = user.getPassword();
                    user.setPassword(context.getPassword());
                    features.addAll(FeatureExtractorService.getInstance().getPasswordReleasePressFeatures(statistics.getPassword(), user));
                    user.setPassword(hash);
                    break;
                case PRESS_PRESS:
                    features.addAll(FeatureExtractorService.getInstance().getPressPressFeatures(events, user));
                    break;
            }
        }

        for (Feature feature : features) {
            feature.setSession(session);
            FEATURE_SERVICE.save(feature);
        }
        if (!features.isEmpty()) {
            FEATURE_SERVICE.invalidateFeatureCache();
        }
    }

    /**
     * Requests are optionally saved for testing purposes in order not to model behavior but use real data
     */
    private void saveRequest(@NotNull RegistrationContext regContext) throws IOException {
        RegRequest regReq = makeRegRequest(regContext);

        String json = GSON.toJson(regReq);
        String savedReqPath = getSavedRequestsPath(regContext.getPassword());

        File loginDir = new File(savedReqPath + "/" + regContext.getLogin());
        if (!loginDir.exists()) {
            loginDir.mkdirs();
        }
        File reqFile = new File(loginDir + "/req-" + LocalDateTime.now());
        FileUtils.writeStringToFile(reqFile, json);
    }

    @NotNull
    private String getSavedRequestsPath(String password) {
        Optional<String> savedRegReqPathOpt = PropertiesService.getInstance().getStaticPropertyValue("saved_reg_requests_path");
	    if (!savedRegReqPathOpt.isPresent()) {
            throw new RuntimeException(REG_REQ_PATH_NOT_SPECIFIED);
        }
        String common = savedRegReqPathOpt.get() + "/" + password.length();
        if (Utils.isOpenShift()) {
            return System.getenv("OPENSHIFT_DATA_DIR") + common;
        }
        return common;
    }

	@NotNull
	private User makeUser(@NotNull RegistrationContext context) {
        User user = new User();
        user.setName(makeFullname(context.getName(), context.getSurname()));
        user.setLogin(context.getLogin());
        user.setPassword(context.getPasswordSha1());
        user.setUserType(User.Type.REGULAR);

        return user;
	}

	@NotNull
	private String makeFullname(@Nullable String name, @Nullable String surname) {
        StringJoiner joiner = new StringJoiner(" ");
        if (name != null) joiner.add(name);
        if (surname != null) joiner.add(surname);
        return joiner.toString();
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
