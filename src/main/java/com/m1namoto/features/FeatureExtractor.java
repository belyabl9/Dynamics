package com.m1namoto.features;

import com.google.common.base.Optional;
import com.m1namoto.domain.*;
import com.m1namoto.service.FeatureService;
import com.m1namoto.utils.Utils;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FeatureExtractor {
    private static final String NO_RELEASE_EVENT_FOR_PRESS = "Can not find release event for pressed key";

    private FeatureExtractor() {}
    private static final FeatureExtractor INSTANCE = new FeatureExtractor();

    public static FeatureExtractor getInstance() {
        return INSTANCE;
    }

    /**
     * Returns a mean time interval value between release and press events
     * @param events
     * @return Mean time interval value between release and press events
     */
    public double getMeanTimeBetweenKeys(@NotNull List<Event> events) {
        if (events.isEmpty()) {
            return 0;
        }
        List<Double> timeDiffs = getTimeBetweenKeysList(events);
        if (timeDiffs.isEmpty()) {
            return 0;
        }

        return Utils.mean(timeDiffs);
    }

    /**
     * Returns a list of time interval values from a list of events
     */
    @NotNull
    public List<Double> getTimeBetweenKeysList(@NotNull List<Event> events) {
        List<Double> timeDiffs = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            if (!event.getAction().equals(Event.ACTION_RELEASE)) {
                continue;
            }
            Optional<Event> pressEvent = getKeyEvent(events, Event.ACTION_PRESS, i + 1);
            if (!pressEvent.isPresent()) {
                continue;
            }
            double timeDiff = pressEvent.get().getTime() - event.getTime();
            timeDiffs.add(timeDiff);
        }

        return timeDiffs;
    }

    /**
     * Returns a mean key press time interval value for a list of events
     */
    public double getMeanKeyPressTime(@NotNull List<Event> events) {
        if (events.isEmpty()) {
            return 0;
        }
        List<Double> timeDiffs = getKeyPressTimeList(events);
        if (timeDiffs.isEmpty()) {
            return 0;
        }

        return Utils.mean(timeDiffs);
    }

    /**
     * Returns a list of key press time interval values for a list of events
     */
    @NotNull
    public List<Double> getKeyPressTimeList(@NotNull List<Event> events) {
        List<Double> timeDiffs = new ArrayList<>();

        for (int i = 0; i < events.size(); i++) {
            Event pressEvent = events.get(i);
            if (!pressEvent.getAction().equals(Event.ACTION_PRESS)) {
                continue;
            }
            int code = pressEvent.getCode();
            Optional<Event> releaseEvent = getKeyEvent(events, Event.ACTION_RELEASE, code, i + 1);
            if (!releaseEvent.isPresent()) {
                throw new RuntimeException("Can not find release event for pressed key (" + code + ")");
            }
            double timeDiff = releaseEvent.get().getTime() - pressEvent.getTime();
            timeDiffs.add(timeDiff);
        }

        return timeDiffs;
    }

    /**
     * Returns a list of hold features from the list of events
     */
    @NotNull
    public List<HoldFeature> getHoldFeatures(@NotNull List<Event> events, @NotNull User user) {
        List<HoldFeature> holdFeatures = new ArrayList<>();

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            if (event.getAction().equals(Event.ACTION_PRESS)) {
                int code = event.getCode();
                Optional<Event> keyReleaseEvent = getKeyEvent(events, Event.ACTION_RELEASE, code, i + 1);
                if (!keyReleaseEvent.isPresent()) {
                    throw new RuntimeException(NO_RELEASE_EVENT_FOR_PRESS);
                }
                double timeDiff = keyReleaseEvent.get().getTime() - event.getTime();
                holdFeatures.add(new HoldFeature(timeDiff, code, user));
            }
        }
        return holdFeatures;
    }

    /**
     * Returns a list of release-press features from the list of events
     */
    @NotNull
    public List<ReleasePressFeature> getReleasePressFeatures(@NotNull List<Event> events, @NotNull User user) {
        List<ReleasePressFeature> releasePressFeatures = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            Event releaseEvent = events.get(i);
            if (!releaseEvent.getAction().equals(Event.ACTION_RELEASE)) {
                continue;
            }
            Optional<Event> pressEvent = getKeyEvent(events, Event.ACTION_PRESS, i + 1);
            if (!pressEvent.isPresent()) {
                continue;
            }
            double timeDiff = pressEvent.get().getTime() - releaseEvent.getTime();

            ReleasePressFeature feature = new ReleasePressFeature(
                    timeDiff, releaseEvent.getCode(), pressEvent.get().getCode(), user
            );
            releasePressFeatures.add(feature);
        }

        return releasePressFeatures;
    }

    @NotNull
    public List<Double> getFeatureValues(@NotNull List<Event> events, @NotNull User authUser) {
        List<Double> featureValues = new ArrayList<>();
        featureValues.addAll(getHoldFeatureValues(events, authUser));
        featureValues.addAll(getReleasePressFeatureValues(events, authUser));
        featureValues.add(getMeanKeyPressTime(events));
        return featureValues;
    }

    /**
     * When user passes authentication procedure, he sends a list of events related to the password he typed (e.g. "test").
     * This method would return a list of time intervals for pressing keys 't', 'e', 's' and 't'.
     *
     * Size of the returned list MUST be equal to password length
     */
    @NotNull
    private List<Double> getHoldFeatureValues(@NotNull List<Event> events, @NotNull User authUser) {
        List<Double> values = new ArrayList<>();
        List<HoldFeature> sessionHoldFeatures = getHoldFeatures(events, authUser);
        Map<Integer, List<HoldFeature>> holdFeaturesPerCode = FeatureService.extractHoldFeaturesPerCode(sessionHoldFeatures);
        for (char c : authUser.getPassword().toCharArray()) {
            List<HoldFeature> featuresByCode = holdFeaturesPerCode.get((int)c);
            // TODO don't we always have values here ?
            if (CollectionUtils.isNotEmpty(featuresByCode)) {
                values.add(featuresByCode.get(0).getValue());
            } else {
                values.add(null);
            }
        }
        return values;
    }

    /**
     * When user passes authentication procedure, he sends a list of events related to the password he typed (e.g. "test").
     * This method would return a list of time intervals between 't' and 'e', 'e' and 's', 's' and 't'.
     *
     * Size of the returned list MUST be equal to (password_length - 1)
     */
    @NotNull
    private List<Double> getReleasePressFeatureValues(@NotNull List<Event> events, @NotNull User authUser) {
        List<Double> values = new ArrayList<>();
        List<ReleasePressFeature> sessionReleasePressFeatures = getReleasePressFeatures(events, authUser);
        Map<ReleasePressPair, List<ReleasePressFeature>> releasePressFeaturesPerCode = FeatureService.extractReleasePressFeaturesPerCode(sessionReleasePressFeatures);
        char[] passwordCharacters = authUser.getPassword().toCharArray();
        for (int i = 1; i < passwordCharacters.length; i++) {
            int releaseCode = passwordCharacters[i-1],
                    pressCode = passwordCharacters[i];
            ReleasePressPair codePair = new ReleasePressPair(releaseCode, pressCode);
            List<ReleasePressFeature> featuresByCode = releasePressFeaturesPerCode.get(codePair);

            // TODO don't we always have values here ?
            if (CollectionUtils.isNotEmpty(featuresByCode)) {
                values.add(featuresByCode.get(0).getValue());
            } else {
                values.add(null);
            }
        }
        return values;
    }



    /**
     * Finds an event with specific action
     * in a list of events starting from specified position
     * @param action - [press, release]
     * @param start - Position to start from in events list
     * @return Event
     */
    private Optional<Event> getKeyEvent(@NotNull List<Event> events, @NotNull String action, int start) {
        for (int i = start; i < events.size(); i++) {
            Event event = events.get(i);
            if (event.getAction().equals(action)) {
                return Optional.of(event);
            }
        }
        return Optional.absent();
    }

    /**
     * Finds an event with specific code and action
     * in a list of events starting from specified position
     * @param action - [press, release]
     * @param code - key code
     * @param start - Position to start from in events list
     * @return Event or null
     */
    private Optional<Event> getKeyEvent(List<Event> events, String action, int code, int start) {
        for (int i = start; i < events.size(); i++) {
            Event event = events.get(i);
            if ( (event.getCode() == code) && (event.getAction().equals(action)) ) {
                return Optional.of(event);
            }
        }
        return Optional.absent();
    }

}
