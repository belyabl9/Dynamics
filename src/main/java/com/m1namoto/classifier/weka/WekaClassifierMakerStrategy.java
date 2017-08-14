package com.m1namoto.classifier.weka;

import com.m1namoto.api.ClassifierMakerStrategy;
import com.m1namoto.api.IClassifier;
import com.m1namoto.domain.User;
import com.m1namoto.service.ConfigurationService;
import org.jetbrains.annotations.NotNull;

public class WekaClassifierMakerStrategy implements ClassifierMakerStrategy {

    private WekaClassifierMakerStrategy() {}
    private static final WekaClassifierMakerStrategy INSTANCE = new WekaClassifierMakerStrategy();

    public static WekaClassifierMakerStrategy getInstance() {
        return INSTANCE;
    }

    @Override
    public IClassifier makeClassifier(@NotNull User user) {
        WekaClassifier classifier;
        try {
            // TODO cache configuration or/and classifier
            Configuration configuration = ConfigurationService.getInstance().create(user);
            classifier = new WekaClassifier(configuration);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return classifier;
    }
}
