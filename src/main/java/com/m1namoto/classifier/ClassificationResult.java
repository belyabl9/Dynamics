package com.m1namoto.classifier;

public class ClassificationResult {
    
    private double probability;
    
    public ClassificationResult(double probability) {
        this.probability = probability;
    }
    
    /**
     * Returns the similarity percentage in the rage [0-1].
     * 1 - absolute similarity
     * @return Similarity percentage
     */
    public double getProbability() {
        return probability;
    }
    public void setProbability(double probability) {
        this.probability = probability;
    }
    
}
