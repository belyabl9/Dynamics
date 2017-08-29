package com.m1namoto.service.weka.configuration.attribute;

import com.m1namoto.identification.classifier.weka.Configuration;
import org.jetbrains.annotations.NotNull;

public interface AddAttributeCommand {
    void add(@NotNull Configuration.Builder confBuilder, @NotNull String password);
}