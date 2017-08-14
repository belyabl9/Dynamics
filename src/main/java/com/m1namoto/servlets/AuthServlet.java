package com.m1namoto.servlets;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.m1namoto.classifier.weka.WekaClassifierMakerStrategy;
import com.m1namoto.etc.AuthRequest;
import com.m1namoto.service.PropertiesService;
import com.m1namoto.service.UserService;
import com.m1namoto.service.auth.AuthenticationContext;
import com.m1namoto.service.auth.AuthenticationResult;
import com.m1namoto.service.auth.AuthenticationService;
import com.m1namoto.service.auth.AuthenticationStatus;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * Servlet implementation for passing authentication procedure using keystroke dynamics as an additional factor
 */
@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final static Logger logger = Logger.getLogger(AuthServlet.class);

    private static final String REQ_FILE_PREFIX = "req-";
    private static final String OWN_DIR_PREFIX = "own";
    private static final String STOLEN_DIR_PREFIX = "stolen";

    private static final String SAVED_AUTH_REQUESTS_PATH_PROP = "saved_auth_requests_path";

    private static final String DYNAMICS_NOT_PASSED = "Keystroke dynamics was not passed";
    private static final String WRONG_PASSWORD = "Wrong password";
    private static final String CAN_NOT_FIND_USER = "Can not find user";
    private static final String EMPTY_LOGIN_OR_PASSWORD = "Login or password is empty";
    private static final String DYNAMICS_DOES_NOT_MATCH = "Keystroke dynamics does not match";
    private static final String AUTHENTICATION_PASSED = "Authentication has successfully passed";

    private static final WekaClassifierMakerStrategy CLASSIFIER_MAKER_STRATEGY = WekaClassifierMakerStrategy.getInstance();

    private static class RequestParam {
        static final String LOGIN = "login";
        static final String PASSWORD = "password";
        static final String STAT = "stat";
        static final String IS_STOLEN = "isStolen";
    }

    private static class ResponseParam {
        static final String SUCCESS = "success";
        static final String THRESHOLD = "threshold";
    }

    private static class Message {
        static final String SAVED_AUTH_REQ_PATH_NOT_SPECIFIED = "Path for saving authentication requests is not specified.";
    }

    private static final Gson GSON = new Gson();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public AuthServlet() {
        super();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	response.setContentType("application/json");
    	response.setCharacterEncoding("utf8");
    	PrintWriter out = response.getWriter();

        authenticate(request, response, out);
    }

    private void authenticate(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException, ServletException {
        AuthenticationContext context = new AuthenticationContext(
                request.getParameter(RequestParam.LOGIN),
                request.getParameter(RequestParam.PASSWORD),
                request.getParameter(RequestParam.STAT),
                Boolean.parseBoolean(request.getParameter(RequestParam.IS_STOLEN))
        );
        if (context.isSaveRequest()) {
            saveRequest(context);
        }
        AuthenticationResult authResult = AuthenticationService.getInstance().authenticate(context, CLASSIFIER_MAKER_STRATEGY);

        JSONObject responseObj = new JSONObject();
        if (authResult.isSuccess()) {
            if (authResult.getStatus() == AuthenticationStatus.ADMIN_ACCESS) {
                request.getSession();
                response.sendRedirect("/");
                return;
            }
            UserService.getInstance().incrementAuthCounter(context.getLogin());
            responseObj.put(ResponseParam.SUCCESS, true);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            responseObj.put(ResponseParam.SUCCESS, false);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
        if (authResult.getThreshold().isPresent()) {
            responseObj.put(ResponseParam.THRESHOLD, authResult.getThreshold().get());
        }
        out.print(responseObj);
        logAuthResultStatus(authResult);
    }

    private void logAuthResultStatus(AuthenticationResult authResult) {
        AuthenticationStatus.assertSize(8);
        switch (authResult.getStatus()) {
            case SUCCESS:
                logger.debug(AUTHENTICATION_PASSED);
                break;
            case ADMIN_ACCESS:
                logger.debug(AUTHENTICATION_PASSED + " (admin access)");
                break;
            case FIRST_TRUSTED_ATTEMPTS:
                logger.debug(AUTHENTICATION_PASSED + " (first trusted attempts)");
                break;
            case FAIL:
                logger.debug(DYNAMICS_DOES_NOT_MATCH);
                break;
            case EMPTY_LOGIN_OR_PASSWORD:
                logger.debug(EMPTY_LOGIN_OR_PASSWORD);
                break;
            case CAN_NOT_FIND_USER:
                logger.debug(CAN_NOT_FIND_USER);
                break;
            case WRONG_PASSWORD:
                logger.debug(WRONG_PASSWORD);
                break;
            case DYNAMICS_NOT_PASSED:
                logger.debug(DYNAMICS_NOT_PASSED);
                break;
            default:
                throw new UnsupportedOperationException("Specified authentication result status is not supported.");
        }
    }

    /**
     * Saves an authentication request to a file in .json format if corresponding property is set in he configuration file
     */
    private void saveRequest(@NotNull AuthenticationContext context) throws IOException {
        AuthRequest authReq = new AuthRequest(context.getLogin(), context.getPassword(), context.getStat());

        String json = GSON.toJson(authReq);
        Optional<String> savedAuthReqPathOpt = PropertiesService.getInstance().getStaticPropertyValue(SAVED_AUTH_REQUESTS_PATH_PROP);
        if (!savedAuthReqPathOpt.isPresent()) {
            throw new RuntimeException(Message.SAVED_AUTH_REQ_PATH_NOT_SPECIFIED);
        }
        String savedReqPath = savedAuthReqPathOpt.get() + "/" + context.getPassword().length();

        /* Should be used if app is deployed to OpenShift
           String savedReqPath = System.getenv(OPENSHIFT_DATA_DIR_VAR)
               + PropertiesService.getStaticPropertyValue(SAVED_AUTH_REQUESTS_PATH_PROP) + "/" + password.length();
        */

        String dirPath = String.join("/", new String[] { savedReqPath, context.getLogin(), (context.isStolen() ? STOLEN_DIR_PREFIX : OWN_DIR_PREFIX) });
        File ownershipDir = new File(dirPath);
        if (!ownershipDir.exists()) {
            ownershipDir.mkdirs();
        }

        File reqFile = new File(ownershipDir, REQ_FILE_PREFIX + LocalDateTime.now());
        FileUtils.writeStringToFile(reqFile, json);
    }

}
