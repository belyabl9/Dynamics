package com.m1namoto.action;

import com.m1namoto.domain.User;
import com.m1namoto.page.UserInfoPageData;
import com.m1namoto.service.Users;
import com.m1namoto.utils.Const;

public class UserInfoAction extends Action {

    @Override
    protected ActionResult execute() {
        String userId = getRequestParamValue("id");
        
        System.out.println("User ID: " + userId);
        
        if (userId == null) {
            // TODO
            return null;
        }
        
        User user = Users.findById(Long.valueOf(userId));
        if (user == null) {
            
        }

        UserInfoPageData data = new UserInfoPageData();
        data.setUser(user);
        
        return createShowPageResult(Const.ViewURIs.USER_INFO, data);
    }

}
