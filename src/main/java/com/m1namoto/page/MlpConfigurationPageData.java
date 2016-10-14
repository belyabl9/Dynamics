package com.m1namoto.page;

public class MlpConfigurationPageData extends PageData {

    private int hiddenLayers;
    private float learningRate;

    public int getHiddenLayers() {
        return hiddenLayers;
    }
    public void setHiddenLayers(int hiddenLayers) {
        this.hiddenLayers = hiddenLayers;
    }
    public float getLearningRate() {
        return learningRate;
    }
    public void setLearningRate(float learningRate) {
        this.learningRate = learningRate;
    }
    
}
