package com.m1namoto.service;

import java.util.List;

public class FeaturesSample {
    private List<Double> features;
    private boolean isEmpty = true;
    
    public void setFeatures(List<Double> features) {
        this.features = features;
    }

    public void setEmpty(boolean isEmpty) {
        this.isEmpty = isEmpty;
    }

    public boolean isEmpty() {
        return isEmpty;
    }
    
    public List<Double> getFeatures() {
        return features;
    }

    public double definedElementsPercentage() {
        int definedCnt = 0;
        for (Double val : features) {
            if (val != null) {
                definedCnt++;
            }
        }
        
        return definedCnt == 0 ? 0 : (definedCnt * 100) / features.size();
    }
    
}
