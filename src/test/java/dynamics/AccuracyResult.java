package dynamics;

import java.util.Map;

public class AccuracyResult {

    private int passwordLength;
    private int learingRate;
    
    private Map<Double, Double> farResults;
    private Map<Double, Double> frrResults;
    
    public AccuracyResult(int passwordLength, int learningRate, Map<Double, Double> farResults, Map<Double, Double> frrResults) {
        this.passwordLength = passwordLength;
        this.learingRate = learningRate;
        this.farResults = farResults;
        this.frrResults = frrResults;
    }
    
    public int getPasswordLength() {
        return passwordLength;
    }

    public void setPasswordLength(int passwordLength) {
        this.passwordLength = passwordLength;
    }

    public int getLearingRate() {
        return learingRate;
    }

    public void setLearingRate(int learingRate) {
        this.learingRate = learingRate;
    }

    public Map<Double, Double> getFarResults() {
        return farResults;
    }

    public void setFarResults(Map<Double, Double> farResults) {
        this.farResults = farResults;
    }

    public Map<Double, Double> getFrrResults() {
        return frrResults;
    }

    public void setFrrResults(Map<Double, Double> frrResults) {
        this.frrResults = frrResults;
    }
    
}
