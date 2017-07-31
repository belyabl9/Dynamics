package com.m1namoto.action;

import com.m1namoto.service.FeatureService;
import com.m1namoto.service.SessionService;
import com.m1namoto.service.UserService;

public class DBCleanupAction extends Action {

    @Override
    protected ActionResult execute() throws Exception {
        FeatureService.getInstance().removeAll();
        SessionService.removeAll();
        UserService.getInstance().removeAll();

        FeatureService.getInstance().invalidateFeatureCache();
        
        return createAjaxResult(null);
    }

}
