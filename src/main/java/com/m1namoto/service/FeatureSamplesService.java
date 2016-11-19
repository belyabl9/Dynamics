package com.m1namoto.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.m1namoto.utils.ReleasePressPair;

public class FeatureSamplesService {
    final static Logger logger = Logger.getLogger(FeatureSamplesService.class);

    public static FeaturesSample getHoldFeaturesSampleByString(Map<Integer, List<Double>> holdFeaturesPerCode, String password) {
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
    
    public static FeaturesSample getReleasePressFeaturesSampleByString(
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
    
    public static FeaturesSample getCoordFeaturesSampleByString(Map<Integer, List<Double>> coordFeaturesPerCode, String password) {
        FeaturesSample sample = new FeaturesSample();

        List<Double> coordFeaturesSample = new ArrayList<Double>();
        for (char code : password.toCharArray()) {
            List<Double> featureValues = coordFeaturesPerCode.get((int)code);
            Double coordFeatureVal = null;
            if ((featureValues != null) && (featureValues.size() > 0)) {
                coordFeatureVal = featureValues.remove(0);
                sample.setEmpty(false);
            }
            coordFeaturesSample.add(coordFeatureVal);
        }
        
        sample.setFeatures(coordFeaturesSample);
        
        return sample;
    }
    
    
}
