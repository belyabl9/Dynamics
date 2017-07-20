package com.m1namoto.action;

import com.google.common.base.Optional;
import com.m1namoto.domain.User;
import com.m1namoto.service.UserService;
import com.m1namoto.utils.Const;
import com.m1namoto.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.StringJoiner;

public class AddUserAction extends Action {

    private static final String[] MANDATORY_PARAMS = new String[] { "userType", "login", "password", "firstName", "surname" };
    
    private User createUser() throws Exception {
        Utils.checkMandatoryParams(requestParameters, MANDATORY_PARAMS);

        User user = new User();
        user.setName(makeFullName());
        user.setLogin(getRequestParamValue("login").get());
        user.setPassword(getRequestParamValue("password").get());
        user.setUserType(User.Type.fromInt(
                Integer.parseInt(getRequestParamValue("userType").get())
        ));

        return UserService.save(user);
    }

    @NotNull
    private String makeFullName() {
        Optional<String> firstNameOpt = getRequestParamValue("firstName");
        Optional<String> surnameOpt = getRequestParamValue("surname");
        StringJoiner joiner = new StringJoiner(" ");
        if (firstNameOpt.isPresent()) {
            joiner.add(firstNameOpt.get());
        }
        if (surnameOpt.isPresent()) {
            joiner.add(surnameOpt.get());
        }
        return joiner.toString();
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
