package com.m1namoto.service.auth;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.m1namoto.classifier.ClassificationResult;
import com.m1namoto.classifier.Classifier;
import com.m1namoto.classifier.Configuration;
import com.m1namoto.classifier.DynamicsInstance;
import com.m1namoto.domain.*;
import com.m1namoto.exception.NotEnoughCollectedStatException;
import com.m1namoto.service.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
    public AuthenticationResult authenticate(@NotNull AuthenticationContext context) {
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
        boolean trustedAuthenticationsExpired = user.getAuthenticatedCnt() < context.getLearningRate();
        if (!trustedAuthenticationsExpired) {
            if (context.isUpdateTemplate() && !context.isStolen()) {
                saveSession(statistics, user);
            }
            return new AuthenticationResult(true, AuthenticationStatus.FIRST_TRUSTED_ATTEMPTS);
        }

        ClassificationResult classificationResult;
        try {
            User userWithPlainPassword = new User(user);
            userWithPlainPassword.setPassword(context.getPassword());
            classificationResult = classify(statistics.getPassword(), userWithPlainPassword);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (isThresholdAccepted(classificationResult, context.getThreshold())) {
            if (context.isUpdateTemplate() && !context.isStolen()) {
                saveSession(statistics, user);
            }
            return new AuthenticationResult(true, AuthenticationStatus.SUCCESS, Optional.of(context.getThreshold()));
        }

        return new AuthenticationResult(false, AuthenticationStatus.FAIL, Optional.of(context.getThreshold()));
    }

    /**
     * Saves a session with its features
     */
    @NotNull
    private Session saveSession(@NotNull InputStatistics statistics, @NotNull User user) {
        Session session = new Session(user);
        session = SessionService.save(session);

        List<Event> events = new ArrayList<>(statistics.getPassword());
        events.addAll(statistics.getAdditional());

        List<Feature> features = new ArrayList<>();
        features.addAll(FEATURE_EXTRACTOR.getHoldFeatures(events, user));
        features.addAll(FEATURE_EXTRACTOR.getReleasePressFeatures(events, user));
        for (Feature feature : features) {
            feature.setSession(session);
            FEATURE_SERVICE.save(feature);
        }
        if (!features.isEmpty()) {
            FEATURE_SERVICE.invalidateFeatureCache();
        }

        return session;
    }

    private ClassificationResult classify(@NotNull List<Event> events,
                                          @NotNull User authUser) throws Exception {
        Classifier classifier;
        try {
            classifier = makeClassifier(authUser);
        } catch (NotEnoughCollectedStatException e) {
            return new ClassificationResult(1d);
        }

        DynamicsInstance instance = new DynamicsInstance(FEATURE_EXTRACTOR.getFeatureValues(events, authUser));
        try {
            return classifier.getClassForInstance(instance, authUser.getId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private Classifier makeClassifier(@NotNull User user) throws NotEnoughCollectedStatException {
        Classifier classifier;
        try {
            // TODO cache configuration or/and classifier
            Configuration configuration = ConfigurationService.getInstance().create(user);
            classifier = new Classifier(configuration);
        } catch (NotEnoughCollectedStatException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return classifier;
    }

    private boolean isThresholdAccepted(@NotNull ClassificationResult classificationResult, double threshold) {
        return classificationResult.getProbability() >= threshold;
    }

}
