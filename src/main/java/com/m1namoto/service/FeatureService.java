package com.m1namoto.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.m1namoto.dao.DaoFactory;
import com.m1namoto.dao.FeaturesDao;
import com.m1namoto.domain.*;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureService {
    private final static Logger logger = Logger.getLogger(FeatureService.class);

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
        ListMultimap<Long, HoldFeature> featuresPerUser = ArrayListMultimap.create();
        for (HoldFeature feature : features) {
	        long userId = feature.getUser().getId();
            featuresPerUser.put(userId, feature);
	    }
	    return Multimaps.asMap(featuresPerUser);
	}
	
    /**
     * Returns hold features per key code. Map key is key code
     * @param features List of hold features
     * @return Map of hold features per code
     */
    @NotNull
	public static Map<Integer, List<HoldFeature>> extractHoldFeaturesPerCode(@NotNull List<HoldFeature> features) {
        ListMultimap<Integer, HoldFeature> featuresPerCode = ArrayListMultimap.create();
	    for (HoldFeature feature : features) {
	        int code = feature.getCode();
	        featuresPerCode.put(code, feature);
	    }
	    return Multimaps.asMap(featuresPerCode);
	}

    /**
     * Returns release-press features per key codes. Map key is key codes pair (release and press)
     * @param userFeatures List of release-press features
     * @return Map of release-press features per key codes
     */
    @NotNull
    public static Map<ReleasePressPair, List<ReleasePressFeature>> extractReleasePressFeaturesPerCode(
            @NotNull List<ReleasePressFeature> userFeatures
    ) {
        ListMultimap<ReleasePressPair, ReleasePressFeature> featuresPerCode = ArrayListMultimap.create();
        for (ReleasePressFeature feature : userFeatures) {
            int releaseCode = feature.getReleaseCode(),
                pressCode = feature.getPressCode();
            ReleasePressPair codePair = new ReleasePressPair(releaseCode, pressCode);
            featuresPerCode.put(codePair, feature);
        }
        return Multimaps.asMap(featuresPerCode);
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

	    userHoldFeaturesMap = new HashMap<>();
	    List<HoldFeature> features = getHoldFeatures();
	    extractHoldFeaturesPerCode(features);
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
	    
//	    for (Entry<Long, Map<Integer, List<HoldFeature>>> userFeaturesEntry: userHoldFeaturesMap.entrySet()) {
//	    	long userId = userFeaturesEntry.getKey();
//	    	for (Entry<Integer, List<HoldFeature>> holdFeaturesEntry : userFeaturesEntry.getValue().entrySet()) {
//		        int code = holdFeaturesEntry.getKey();
//	    		List<HoldFeature> anomalyDetectionList = holdFeaturesEntry.getValue();
//		        String description = String.format("[HoldFeature] Code: %d; User: %d", code, userId);
//		        AnomalyDetector anomalyDetector = new AnomalyDetector(anomalyDetectionList, description);
//		        anomalyDetector.removeAnomalies();
//	    	}
//	    }
	    
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
        userReleasePressFeaturesMap = new HashMap<>();
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
        
//        logger.info("Check Release Press Anomalies");
//        for (Map<ReleasePressPair, List<ReleasePressFeature>> userReleasePressFeatures : userReleasePressFeaturesMap.values()) {
//        	for (Entry<ReleasePressPair, List<ReleasePressFeature>> releasePressPairEntry : userReleasePressFeatures.entrySet()) {
//        		ReleasePressPair releasePressPair = releasePressPairEntry.getKey();
//        		List<ReleasePressFeature> releasePressFeaturesList = releasePressPairEntry.getValue();
//        		String description = String.format("[ReleasePressFeature] Release code: %d; Press code: %d; User: %s",
//        				releasePressPair.getReleaseCode(), releasePressPair.getPressCode(), releasePressFeaturesList.get(0).getUser().getLogin());
//        		AnomalyDetector anomalyDetector = new AnomalyDetector(releasePressFeaturesList, description);
//        		anomalyDetector.removeAnomalies();
//        	}
//        }

        return userReleasePressFeaturesMap;
    }

    /**
     * Wrapper around FeaturesDAO.getHoldFeatures()
     * @param user
     * @return List of user hold features
     */
    public static List<HoldFeature> getHoldFeatures(@NotNull User user) {
        return DaoFactory.getFeaturesDAO().getHoldFeatures(user);
    }
    
    /**
     * Wrapper around FeaturesDAO.getReleasePressFeatures()
     * @param user
     * @return List of user release-press features
     */
    @NotNull
    public static List<ReleasePressFeature> getReleasePressFeatures(@NotNull User user) {
        return DaoFactory.getFeaturesDAO().getReleasePressFeatures(user);
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
    public static void remove(@NotNull User user) {
        FeaturesDao dao = DaoFactory.getFeaturesDAO();
        dao.removeAll(user);
        clearFeatureMaps();
    }

    /**
     * Deletes all features and clears cached feature maps
     */
    public static void removeAll() {
        DaoFactory.getFeaturesDAO().removeAll();
        clearFeatureMaps();
    }


    /**
     * Clears cached feature maps
     */
    public static void clearFeatureMaps() {
        userHoldFeaturesMap = null;
        userReleasePressFeaturesMap = null;
    }

}
