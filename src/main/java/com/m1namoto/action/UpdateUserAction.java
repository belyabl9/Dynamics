package com.m1namoto.action;

import com.m1namoto.domain.User;
import com.m1namoto.page.UserInfoPageData;
import com.m1namoto.service.UsersService;

public class UpdateUserAction extends Action {

    private User updateUser(User user) {
        String userType = getRequestParamValue("userType");
        String login = getRequestParamValue("login");
        String password = getRequestParamValue("password");
        String firstName = getRequestParamValue("firstName");
        String surname = getRequestParamValue("surname");
        
        if (userType != null) {
            user.setUserType(Integer.valueOf(userType));
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
        
        return UsersService.save(user);
    }
    
    @Override
    protected ActionResult execute() throws Exception {
        String id = getRequestParamValue("id");
        if (id == null) {
            // TODO send error
            throw new Exception("User ID was not passed");
        }
        
        User user = UsersService.findById(Long.valueOf(id));
        if (user == null) {
            throw new Exception("User with specified ID was not found");
        }
        
        user = updateUser(user);
        
        UserInfoPageData data = new UserInfoPageData();
        data.setUser(user);
        
        return createAjaxResult(data);
    }

}
