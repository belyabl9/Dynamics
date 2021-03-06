package com.m1namoto.service;

import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.m1namoto.dao.DaoFactory;
import com.m1namoto.domain.*;
import com.m1namoto.service.verification.UserTemplateService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FeatureService {
    private final static Logger logger = Logger.getLogger(FeatureService.class);
    public static final String PASSWORD_MUST_BE_SPECIFIED = "Password must be specified.";

    private FeatureService() {}

    private static class LazyHolder {
        static final FeatureService INSTANCE = new FeatureService();
    }
    public static FeatureService getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * Cached map of hold features per user grouped by key code
     */
    private Map<Long, Map<Integer, List<HoldFeature>>> userHoldFeaturesMap;

    /**
     * Cached map of release-press features per user grouped by release-press key code pair
     */
    private Map<Long, Map<KeyCodePair, List<ReleasePressFeature>>> userReleasePressFeaturesMap;

    /**
     * Cached map of release-press features per user grouped by release-press key code pair
     */
    private Map<Long, Map<KeyCodePair, List<PressPressFeature>>> userPressPressFeaturesMap;

	/**
	 * @return List of all hold features from the storage
	 */
	public List<HoldFeature> getHoldFeatures() {
	    return DaoFactory.getFeaturesDAO().getHoldFeatures(); 
	}

    /**
     * @return List of all release-press features from the storage
     */	
    public List<ReleasePressFeature> getReleasePressFeatures() {
        return DaoFactory.getFeaturesDAO().getReleasePressFeatures();
    }

    /**
     * @return List of all press-press features from the storage
     */
    public List<PressPressFeature> getPressPressFeatures() {
        return DaoFactory.getFeaturesDAO().getPressPressFeatures();
    }

    /**
     * @return map of hold features per user (key=user id)
     */
    @NotNull
	public Map<Long, List<HoldFeature>> getHoldFeaturesPerUser() {
	    List<HoldFeature> features = getHoldFeatures();
        ListMultimap<Long, HoldFeature> featuresPerUser = ArrayListMultimap.create();
        for (HoldFeature feature : features) {
	        long userId = feature.getUser().getId();
            featuresPerUser.put(userId, feature);
	    }
	    return Multimaps.asMap(featuresPerUser);
	}

    @Nullable
    public Map<Integer, List<Double>> getHoldFeatureValuesByString(@NotNull User user, @NotNull String password) {
        if (password.isEmpty()){
            throw new IllegalArgumentException(PASSWORD_MUST_BE_SPECIFIED);
        }

	    Map<Integer, List<HoldFeature>> userHoldFeaturesPerCode = getUserHoldFeaturesMap().get(user.getId());
        if (userHoldFeaturesPerCode == null) {
            return null;
        }

        Map<Integer, List<Double>> userFeaturesByString = new HashMap<>();
        for (char code : password.toCharArray()) {
            List<HoldFeature> userHoldFeaturesByCode = userHoldFeaturesPerCode.get((int)code);
            if (CollectionUtils.isEmpty(userHoldFeaturesByCode)) {
                userFeaturesByString.put((int)code, null);
                continue;
            }
            List<Double> featureValuesByCode = new ArrayList<>();
            for (HoldFeature holdFeature : userHoldFeaturesByCode) {
                featureValuesByCode.add(holdFeature.getValue());
            }
            userFeaturesByString.put((int)code, featureValuesByCode);
        }

        return userFeaturesByString;
    }

    @Nullable
    public Map<KeyCodePair, List<Double>> getReleasePressFeatureValuesByString(@NotNull User user, @NotNull String password) {
        Map<KeyCodePair, List<ReleasePressFeature>> userReleasePressFeaturesPerCode = getUserReleasePressFeaturesMap().get(user.getId());
        if (userReleasePressFeaturesPerCode == null) {
            return null;
        }

        Map<KeyCodePair, List<Double>> userFeaturesByString = new HashMap<>();
        char[] passwordCharacters = password.toCharArray();
        for (int i = 1; i < passwordCharacters.length; i++) {
            char pressCode = passwordCharacters[i],
                 releaseCode = passwordCharacters[i-1];
            KeyCodePair codePair = new KeyCodePair(releaseCode, pressCode);

            List<ReleasePressFeature> userReleasePressFeaturesByCode = userReleasePressFeaturesPerCode.get(codePair);
            if (CollectionUtils.isEmpty(userReleasePressFeaturesByCode)) {
                userFeaturesByString.put(codePair, null);
                continue;
            }
            List<Double> featureValuesByCode = new ArrayList<>();
            for (ReleasePressFeature releasePressFeature : userReleasePressFeaturesByCode) {
                featureValuesByCode.add(releasePressFeature.getValue());
            }
            userFeaturesByString.put(codePair, featureValuesByCode);
        }

        return userFeaturesByString;
    }

    @Nullable
    public Map<KeyCodePair, List<Double>> getPressPressFeatureValuesByString(@NotNull User user, @NotNull String password) {
        Map<KeyCodePair, List<PressPressFeature>> userPressPressFeaturesPerCode = getUserPressPressFeaturesMap().get(user.getId());
        if (userPressPressFeaturesPerCode == null) {
            return null;
        }

        Map<KeyCodePair, List<Double>> userFeaturesByString = new HashMap<>();
        char[] passwordCharacters = password.toCharArray();
        for (int i = 1; i < passwordCharacters.length; i++) {
            char firstPressCode = passwordCharacters[i-1],
                 secondPressCode = passwordCharacters[i];
            KeyCodePair codePair = new KeyCodePair(firstPressCode, secondPressCode);

            List<PressPressFeature> userPressPressFeaturesByCode = userPressPressFeaturesPerCode.get(codePair);
            if (CollectionUtils.isEmpty(userPressPressFeaturesByCode)) {
                userFeaturesByString.put(codePair, null);
                continue;
            }
            List<Double> featureValuesByCode = new ArrayList<>();
            for (PressPressFeature pressPressFeature : userPressPressFeaturesByCode) {
                featureValuesByCode.add(pressPressFeature.getValue());
            }
            userFeaturesByString.put(codePair, featureValuesByCode);
        }

        return userFeaturesByString;
    }

    /**
     * Converts a list of hold features into a map per key code
     * @param features list of hold features
     * @return map of hold features per key code (key is key code)
     */
    @NotNull
	public Map<Integer, List<HoldFeature>> extractHoldFeaturesPerCode(@NotNull List<HoldFeature> features) {
        if (features.isEmpty()) {
            return Collections.emptyMap();
        }
        ListMultimap<Integer, HoldFeature> featuresPerCode = ArrayListMultimap.create();
	    for (HoldFeature feature : features) {
	        int code = feature.getCode();
	        featuresPerCode.put(code, feature);
	    }
	    return Multimaps.asMap(featuresPerCode);
	}

    /**
     * Converts a list of release-press features into a map per release-press key code pair
     * @param features list of release-press features
     * @return map of release-press features per key codes (key is key code pair (release and press))
     */
    @NotNull
    public Map<KeyCodePair, List<ReleasePressFeature>> extractReleasePressFeaturesPerCode(
            @NotNull List<ReleasePressFeature> features
    ) {
        if (features.isEmpty()) {
            return Collections.emptyMap();
        }
        ListMultimap<KeyCodePair, ReleasePressFeature> featuresPerCode = ArrayListMultimap.create();
        for (ReleasePressFeature feature : features) {
            int releaseCode = feature.getReleaseCode(),
                pressCode = feature.getPressCode();
            KeyCodePair codePair = new KeyCodePair(releaseCode, pressCode);
            featuresPerCode.put(codePair, feature);
        }
        return Multimaps.asMap(featuresPerCode);
    }

    /**
     * Iterates all saved hold features in the storage to return a map of hold features per user grouped by key code
     * @return map of hold features per user grouped by key code
     */
	public Map<Long, Map<Integer, List<HoldFeature>>> getUserHoldFeaturesMap() {
	    if (userHoldFeaturesMap != null) {
	        return userHoldFeaturesMap;
	    }

	    synchronized (this) {
            if (userHoldFeaturesMap != null) {
                return userHoldFeaturesMap;
            }
            userHoldFeaturesMap = new HashMap<>();

            List<HoldFeature> features = getHoldFeatures();
            for (HoldFeature feature : features) {
                // 1. Group by user id
                long userId = feature.getUser().getId();
                Map<Integer, List<HoldFeature>> featuresPerCode = userHoldFeaturesMap.get(userId);
                if (featuresPerCode == null) {
                    featuresPerCode = new HashMap<>();
                    userHoldFeaturesMap.put(userId, featuresPerCode);
                }

                // 2. Group by key code
                int code = feature.getCode();
                List<HoldFeature> userHoldFeatures = featuresPerCode.get(code);
                if (userHoldFeatures == null) {
                    userHoldFeatures = new ArrayList<>();
                    featuresPerCode.put(code, userHoldFeatures);
                }
                userHoldFeatures.add(feature);
            }
	    }


	    return userHoldFeaturesMap;
	}

    /**
     * Iterates all saved release-press features in the storage to return a map of release-press features per user grouped by release-press key codes
     * @return map of release-press features grouped by key code per user
     */
    public Map<Long, Map<KeyCodePair, List<ReleasePressFeature>>> getUserReleasePressFeaturesMap() {
        if (userReleasePressFeaturesMap != null) {
            return userReleasePressFeaturesMap;
        }

        synchronized (this) {
            if (userReleasePressFeaturesMap != null) {
                return userReleasePressFeaturesMap;
            }
            userReleasePressFeaturesMap = new HashMap<>();
            List<ReleasePressFeature> features = getReleasePressFeatures();
            for (ReleasePressFeature feature : features) {
                long userId = feature.getUser().getId();
                if (!userReleasePressFeaturesMap.containsKey(userId)) {
                    userReleasePressFeaturesMap.put(userId, new HashMap<KeyCodePair, List<ReleasePressFeature>>());
                }
                Map<KeyCodePair, List<ReleasePressFeature>> featuresPerCode = userReleasePressFeaturesMap.get(userId);
                KeyCodePair codePair = new KeyCodePair(feature.getReleaseCode(), feature.getPressCode());
                if (!featuresPerCode.containsKey(codePair)) {
                    featuresPerCode.put(codePair, new ArrayList<ReleasePressFeature>());
                }
                featuresPerCode.get(codePair).add(feature);
            }
        }

        return userReleasePressFeaturesMap;
    }

    public Map<Long, Map<KeyCodePair, List<PressPressFeature>>> getUserPressPressFeaturesMap() {
        if (userPressPressFeaturesMap != null) {
            return userPressPressFeaturesMap;
        }

        synchronized (this) {
            if (userPressPressFeaturesMap != null) {
                return userPressPressFeaturesMap;
            }
            userPressPressFeaturesMap = new HashMap<>();
            List<PressPressFeature> features = getPressPressFeatures();
            for (PressPressFeature feature : features) {
                long userId = feature.getUser().getId();
                if (!userPressPressFeaturesMap.containsKey(userId)) {
                    userPressPressFeaturesMap.put(userId, new HashMap<KeyCodePair, List<PressPressFeature>>());
                }
                Map<KeyCodePair, List<PressPressFeature>> featuresPerCode = userPressPressFeaturesMap.get(userId);
                KeyCodePair codePair = new KeyCodePair(feature.getFirstPressCode(), feature.getSecondPressCode());
                if (!featuresPerCode.containsKey(codePair)) {
                    featuresPerCode.put(codePair, new ArrayList<PressPressFeature>());
                }
                featuresPerCode.get(codePair).add(feature);
            }
        }

        return userPressPressFeaturesMap;
    }

    /**
     * @return list of user hold features
     */
    public List<HoldFeature> getHoldFeatures(@NotNull User user) {
        return DaoFactory.getFeaturesDAO().getHoldFeatures(user);
    }
    
    /**
     * @return list of user release-press features
     */
    @NotNull
    public List<ReleasePressFeature> getReleasePressFeatures(@NotNull User user) {
        return DaoFactory.getFeaturesDAO().getReleasePressFeatures(user);
    }

    @NotNull
    public List<PressPressFeature> getPressPressFeatures(@NotNull User user) {
        return DaoFactory.getFeaturesDAO().getPressPressFeatures(user);
    }

    @NotNull
    public Optional<Double> getMeanKeypressTime(@NotNull User user) {
        return getMeanTime(getHoldFeatures(user));
    }

    @NotNull
    public Optional<Double> getMeanReleasePressTime(@NotNull User user) {
        return getMeanTime(getReleasePressFeatures(user));
    }

    @NotNull
    public Optional<Double> getMeanTime(@NotNull List<? extends Feature> features) {
        if (features.isEmpty()) {
            return Optional.absent();
        }

        double meanSum = 0d;
        for (Feature feature : features) {
            meanSum += feature.getValue();
        }

        return Optional.of(meanSum / features.size());
    }

    /**
     * Saves a feature
     * @return saved feature
     */
    public Feature save(Feature feature) {
        Feature savedFeature = DaoFactory.getFeaturesDAO().save(feature);
        UserTemplateService.getInstance().invalidateCache(feature.getUser().getLogin());
        return savedFeature;
    }

    /**
     * Removes user features
     */
    public void remove(@NotNull User user) {
        DaoFactory.getFeaturesDAO().removeAll(user);
        invalidateFeatureCache();
    }

    /**
     * Deletes all features and clears cached feature maps
     */
    public void removeAll() {
        DaoFactory.getFeaturesDAO().removeAll();
        invalidateFeatureCache();
    }

    /**
     * Clears cached feature maps
     */
    public void invalidateFeatureCache() {
        userHoldFeaturesMap = null;
        userReleasePressFeaturesMap = null;
        userPressPressFeaturesMap = null;
    }

}
