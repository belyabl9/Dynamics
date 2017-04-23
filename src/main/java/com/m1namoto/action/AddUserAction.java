package com.m1namoto.action;

import com.m1namoto.domain.User;
import com.m1namoto.service.UsersService;
import com.m1namoto.utils.Const;
import com.m1namoto.utils.Utils;

public class AddUserAction extends Action {

    private String[] mandatoryParams = new String[] { "userType", "login", "password", "firstName", "surname" };
    
    private User createUser() throws Exception {
        Utils.checkMandatoryParams(requestParameters, mandatoryParams);

        User user = new User();
        String fullName = getRequestParamValue("firstName") + " " + getRequestParamValue("surname");
        user.setName(fullName);
        user.setLogin(getRequestParamValue("login"));
        user.setPassword(getRequestParamValue("password"));
        user.setUserType(User.Type.fromInt(Integer.parseInt(getRequestParamValue("userType"))));

        return UsersService.save(user);
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
            e.printStackTrace();
            System.out.println(e);
            return createRedirectResult(Const.ActionURIs.ADD_USER_PAGE);
        }

        return createRedirectResult(Const.ActionURIs.USER_INFO_PAGE + "?id=" + user.getId());
        
    }

}
