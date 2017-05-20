package com.m1namoto.features;

import com.google.common.base.Optional;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.HoldFeature;
import com.m1namoto.domain.ReleasePressFeature;
import com.m1namoto.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
    public double getMeanKeyPressTime(@NotNull List<Event> events) throws Exception {
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
    public List<Double> getKeyPressTimeList(@NotNull List<Event> events) throws Exception {
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
    public List<HoldFeature> getHoldFeatures(@NotNull List<Event> events) {
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
                holdFeatures.add(new HoldFeature(timeDiff, code, event.getUser()));
            }
        }
        return holdFeatures;
    }

    /**
     * Returns a list of release-press features from the list of events
     */
    @NotNull
    public List<ReleasePressFeature> getReleasePressFeatures(@NotNull List<Event> events) {
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
                    timeDiff, releaseEvent.getCode(), pressEvent.get().getCode(), releaseEvent.getUser()
            );
            releasePressFeatures.add(feature);
        }

        return releasePressFeatures;
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
