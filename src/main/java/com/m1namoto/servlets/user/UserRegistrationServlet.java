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
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.Feature;
import com.m1namoto.domain.Session;
import com.m1namoto.domain.User;
import com.m1namoto.etc.RegRequest;
import com.m1namoto.service.EventService;
import com.m1namoto.service.FeatureService;
import com.m1namoto.service.SessionService;
import com.m1namoto.service.UsersService;
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

        if (UsersService.findByLogin(regContext.getLogin()) != null) {
            logger.info(Message.LOGIN_ALREADY_EXISTS);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        User user = makeUser(regContext);
        user = UsersService.save(user);
        if (user.getId() == 0) {
            logger.error(Message.USER_WAS_NOT_CREATED);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        Type type = new TypeToken<Map<String, List<Event>>>(){}.getType();
        Map<String, List<Event>> sessionsMap = new Gson().fromJson(regContext.getStat(), type);
        List<Session> statSessions = prepareSessions(sessionsMap, user);
        saveSessionEvents(statSessions);

        UsersService.save(user);
        
        response.setStatus(HttpServletResponse.SC_OK);
	}

    private List<Session> prepareSessions(Map<String, List<Event>> sessionsMap, User user) {
        List<Session> statSessions = new ArrayList<>();
        // TODO process only one session
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
        // TODO process only one session
        for (Session session : statSessions) {
            SessionService.save(session);
            List<Event> events = session.getEvents();
            if (events.size() == 0) {
                continue;
            }
            for (Event event : events) {
                EventService.save(event);
            }
            for (Feature feature : session.getFeaturesFromEvents()) {
                feature.setSession(session);
                FeatureService.save(feature);
            }
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
