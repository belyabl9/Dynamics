package com.m1namoto.action;

import com.m1namoto.domain.User;
import com.m1namoto.service.UserService;
import com.m1namoto.utils.Const;
import com.m1namoto.utils.Utils;

public class AddUserAction extends Action {

    private static final String[] MANDATORY_PARAMS = new String[] { "userType", "login", "password", "firstName", "surname" };
    
    private User createUser() throws Exception {
        Utils.checkMandatoryParams(requestParameters, MANDATORY_PARAMS);

        User user = new User();
        String fullName = getRequestParamValue("firstName") + " " + getRequestParamValue("surname");
        user.setName(fullName);
        user.setLogin(getRequestParamValue("login").get());
        user.setPassword(getRequestParamValue("password").get());
        user.setUserType(User.Type.fromInt(Integer.parseInt(getRequestParamValue("userType").get())));

        return UserService.save(user);
    }
    
    @Override
    protected ActionResult execute() {
        if (requestParameters.isEmpty()) {
            return createShowPageResult(Const.ViewURIs.ADD_USER, null);
        }
        
        User user;
        try {
            user = createUser();
        } catch (Exception e) {
            return createRedirectResult(Const.ActionURIs.ADD_USER_PAGE);
        }

        return createRedirectResult(Const.ActionURIs.USER_INFO_PAGE + "?id=" + user.getId());
        
    }

}
