package com.m1namoto.action;

import com.google.common.base.Optional;
import com.m1namoto.domain.User;
import com.m1namoto.page.UserInfoPageData;
import com.m1namoto.service.UserService;

public class UpdateUserAction extends Action {

    private User updateUser(User user) {
        String userType = getRequestParamValue("userType");
        String login = getRequestParamValue("login");
        String password = getRequestParamValue("password");
        String firstName = getRequestParamValue("firstName");
        String surname = getRequestParamValue("surname");
        
        if (userType != null) {
            user.setUserType(User.Type.fromInt(Integer.valueOf(userType)));
        }
        
        if (login != null) {
            user.setLogin(login);
        }
        if (password != null) {
            user.setPassword(password);
        }
        if (firstName != null && surname != null) {
            user.setName(firstName + " " + surname);
        }
        
        return UserService.save(user);
    }
    
    @Override
    protected ActionResult execute() throws Exception {
        String id = getRequestParamValue("id");
        if (id == null) {
            // TODO send error
            throw new Exception("User ID was not passed");
        }
        
        Optional<User> userOpt = UserService.findById(Long.valueOf(id));
        if (!userOpt.isPresent()) {
            throw new Exception("User with specified ID was not found");
        }
        
        User user = updateUser(userOpt.get());
        
        UserInfoPageData data = new UserInfoPageData();
        data.setUser(user);
        
        return createAjaxResult(data);
    }

}
