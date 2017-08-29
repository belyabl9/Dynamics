package com.m1namoto.action;

import com.google.common.base.Optional;
import com.m1namoto.domain.User;
import com.m1namoto.page.UserInfoPageData;
import com.m1namoto.service.UserService;
import com.m1namoto.utils.Const;

public class UserInfoAction extends Action {

    private static final String USER_ID_MUST_BE_SPECIFIED = "User ID must be specified.";
    private static final String INVALID_USER_ID = "Can not parse user ID.";
    private static final String USER_NOT_FOUND = "Can not find a user with specified id.";

    @Override
    protected ActionResult execute() {
        Optional<String> userIdOpt = getRequestParamValue("id");
        if (!userIdOpt.isPresent()) {
            throw new RuntimeException(USER_ID_MUST_BE_SPECIFIED);
        }

        Optional<User> userOpt;
        try {
            userOpt = UserService.getInstance().findById(Long.valueOf(userIdOpt.get()));
        } catch (Exception e) {
            throw new RuntimeException(INVALID_USER_ID);
        }
        if (!userOpt.isPresent()) {
            throw new RuntimeException(USER_NOT_FOUND);
        }

        UserInfoPageData data = new UserInfoPageData();
        data.setUser(userOpt.get());
        
        return createShowPageResult(Const.ViewURIs.USER_INFO, data);
    }

}
