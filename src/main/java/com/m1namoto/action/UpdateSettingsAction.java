package com.m1namoto.action;

import java.util.HashMap;
import java.util.Map;

import com.m1namoto.page.PageData;
import com.m1namoto.utils.PropertiesService;

public class UpdateSettingsAction extends Action {

    @Override
    protected ActionResult execute() throws Exception {
        PageData data = new PageData();

        Map<String, String> settings = new HashMap<String, String>();

        String[] optNames = new String[] { "save_requests", "update_template", "learning_rate", "threshold" }; 
        for (String optName : optNames) {
            String value = getRequestParamValue(optName);
            settings.put(optName, value);
        }

        try {
            PropertiesService.setDynamicPropertyValues(settings);
        } catch(Exception e) {
            data.setError(true);
        }

        return createAjaxResult(data);
    }

}
