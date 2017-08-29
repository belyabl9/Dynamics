package com.m1namoto.service.weka.configuration.attribute;

import com.m1namoto.identification.classifier.weka.Configuration;
import org.jetbrains.annotations.NotNull;

public class AddMeanHoldAttributeCommand implements AddAttributeCommand {
    private static final String ATTR_MEAN_KEYPRESS_TIME = "meanKeypressTime";

    private AddMeanHoldAttributeCommand() {}

    private static class LazyHolder {
        static final AddMeanHoldAttributeCommand INSTANCE = new AddMeanHoldAttributeCommand();
    }
    public static AddMeanHoldAttributeCommand getInstance() {
        return AddMeanHoldAttributeCommand.LazyHolder.INSTANCE;
    }

    @Override
    public void add(@NotNull Configuration.Builder confBuilder, @NotNull String password) {
        confBuilder.attribute(ATTR_MEAN_KEYPRESS_TIME);
    }
}
