package com.m1namoto.service;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.m1namoto.dao.DaoFactory;
import com.m1namoto.dao.FeaturesDao;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.Feature;
import com.m1namoto.domain.HoldFeature;
import com.m1namoto.domain.ReleasePressFeature;
import com.m1namoto.domain.User;
import com.m1namoto.utils.ReleasePressPair;
import com.m1namoto.utils.Utils;

import weka.core.Instances;

public class FeaturesService {
    final static Logger logger = Logger.getLogger(FeaturesService.class);

    private static int KEY_PRESS_MAX_TIME = 1000;
    private static int BETWEEN_KEYS_MAX_TIME = 10000;
    
    private static Map<Long, Map<Integer, List<HoldFeature>>> userHoldFeaturesMap;
    private static Map<Long, Map<ReleasePressPair, List<ReleasePressFeature>>> userReleasePressFeaturesMap;
    
	public static double getMeanTimeBetweenKeys(List<Event> events) throws Exception {
		List<Double> timeDiffs = getTimeBetweenKeysList(events);
		if (timeDiffs.size() == 0) {
		    throw new Exception("List with time between keys is empty");
		}
		
		return Utils.mean(timeDiffs);
	}

	public static double getMeanKeyPressTime(List<Event> events) {
		List<Double> timeDiffs = getKeyPressTimeList(events);
		return Utils.mean(timeDiffs);
	}

	public static List<Double> getKeyPressTimeList(List<Event> events) {
		List<Double> timeDiffs = new ArrayList<Double>();

		for (int i = 0; i < events.size(); i++) {
			Event event = events.get(i);
			if (event.getAction().equals(Event.ACTION_PRESS)) {
				int code = event.getCode();
				Event keyReleaseEvent = EventsService.getKeyEvent(events, Event.ACTION_RELEASE, code, i + 1);
				if (keyReleaseEvent == null) {
				    continue;
				}
				double timeDiff = keyReleaseEvent.getTime() - event.getTime();
				// Anomalies
				//if (!Double.isInfinite(timeDiff) && !Double.isNaN(timeDiff) && timeDiff < KEY_PRESS_MAX_TIME) {
				    timeDiffs.add(timeDiff);
				//}
			}
		}

		return timeDiffs;
	}
	
    public static List<HoldFeature> getHoldFeatures(List<Event> events) {
        List<HoldFeature> holdFeatures = new ArrayList<HoldFeature>();

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            if (event.getAction().equals(Event.ACTION_PRESS)) {
                int code = event.getCode();
                Event keyReleaseEvent = EventsService.getKeyEvent(events, Event.ACTION_RELEASE, code, i + 1);
                if (keyReleaseEvent == null) {
                    continue;
                }
                double timeDiff = keyReleaseEvent.getTime() - event.getTime();
                // Anomalies
               // if (!Double.isInfinite(timeDiff) && !Double.isNaN(timeDiff) && timeDiff < KEY_PRESS_MAX_TIME) {
                    holdFeatures.add(new HoldFeature(timeDiff, code, event.getUser()));
               // }
            }
        }

        return holdFeatures;
    }
	
    public static List<ReleasePressFeature> getReleasePressFeatures(List<Event> events) {
        List<ReleasePressFeature> releasePressFeatures = new ArrayList<ReleasePressFeature>();

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            if (event.getAction().equals(Event.ACTION_RELEASE)) {
                Event keyPressEvent = EventsService.getKeyEvent(events, Event.ACTION_PRESS, i + 1);
                if (keyPressEvent == null) {
                    continue;
                }
                double timeDiff = keyPressEvent.getTime() - event.getTime();
                // Anomalies
               // if (!Double.isInfinite(timeDiff) && !Double.isNaN(timeDiff) && timeDiff > BETWEEN_KEYS_MAX_TIME) {
               //     continue;
               // }

                ReleasePressFeature feature = new ReleasePressFeature(
                        timeDiff, event.getCode(), keyPressEvent.getCode(), event.getUser());
                releasePressFeatures.add(feature);
            }
        }

        return releasePressFeatures;
    }
	
	public static List<Double> getTimeBetweenKeysList(List<Event> events) {
	    List<Double> timeDiffs = new ArrayList<Double>();

		for (int i = 0; i < events.size(); i++) {
			Event event = events.get(i);
			if (event.getAction().equals(Event.ACTION_RELEASE)) {
				Event keyPressEvent = EventsService.getKeyEvent(events, Event.ACTION_PRESS, i + 1);
				if (keyPressEvent == null) {
				    continue;
				}
				double timeDiff = keyPressEvent.getTime() - event.getTime();
                // Anomalies
				//if (!Double.isInfinite(timeDiff) && !Double.isNaN(timeDiff) && timeDiff > BETWEEN_KEYS_MAX_TIME) {
				//	continue;
				//}
				timeDiffs.add(timeDiff);
			}
		}

		return timeDiffs;
	}
    
	public static List<HoldFeature> getHoldFeatures() {
	    return DaoFactory.getFeaturesDAO().getHoldFeatures(); 
	}
	
	public static Map<Long, List<HoldFeature>> getHoldFeaturesPerUser() {
	    List<HoldFeature> features = getHoldFeatures();
	    Map<Long, List<HoldFeature>> featuresPerUser = new HashMap<Long, List<HoldFeature>>();
	    for (HoldFeature feature : features) {
	        long userId = feature.getUser().getId();
	        if (!featuresPerUser.containsKey(userId)) {
	            featuresPerUser.put(userId, new ArrayList<HoldFeature>());
	        }
	        featuresPerUser.get(userId).add(feature);
	    }
	    
	    return featuresPerUser;
	}
	
	public static Map<Integer, List<HoldFeature>> getHoldFeaturesPerCode(List<HoldFeature> userFeatures) {
	    Map<Integer, List<HoldFeature>> featuresPerCode = new HashMap<Integer, List<HoldFeature>>();
	    
	    for (HoldFeature feature : userFeatures) {
	        int code = feature.getCode();
	        if (!featuresPerCode.containsKey(code)) {
	            featuresPerCode.put(code, new ArrayList<HoldFeature>());
	        }
	        featuresPerCode.get(code).add(feature);
	    }
	    
	    return featuresPerCode;
	}
	
    public static Map<ReleasePressPair, List<ReleasePressFeature>> getReleasePressFeaturesPerCode(List<ReleasePressFeature> userFeatures) {
        Map<ReleasePressPair, List<ReleasePressFeature>> featuresPerCode = new HashMap<ReleasePressPair, List<ReleasePressFeature>>();

        for (ReleasePressFeature feature : userFeatures) {
            int releaseCode = feature.getReleaseCode(),
                pressCode = feature.getPressCode();
            ReleasePressPair codePair = new ReleasePressPair(releaseCode, pressCode);
            if (!featuresPerCode.containsKey(codePair)) {
                featuresPerCode.put(codePair, new ArrayList<ReleasePressFeature>());
            }
            featuresPerCode.get(codePair).add(feature);
        }

        return featuresPerCode;
    }
	
	public static Map<Long, Map<Integer, List<HoldFeature>>> getUserHoldFeaturesMap() {
	    if (userHoldFeaturesMap != null) {
	        return userHoldFeaturesMap;
	    }

	    userHoldFeaturesMap = new HashMap<Long, Map<Integer, List<HoldFeature>>>();

	    List<HoldFeature> features = getHoldFeatures();
	    for (HoldFeature feature : features) {
	        long userId = feature.getUser().getId();
	        if (!userHoldFeaturesMap.containsKey(userId)) {
	            userHoldFeaturesMap.put(userId, new HashMap<Integer, List<HoldFeature>>());
	        }
	        Map<Integer, List<HoldFeature>> featuresPerCode = userHoldFeaturesMap.get(userId);
	        int code = feature.getCode();
	        if (!featuresPerCode.containsKey(code)) {
	            featuresPerCode.put(code, new ArrayList<HoldFeature>());
	        }
	        featuresPerCode.get(code).add(feature);
	    }

	    return userHoldFeaturesMap;
	}

    public static Map<Long, Map<ReleasePressPair, List<ReleasePressFeature>>> getUserReleasePressFeaturesMap() {
        if (userReleasePressFeaturesMap != null) {
            return userReleasePressFeaturesMap;
        }

        userReleasePressFeaturesMap = new HashMap<Long, Map<ReleasePressPair, List<ReleasePressFeature>>>();

        List<ReleasePressFeature> features = getReleasePressFeatures();
        for (ReleasePressFeature feature : features) {
            long userId = feature.getUser().getId();
            if (!userReleasePressFeaturesMap.containsKey(userId)) {
                userReleasePressFeaturesMap.put(userId, new HashMap<ReleasePressPair, List<ReleasePressFeature>>());
            }
            Map<ReleasePressPair, List<ReleasePressFeature>> featuresPerCode = userReleasePressFeaturesMap.get(userId);
            ReleasePressPair codePair = new ReleasePressPair(feature.getReleaseCode(), feature.getPressCode());
            if (!featuresPerCode.containsKey(codePair)) {
                featuresPerCode.put(codePair, new ArrayList<ReleasePressFeature>());
            }
            featuresPerCode.get(codePair).add(feature);
        }

        return userReleasePressFeaturesMap;
    }

    public static List<HoldFeature> getUserHoldFeatures(User user) {
        return DaoFactory.getFeaturesDAO().getUserHoldFeatures(user);
    }
    
    public static List<ReleasePressFeature> getUserReleasePressFeatures(User user) {
        return DaoFactory.getFeaturesDAO().getUserReleasePressFeatures(user);
    }
    
    public static List<ReleasePressFeature> getReleasePressFeatures() {
        return DaoFactory.getFeaturesDAO().getReleasePressFeatures();
    }	
	
    public static Feature save(Feature feature) {
        return DaoFactory.getFeaturesDAO().save(feature);
    }

    public static void deleteFeatures(User user) {
        FeaturesDao dao = DaoFactory.getFeaturesDAO();
        dao.deleteFeatures(user);
        EventsService.deleteUserEvents(user);
    }
    
    public static void deleteAll() {
        FeaturesDao dao = DaoFactory.getFeaturesDAO();
        dao.deleteAll();
    }

}
