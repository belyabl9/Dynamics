package com.m1namoto.service.auth;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.m1namoto.api.AnomalyDetector;
import com.m1namoto.api.ClassificationResult;
import com.m1namoto.api.ClassifierMakerStrategy;
import com.m1namoto.api.IClassifier;
import com.m1namoto.domain.*;
import com.m1namoto.entity.FeatureType;
import com.m1namoto.service.*;
import com.m1namoto.service.verification.VerificationService;
import com.m1namoto.service.verification.VerificationType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.*;

public class AuthenticationService {

    private static final Gson GSON = new Gson();

    private static final FeatureExtractorService FEATURE_EXTRACTOR = FeatureExtractorService.getInstance();
    private static final FeatureService FEATURE_SERVICE = FeatureService.getInstance();

    private AuthenticationService() {}

    private static class LazyHolder {
        static final AuthenticationService INSTANCE = new AuthenticationService();
    }
    public static AuthenticationService getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * Simplified authentication algorithm:
     * 1. Check credentials
     * 2. Check if this is an authentication for administrator access
     * 3. Check if account is new and limit of first trusted authentications is not reached
     * 4. Check that sent keystroke dynamics matches to stored biometric template for this user
     */
    public AuthenticationResult authenticateViaIdentification(@NotNull AuthenticationContext context, @NotNull ClassifierMakerStrategy classifierMakerStrategy) {
        if (Strings.isNullOrEmpty(context.getLogin()) || Strings.isNullOrEmpty(context.getPassword())) {
            return new AuthenticationResult(false, AuthenticationStatus.EMPTY_LOGIN_OR_PASSWORD);
        }

        Optional<User> userOpt = UserService.getInstance().findByLogin(context.getLogin());
        if (!userOpt.isPresent()) {
            return new AuthenticationResult(false, AuthenticationStatus.CAN_NOT_FIND_USER);
        }
        User user = userOpt.get();
        if (!user.getPassword().equals(PasswordService.getInstance().makeHash(context.getPassword()))) {
            return new AuthenticationResult(false, AuthenticationStatus.WRONG_PASSWORD);
        }

        if (user.getUserType() == User.Type.ADMIN) {
            return new AuthenticationResult(true, AuthenticationStatus.ADMIN_ACCESS);
        }

        if (context.getStat() == null) {
            return new AuthenticationResult(false, AuthenticationStatus.DYNAMICS_NOT_PASSED);
        }

        Type type = new TypeToken<InputStatistics>(){}.getType();
        InputStatistics statistics = GSON.fromJson(context.getStat(), type);

        // First <learningRate> authentication attempts are considered genuine
        boolean trustedAuthenticationsExpired = user.getAuthenticatedCnt() > context.getLearningRate();
        if (!trustedAuthenticationsExpired) {
            if (context.isUpdateTemplate() && !context.isStolen()) {
                saveSession(context, statistics, user);
            }
            return new AuthenticationResult(true, AuthenticationStatus.FIRST_TRUSTED_ATTEMPTS);
        }

        User userWithPlainPassword = new User(user);
        userWithPlainPassword.setPassword(context.getPassword());
        IClassifier classifier = classifierMakerStrategy.makeClassifier(userWithPlainPassword);
        ClassificationResult classificationResult = classifier.classify(statistics.getPassword(), userWithPlainPassword);

        if (isThresholdAccepted(classificationResult, context.getThreshold())) {
            if (context.isUpdateTemplate() && !context.isStolen()) {
                saveSession(context, statistics, user);
            }
            return new AuthenticationResult(true, AuthenticationStatus.SUCCESS, Optional.of(classificationResult.getProbability()));
        }

        return new AuthenticationResult(false, AuthenticationStatus.FAIL, Optional.of(classificationResult.getProbability()));
    }

    public AuthenticationResult authenticateViaVerification(@NotNull AuthenticationContext context, @NotNull VerificationType verificationType) {
        if (Strings.isNullOrEmpty(context.getLogin()) || Strings.isNullOrEmpty(context.getPassword())) {
            return new AuthenticationResult(false, AuthenticationStatus.EMPTY_LOGIN_OR_PASSWORD);
        }

        Optional<User> userOpt = UserService.getInstance().findByLogin(context.getLogin());
        if (!userOpt.isPresent()) {
            return new AuthenticationResult(false, AuthenticationStatus.CAN_NOT_FIND_USER);
        }
        User user = userOpt.get();
        if (!user.getPassword().equals(PasswordService.getInstance().makeHash(context.getPassword()))) {
            return new AuthenticationResult(false, AuthenticationStatus.WRONG_PASSWORD);
        }

        if (user.getUserType() == User.Type.ADMIN) {
            return new AuthenticationResult(true, AuthenticationStatus.ADMIN_ACCESS);
        }

        if (context.getStat() == null) {
            return new AuthenticationResult(false, AuthenticationStatus.DYNAMICS_NOT_PASSED);
        }

        Type type = new TypeToken<InputStatistics>(){}.getType();
        InputStatistics statistics = GSON.fromJson(context.getStat(), type);

        // First <learningRate> authentication attempts are considered genuine
        boolean trustedAuthenticationsExpired = user.getAuthenticatedCnt() >= context.getLearningRate();
        if (!trustedAuthenticationsExpired) {
            if (context.isUpdateTemplate() && !context.isStolen()) {
                saveSession(context, statistics, user);
            }
            return new AuthenticationResult(true, AuthenticationStatus.FIRST_TRUSTED_ATTEMPTS);
        }

        User userWithPlainPassword = new User(user);
        userWithPlainPassword.setPassword(context.getPassword());
        AnomalyDetector anomalyDetector = new VerificationService(verificationType);

        Map<FeatureType, List<Double>> testFeatureValuesMap = new HashMap<>();
        for (FeatureType featureType : FeatureSelectionService.getInstance().getFeatureTypes()) {
            switch (featureType) {
                case HOLD:
                    testFeatureValuesMap.put(
                            featureType,
                            FeatureExtractorService.getInstance().getHoldFeatureValues(
                                    statistics.getPassword(), userWithPlainPassword
                            )
                    );
                    break;
                case RELEASE_PRESS:
                    testFeatureValuesMap.put(
                            featureType,
                            FeatureExtractorService.getInstance().getPasswordReleasePressFeatureValues(
                                    statistics.getPassword(), userWithPlainPassword
                            )
                    );
                    break;
                case PRESS_PRESS:
                    testFeatureValuesMap.put(
                            featureType,
                            FeatureExtractorService.getInstance().getPressPressFeatureValues(
                                    statistics.getPassword(), userWithPlainPassword
                            )
                    );
                    break;
                default:
                    throw new UnsupportedOperationException("Specified feature type is not supported.");
            }
        }
        boolean isAnomaly = anomalyDetector.isAnomaly(testFeatureValuesMap, userWithPlainPassword, Optional.<Double>absent());

        ClassificationResult classificationResult;
        if (isAnomaly) {
            classificationResult = new ClassificationResult(0d);
        } else {
            classificationResult = new ClassificationResult(1d);
        }

        if (isThresholdAccepted(classificationResult, context.getThreshold())) {
            if (context.isUpdateTemplate() && !context.isStolen()) {
                saveSession(context, statistics, user);
            }
            return new AuthenticationResult(true, AuthenticationStatus.SUCCESS, Optional.of(classificationResult.getProbability()));
        }

        return new AuthenticationResult(false, AuthenticationStatus.FAIL, Optional.of(classificationResult.getProbability()));
    }

    /**
     * Saves a session with its features
     */
    @NotNull
    private Session saveSession(@NotNull AuthenticationContext context, @NotNull InputStatistics statistics, @NotNull User user) {
        Session session = new Session(user);
        session = SessionService.save(session);

        List<Event> events = new ArrayList<>(statistics.getPassword());
        events.addAll(statistics.getAdditional());

        Set<FeatureType> featureTypes = FeatureSelectionService.getInstance().getFeatureTypes();
        List<Feature> features = new ArrayList<>();
        for (FeatureType featureType : featureTypes) {
            switch (featureType) {
                case HOLD:
                    features.addAll(FEATURE_EXTRACTOR.getHoldFeatures(events, user));
                    break;
                case RELEASE_PRESS:
                    features.addAll(FEATURE_EXTRACTOR.getReleasePressFeatures(events, user));

                    // It's not guaranteed that events contain all required release-press pairs for password.
                    // If user doesn't press more than one key simultaneously it's not needed.
                    // E.g. Password - "test". [ T-press, E-press, T-release, E-release, ...].
                    // In such case, T-release is before E-press, so we would miss it
                    String prevPass = user.getPassword();
                    user.setPassword(context.getPassword());
                    features.addAll(FEATURE_EXTRACTOR.getPasswordReleasePressFeatures(statistics.getPassword(), user));
                    user.setPassword(prevPass);
                    break;
                case PRESS_PRESS:
                    features.addAll(FEATURE_EXTRACTOR.getPressPressFeatures(events, user));
                    break;
                default:
                    throw new UnsupportedOperationException("Specified feature type is not supported.");
            }
        }

        for (Feature feature : features) {
            feature.setSession(session);
            FEATURE_SERVICE.save(feature);
        }
        if (!features.isEmpty()) {
            FEATURE_SERVICE.invalidateFeatureCache();
        }

        return session;
    }

    private boolean isThresholdAccepted(@NotNull ClassificationResult classificationResult, double threshold) {
        return classificationResult.getProbability() >= threshold;
    }

}
