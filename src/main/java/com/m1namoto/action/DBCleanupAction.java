package com.m1namoto.action;

import com.m1namoto.service.EventService;
import com.m1namoto.service.FeatureService;
import com.m1namoto.service.SessionService;
import com.m1namoto.service.UsersService;

public class DBCleanupAction extends Action {

    @Override
    protected ActionResult execute() throws Exception {
        FeatureService.removeAll();
        SessionService.removeAll();
        EventService.removeAll();
        UsersService.removeAll();

        FeatureService.clearFeatureMaps();
        
        return createAjaxResult(null);
    }

}
