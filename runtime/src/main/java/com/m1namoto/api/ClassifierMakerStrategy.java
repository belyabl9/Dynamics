package com.m1namoto.api;

import com.m1namoto.domain.User;
import org.jetbrains.annotations.NotNull;

/**
 * Strategy for creating {@link IClassifier}
 */
public interface ClassifierMakerStrategy {

    /**
     * Constructs a classifier for specified user to pass authentication procedure
     */
    IClassifier makeClassifier(@NotNull User user);

}
