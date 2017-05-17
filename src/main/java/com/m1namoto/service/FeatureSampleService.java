package com.m1namoto.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.m1namoto.domain.FeaturesSample;
import com.m1namoto.domain.ReleasePressPair;

public class FeatureSampleService {
    final static Logger logger = Logger.getLogger(FeatureSampleService.class);

    private static final FeatureSampleService INSTANCE = new FeatureSampleService();
    private FeatureSampleService() {}

    public static FeatureSampleService getInstance() {
        return INSTANCE;
    }

    /**
     * Returns a sample of hold features by string
     * from a map of feature values grouped by key code
     * @param holdFeaturesPerCode - Map of feature values grouped by key code
     * @param password - String which is iterated by symbols to get corresponding features
     * @return Hold features sample
     */
    public FeaturesSample getHoldFeaturesSampleByString(Map<Integer, List<Double>> holdFeaturesPerCode, String password) {
        FeaturesSample sample = new FeaturesSample();

        if (holdFeaturesPerCode == null) {
        	sample.setEmpty(true);
        	return sample;
        }
        
        List<Double> holdFeaturesSample = new ArrayList<Double>();
        for (char code : password.toCharArray()) {
            List<Double> featureValues = holdFeaturesPerCode.get((int)code);
            Double holdFeatureVal = null;
            if ((featureValues != null) && (featureValues.size() > 0)) {
                holdFeatureVal = featureValues.remove(0);
                sample.setEmpty(false);
            }
            holdFeaturesSample.add(holdFeatureVal);
        }
        
        sample.setFeatures(holdFeaturesSample);
        
        return sample;
    }
    
    /**
     * Returns a sample of release-press features by string
     * from a map of feature values grouped by key code
     * @param holdFeaturesPerCode - Map of feature values grouped by release-press key codes pair
     * @param password - String which is iterated by symbols to get corresponding features
     * @return Release-press features sample
     */
    public FeaturesSample getReleasePressFeaturesSampleByString(
            Map<ReleasePressPair, List<Double>> releasePressFeaturesPerCode, String password) {
       
        FeaturesSample sample = new FeaturesSample();

        if (releasePressFeaturesPerCode == null) {
        	sample.setEmpty(true);
        	return sample;
        }
        
        char[] passwordCharacters = password.toCharArray();
        List<Double> releasePressSample = new ArrayList<Double>();
        for (int i = 1; i < passwordCharacters.length; i++) {
            char releaseCode = passwordCharacters[i-1],
                 pressCode = passwordCharacters[i];
            ReleasePressPair codePair = new ReleasePressPair(releaseCode, pressCode);
            List<Double> releasePressValues = releasePressFeaturesPerCode.get(codePair);
            
            Double releasePressValue = null;
            
            if (releasePressValues != null && releasePressValues.size() > 0) {
                releasePressValue = releasePressValues.remove(0);
                sample.setEmpty(false);
            }

            releasePressSample.add(releasePressValue);
        }
        
        sample.setFeatures(releasePressSample);
        
        return sample;
    }

}
