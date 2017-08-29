package com.m1namoto.service.weka.configuration.attribute;

import com.m1namoto.identification.classifier.weka.Configuration;
import org.jetbrains.annotations.NotNull;

public class AddPressPressAttributesCommand implements AddAttributeCommand {
    private static final String ATTR_PRESS_PRESS = "pressPress";

    private AddPressPressAttributesCommand() {}

    private static class LazyHolder {
        static final AddPressPressAttributesCommand INSTANCE = new AddPressPressAttributesCommand();
    }
    public static AddPressPressAttributesCommand getInstance() {
        return AddPressPressAttributesCommand.LazyHolder.INSTANCE;
    }

    @Override
    public void add(@NotNull Configuration.Builder confBuilder, @NotNull String password) {
        for (int i = 1; i < password.length(); i++) {
            confBuilder.attribute(ATTR_PRESS_PRESS + i);
        }
    }
}