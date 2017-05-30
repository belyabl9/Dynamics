package com.m1namoto.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.m1namoto.domain.FeaturesSample;
import com.m1namoto.domain.ReleasePressPair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.m1namoto.domain.FeaturesSample.EMPTY_SAMPLE;

public class FeatureSampleService {
    private static final Logger log = Logger.getLogger(FeatureSampleService.class);

    /**
     * Returns a sample of hold features by string
     * from a map of feature values grouped by key code
     * @param holdFeaturesPerCode - Map of feature values grouped by key code
     * @param password - String which is iterated by symbols to get corresponding features
     * @return Hold features sample
     */
    public static FeaturesSample getHoldFeaturesSampleByString(@Nullable Map<Integer, List<Double>> holdFeaturesPerCode,
                                                               @NotNull String password) {
        if (holdFeaturesPerCode == null) {
        	return EMPTY_SAMPLE;
        }
        
        List<Double> holdFeaturesSampleLst = new ArrayList<>();
        for (char code : password.toCharArray()) {
            List<Double> featureValues = holdFeaturesPerCode.get((int)code);
            if (CollectionUtils.isNotEmpty(featureValues)) {
                holdFeaturesSampleLst.add(featureValues.remove(0));
            } else {
                holdFeaturesSampleLst.add(null);
            }
        }
        return new FeaturesSample(holdFeaturesSampleLst);
    }
    
    /**
     * Returns a sample of release-press features by string
     * from a map of feature values grouped by key code
     * @param releasePressFeaturesPerCode - Map of feature values grouped by release-press key codes pair
     * @param password - String which is iterated by symbols to get corresponding features
     * @return Release-press features sample
     */
    public static FeaturesSample getReleasePressFeaturesSampleByString(
            @Nullable Map<ReleasePressPair, List<Double>> releasePressFeaturesPerCode,
            @NotNull String password
    ) {
        if (releasePressFeaturesPerCode == null) {
        	return EMPTY_SAMPLE;
        }
        
        char[] passwordCharacters = password.toCharArray();
        List<Double> releasePressSampleLst = new ArrayList<>();
        for (int i = 1; i < passwordCharacters.length; i++) {
            char releaseCode = passwordCharacters[i-1],
                 pressCode = passwordCharacters[i];
            ReleasePressPair codePair = new ReleasePressPair(releaseCode, pressCode);
            List<Double> releasePressValues = releasePressFeaturesPerCode.get(codePair);

            if (CollectionUtils.isNotEmpty(releasePressValues)) {
                releasePressSampleLst.add(releasePressValues.remove(0));
            } else {
                releasePressSampleLst.add(null);
            }
        }
        return new FeaturesSample(releasePressSampleLst);
    }

}
