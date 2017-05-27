package com.m1namoto.action;

import com.google.common.base.Optional;
import com.m1namoto.domain.User;
import com.m1namoto.page.UserInfoPageData;
import com.m1namoto.service.UserService;
import com.m1namoto.utils.Const;

public class UserInfoAction extends Action {

    @Override
    protected ActionResult execute() {
        String userId = getRequestParamValue("id");
        if (userId == null) {
            // TODO
            return null;
        }
        
        Optional<User> userOpt = UserService.findById(Long.valueOf(userId));
        if (!userOpt.isPresent()) {
            return null;
        }

        UserInfoPageData data = new UserInfoPageData();
        data.setUser(userOpt.get());
        
        return createShowPageResult(Const.ViewURIs.USER_INFO, data);
    }

}
