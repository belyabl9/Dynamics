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
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class UserTemplate {

    private static final String WRONG_FEATURE_SAMPLE_SIZE = "Feature samples must have the same size.";
    private static final String NO_FEATURE_SAMPLES = "Can not create user template without at least one feature sample.";
    private static final String ABSENT_FEATURE_SAMPLE_VALUES = "Feature sample values must not be absent.";

    private List<FeaturesSample> holdFeatureSamples;
    private List<FeaturesSample> releasePressFeatureSamples;
    private List<FeaturesSample> pressPressFeatureSamples;

    private static final FeatureSampleService FEATURE_SAMPLE_SERVICE = FeatureSampleService.getInstance();
    private static final FeatureService FEATURE_SERVICE = FeatureService.getInstance();

    private final Map<FeatureType, List<Double>> meanVectors;
    private final Map<FeatureType, List<Double>> meanAbsDeviationVectors;
    private final Map<FeatureType, Double> thresholds;

    @NotNull
    private final VerificationType verificationType;

    public UserTemplate(@NotNull User user, @NotNull VerificationType verificationType) {
        this.verificationType = verificationType;

        holdFeatureSamples = getHoldFeatureSamples(user);
        validate(holdFeatureSamples);

        releasePressFeatureSamples = getReleasePressFeatureSamples(user);
        validate(releasePressFeatureSamples);

        pressPressFeatureSamples = getPressPressFeatureSamples(user);
        validate(pressPressFeatureSamples);

        meanVectors = getMeanVectors();
        meanAbsDeviationVectors = getMeanAbsDeviationVectors();
        thresholds = getThresholds();
    }

    @NotNull
    private List<FeaturesSample> getHoldFeatureSamples(@NotNull User user) {
        List<FeaturesSample> holdFeatureSamples = new ArrayList<>();
        if (FeatureSelectionService.getInstance().getFeatureTypes().contains(FeatureType.HOLD)) {
            Map<Integer, List<Double>> holdFeaturesByString = FEATURE_SERVICE.getHoldFeatureValuesByString(user, user.getPassword());

            boolean hasElements = true;
            while (hasElements) {
                FeaturesSample holdFeaturesSample = FEATURE_SAMPLE_SERVICE.getHoldFeaturesSampleByString(holdFeaturesByString, user.getPassword());
                if (holdFeaturesSample.definedElementsPercentage() < 100) {
                    break;
                }
                holdFeatureSamples.add(holdFeaturesSample);
            }
        }
        return holdFeatureSamples;
    }

    private List<FeaturesSample> getReleasePressFeatureSamples(@NotNull User user) {
        List<FeaturesSample> releasePressFeatureSamples = new ArrayList<>();
        if (FeatureSelectionService.getInstance().getFeatureTypes().contains(FeatureType.RELEASE_PRESS)) {
            Map<KeyCodePair, List<Double>> releasePressFeaturesByString = FEATURE_SERVICE.getReleasePressFeatureValuesByString(user, user.getPassword());

            boolean hasElements = true;
            while (hasElements) {
                FeaturesSample releasePressFeaturesSample = FEATURE_SAMPLE_SERVICE.getReleasePressFeaturesSampleByString(releasePressFeaturesByString, user.getPassword());
                if (releasePressFeaturesSample.definedElementsPercentage() < 100) {
                    break;
                }
                releasePressFeatureSamples.add(releasePressFeaturesSample);
            }
        }
        return releasePressFeatureSamples;
    }

    private List<FeaturesSample> getPressPressFeatureSamples(@NotNull User user) {
        List<FeaturesSample> pressPressFeatureSamples = new ArrayList<>();
        if (FeatureSelectionService.getInstance().getFeatureTypes().contains(FeatureType.PRESS_PRESS)) {
            Map<KeyCodePair, List<Double>> pressPressFeaturesByString = FEATURE_SERVICE.getPressPressFeatureValuesByString(user, user.getPassword());

            boolean hasElements = true;
            while (hasElements) {
                FeaturesSample pressPressFeaturesSample = FEATURE_SAMPLE_SERVICE.getPressPressFeaturesSampleByString(pressPressFeaturesByString, user.getPassword());
                if (pressPressFeaturesSample.definedElementsPercentage() < 100) {
                    break;
                }
                pressPressFeatureSamples.add(pressPressFeaturesSample);
            }
        }
        return pressPressFeatureSamples;
    }

    @NotNull
    private Map<FeatureType, List<Double>> getMeanVectors() {
        Map<FeatureType, List<Double>> meanVectorsMap = new HashMap<>();
        for (FeatureType featureType : FeatureSelectionService.getInstance().getFeatureTypes()) {
            switch (featureType) {
                case HOLD:
                    meanVectorsMap.put(featureType, getMeanVector(holdFeatureSamples));
                    break;
                case RELEASE_PRESS:
                    meanVectorsMap.put(featureType, getMeanVector(releasePressFeatureSamples));
                    break;
                case PRESS_PRESS:
                    meanVectorsMap.put(featureType, getMeanVector(pressPressFeatureSamples));
                    break;
                default:
                    throw new UnsupportedOperationException("Specified feature type is not supported.");
            }
        }
        return meanVectorsMap;
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
    private Map<FeatureType, List<Double>> getMeanAbsDeviationVectors() {
        Map<FeatureType, List<Double>> meanVectorsMap = new HashMap<>();
        for (FeatureType featureType : FeatureSelectionService.getInstance().getFeatureTypes()) {
            switch (featureType) {
                case HOLD:
                    meanVectorsMap.put(featureType, getMeanAbsDeviationVector(holdFeatureSamples));
                    break;
                case RELEASE_PRESS:
                    meanVectorsMap.put(featureType, getMeanAbsDeviationVector(releasePressFeatureSamples));
                    break;
                case PRESS_PRESS:
                    meanVectorsMap.put(featureType, getMeanAbsDeviationVector(pressPressFeatureSamples));
                    break;
                default:
                    throw new UnsupportedOperationException("Specified feature type is not supported.");
            }
        }
        return meanVectorsMap;
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

    @Nullable
    public Double getThreshold(@NotNull FeatureType featureType) {
        return thresholds.get(featureType);
    }

    @NotNull
    private Map<FeatureType, Double> getThresholds() {
        Map<FeatureType, Double> thresholdsMap = new HashMap<>();
        for (FeatureType featureType : FeatureSelectionService.getInstance().getFeatureTypes()) {
            thresholdsMap.put(featureType, makeThreshold(featureType));
        }
        return thresholdsMap;
    }

    @Nullable
    public List<Double> getMeanVector(@NotNull FeatureType featureType) {
        return meanVectors.get(featureType);
    }

    @Nullable
    public List<Double> getMeanAbsDeviationVector(@NotNull FeatureType featureType) {
        return meanAbsDeviationVectors.get(featureType);
    }

    private double makeThreshold(@NotNull FeatureType featureType) {
        List<Double> distanceLst = new ArrayList<>();

        List<FeaturesSample> samples;
        switch (featureType) {
            case HOLD:
                samples = holdFeatureSamples;
                break;
            case RELEASE_PRESS:
                samples = releasePressFeatureSamples;
                break;
            case PRESS_PRESS:
                samples = pressPressFeatureSamples;
                break;
            default:
                throw new UnsupportedOperationException("Specified feature type is not supported.");
        }
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
