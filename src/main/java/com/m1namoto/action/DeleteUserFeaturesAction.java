package com.m1namoto.action;

import com.m1namoto.domain.User;
import com.m1namoto.page.PageData;
import com.m1namoto.service.FeaturesService;

public class DeleteUserFeaturesAction extends Action {
    
    @Override
    protected ActionResult execute() throws Exception {
        String userId = getRequestParamValue("id");
        if (userId == null) {
            throw new Exception("User ID was not passed");
        }
        
        User user = new User();
        user.setId(Long.valueOf(userId));
        FeaturesService.deleteFeatures(user);
        
        PageData data = new PageData();
        
        return createAjaxResult(data);
    }
}
