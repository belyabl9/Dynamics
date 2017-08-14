package com.m1namoto.action;

import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.m1namoto.domain.HoldFeature;
import com.m1namoto.domain.User;
import com.m1namoto.domain.UserStatistics;
import com.m1namoto.page.StatisticsJsonPageData;
import com.m1namoto.service.FeatureService;
import com.m1namoto.service.UserService;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeypressPlotDataAction extends Action {

    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    private List<UserStatistics> getStatisticsPerUser() {
        List<UserStatistics> statList = new ArrayList<>();

        Map<Long, List<HoldFeature>> meanHoldFeatures = makeMeanHoldFeatureValuesPerUser();
        for (Long userId : meanHoldFeatures.keySet()) {
            Optional<User> userOpt = UserService.getInstance().findById(userId);
            if (userOpt.isPresent()) {
                UserStatistics userStat = new UserStatistics(userOpt.get(), meanHoldFeatures.get(userId));
                statList.add(userStat);
            }
        }
        
        return statList;
    }

    @NotNull
    private Map<Long, List<HoldFeature>> makeMeanHoldFeatureValuesPerUser() {
        ListMultimap<Long, HoldFeature> meanHoldFeatures = ArrayListMultimap.create();
        Map<Long, Map<Integer, List<HoldFeature>>> userHoldFeaturesMap = FeatureService.getInstance().getUserHoldFeaturesMap();

        for (Map.Entry<Long, Map<Integer, List<HoldFeature>>> entry : userHoldFeaturesMap.entrySet()) {
            Map<Integer, List<HoldFeature>> holdFeaturesPerCode = entry.getValue();

            for (Map.Entry<Integer, List<HoldFeature>> innerEntry : holdFeaturesPerCode.entrySet()) {
                Optional<Double> meanTimeOpt = FeatureService.getInstance().getMeanTime(innerEntry.getValue());
                if (meanTimeOpt.isPresent()) {
                    HoldFeature holdFeature = new HoldFeature();
                    holdFeature.setCode(innerEntry.getKey());
                    holdFeature.setValue(meanTimeOpt.get());

                    meanHoldFeatures.put(entry.getKey(), holdFeature);
                }
            }
        }
        return Multimaps.asMap(meanHoldFeatures);
    }

    @Override
    protected ActionResult execute() throws Exception {
        List<UserStatistics> statList = getStatisticsPerUser();
        String statJson = GSON.toJson(statList);
        
        StatisticsJsonPageData data = new StatisticsJsonPageData();
        data.setStatisticsJson(StringEscapeUtils.unescapeEcmaScript(statJson));

        return createAjaxResult(data);
    }

}
