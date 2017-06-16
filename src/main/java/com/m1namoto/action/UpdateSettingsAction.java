package com.m1namoto.action;

import java.util.HashMap;
import java.util.Map;

import com.m1namoto.page.PageData;
import com.m1namoto.service.PropertiesService;

public class UpdateSettingsAction extends Action {

    private static final String[] OPTIONS = new String[] { "save_requests", "update_template", "learning_rate", "threshold" };

    @Override
    protected ActionResult execute() throws Exception {
        PageData data = new PageData();

        Map<String, String> settings = new HashMap<>();
        for (String optName : OPTIONS) {
            String value = getRequestParamValue(optName).get();
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
