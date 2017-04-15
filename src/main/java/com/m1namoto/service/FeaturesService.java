package com.m1namoto.service;

import com.m1namoto.dao.DaoFactory;
import com.m1namoto.dao.FeaturesDao;
import com.m1namoto.domain.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeaturesService {
    final static Logger logger = Logger.getLogger(FeaturesService.class);

    private static Map<Long, Map<Integer, List<HoldFeature>>> userHoldFeaturesMap;
    private static Map<Long, Map<ReleasePressPair, List<ReleasePressFeature>>> userReleasePressFeaturesMap;

	/**
	 * Wrapper around FeaturesDAO.getHoldFeatures()
	 * @return List of hold features from the storage
	 */
	public static List<HoldFeature> getHoldFeatures() {
	    return DaoFactory.getFeaturesDAO().getHoldFeatures(); 
	}

    /**
     * Wrapper around FeaturesDAO.getReleasePressFeatures()
     * @return List of release-press features from the storage
     */	
    public static List<ReleasePressFeature> getReleasePressFeatures() {
        return DaoFactory.getFeaturesDAO().getReleasePressFeatures();
    }   
	
    /**
     * Returns hold features per user map. Key is user id
     * @return Map of hold features per user
     */
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
	
    /**
     * Returns hold features per key code. Map key is key code
     * @param features List of hold features
     * @return Map of hold features per code
     */
	public static Map<Integer, List<HoldFeature>> getHoldFeaturesPerCode(List<HoldFeature> features) {
	    Map<Integer, List<HoldFeature>> featuresPerCode = new HashMap<Integer, List<HoldFeature>>();

	    for (HoldFeature feature : features) {
	        int code = feature.getCode();
	        if (!featuresPerCode.containsKey(code)) {
	            featuresPerCode.put(code, new ArrayList<HoldFeature>());
	        }
	        featuresPerCode.get(code).add(feature);
	    }

	    return featuresPerCode;
	}

	/**
	 * Returns a list of user hold features by code
	 * @param user
	 * @param code
	 * @return List of user hold features by code
	 */
    public static List<HoldFeature> getUserHoldFeaturesByCode(User user, int code) {
        long userId = user.getId();
        Map<Long, List<HoldFeature>> featuresPerUser = getHoldFeaturesPerUser();
        List<HoldFeature> userHoldFeatures = featuresPerUser.get(userId);
        Map<Integer, List<HoldFeature>> featuresPerCode = getUserHoldFeaturesPerCode(userHoldFeatures);
        
        return featuresPerCode.get(code);
    }
    
    /**
     * Returns a map of user hold features by string.
     * Features are grouped by key code
     * @param user
     * @param password
     * @return Map of user hold features by string
     */
    public static Map<Integer, List<Double>> getUserHoldFeaturesByString(User user, String password) {
        Map<Integer, List<Double>> userFeaturesByString = new HashMap<Integer, List<Double>>();
        long userId = user.getId();
        Map<Integer, List<HoldFeature>> userHoldFeaturesPerCode = getUserHoldFeaturesMap().get(userId);

        if (userHoldFeaturesPerCode == null) {
            return null;
        }

        for (char code : password.toCharArray()) {
            List<HoldFeature> userHoldFeaturesByCode = userHoldFeaturesPerCode.get((int)code);
            if (userHoldFeaturesByCode == null || userHoldFeaturesByCode.size() == 0) {
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

    /**
     * Returns a map of user hold features per code from passed hold features
     * @param userFeatures
     * @return Map of user hold features per code
     */
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
    
    /**
     * Returns hold features sample by string from the passed hold features
     * @param holdFeaturesPerCode
     * @param password
     * @return Hold features sample
     */
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

    /**
     * Returns release-press features per key codes. Map key is key codes pair (release and press)
     * @param features List of release-press features
     * @return Map of release-press features per key codes
     */
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
    
    /**
     * Returns a map of user release-press features by string.
     * Features are grouped by key release-press key codes
     * @param user
     * @param password
     * @return Map of user release-press features by string.
     */
    public static Map<ReleasePressPair, List<Double>> getUserReleasePressFeaturesByString(User user, String password) {
        Map<ReleasePressPair, List<Double>> userFeaturesByString = new HashMap<ReleasePressPair, List<Double>>();
        long userId = user.getId();
        Map<ReleasePressPair, List<ReleasePressFeature>> userReleasePressFeaturesPerCode = getUserReleasePressFeaturesMap().get(userId);

        if (userReleasePressFeaturesPerCode == null) {
            return null;
        }

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

    /**
     * Returns release-press features sample by string from the passed hold feature values grouped by release-press key codes
     * @param releasePressFeaturesPerCode
     * @param password
     * @return Release-press features sample
     */
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

    /**
     * Returns a map of hold features per user.
     * Features within a user are grouped by key code.
     * Iterates all the saved hold features in the storage
     * @return Map of hold features grouped by key code per user
     */
	public static Map<Long, Map<Integer, List<HoldFeature>>> getUserHoldFeaturesMap() {
	    if (userHoldFeaturesMap != null) {
	        return userHoldFeaturesMap;
	    }

	    userHoldFeaturesMap = new HashMap<Long, Map<Integer, List<HoldFeature>>>();
	    List<HoldFeature> features = getHoldFeatures();
	    getHoldFeaturesPerCode(features);
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
	    
	  /*  for (Entry<Long, Map<Integer, List<HoldFeature>>> userFeaturesEntry: userHoldFeaturesMap.entrySet()) {
	    	long userId = userFeaturesEntry.getKey();
	    	for (Entry<Integer, List<HoldFeature>> holdFeaturesEntry : userFeaturesEntry.getValue().entrySet()) {
		        int code = holdFeaturesEntry.getKey();
	    		List<HoldFeature> anomalyDetectionList = holdFeaturesEntry.getValue();
		        String description = String.format("[HoldFeature] Code: %d; User: %d", code, userId);
		        AnomalyDetector anomalyDetector = new AnomalyDetector(anomalyDetectionList, description);
		        anomalyDetector.removeAnomalies();
	    	}
	    }*/
	    
	    return userHoldFeaturesMap;
	}

    /**
     * Returns a map of release-press features per user.
     * Features within a user are grouped by release-press key codes.
     * Iterates all the saved release-press features in the storage
     * @return Map of release-press features grouped by key code per user
     */
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
        
       /* logger.info("Check Release Press Anomalies");
        for (Map<ReleasePressPair, List<ReleasePressFeature>> userReleasePressFeatures : userReleasePressFeaturesMap.values()) {
        	for (Entry<ReleasePressPair, List<ReleasePressFeature>> releasePressPairEntry : userReleasePressFeatures.entrySet()) {
        		ReleasePressPair releasePressPair = releasePressPairEntry.getKey();
        		List<ReleasePressFeature> releasePressFeaturesList = releasePressPairEntry.getValue();
        		String description = String.format("[ReleasePressFeature] Release code: %d; Press code: %d; User: %s",
        				releasePressPair.getReleaseCode(), releasePressPair.getPressCode(), releasePressFeaturesList.get(0).getUser().getLogin());
        		AnomalyDetector anomalyDetector = new AnomalyDetector(releasePressFeaturesList, description);
        		anomalyDetector.removeAnomalies();
        	}
        }*/

        return userReleasePressFeaturesMap;
    }

    /**
     * Wrapper around FeaturesDAO.getUserHoldFeatures()
     * @param user
     * @return List of user hold features
     */
    public static List<HoldFeature> getUserHoldFeatures(User user) {
        return DaoFactory.getFeaturesDAO().getUserHoldFeatures(user);
    }
    
    /**
     * Wrapper around FeaturesDAO.getUserReleasePressFeatures()
     * @param user
     * @return List of user release-press features
     */
    public static List<ReleasePressFeature> getUserReleasePressFeatures(User user) {
        return DaoFactory.getFeaturesDAO().getUserReleasePressFeatures(user);
    }
	
    /**
     * Wrapper around FeaturesDAO.save().
     * Saves a feature
     * @param feature
     * @return Saved feature
     */
    public static Feature save(Feature feature) {
        return DaoFactory.getFeaturesDAO().save(feature);
    }

    /**
     * Deletes user features
     * @param user
     */
    public static void deleteFeatures(User user) {
        FeaturesDao dao = DaoFactory.getFeaturesDAO();
        dao.deleteFeatures(user);
        EventsService.deleteUserEvents(user);
    }

    /**
     * Clears cached feature maps
     */
    public static void clearFeatureMaps() {
        userHoldFeaturesMap = null;
        userReleasePressFeaturesMap = null;
    }

    /**
     * Deletes all features and clears cached feature maps
     */
    public static void deleteAll() {
        DaoFactory.getFeaturesDAO().deleteAll();
        clearFeatureMaps();
    }

}
