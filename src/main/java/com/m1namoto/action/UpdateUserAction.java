package com.m1namoto.action;

import com.google.common.base.Optional;
import com.m1namoto.domain.User;
import com.m1namoto.page.UserInfoPageData;
import com.m1namoto.service.UserService;

public class UpdateUserAction extends Action {

    private static final String INVALID_USER_TYPE = "Can not parse user type.";
    private static final String USER_ID_MUST_BE_SPECIFIED = "User ID must be specified.";
    private static final String INVALID_USER_ID = "Can not parse user id.";
    private static final String USER_NOT_FOUND = "User with specified ID was not found.";

    private User updateUser(User user) {
        Optional<String> userTypeOpt = getRequestParamValue("userType");
        Optional<String> loginOpt = getRequestParamValue("login");
        Optional<String> passwordOpt = getRequestParamValue("password");
        Optional<String> firstNameOpt = getRequestParamValue("firstName");
        Optional<String> surnameOpt = getRequestParamValue("surname");

        if (userTypeOpt.isPresent()) {
            try {
                user.setUserType(User.Type.fromInt(Integer.parseInt(userTypeOpt.get())));
            } catch (Exception e) {
                throw new RuntimeException(INVALID_USER_TYPE);
            }
        }
        
        if (loginOpt.isPresent()) {
            user.setLogin(loginOpt.get());
        }
        if (passwordOpt.isPresent()) {
            user.setPassword(passwordOpt.get());
        }
        if (firstNameOpt.isPresent() && surnameOpt.isPresent()) {
            user.setName(firstNameOpt.get() + " " + surnameOpt.get());
        }
        
        return UserService.getInstance().save(user);
    }
    
    @Override
    protected ActionResult execute() throws Exception {
        Optional<String> idOpt = getRequestParamValue("id");
        if (!idOpt.isPresent()) {
            // TODO send error
            throw new RuntimeException(USER_ID_MUST_BE_SPECIFIED);
        }

        Optional<User> userOpt;
        try {
            userOpt = UserService.getInstance().findById(Long.valueOf(idOpt.get()));
        } catch (Exception e) {
            throw new RuntimeException(INVALID_USER_ID);
        }

        if (!userOpt.isPresent()) {
            throw new Exception(USER_NOT_FOUND);
        }
        
        User user = updateUser(userOpt.get());
        UserInfoPageData data = new UserInfoPageData();
        data.setUser(user);
        
        return createAjaxResult(data);
    }

}
