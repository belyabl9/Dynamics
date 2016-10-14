package com.m1namoto.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import com.google.gson.Gson;
import com.m1namoto.domain.UserStatistics;
import com.m1namoto.page.StatisticsPageData;
import com.m1namoto.utils.Const;

public class StatisticsPageAction extends Action {
    
    private List<UserStatistics> getStatisticsPerUser() {
     //   List<SessionFeatures> sessionFeaturesList = Features.getList();
        
        List<UserStatistics> statisticsList = new ArrayList<UserStatistics>();
       /* UserStatistics statistics = null;
        long userId = -1;
        for (SessionFeatures features : sessionFeaturesList) {
            long curUserId = features.getUser().getId();
            if (userId != curUserId) {
                userId = curUserId;
                if (statistics != null) {
                    statisticsList.add(statistics);
                }
                statistics = new UserStatistics();
                
                User user = Users.findById(curUserId);
                statistics.setUser(user);
            }*/
      //      statistics.addKeypressTime(features.getHoldTime());
       //     statistics.addTimeBetweenKeypress(features.getReleasePressTime());
        //}

        return statisticsList;
    }
    
    @Override
    protected ActionResult execute() throws Exception {
        List<UserStatistics> statList = getStatisticsPerUser();
        String statJson = new Gson().toJson(statList);
        
        StatisticsPageData data = new StatisticsPageData();
        data.setStatisticsJson(StringEscapeUtils.unescapeEcmaScript(statJson));
        
        return createShowPageResult(Const.ViewURIs.STATISTICS, data);
    }

}
