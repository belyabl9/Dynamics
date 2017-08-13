package com.m1namoto.action;

import java.util.*;

import com.google.common.collect.ImmutableSet;
import com.m1namoto.page.SettingsPageData;
import com.m1namoto.utils.Const;
import com.m1namoto.service.PropertiesService;

public class SettingsPageAction extends Action {

    private static final Set<String> OPTIONS = ImmutableSet.of(
            "save_requests", "update_template", "learning_rate", "threshold"
    );

    private static final Map<String, String> DEFAULT_VALUES = new HashMap<>();

    static {
        DEFAULT_VALUES.put("save_requests", "false");
        DEFAULT_VALUES.put("update_template", "true");
        DEFAULT_VALUES.put("learning_rate", "5");
        DEFAULT_VALUES.put("threshold", "0.7");
    }

    @Override
    protected ActionResult execute() {
        SettingsPageData data = new SettingsPageData();
        
        Map<String, String> dynamicsProperties = PropertiesService.getInstance().getDynamicPropertyValues();

        Map<String, String> settings = new HashMap<>();

        for (String optionName : OPTIONS) {
            String val = dynamicsProperties.get(optionName);
            if (val == null) {
                val = DEFAULT_VALUES.get(optionName);
            }
            settings.put(optionName, val);
        }

        data.setSettings(settings);

        return createShowPageResult(Const.ViewURIs.SETTINGS, data);
    }

}
