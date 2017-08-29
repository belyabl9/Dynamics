package com.m1namoto.api;

import com.google.common.base.Optional;
import org.jetbrains.annotations.NotNull;

public class ClassificationResult {

    private static final String WRONG_PROBABILITY_RANGE = "Probability must be in range [0-1].";

    private final double probability;

    @NotNull
    private final Optional<String> details;
    
    public ClassificationResult(double probability) {
        this(probability, Optional.<String>absent());
    }

    public ClassificationResult(double probability, @NotNull Optional<String> details) {
        if (probability < 0d || probability > 1d) {
            throw new IllegalArgumentException(WRONG_PROBABILITY_RANGE);
        }
        this.probability = probability;
        this.details = details;
    }

    /**
     * Returns the similarity percentage in the range [0-1].
     * 1 - absolute similarity
     * @return Similarity percentage
     */
    public double getProbability() {
        return probability;
    }

    @NotNull
    public Optional<String> getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "ClassificationResult{" +
                "probability=" + probability +
                ", details=" + details +
                '}';
    }
}
