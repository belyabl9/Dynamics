package com.m1namoto.action;

import java.util.Map;

import com.m1namoto.page.SettingsPageData;
import com.m1namoto.utils.Const;
import com.m1namoto.service.PropertiesService;

public class SettingsPageAction extends Action {

    @Override
    protected ActionResult execute() {
        SettingsPageData data = new SettingsPageData();
        
        Map<String, String> settings = PropertiesService.getInstance().getDynamicPropertyValues();
        data.setSettings(settings);

        return createShowPageResult(Const.ViewURIs.SETTINGS, data);
    }

}
