package com.m1namoto.action;

import com.m1namoto.service.FeatureService;
import com.m1namoto.service.SessionService;
import com.m1namoto.service.UserService;
import com.m1namoto.utils.Utils;

public class DBCleanupAction extends Action {

    @Override
    protected ActionResult execute() throws Exception {
        if (!Utils.isTest()) {
            throw new RuntimeException("DB cleanup action is allowed only for testing purposes.");
        }

        FeatureService.getInstance().removeAll();
        SessionService.removeAll();
        UserService.getInstance().removeAll();

        FeatureService.getInstance().invalidateFeatureCache();
        
        return createAjaxResult(null);
    }

}
