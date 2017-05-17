package com.m1namoto.features;

import com.m1namoto.domain.Event;
import com.m1namoto.domain.HoldFeature;
import com.m1namoto.domain.ReleasePressFeature;
import com.m1namoto.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

// TODO: String action to enum
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
     * @throws Exception
     */
    public double getMeanTimeBetweenKeys(@NotNull List<Event> events) throws Exception {
        if (events.isEmpty()) {
            return 0;
        }

        List<Double> timeDiffs = getTimeBetweenKeysList(events);
        if (timeDiffs.size() == 0) {
            return 0;
        }

        return Utils.mean(timeDiffs);
    }

    /**
     * Returns a list of time interval values from a list of events
     */
    @NotNull
    public List<Double> getTimeBetweenKeysList(@NotNull List<Event> events) {
        List<Double> timeDiffs = new ArrayList<Double>();

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            if (!event.getAction().equals(Event.ACTION_RELEASE)) {
                continue;
            }
            Event pressEvent = getKeyEvent(events, Event.ACTION_PRESS, i + 1);
            if (pressEvent == null) {
                continue;
            }
            double timeDiff = pressEvent.getTime() - event.getTime();
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
        if (timeDiffs.size() == 0) {
            return 0;
        }

        return Utils.mean(timeDiffs);
    }

    /**
     * Returns a list of key press time interval values for a list of events
     */
    @NotNull
    public List<Double> getKeyPressTimeList(@NotNull List<Event> events) throws Exception {
        List<Double> timeDiffs = new ArrayList<Double>();

        for (int i = 0; i < events.size(); i++) {
            Event pressEvent = events.get(i);
            if (!pressEvent.getAction().equals(Event.ACTION_PRESS)) {
                continue;
            }
            int code = pressEvent.getCode();
            Event releaseEvent = getKeyEvent(events, Event.ACTION_RELEASE, code, i + 1);
            if (releaseEvent == null) {
                throw new Exception("Can not find release event for pressed key (" + code + ")");
            }
            double timeDiff = releaseEvent.getTime() - pressEvent.getTime();
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
                Event keyReleaseEvent = getKeyEvent(events, Event.ACTION_RELEASE, code, i + 1);
                if (keyReleaseEvent == null) {
                    throw new RuntimeException(NO_RELEASE_EVENT_FOR_PRESS);
                }
                double timeDiff = keyReleaseEvent.getTime() - event.getTime();
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
        List<ReleasePressFeature> releasePressFeatures = new ArrayList<ReleasePressFeature>();

        for (int i = 0; i < events.size(); i++) {
            Event releaseEvent = events.get(i);
            if (!releaseEvent.getAction().equals(Event.ACTION_RELEASE)) {
                continue;
            }
            Event pressEvent = getKeyEvent(events, Event.ACTION_PRESS, i + 1);
            if (pressEvent == null) {
                continue;
            }
            double timeDiff = pressEvent.getTime() - releaseEvent.getTime();

            ReleasePressFeature feature = new ReleasePressFeature(
                    timeDiff, releaseEvent.getCode(), pressEvent.getCode(), releaseEvent.getUser());
            releasePressFeatures.add(feature);
        }

        return releasePressFeatures;
    }

    /**
     * Finds an event with specific action
     * in a list of events starting from specified position
     * @param action - [press, release]
     * @param start - Position to start from in events list
     * @return Event or null
     */
    private Event getKeyEvent(@NotNull List<Event> events, @NotNull String action, int start) {
        for (int i = start; i < events.size(); i++) {
            Event event = events.get(i);
            if (event.getAction().equals(action)) {
                return event;
            }
        }

        return null;
    }

    /**
     * Finds an event with specific code and action
     * in a list of events starting from specified position
     * @param action - [press, release]
     * @param code - key code
     * @param start - Position to start from in events list
     * @return Event or null
     */
    private Event getKeyEvent(List<Event> events, String action, int code, int start) {
        for (int i = start; i < events.size(); i++) {
            Event event = events.get(i);
            if ( (event.getCode() == code) && (event.getAction().equals(action)) ) {
                return event;
            }
        }

        return null;
    }

}
