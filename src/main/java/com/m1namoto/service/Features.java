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

public class Features {
    final static Logger logger = Logger.getLogger(Features.class);

    private static int KEY_PRESS_MAX_TIME = 1000;
    private static int BETWEEN_KEYS_MAX_TIME = 10000;
    
    private static Map<Long, Map<Integer, List<HoldFeature>>> userHoldFeaturesMap;
    private static Map<Long, Map<ReleasePressPair, List<ReleasePressFeature>>> userReleasePressFeaturesMap;
    
	private static Event getKeyEvent(List<Event> events, String action, int code, int start) {
	    for (int i = start; i < events.size(); i++) {
			Event event = events.get(i);
	        if ( (event.getCode() == code) && (event.getAction().equals(action)) ) {
	        	return event;
	        }
		}

		return null;
	}

	private static Event getKeyEvent(List<Event> events, String action, int start) {
	    for (int i = start; i < events.size(); i++) {
			Event event = events.get(i);
	        if (event.getAction().equals(action)) {
	        	return event;
	        }
		}

		return null;
	}

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
				Event keyReleaseEvent = getKeyEvent(events, Event.ACTION_RELEASE, code, i + 1);
				if (keyReleaseEvent == null) {
				    continue;
				}
				double timeDiff = keyReleaseEvent.getTime() - event.getTime();
				// Anomalies
				if (!Double.isInfinite(timeDiff) && timeDiff < KEY_PRESS_MAX_TIME) {
				    timeDiffs.add(timeDiff);
				}
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
                Event keyReleaseEvent = getKeyEvent(events, Event.ACTION_RELEASE, code, i + 1);
                if (keyReleaseEvent == null) {
                    continue;
                }
                double timeDiff = keyReleaseEvent.getTime() - event.getTime();
                // Anomalies
                if (!Double.isInfinite(timeDiff) && timeDiff < KEY_PRESS_MAX_TIME) {
                    holdFeatures.add(new HoldFeature(timeDiff, code, event.getUser()));
                }
            }
        }

        return holdFeatures;
    }
	
    public static List<ReleasePressFeature> getReleasePressFeatures(List<Event> events) {
        List<ReleasePressFeature> releasePressFeatures = new ArrayList<ReleasePressFeature>();

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            if (event.getAction().equals(Event.ACTION_RELEASE)) {
                Event keyPressEvent = getKeyEvent(events, Event.ACTION_PRESS, i + 1);
                if (keyPressEvent == null) {
                    continue;
                }
                double timeDiff = keyPressEvent.getTime() - event.getTime();
                // Anomalies
                if (!Double.isInfinite(timeDiff) && timeDiff > BETWEEN_KEYS_MAX_TIME) {
                    continue;
                }

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
				Event keyPressEvent = getKeyEvent(events, Event.ACTION_PRESS, i + 1);
				if (keyPressEvent == null) {
				    continue;
				}
				double timeDiff = keyPressEvent.getTime() - event.getTime();
                // Anomalies
				if (!Double.isInfinite(timeDiff) && timeDiff > BETWEEN_KEYS_MAX_TIME) {
					continue;
				}
				timeDiffs.add(timeDiff);
			}
		}

		return timeDiffs;
	}
	
	public static Instances readInstancesFromFile(String filename) {
	    Instances instances = null;

        try {
            byte[] encoded = Files.readAllBytes(Paths.get(filename));
            String content = new String(encoded, StandardCharsets.UTF_8);
            instances = new Instances(new StringReader(content));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return instances;
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
	
	public static Map<Integer, List<HoldFeature>> getUserHoldFeaturesPerCode(List<HoldFeature> userFeatures) {
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
	
    public static Map<ReleasePressPair, List<ReleasePressFeature>> getUserReleasePressFeaturesPerCode(List<ReleasePressFeature> userFeatures) {
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
	
	public static List<HoldFeature> getUserHoldFeaturesByCode(User user, int code) {
        long userId = user.getId();
	    Map<Long, List<HoldFeature>> featuresPerUser = getHoldFeaturesPerUser();
	    List<HoldFeature> userHoldFeatures = featuresPerUser.get(userId);
        Map<Integer, List<HoldFeature>> featuresPerCode = Features.getUserHoldFeaturesPerCode(userHoldFeatures);
        
        return featuresPerCode.get(code);
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

	
	public static Map<Integer, List<Double>> getUserHoldFeaturesByString(User user, String password) {
	    Map<Integer, List<Double>> userFeaturesByString = new HashMap<Integer, List<Double>>();
	    long userId = user.getId();
	    Map<Integer, List<HoldFeature>> userHoldFeaturesPerCode = getUserHoldFeaturesMap().get(userId);

	    if (userHoldFeaturesPerCode == null) {
	        logger.debug("User Hold Featuers Per Code Map is null");
	        return null;
	    }

	    logger.debug("User hold features per code map:");
	    logger.debug(userHoldFeaturesPerCode);
	    
	    for (char code : password.toCharArray()) {
	        List<HoldFeature> userHoldFeaturesByCode = userHoldFeaturesPerCode.get((int)code);
	        if (userHoldFeaturesByCode == null || userHoldFeaturesByCode.size() == 0) {
	            logger.debug("Null Code: " + code);
	            userFeaturesByString.put((int)code, null);
	            continue;
	        }
	        List<Double> featureValuesByCode = new ArrayList<Double>();
	        for (int i = 0; i < userHoldFeaturesByCode.size(); i++) {
	            featureValuesByCode.add(userHoldFeaturesByCode.get(i).getValue());
	        }
	        userFeaturesByString.put((int)code, featureValuesByCode);
	    }

	    return userFeaturesByString;
	}
	
	public static Map<ReleasePressPair, List<Double>> getUserReleasePressFeaturesByString(User user, String password) {
        Map<ReleasePressPair, List<Double>> userFeaturesByString = new HashMap<ReleasePressPair, List<Double>>();
        long userId = user.getId();
        Map<ReleasePressPair, List<ReleasePressFeature>> userReleasePressFeaturesPerCode = getUserReleasePressFeaturesMap().get(userId);

        if (userReleasePressFeaturesPerCode == null) {
            return null;
        }

        logger.debug("User release-press features per code map:");
        logger.debug(userReleasePressFeaturesPerCode);
        
        char[] passwordCharacters = password.toCharArray();
        
        for (int i = 1; i < passwordCharacters.length; i++) {
            char pressCode = passwordCharacters[i],
                 releaseCode = passwordCharacters[i-1];
            ReleasePressPair codePair = new ReleasePressPair(releaseCode, pressCode);

            List<ReleasePressFeature> userReleasePressFeaturesByCode = userReleasePressFeaturesPerCode.get(codePair);
            if (userReleasePressFeaturesByCode == null || userReleasePressFeaturesByCode.size() == 0) {
                logger.debug(String.format("Can not find release-press features for codes: %c - %c", releaseCode, pressCode));
                userFeaturesByString.put(codePair, null);
                continue;
            }
            List<Double> featureValuesByCode = new ArrayList<Double>();
            for (int j = 0; j < userReleasePressFeaturesByCode.size(); j++) {
                featureValuesByCode.add(userReleasePressFeaturesByCode.get(j).getValue());
            }
            userFeaturesByString.put(codePair, featureValuesByCode);
        }

        return userFeaturesByString;
	}
	
	private static class FeaturesSample {
        private List<Double> features;
	    private boolean isEmpty = true;
	    
        public void setFeatures(List<Double> features) {
            this.features = features;
        }

        public void setEmpty(boolean isEmpty) {
            this.isEmpty = isEmpty;
        }

	    public boolean isEmpty() {
	        return isEmpty;
	    }
	    
	    public List<Double> getFeatures() {
	        return features;
	    }
	}
	
	private static FeaturesSample getHoldFeaturesSampleByString(Map<Integer, List<Double>> holdFeaturesPerCode, String password) {
	    FeaturesSample sample = new FeaturesSample();
	    
	    List<Double> holdFeaturesSample = new ArrayList<Double>();
        for (char code : password.toCharArray()) {
            List<Double> featureValues = holdFeaturesPerCode.get((int)code);
            Double holdFeatureVal = null;
            if ((featureValues != null) && (featureValues.size() > 0)) {
                holdFeatureVal = featureValues.remove(0);
                sample.setEmpty(false);
            }
            holdFeaturesSample.add(holdFeatureVal);
        }
        
        sample.setFeatures(holdFeaturesSample);
        
        return sample;
	}
	
    private static FeaturesSample getReleasePressFeaturesSampleByString(
            Map<ReleasePressPair, List<Double>> releasePressFeaturesPerCode, String password) {
       
        FeaturesSample sample = new FeaturesSample();

        char[] passwordCharacters = password.toCharArray();
        List<Double> releasePressSample = new ArrayList<Double>();
        for (int i = 1; i < passwordCharacters.length; i++) {
            char releaseCode = passwordCharacters[i-1],
                 pressCode = passwordCharacters[i];
            ReleasePressPair codePair = new ReleasePressPair(releaseCode, pressCode);
            List<Double> releasePressValues = releasePressFeaturesPerCode.get(codePair);
            
            Double releasePressValue = null;
            
            if (releasePressValues != null && releasePressValues.size() > 0) {
                releasePressValue = releasePressValues.remove(0);
                sample.setEmpty(false);
            }

            releasePressSample.add(releasePressValue);
        }
        
        sample.setFeatures(releasePressSample);
        
        return sample;
    }
	
	public static List<List<Double>> getUserSamples(User user, String password) {
	    logger.debug("Get User Samples");
	    List<List<Double>> samples = new ArrayList<List<Double>>();

	    Map<Integer, List<Double>> holdFeaturesByString =  getUserHoldFeaturesByString(user, password);
	    Map<ReleasePressPair, List<Double>> releasePressFeaturesByString = getUserReleasePressFeaturesByString(user, password);

	    boolean isEmptySample = false;
	    while (!isEmptySample) {
	        FeaturesSample holdFeaturesSample = getHoldFeaturesSampleByString(holdFeaturesByString, password);
	        FeaturesSample releasePressFeaturesSample = getReleasePressFeaturesSampleByString(releasePressFeaturesByString, password);

            List<Double> featuresSample = new ArrayList<Double>();
            featuresSample.addAll(holdFeaturesSample.getFeatures());
            featuresSample.addAll(releasePressFeaturesSample.getFeatures());

            logger.debug("Add sample: " + featuresSample);
            samples.add(featuresSample);
	        
            isEmptySample = (holdFeaturesSample.isEmpty() && releasePressFeaturesSample.isEmpty());
	    }

        return samples;
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
        Events.deleteUserEvents(user);
    }

}
