package com.m1namoto.domain;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeaturesSample {

    public static final FeaturesSample EMPTY_SAMPLE = new FeaturesSample(Collections.<Double>emptyList());

    /**
     * May contain null values for absent sample values
     */
    @NotNull
    private final List<Double> features;
    private final boolean isEmpty;

    public FeaturesSample(@NotNull List<Double> features) {
        this.features = Collections.unmodifiableList(new ArrayList<>(features));
        if (features.isEmpty()) {
            isEmpty = true;
        } else {
            boolean isEmpty = true;
            for (Double val : features) {
                if (val != null) {
                    isEmpty = false;
                    break;
                }
            }
            this.isEmpty = isEmpty;
        }
    }

    public boolean isEmpty() {
        return isEmpty;
    }
    
    public List<Double> getFeatures() {
        return features;
    }

    /**
     * Determines the percentage of the non-null features in the list 
     * @return Percentage value in a range [0-100]
     */
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
