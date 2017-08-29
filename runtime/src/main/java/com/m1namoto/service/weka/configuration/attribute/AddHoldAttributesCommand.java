package com.m1namoto.service.weka.configuration.attribute;

import com.m1namoto.identification.classifier.weka.Configuration;
import org.jetbrains.annotations.NotNull;

public class AddHoldAttributesCommand implements AddAttributeCommand {
    private static final String ATTR_KEY_PRESS = "keypress";

    private AddHoldAttributesCommand() {}

    private static class LazyHolder {
        static final AddHoldAttributesCommand INSTANCE = new AddHoldAttributesCommand();
    }
    public static AddHoldAttributesCommand getInstance() {
        return AddHoldAttributesCommand.LazyHolder.INSTANCE;
    }

    @Override
    public void add(@NotNull Configuration.Builder confBuilder, @NotNull String password) {
        for (int i = 0; i < password.length(); i++) {
            confBuilder.attribute(ATTR_KEY_PRESS + (i + 1));
        }
    }
}