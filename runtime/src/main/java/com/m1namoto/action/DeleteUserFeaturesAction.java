package com.m1namoto.action;

import com.google.common.base.Optional;
import com.m1namoto.domain.User;
import com.m1namoto.page.PageData;
import com.m1namoto.service.FeatureService;

public class DeleteUserFeaturesAction extends Action {

    private static final String USER_ID_WAS_NOT_PASSED = "User ID was not passed";
    private static final String INVALID_USER_ID = "Can not parse user id";

    @Override
    protected ActionResult execute() throws Exception {
        Optional<String> userIdOpt = getRequestParamValue("id");
        if (!userIdOpt.isPresent()) {
            throw new Exception(USER_ID_WAS_NOT_PASSED);
        }
        
        User user = new User();
        try {
            user.setId(Long.valueOf(userIdOpt.get()));
        } catch (Exception e) {
            throw new Exception(INVALID_USER_ID + ": " + userIdOpt.get());
        }
        FeatureService.getInstance().remove(user);
        
        return createAjaxResult(new PageData());
    }
}
