package com.m1namoto.service.weka.configuration.attribute;

import com.m1namoto.identification.classifier.weka.Configuration;
import org.jetbrains.annotations.NotNull;

public class AddReleasePressAttributesCommand implements AddAttributeCommand {
    private static final String ATTR_RELEASE_PRESS = "releasePress";

    private AddReleasePressAttributesCommand() {}

    private static class LazyHolder {
        static final AddReleasePressAttributesCommand INSTANCE = new AddReleasePressAttributesCommand();
    }
    public static AddReleasePressAttributesCommand getInstance() {
        return AddReleasePressAttributesCommand.LazyHolder.INSTANCE;
    }

    @Override
    public void add(@NotNull Configuration.Builder confBuilder, @NotNull String password) {
        for (int i = 1; i < password.length(); i++) {
            confBuilder.attribute(ATTR_RELEASE_PRESS + i);
        }
    }
}