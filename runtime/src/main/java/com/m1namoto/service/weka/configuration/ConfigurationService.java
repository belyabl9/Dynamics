package com.m1namoto.service.weka.configuration;

import com.google.common.base.Optional;
import com.m1namoto.domain.FeaturesSample;
import com.m1namoto.domain.User;
import com.m1namoto.entity.FeatureType;
import com.m1namoto.exception.NotEnoughCollectedStatException;
import com.m1namoto.identification.classifier.weka.Configuration;
import com.m1namoto.service.FeatureSampleService;
import com.m1namoto.service.FeatureSelectionService;
import com.m1namoto.service.UserService;
import com.m1namoto.service.weka.configuration.attribute.AddAttributeCommand;
import com.m1namoto.service.weka.configuration.attribute.AddHoldAttributesCommand;
import com.m1namoto.service.weka.configuration.attribute.AddPressPressAttributesCommand;
import com.m1namoto.service.weka.configuration.attribute.AddReleasePressAttributesCommand;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Creates a configuration in .arff format
 */
public class ConfigurationService {
    private static final Logger logger = Logger.getLogger(ConfigurationService.class);

    private static final int MINIMUM_NUMBER_OF_USERS = 2;

    private static String CONFIGURATION_NAME = "Dynamics";

    private static String ATTR_CLASS = "classVal";

    private static final String NOT_ENOUGH_USERS = "Can not create configuration if there are less than two users.";
    private static final String NOT_ENOUGH_CLASSES = "At least 2 classes must be present for multi-class classification.";
    private static final String NOT_ENOUGH_COLLECTED_DYNAMICS_FOR_ORIGIN_USER = "Not enough collected dynamics even for user who passes authentication.";

    public static final int EXPECTED_CLASS_LABEL = 1;
    public static final int DIFFERENT_CLASS_LABEL = 2;

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
     * @param password string
     * @param authUser User who passes authentication procedure
     * @return Configuration in .arff format
     * @throws NotEnoughCollectedStatException
     */
    public Configuration forMultiClass(@NotNull String password, @NotNull User authUser) throws NotEnoughCollectedStatException {
       return createConfiguration(password, Optional.of(authUser));
    }

    public Configuration forOneClass(@NotNull String password, @NotNull User authUser) throws Exception {
        logger.debug("Create configuration " + CONFIGURATION_NAME);
        Configuration.Builder confBuilder = new Configuration.Builder().name(CONFIGURATION_NAME);

        for (AddAttributeCommand addAttributeCommand : getAddAttributeCommands()) {
            addAttributeCommand.add(confBuilder, password);
        }

        List<FeaturesSample> featuresSamples = FeatureSampleService.getInstance().getFeatureSamples(authUser, password, true);
        for (FeaturesSample sample : featuresSamples) {
            confBuilder.instance(sample.getFeatures(), EXPECTED_CLASS_LABEL);
        }

        confBuilder.classAttribute(ATTR_CLASS, Collections.singletonList(EXPECTED_CLASS_LABEL));

//      This situation MUST never happen because every user must have at least one sample passed at registration step
//      Moreover, every user has first trusted authentication which have to provide feature samples to teach classifier
        if (featuresSamples.isEmpty()) {
            throw new RuntimeException(NOT_ENOUGH_COLLECTED_DYNAMICS_FOR_ORIGIN_USER);
        }

        Configuration conf = confBuilder.build();
        logger.debug("Created classifier configuration (.arff):" + System.lineSeparator() + conf);

        return conf;
    }

    public Configuration forTwoClass(@NotNull String password, @NotNull User authUser) throws Exception {
        logger.debug("Create configuration " + CONFIGURATION_NAME);
        Configuration.Builder confBuilder = new Configuration.Builder().name(CONFIGURATION_NAME);

        for (AddAttributeCommand addAttributeCommand : getAddAttributeCommands()) {
            addAttributeCommand.add(confBuilder, password);
        }

        List<User> users = UserService.getInstance().getList(User.Type.REGULAR);
        if (users.size() < MINIMUM_NUMBER_OF_USERS) {
            throw new NotEnoughCollectedStatException(NOT_ENOUGH_USERS);
        }

        for (User user : users) {
            // For user who passes authentication procedure we need more samples and they should be full without missing values.
            // For classifier evaluation we don't need it
            boolean isUserToCheck = authUser.getId() == user.getId();
            List<FeaturesSample> featuresSamples = FeatureSampleService.getInstance().getFeatureSamples(user, password, isUserToCheck);
            for (FeaturesSample sample : featuresSamples) {
                confBuilder.instance(sample.getFeatures(), isUserToCheck ? EXPECTED_CLASS_LABEL : DIFFERENT_CLASS_LABEL);
            }
        }

        confBuilder.classAttribute(ATTR_CLASS, Arrays.asList(EXPECTED_CLASS_LABEL, DIFFERENT_CLASS_LABEL));

        Configuration conf = confBuilder.build();
        logger.debug("Created classifier configuration (.arff):" + System.lineSeparator() + conf);

        return conf;
    }

    public Configuration forEvaluation(@NotNull String password) throws Exception {
        return createConfiguration(password, Optional.<User>absent());
    }

    private Configuration createConfiguration(@NotNull String password, @NotNull Optional<User> authUserOpt) throws NotEnoughCollectedStatException {
        logger.debug("Create configuration " + CONFIGURATION_NAME);
        Configuration.Builder confBuilder = new Configuration.Builder().name(CONFIGURATION_NAME);

        for (AddAttributeCommand addAttributeCommand : getAddAttributeCommands()) {
            addAttributeCommand.add(confBuilder, password);
        }

        List<User> users = UserService.getInstance().getList(User.Type.REGULAR);
        if (users.size() < MINIMUM_NUMBER_OF_USERS) {
            throw new NotEnoughCollectedStatException(NOT_ENOUGH_USERS);
        }

        List<Integer> classValues = new ArrayList<>();
        for (User user : users) {
            // For user who passes authentication procedure we need more samples and they should be full without missing values.
            // For classifier evaluation we don't need it
            boolean isUserToCheck = authUserOpt.isPresent() ? authUserOpt.get().getId() == user.getId() : true;
            List<FeaturesSample> featuresSamples = FeatureSampleService.getInstance().getFeatureSamples(user, password, isUserToCheck);
            for (FeaturesSample sample : featuresSamples) {
                confBuilder.instance(sample.getFeatures(), user.getId());
            }
            if (featuresSamples.isEmpty()) {
                continue;
            }
            classValues.add((int) user.getId());
        }
//      This situation MUST never happen because every user must have at least one sample passed at registration step
//      Moreover, every user has first trusted authentication which have to provide feature samples to teach classifier
        if (classValues.isEmpty()) {
            throw new RuntimeException(NOT_ENOUGH_COLLECTED_DYNAMICS_FOR_ORIGIN_USER);
        }

        if (classValues.size() == 1) {
            throw new NotEnoughCollectedStatException(NOT_ENOUGH_CLASSES);
        }
        confBuilder.classAttribute(ATTR_CLASS, classValues);

        Configuration conf = confBuilder.build();
        logger.debug("Created classifier configuration (.arff):" + System.lineSeparator() + conf);

        return conf;
    }

    private Set<AddAttributeCommand> getAddAttributeCommands() {
        Set<AddAttributeCommand> addAttributeCommands = new HashSet<>();
        for (FeatureType featureType : FeatureSelectionService.getInstance().getFeatureTypes()) {
            switch (featureType) {
                case HOLD:
                    addAttributeCommands.add(AddHoldAttributesCommand.getInstance());
                    break;
                case RELEASE_PRESS:
                    addAttributeCommands.add(AddReleasePressAttributesCommand.getInstance());
                    break;
                case PRESS_PRESS:
                    addAttributeCommands.add(AddPressPressAttributesCommand.getInstance());
                    break;
                default:
                    throw new UnsupportedOperationException("Specified feature type is not supported.");
            }
        }
        return addAttributeCommands;
    }

}
