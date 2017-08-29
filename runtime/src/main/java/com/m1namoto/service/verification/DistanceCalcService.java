package com.m1namoto.service.verification;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DistanceCalcService {

    private DistanceCalcService() {}

    private static class LazyHolder {
        static final DistanceCalcService INSTANCE = new DistanceCalcService();
    }
    public static DistanceCalcService getInstance() {
        return LazyHolder.INSTANCE;
    }

    public double mahanabolis(@NotNull List<Double> trainingVector, @NotNull List<Double> testVector, @NotNull List<Double> deviationVector) {
        double sum = 0;
        for (int i = 0; i < trainingVector.size(); i++) {
            sum += Math.pow( (testVector.get(i) - trainingVector.get(i)) / deviationVector.get(i), 2);
        }
        return Math.sqrt(sum);
    }

    public double manhattan(@NotNull List<Double> trainingVector, @NotNull List<Double> testVector) {
        double sum = 0;
        for (int i = 0; i < trainingVector.size(); i++) {
            sum += Math.abs(testVector.get(i) - trainingVector.get(i));
        }
        return sum;
    }

    public double manhattanScaled(@NotNull List<Double> trainingVector, @NotNull List<Double> testVector, @NotNull List<Double> deviationVector) {
        double sum = 0;
        for (int i = 0; i < trainingVector.size(); i++) {
            sum += Math.abs(testVector.get(i) - trainingVector.get(i)) / deviationVector.get(i);
        }
        return sum;
    }

    public double dtw(@NotNull List<Double> trainingVector, @NotNull List<Double> testVector) {
        DTW dtw = new DTW(convert(trainingVector), convert(testVector));
        return dtw.getDistance();
    }

    private double[] convert(@NotNull List<Double> lst) {
        double[] target = new double[lst.size()];
        for (int i = 0; i < target.length; i++) {
            target[i] = lst.get(i);
        }
        return target;
    }

}
