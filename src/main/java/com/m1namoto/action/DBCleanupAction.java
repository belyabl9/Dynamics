package com.m1namoto.action;

import com.m1namoto.service.EventsService;
import com.m1namoto.service.FeaturesService;
import com.m1namoto.service.SessionsService;
import com.m1namoto.service.UsersService;

public class DBCleanupAction extends Action {

    @Override
    protected ActionResult execute() throws Exception {
        FeaturesService.deleteAll();
        SessionsService.deleteAll();
        EventsService.removeAll();
        UsersService.removeAll();

        FeaturesService.clearFeatureMaps();
        
        return createAjaxResult(null);
    }

}
