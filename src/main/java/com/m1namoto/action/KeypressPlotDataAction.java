package com.m1namoto.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import org.apache.commons.lang3.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.m1namoto.domain.HoldFeature;
import com.m1namoto.domain.User;
import com.m1namoto.domain.UserStatistics;
import com.m1namoto.page.StatisticsJsonPageData;
import com.m1namoto.service.FeaturesService;
import com.m1namoto.service.UsersService;

public class KeypressPlotDataAction extends Action {

    private List<UserStatistics> getStatisticsPerUser() {
        List<UserStatistics> statList = new ArrayList<UserStatistics>();
        
        Map<Long, List<HoldFeature>> holdFeaturesMap = FeaturesService.getHoldFeaturesPerUser();
        for (Long userId : holdFeaturesMap.keySet()) {
            Optional<User> userOpt = UsersService.findById(userId);
            if (userOpt.isPresent()) {
                UserStatistics userStat = new UserStatistics(userOpt.get(), holdFeaturesMap.get(userId));
                statList.add(userStat);
            }
        }
        
        return statList;
    }
    
    @Override
    protected ActionResult execute() throws Exception {
        List<UserStatistics> statList = getStatisticsPerUser();
        
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        
        String statJson = gson.toJson(statList);
        
        StatisticsJsonPageData data = new StatisticsJsonPageData();
        data.setStatisticsJson(StringEscapeUtils.unescapeEcmaScript(statJson));

        return createAjaxResult(data);
    }

}
