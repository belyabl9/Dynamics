package com.m1namoto.classifier;

public class ClassificationResult {
    
    private int predictedClass;
    private double probability;
    
    public ClassificationResult(int predictedClass, double probability) {
        this.predictedClass = predictedClass;
        this.probability = probability;
    }
    
    public int getPredictedClass() {
        return predictedClass;
    }
    public void setPredictedClass(int predictedClass) {
        this.predictedClass = predictedClass;
    }
    public double getProbability() {
        return probability;
    }
    public void setProbability(double probability) {
        this.probability = probability;
    }
    
}
