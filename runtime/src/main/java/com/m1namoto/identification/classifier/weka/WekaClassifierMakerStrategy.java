package com.m1namoto.identification.classifier.weka;

import com.m1namoto.api.ClassifierMakerStrategy;
import com.m1namoto.api.IClassifier;
import com.m1namoto.domain.User;
import com.m1namoto.identification.classifier.ClassifierType;
import com.m1namoto.service.weka.configuration.ConfigurationService;
import org.jetbrains.annotations.NotNull;

public class WekaClassifierMakerStrategy implements ClassifierMakerStrategy {

    private static final String UNSUPPORTED_CLASSIFIER_TYPE = "Classifier type must be either multi-class or one-class.";

    @NotNull
    private final ClassifierType classifierType;

    public WekaClassifierMakerStrategy(@NotNull ClassifierType classifierType) {
        this.classifierType = classifierType;
    }

    @Override
    public IClassifier makeClassifier(@NotNull User user) {
        try {
            Configuration configuration;

            // TODO cache configuration or/and classifier
            if (classifierType.isMultiClass()) {
                configuration = ConfigurationService.getInstance().forMultiClass(user.getPassword(), user);
            } else if (classifierType.isOneClass()) {
                configuration = ConfigurationService.getInstance().forOneClass(user.getPassword(), user);
            } else if (classifierType.isTwoClass()) {
                configuration = ConfigurationService.getInstance().forTwoClass(user.getPassword(), user);
            } else {
                throw new UnsupportedOperationException(UNSUPPORTED_CLASSIFIER_TYPE);
            }
           return new WekaClassifier(classifierType, configuration);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
