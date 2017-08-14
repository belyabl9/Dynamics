package com.m1namoto.api;

import com.m1namoto.domain.Event;
import com.m1namoto.domain.User;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface for classifier whose main task is to get a list of collected events and expected user
 * and determine whether these events match with stored biometric template for specified user
 */
public interface IClassifier {

    /**
     * Performs a classification of an unknown list of collected events.
     * Tells how similar is this sample of events to stored biometric template of specified user.
     *
     * @param events collected keystroke dynamics events used for extracting features
     * @param authUser user who passes authentication procedure
     * @return classification result that contains the following information: predicted probability and details
     */
    ClassificationResult classify(@NotNull List<Event> events, @NotNull User authUser);

}
