package com.m1namoto.service;

import com.google.common.base.Optional;
import com.m1namoto.classifier.Configuration;
import com.m1namoto.domain.FeaturesSample;
import com.m1namoto.domain.User;
import com.m1namoto.exception.NotEnoughCollectedStatException;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a configuration in .arff format
 */
public class ConfigurationService {
    private static final Logger logger = Logger.getLogger(ConfigurationService.class);

    private static final int MINIMUM_NUMBER_OF_USERS = 2;

    private static String CONFIGURATION_NAME = "Dynamics";

    private static String ATTR_KEY_PRESS = "keypress";
    private static String ATTR_BETWEEN_KEYS = "betweenKeys";
    private static String ATTR_MEAN_KEYPRESS_TIME = "meanKeypressTime";
    private static String ATTR_CLASS = "classVal";

    private static final String NOT_ENOUGH_USERS = "Can not create configuration if there are less than two users.";
    private static final String NOT_ENOUGH_COLLECTED_DYNAMICS = "Not enough keystroke dynamics has been collected to classify.";
    private static final String NOT_ENOUGH_COLLECTED_DYNAMICS_FOR_ORIGIN_USER = "Not enough collected dynamics even for user who passes authentication.";

    private ConfigurationService() {}

    private static class LazyHolder {
        static final ConfigurationService INSTANCE = new ConfigurationService();
    }
    public static ConfigurationService getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * Creates a configuration in .arff format for passed string (password).
     * Only features for symbols which exist in passed string are considered
     * @param password
     * @param authUserIdOpt id of the user who passes authentication procedure
     * @return Configuration in .arff format
     * @throws Exception
     */
    public Configuration create(@NotNull String password, @NotNull Optional<Long> authUserIdOpt) throws Exception {
        logger.debug("Create configuration " + CONFIGURATION_NAME);
        Configuration.Builder confBuilder = new Configuration.Builder().name(CONFIGURATION_NAME);

        for (int i = 0; i < password.length(); i++) {
            confBuilder.attribute(ATTR_KEY_PRESS + (i + 1));
        }
        for (int i = 1; i < password.length(); i++) {
            confBuilder.attribute(ATTR_BETWEEN_KEYS + i);
        }
        confBuilder.attribute(ATTR_MEAN_KEYPRESS_TIME);

        List<User> users = UserService.getInstance().getList(User.Type.REGULAR);
        if (users.size() < MINIMUM_NUMBER_OF_USERS) {
            throw new NotEnoughCollectedStatException(NOT_ENOUGH_USERS);
        }

        List<Integer> allowedValues = new ArrayList<>();
        for (User user : users) {
            // For user who passes authentication procedure we need more samples and they should be full without missing values.
            // For classifier evaluation we don't need it
            boolean isUserToCheck = authUserIdOpt.isPresent() ? authUserIdOpt.get() == user.getId() : false;
            List<FeaturesSample> featuresSamples = FeatureSampleService.getInstance().getFeatureSamples(user, password, isUserToCheck);

            for (FeaturesSample sample : featuresSamples) {
                confBuilder.instance(sample.getFeatures(), user.getId());
            }
            if (featuresSamples.isEmpty()) {
                continue;
            }
            allowedValues.add((int) user.getId());
        }
        // This situation MUST never happen because every user must have at least one sample passed at registration step
        // Moreover, every user has first trusted authentication which have to provide feature samples to teach classifier
        if (allowedValues.isEmpty()) {
            throw new RuntimeException(NOT_ENOUGH_COLLECTED_DYNAMICS_FOR_ORIGIN_USER);
        }

        // Current classifier (J48) can't handle unary class
        if (allowedValues.size() == 1) {
            throw new NotEnoughCollectedStatException(NOT_ENOUGH_COLLECTED_DYNAMICS);
        }
        confBuilder.classAttribute(ATTR_CLASS, allowedValues);

        Configuration conf = confBuilder.build();
        logger.debug("Created classifier configuration (.arff):" + System.lineSeparator() + conf);

        return conf;
    }

    public Configuration create(@NotNull User user) throws Exception {
        return create(user.getPassword(), Optional.of(user.getId()));
    }

}
