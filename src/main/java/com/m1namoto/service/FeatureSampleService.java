package com.m1namoto.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.m1namoto.domain.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.m1namoto.domain.FeaturesSample;
import com.m1namoto.domain.ReleasePressPair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FeatureSampleService {
    private static final Logger log = Logger.getLogger(FeatureSampleService.class);

    private final static int ORIGIN_HOLD_FEATURES_THRESHOLD = 100;
    private final static int OTHER_HOLD_FEATURES_THRESHOLD = 80;

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
        Optional<Double> meanKeyPressTimeOpt = FeatureService.getInstance().getMeanKeypressTime(user);

        Map<Integer, List<Double>> holdFeaturesByString = FEATURE_SERVICE.getHoldFeatureValuesByString(user, password);
        Map<ReleasePressPair, List<Double>> releasePressFeaturesByString = FEATURE_SERVICE.getReleasePressFeatureValuesByString(user, password);

        final int holdFeaturesMin = fullSample ? ORIGIN_HOLD_FEATURES_THRESHOLD : OTHER_HOLD_FEATURES_THRESHOLD;

        boolean isFullSampleEmpty = false;
        while (!isFullSampleEmpty) {
            FeaturesSample holdFeaturesSample = FEATURE_SAMPLE_SERVICE.getHoldFeaturesSampleByString(holdFeaturesByString, password);
            FeaturesSample releasePressFeaturesSample = FEATURE_SAMPLE_SERVICE.getReleasePressFeaturesSampleByString(
                    releasePressFeaturesByString, password
            );

            List<Double> fullFeatureSampleLst = new ArrayList<>();
            fullFeatureSampleLst.addAll(holdFeaturesSample.getFeatures());
            fullFeatureSampleLst.addAll(releasePressFeaturesSample.getFeatures());
            fullFeatureSampleLst.add(meanKeyPressTimeOpt.orNull());

            isFullSampleEmpty = holdFeaturesSample.isEmpty() && releasePressFeaturesSample.isEmpty();
            if (!isFullSampleEmpty) {
                boolean isEnoughElements = holdFeaturesSample.definedElementsPercentage() >= holdFeaturesMin;
                if (isEnoughElements) {
                    samples.add(new FeaturesSample(fullFeatureSampleLst));
                }
            }
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
    private FeaturesSample getReleasePressFeaturesSampleByString(
            @Nullable Map<ReleasePressPair, List<Double>> releasePressFeaturesPerCode,
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
            ReleasePressPair codePair = new ReleasePressPair(releaseCode, pressCode);
            List<Double> releasePressValues = releasePressFeaturesPerCode.get(codePair);

            if (CollectionUtils.isNotEmpty(releasePressValues)) {
                releasePressSampleLst.add(releasePressValues.remove(0));
            } else {
                releasePressSampleLst.add(null);
            }
        }
        return new FeaturesSample(releasePressSampleLst);
    }

}
