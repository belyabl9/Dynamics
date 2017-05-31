package com.m1namoto.action;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.m1namoto.utils.Const;
import org.jetbrains.annotations.NotNull;

/**
* Is used to generate the matching {@link Action} for a given URI.
*/
public class ActionFactory {

    public static final String CONTROLLER_NOT_FOUND = "Controller not found";
    private static HashMap<String, Class<? extends Action>> actionMappings = new HashMap<>();
 
    static {
        map(Const.ActionURIs.USERS_PAGE, UsersPageAction.class);
        map(Const.ActionURIs.ADD_USER_PAGE, AddUserAction.class);
        map(Const.ActionURIs.UPDATE_USER_INFO_AJAX, UpdateUserAction.class);
        map(Const.ActionURIs.USER_INFO_PAGE, UserInfoAction.class);
        map(Const.ActionURIs.USERS_LIST_AJAX, GetUsersListAction.class);
        map(Const.ActionURIs.DEL_USER_AJAX, DelUserAction.class);
        map(Const.ActionURIs.DEL_USER_FEATURES_AJAX, DeleteUserFeaturesAction.class);
        map(Const.ActionURIs.STATISTICS_PAGE, StatisticsPageAction.class);
        map(Const.ActionURIs.SETTINGS_PAGE, SettingsPageAction.class);
        map(Const.ActionURIs.UPDATE_SETTINGS_AJAX, UpdateSettingsAction.class);
        map(Const.ActionURIs.USER_SESSIONS_PAGE, UserSessionsAction.class);
        map(Const.ActionURIs.DEL_SESSION, DeleteSessionAction.class);
        map(Const.ActionURIs.KEYPRESS_PLOT_DATA_AJAX, KeypressPlotDataAction.class);
        map(Const.ActionURIs.EVAL_CLASSIFIER_PAGE, EvaluationPageAction.class);

        map(Const.ActionURIs.BROWSER_USER_REG_AJAX, BrowserUserRegistrationAction.class);
        map(Const.ActionURIs.DB_CLEANUP, DBCleanupAction.class);
    }

    /**
     * @return the matching {@link Action} object for the URI in the {@code req}.
     * The returned {@code Action} is already initialized using the {@code req}.
     * @throws Exception 
     */
    public Action getAction(@NotNull HttpServletRequest req) throws Exception {
        String uri = req.getRequestURI();
        if (uri.contains(";")) {
            uri = uri.split(";")[0];
        }
        Action action = getAction(uri);
        action.init(req);
        return action;
    }

    private static Action getAction(String uri) throws Exception {
        Class<? extends Action> controllerClass = actionMappings.get(uri);
        if (controllerClass == null) {
            throw new Exception(CONTROLLER_NOT_FOUND);
        }
     
        try {
            return controllerClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not create the action for " + uri);
        }
    }

    private static void map(@NotNull String actionUri, Class<? extends Action> actionClass) {
        actionMappings.put(actionUri, actionClass);
    }

}
