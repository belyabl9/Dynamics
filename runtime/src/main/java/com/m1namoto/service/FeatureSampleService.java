package com.m1namoto.service;

import com.m1namoto.domain.FeaturesSample;
import com.m1namoto.domain.KeyCodePair;
import com.m1namoto.domain.User;
import com.m1namoto.entity.FeatureType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeatureSampleService {
    private static final Logger log = Logger.getLogger(FeatureSampleService.class);

    private final static int ORIGIN_FEATURE_THRESHOLD = 100;
    private final static int OTHER_FEATURE_THRESHOLD = 80;

    private static final String PASSWORD_MUST_BE_SPECIFIED = "Password must be specified.";

    private static final FeatureSampleService FEATURE_SAMPLE_SERVICE = FeatureSampleService.getInstance();
    private static final FeatureService FEATURE_SERVICE = FeatureService.getInstance();

    private FeatureSampleService() {}

    private static class LazyHolder {
        static final FeatureSampleService INSTANCE = new FeatureSampleService();
    }
    public static FeatureSampleService getInstance() {
        return FeatureSampleService.LazyHolder.INSTANCE;
    }

    /**
     * It collects as many full {@link FeaturesSample} as possible for specified user
     * @param fullSample for user who passes authentication procedure we require more complete sample.
     *                   This parameter is used to differ a user who passes auth. from other users
     * @return a list of feature samples for specified user.
     */
    @NotNull
    public List<FeaturesSample> getFeatureSamples(@NotNull User user, @NotNull String password, boolean fullSample) {
        if (password.isEmpty()) {
            throw new IllegalArgumentException(PASSWORD_MUST_BE_SPECIFIED);
        }
        List<FeaturesSample> samples = new ArrayList<>();
//        Optional<Double> meanKeyPressTimeOpt = FeatureService.getInstance().getMeanKeypressTime(user);

        Map<Integer, List<Double>> holdFeaturesByString = null;
        Set<FeatureType> featureTypes = FeatureSelectionService.getInstance().getFeatureTypes();
        if (featureTypes.contains(FeatureType.HOLD)) {
            holdFeaturesByString = FEATURE_SERVICE.getHoldFeatureValuesByString(user, password);
        }

        Map<KeyCodePair, List<Double>> releasePressFeaturesByString = null;
        if (featureTypes.contains(FeatureType.RELEASE_PRESS)) {
            releasePressFeaturesByString = FEATURE_SERVICE.getReleasePressFeatureValuesByString(user, password);
        }

        Map<KeyCodePair, List<Double>> pressPressFeaturesByString = null;
        if (featureTypes.contains(FeatureType.PRESS_PRESS)) {
            pressPressFeaturesByString = FEATURE_SERVICE.getPressPressFeatureValuesByString(user, password);
        }

        final int featureThreshold = fullSample ? ORIGIN_FEATURE_THRESHOLD : OTHER_FEATURE_THRESHOLD;

        boolean hasElements = true;
        while (hasElements) {
            List<Double> fullFeatureSampleLst = new ArrayList<>();

            if (featureTypes.contains(FeatureType.HOLD)) {
                FeaturesSample holdFeaturesSample = FEATURE_SAMPLE_SERVICE.getHoldFeaturesSampleByString(holdFeaturesByString, password);

                double elementsPercentage = holdFeaturesSample.definedElementsPercentage();
                if (fullSample && elementsPercentage < ORIGIN_FEATURE_THRESHOLD) {
                    break;
                }

                fullFeatureSampleLst.addAll(holdFeaturesSample.getFeatures());
            }

            if (featureTypes.contains(FeatureType.RELEASE_PRESS)) {
                FeaturesSample releasePressFeaturesSample = FEATURE_SAMPLE_SERVICE.getReleasePressFeaturesSampleByString(
                        releasePressFeaturesByString, password
                );

                double elementsPercentage = releasePressFeaturesSample.definedElementsPercentage();
                if (fullSample && elementsPercentage < ORIGIN_FEATURE_THRESHOLD) {
                    break;
                }

                fullFeatureSampleLst.addAll(releasePressFeaturesSample.getFeatures());
            }

            if (featureTypes.contains(FeatureType.PRESS_PRESS)) {
                FeaturesSample pressPressFeaturesSample = FEATURE_SAMPLE_SERVICE.getPressPressFeaturesSampleByString(
                        pressPressFeaturesByString, password
                );

                double elementsPercentage = pressPressFeaturesSample.definedElementsPercentage();
                if (fullSample && elementsPercentage < ORIGIN_FEATURE_THRESHOLD) {
                    break;
                }

                fullFeatureSampleLst.addAll(pressPressFeaturesSample.getFeatures());
            }
//            fullFeatureSampleLst.add(meanKeyPressTimeOpt.orNull());

            FeaturesSample featuresSample = new FeaturesSample(fullFeatureSampleLst);
            if (featuresSample.definedElementsPercentage() < 30) {
                break;
            }
            samples.add(featuresSample);
        }

        return samples;
    }

    /**
     * Returns a sample of hold features by string from a map of feature values grouped by key code
     * @param holdFeaturesPerCode Map of feature values grouped by key code.
     *                            Lists of hold features are MODIFIED
     * @param password String which is iterated by symbols to get corresponding features
     * @return Hold feature sample
     */
    @NotNull
    public FeaturesSample getHoldFeaturesSampleByString(@Nullable Map<Integer, List<Double>> holdFeaturesPerCode,
                                                               @NotNull String password) {
        if (holdFeaturesPerCode == null) {
            return FeaturesSample.EMPTY_SAMPLE;
        }

        List<Double> holdFeaturesSampleLst = new ArrayList<>();
        for (char code : password.toCharArray()) {
            List<Double> featureValues = holdFeaturesPerCode.get((int)code);
            if (CollectionUtils.isNotEmpty(featureValues)) {
                holdFeaturesSampleLst.add(featureValues.remove(0));
            } else {
                holdFeaturesSampleLst.add(null);
            }
        }
        return new FeaturesSample(holdFeaturesSampleLst);
    }

    /**
     * Returns a sample of release-press features by string from a map of feature values grouped by key code
     * @param releasePressFeaturesPerCode Map of feature values grouped by release-press key codes pair.
     *                                    Lists of release-press features are MODIFIED
     * @param password String which is iterated by symbols to get corresponding features
     * @return Release-press feature sample
     */
    @NotNull
    public FeaturesSample getReleasePressFeaturesSampleByString(
            @Nullable Map<KeyCodePair, List<Double>> releasePressFeaturesPerCode,
            @NotNull String password
    ) {
        if (releasePressFeaturesPerCode == null) {
            return FeaturesSample.EMPTY_SAMPLE;
        }

        char[] passwordCharacters = password.toCharArray();
        List<Double> releasePressSampleLst = new ArrayList<>();
        for (int i = 1; i < passwordCharacters.length; i++) {
            char releaseCode = passwordCharacters[i-1],
                    pressCode = passwordCharacters[i];
            KeyCodePair codePair = new KeyCodePair(releaseCode, pressCode);
            List<Double> releasePressValues = releasePressFeaturesPerCode.get(codePair);

            if (CollectionUtils.isNotEmpty(releasePressValues)) {
                releasePressSampleLst.add(releasePressValues.remove(0));
            } else {
                releasePressSampleLst.add(null);
            }
        }
        return new FeaturesSample(releasePressSampleLst);
    }

    @NotNull
    public FeaturesSample getPressPressFeaturesSampleByString(
            @Nullable Map<KeyCodePair, List<Double>> pressPressFeaturesPerCode,
            @NotNull String password
    ) {
        if (pressPressFeaturesPerCode == null) {
            return FeaturesSample.EMPTY_SAMPLE;
        }

        char[] passwordCharacters = password.toCharArray();
        List<Double> pressPressSampleLst = new ArrayList<>();
        for (int i = 1; i < passwordCharacters.length; i++) {
            char firstPressCode = passwordCharacters[i-1],
                 secondPressCode = passwordCharacters[i];
            KeyCodePair codePair = new KeyCodePair(firstPressCode, secondPressCode);
            List<Double> pressPressValues = pressPressFeaturesPerCode.get(codePair);

            if (CollectionUtils.isNotEmpty(pressPressValues)) {
                pressPressSampleLst.add(pressPressValues.remove(0));
            } else {
                pressPressSampleLst.add(null);
            }
        }
        return new FeaturesSample(pressPressSampleLst);
    }

}
