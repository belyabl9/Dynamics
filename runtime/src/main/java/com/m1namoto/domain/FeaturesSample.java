package com.m1namoto.domain;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configuration in .arff format contains a list of instances.
 * Every instance contains a list of feature values and class value.
 * Feature values are represented as double values.
 * Class value is an integer from defined set of available classes.
 *
 * FeaturesSample class represents feature values from instance in configuration used by learning classifier
 */
public class FeaturesSample {

    public static final FeaturesSample EMPTY_SAMPLE = new FeaturesSample();

    /**
     * May contain null values for absent sample values
     */
    @NotNull
    private final List<Double> features;
    private final boolean isEmpty;

    private FeaturesSample() {
        this.features = Collections.emptyList();
        this.isEmpty = true;
    }

    public FeaturesSample(@NotNull List<Double> features) {
        // todo can't use ImmutableList.copyOf() because of null values
        this.features = Collections.unmodifiableList(new ArrayList<>(features));

        boolean isEmpty = true;
        for (Double val : features) {
            if (val != null) {
                isEmpty = false;
                break;
            }
        }
        this.isEmpty = isEmpty;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    @NotNull
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
