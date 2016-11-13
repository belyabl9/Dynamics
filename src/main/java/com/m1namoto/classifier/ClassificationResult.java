package com.m1namoto.classifier;

public class ClassificationResult {
    
    private double probability;
    
    public ClassificationResult(double probability) {
        this.probability = probability;
    }
    
    public double getProbability() {
        return probability;
    }
    public void setProbability(double probability) {
        this.probability = probability;
    }
    
}
