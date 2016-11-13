package com.m1namoto.action;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.m1namoto.utils.Const;

/**
* Is used to generate the matching {@link Action} for a given URI.
*/
public class ActionFactory {
 
    private static HashMap<String, Class<? extends Action>> actionMappings = new HashMap<String, Class<? extends Action>>();
 
    static {
        map(Const.ActionURIs.USERS_PAGE, UsersPageAction.class);
        map(Const.ActionURIs.ADD_USER_PAGE, AddUserAction.class);
        map(Const.ActionURIs.UPDATE_USER_INFO_AJAX, UpdateUserAction.class);
        map(Const.ActionURIs.USER_INFO_PAGE, UserInfoAction.class);
        map(Const.ActionURIs.USERS_LIST_AJAX, GetUsersList.class);
        map(Const.ActionURIs.DEL_USER_AJAX, DelUserAction.class);
        map(Const.ActionURIs.DEL_USER_FEATURES_AJAX, DeleteUserFeaturesAction.class);
        map(Const.ActionURIs.STATISTICS_PAGE, StatisticsPageAction.class);
        map(Const.ActionURIs.USER_SESSIONS_PAGE, UserSessionsAction.class);
        map(Const.ActionURIs.DEL_SESSION, DeleteSessionAction.class);
        map(Const.ActionURIs.KEYPRESS_PLOT_DATA_AJAX, KeypressPlotDataAction.class);
        map(Const.ActionURIs.EVAL_CLASSIFIER_PAGE, EvaluationPageAction.class);
        map(Const.ActionURIs.EVAL_CLASSIFIER_AJAX, EvalClassifierAction.class);

        map(Const.ActionURIs.DB_CLEANUP, DBCleanupAction.class);
    }

    /**
     * @return the matching {@link Action} object for the URI in the {@code req}.
     *   The returned {@code Action} is already initialized using the {@code req}.
     * @throws Exception 
     */
    public Action getAction(HttpServletRequest req) throws Exception {
        String uri = req.getRequestURI();
        if (uri.contains(";")) {
            uri = uri.split(";")[0];
        }
        Action c = getAction(uri);
        c.init(req);
        return c;
         
    }

    private static Action getAction(String uri) throws Exception {
        Class<? extends Action> controllerClass = actionMappings.get(uri);
     
        if (controllerClass == null) {
            throw new Exception("Controller not found");
        }
     
        try {
            return controllerClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not create the action for " + uri);
        }
    }

    private static void map(String actionUri, Class<? extends Action> actionClass) {
        actionMappings.put(actionUri, actionClass);
    }

}
