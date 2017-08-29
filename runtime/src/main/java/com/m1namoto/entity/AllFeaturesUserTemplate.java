package com.m1namoto.entity;

import com.m1namoto.domain.FeaturesSample;
import com.m1namoto.domain.KeyCodePair;
import com.m1namoto.domain.User;
import com.m1namoto.service.FeatureSampleService;
import com.m1namoto.service.FeatureSelectionService;
import com.m1namoto.service.FeatureService;
import com.m1namoto.service.verification.DistanceCalcService;
import com.m1namoto.service.verification.VerificationType;
import com.m1namoto.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AllFeaturesUserTemplate {

    private static final String WRONG_FEATURE_SAMPLE_SIZE = "Feature samples must have the same size.";
    private static final String NO_FEATURE_SAMPLES = "Can not create user template without at least one feature sample.";
    private static final String ABSENT_FEATURE_SAMPLE_VALUES = "Feature sample values must not be absent.";

    private static final FeatureSampleService FEATURE_SAMPLE_SERVICE = FeatureSampleService.getInstance();

    private final List<Double> meanVector;
    private final List<Double> meanAbsDeviationVector;
    private final double threshold;

    @NotNull
    private final VerificationType verificationType;

    public AllFeaturesUserTemplate(@NotNull User user, @NotNull VerificationType verificationType) {
        this.verificationType = verificationType;
        List<FeaturesSample> samples = FEATURE_SAMPLE_SERVICE.getFeatureSamples(user, user.getPassword(), true);
        validate(samples);

        meanVector = getMeanVector(samples);
        meanAbsDeviationVector = getMeanAbsDeviationVector(samples);
        threshold = makeThreshold(samples);
    }

    public List<Double> getMeanVector() {
        return meanVector;
    }

    public List<Double> getMeanAbsDeviationVector() {
        return meanAbsDeviationVector;
    }

    public double getThreshold() {
        return threshold;
    }

    /**
     * User template consists of samples. Each sample has N features.
     * This structure is similar to matrix. We make N:M matrix to 1:M with mean value in each cell
     * @return vector of mean values for each feature
     */
    @NotNull
    private List<Double> getMeanVector(@NotNull List<FeaturesSample> samples) {
        int sampleSize = samples.get(0).getFeatures().size();
        List<Double> meanVector = new ArrayList<>(Collections.nCopies(sampleSize, 0d));

        for (FeaturesSample sample : samples) {
            List<Double> features = sample.getFeatures();
            for (int pos = 0; pos < features.size(); pos++) {
                double prevValue = meanVector.get(pos);
                meanVector.set(pos, prevValue + features.get(pos));
            }
        }
        for (int pos = 0; pos < meanVector.size(); pos++) {
            meanVector.set(pos, meanVector.get(pos) / samples.size());
        }

        return meanVector;
    }

    @NotNull
    private List<Double> getMeanAbsDeviationVector(@NotNull List<FeaturesSample> samples) {
        int sampleSize = samples.get(0).getFeatures().size();
        List<Double> deviationVector = new ArrayList<>(Collections.nCopies(sampleSize, 0d));
        List<Double> meanVector = getMeanVector(samples);

        for (FeaturesSample sample : samples) {
            List<Double> features = sample.getFeatures();
            for (int pos = 0; pos < features.size(); pos++) {
                double prevVal = deviationVector.get(pos);
                double newVal = Math.abs(features.get(pos) - meanVector.get(pos));
                deviationVector.set(pos, prevVal + newVal);
            }
        }

        for (int pos = 0; pos < deviationVector.size(); pos++) {
            deviationVector.set(pos, deviationVector.get(pos) / samples.size());
        }

        return deviationVector;
    }

    private double makeThreshold(@NotNull List<FeaturesSample> samples) {
        List<Double> distanceLst = new ArrayList<>();

        for (int i = 0; i < samples.size(); i++) {
            List<FeaturesSample> samplesCopy = new ArrayList<>(samples);
            FeaturesSample testSample = samplesCopy.remove(i);

            double distance;
            switch (verificationType) {
                case DTW:
                    distance = calcDtwDistance(samplesCopy, testSample);
                    break;
                case MANHATTAN:
                    distance = calcManhattanDistance(samplesCopy, testSample);
                    break;
                case MANHATTAN_SCALED:
                    distance = calcManhattanScaledDistance(samplesCopy, testSample);
                    break;
                case MAHANABOLIS:
                    distance = calcMahanabolisDistance(samplesCopy, testSample);
                    break;
                default:
                    throw new UnsupportedOperationException("Specified verification type is not supported.");
            }
            distanceLst.add(distance);
        }
        return Utils.mean(distanceLst);
//        return Collections.max(distanceLst);
    }

    private double calcManhattanScaledDistance(@NotNull List<FeaturesSample> samples, @NotNull FeaturesSample testSample) {
        int sampleSize = getSampleSize(samples);
        List<Double> meanVector = new ArrayList<>(Collections.nCopies(sampleSize, 0d));
        List<Double> deviationVector = new ArrayList<>(Collections.nCopies(sampleSize, 0d));

        for (FeaturesSample sample : samples) {
            List<Double> features = sample.getFeatures();
            for (int pos = 0; pos < features.size(); pos++) {
                meanVector.set(pos, meanVector.get(pos) + features.get(pos));
            }
        }

        for (int pos = 0; pos < sampleSize; pos++) {
            meanVector.set(pos, meanVector.get(pos) / samples.size());
        }

        for (FeaturesSample sample : samples) {
            List<Double> features = sample.getFeatures();
            for (int pos = 0; pos < features.size(); pos++) {
                deviationVector.set(pos, deviationVector.get(pos) + Math.abs(features.get(pos) - meanVector.get(pos)));
            }
        }

        for (int pos = 0; pos < sampleSize; pos++) {
            deviationVector.set(pos, deviationVector.get(pos) / samples.size());
        }

        return DistanceCalcService.getInstance().manhattanScaled(meanVector, testSample.getFeatures(), deviationVector);
    }

    private double calcMahanabolisDistance(@NotNull List<FeaturesSample> samples, @NotNull FeaturesSample testSample) {
        int sampleSize = getSampleSize(samples);
        List<Double> meanVector = new ArrayList<>(Collections.nCopies(sampleSize, 0d));
        List<Double> deviationVector = new ArrayList<>(Collections.nCopies(sampleSize, 0d));

        for (FeaturesSample sample : samples) {
            List<Double> features = sample.getFeatures();
            for (int pos = 0; pos < features.size(); pos++) {
                meanVector.set(pos, meanVector.get(pos) + features.get(pos));
            }
        }

        for (int pos = 0; pos < sampleSize; pos++) {
            meanVector.set(pos, meanVector.get(pos) / samples.size());
        }

        for (FeaturesSample sample : samples) {
            List<Double> features = sample.getFeatures();
            for (int pos = 0; pos < features.size(); pos++) {
                deviationVector.set(pos, deviationVector.get(pos) + Math.abs(features.get(pos) - meanVector.get(pos)));
            }
        }

        for (int pos = 0; pos < sampleSize; pos++) {
            deviationVector.set(pos, deviationVector.get(pos) / samples.size());
        }

        return DistanceCalcService.getInstance().mahanabolis(meanVector, testSample.getFeatures(), deviationVector);
    }

    private double calcManhattanDistance(@NotNull List<FeaturesSample> samples, @NotNull FeaturesSample testSample) {
        int sampleSize = getSampleSize(samples);
        List<Double> meanVector = new ArrayList<>(Collections.nCopies(sampleSize, 0d));

        for (FeaturesSample sample : samples) {
            List<Double> features = sample.getFeatures();
            for (int pos = 0; pos < features.size(); pos++) {
                meanVector.set(pos, meanVector.get(pos) + features.get(pos));
            }
        }

        for (int pos = 0; pos < sampleSize; pos++) {
            meanVector.set(pos, meanVector.get(pos) / samples.size());
        }

        return DistanceCalcService.getInstance().manhattan(meanVector, testSample.getFeatures());
    }

    private double calcDtwDistance(@NotNull List<FeaturesSample> samples, @NotNull FeaturesSample testSample) {
        int sampleSize = getSampleSize(samples);
        List<Double> meanVector = new ArrayList<>(Collections.nCopies(sampleSize, 0d));

        for (FeaturesSample sample : samples) {
            List<Double> features = sample.getFeatures();
            for (int pos = 0; pos < features.size(); pos++) {
                meanVector.set(pos, meanVector.get(pos) + features.get(pos));
            }
        }

        for (int pos = 0; pos < sampleSize; pos++) {
            meanVector.set(pos, meanVector.get(pos) / samples.size());
        }

        return DistanceCalcService.getInstance().dtw(meanVector, testSample.getFeatures());
    }

    private void validate(@NotNull List<FeaturesSample> samples) {
        if (samples.isEmpty()) {
            throw new RuntimeException(NO_FEATURE_SAMPLES);
        }

        Integer prevSize = null;

        for (FeaturesSample sample : samples){
            int sampleSize = sample.getFeatures().size();
            if (prevSize == null) {
                prevSize = sampleSize;
            } else if (prevSize != sampleSize) {
                throw new RuntimeException(WRONG_FEATURE_SAMPLE_SIZE);
            }

            for (Double val : sample.getFeatures()) {
                if (val == null) {
                    throw new RuntimeException(ABSENT_FEATURE_SAMPLE_VALUES);
                }
            }
        }
    }

    private int getSampleSize(@NotNull List<FeaturesSample> samples) {
        return samples.get(0).getFeatures().size();
    }
}
