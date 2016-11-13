package com.m1namoto.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.log4j.Logger;

import com.m1namoto.service.EventsService;
import com.m1namoto.service.FeatureSamplesService;
import com.m1namoto.service.FeaturesService;
import com.m1namoto.service.FeaturesSample;
import com.m1namoto.service.SessionsService;
import com.m1namoto.utils.ReleasePressPair;

@Entity
@Table(name = "Users")
public class User extends DomainSuperClass implements Serializable {
    final static Logger logger = Logger.getLogger(User.class);

    private final static int ORIGIN_HOLD_FEATURES_THRESHOLD = 100;
    private final static int OTHER_HOLD_FEATURES_THRESHOLD = 80;

    private final static int ORIGIN_RELEASE_PRESS_FEATURES_THRESHOLD = 100;
    private final static int OTHER_RELEASE_PRESS_FEATURES_THRESHOLD = 30;

    public static int USER_TYPE_ADMIN = 0;
    public static int USER_TYPE_REGULAR = 1;
    
	@Column(name = "name")
	private String name;

	@Column(name = "login", unique = true)
    private String login;
    
    @Column(name = "password")
    private String password;

    @Column(name = "userType", nullable=false)
    private int userType;

    @Column(name = "authenticatedCnt", columnDefinition = "int default 0", nullable=false)
    private int authenticatedCnt;

    public User() {}

    public int getAuthenticatedCnt() {
        return authenticatedCnt;
    }

    public void setAuthenticatedCnt(int authenticatedCnt) {
        this.authenticatedCnt = authenticatedCnt;
    }
    
    public String getFirstName() {
    	return name.split(" +")[0];
    }
    
    public String getSurname() {
    	return name.split(" +")[1];
    }
    
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }
	
    public List<Session> getSessions() {
        List<Event> events = EventsService.getListByUser(this);
        Map<String, List<Event>> sessionsMap = new HashMap<String, List<Event>>();

        for (Event event : events) {
            String sessionName = event.getSession();
            if (!sessionsMap.containsKey(sessionName)) {
                sessionsMap.put(sessionName, new ArrayList<Event>());
            }
            sessionsMap.get(sessionName).add(event);
        }
        
        List<Session> sessions = new ArrayList<Session>();
        for (String sessionName : sessionsMap.keySet()) {
            sessions.add(new Session(sessionName, sessionsMap.get(sessionName), this));
        }
        
        return sessions;
    }
    
	
	public int getSessionsCount() {
		List<Session> sessions = getSessions();
		return (sessions != null) ? sessions.size() : 0;
	}
	
	public double getMeanKeypressTime() {
		List<Session> sessions = getSessions();
		if (sessions == null) {
			return 0;
		}
		double keyTime = 0,
			   n = 0;
		for (Session session : sessions) {
			keyTime += FeaturesService.getMeanKeyPressTime(session.getEvents());
			n++;
		}
		
		return keyTime/n;
	}
	
	public double getMeanTimeBetweenKeys() throws Exception {
		List<Session> sessions = getSessions();
		if (sessions == null) {
			return 0;
		}
		double keyTime = 0,
			   n = 0;
		for (Session session : sessions) {
			keyTime += FeaturesService.getMeanTimeBetweenKeys(session.getEvents());
			n++;
		}
		
		return keyTime/n;
	}
	
    public List<HoldFeature> getHoldFeaturesByCode(int code) {
        long userId = this.getId();
        Map<Long, List<HoldFeature>> featuresPerUser = FeaturesService.getHoldFeaturesPerUser();
        List<HoldFeature> userHoldFeatures = featuresPerUser.get(userId);
        Map<Integer, List<HoldFeature>> featuresPerCode = FeaturesService.getHoldFeaturesPerCode(userHoldFeatures);
        
        return featuresPerCode.get(code);
    }

    public Map<Integer, List<Double>> getHoldFeaturesByString(String password) {
        Map<Integer, List<Double>> userFeaturesByString = new HashMap<Integer, List<Double>>();
        long userId = this.getId();
        Map<Integer, List<HoldFeature>> userHoldFeaturesPerCode = FeaturesService.getUserHoldFeaturesMap().get(userId);

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

    public Map<ReleasePressPair, List<Double>> getReleasePressFeaturesByString(String password) {
        Map<ReleasePressPair, List<Double>> userFeaturesByString = new HashMap<ReleasePressPair, List<Double>>();
        long userId = this.getId();
        Map<ReleasePressPair, List<ReleasePressFeature>> userReleasePressFeaturesPerCode
            = FeaturesService.getUserReleasePressFeaturesMap().get(userId);

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

    public Map<Integer, List<Double>> getXFeaturesByString(String password) {
        Map<Integer, List<Double>> userFeaturesByString = new HashMap<Integer, List<Double>>();
        long userId = this.getId();
        Map<Integer, List<XFeature>> userXFeaturesPerCode = FeaturesService.getUserXFeaturesMap().get(userId);

        if (userXFeaturesPerCode == null) {
            logger.debug("User Hold Featuers Per Code Map is null");
            return null;
        }

        logger.debug("User hold features per code map:");
        logger.debug(userXFeaturesPerCode);
        
        for (char code : password.toCharArray()) {
            List<XFeature> userXFeaturesByCode = userXFeaturesPerCode.get((int)code);
            if (userXFeaturesByCode == null || userXFeaturesByCode.size() == 0) {
                logger.debug("Null Code: " + code);
                userFeaturesByString.put((int)code, null);
                continue;
            }
            List<Double> featureValuesByCode = new ArrayList<Double>();
            for (int i = 0; i < userXFeaturesByCode.size(); i++) {
                featureValuesByCode.add(userXFeaturesByCode.get(i).getValue());
            }
            userFeaturesByString.put((int)code, featureValuesByCode);
        }

        return userFeaturesByString;
    }
    
    public Map<Integer, List<Double>> getYFeaturesByString(String password) {
        Map<Integer, List<Double>> userFeaturesByString = new HashMap<Integer, List<Double>>();
        long userId = this.getId();
        Map<Integer, List<YFeature>> userYFeaturesPerCode = FeaturesService.getUserYFeaturesMap().get(userId);

        if (userYFeaturesPerCode == null) {
            logger.debug("User Hold Featuers Per Code Map is null");
            return null;
        }

        logger.debug("User hold features per code map:");
        logger.debug(userYFeaturesPerCode);
        
        for (char code : password.toCharArray()) {
            List<YFeature> userYFeaturesByCode = userYFeaturesPerCode.get((int)code);
            if (userYFeaturesByCode == null || userYFeaturesByCode.size() == 0) {
                logger.debug("Null Code: " + code);
                userFeaturesByString.put((int)code, null);
                continue;
            }
            List<Double> featureValuesByCode = new ArrayList<Double>();
            for (int i = 0; i < userYFeaturesByCode.size(); i++) {
                featureValuesByCode.add(userYFeaturesByCode.get(i).getValue());
            }
            userFeaturesByString.put((int)code, featureValuesByCode);
        }

        return userFeaturesByString;
    }
    
    public List<HoldFeature> getHoldFeatures() {
        return FeaturesService.getUserHoldFeatures(this);
    }
    
    public List<ReleasePressFeature> getReleasePressFeatures() {
        return FeaturesService.getUserReleasePressFeatures(this);
    }
    
    public List<List<Double>> getSamples(User user, String password) {
        return getSamples(password, false);
    }

    public List<List<Double>> getSamples(String password, boolean fullSample) {
        logger.debug("Get User Samples");
        List<List<Double>> samples = new ArrayList<List<Double>>();
        double meanKeyPressTime = getMeanKeypressTime();
        
        Map<Integer, List<Double>> holdFeaturesByString = getHoldFeaturesByString(password);
        Map<ReleasePressPair, List<Double>> releasePressFeaturesByString = getReleasePressFeaturesByString(password);
        Map<Integer, List<Double>> xFeaturesByString = null;
        Map<Integer, List<Double>> yFeaturesByString = null;
        if (FeaturesService.includeMobileFeatures()) {
            xFeaturesByString = getXFeaturesByString(password);
            yFeaturesByString = getYFeaturesByString(password);
        }
        
        final int holdFeaturesMin = fullSample ? ORIGIN_HOLD_FEATURES_THRESHOLD : OTHER_HOLD_FEATURES_THRESHOLD;
        final int releasePressMin = fullSample ? ORIGIN_RELEASE_PRESS_FEATURES_THRESHOLD : OTHER_RELEASE_PRESS_FEATURES_THRESHOLD;
        
        boolean isEmptySample = false;
        while (!isEmptySample) {
            FeaturesSample holdFeaturesSample = FeatureSamplesService.getHoldFeaturesSampleByString(holdFeaturesByString, password);
            FeaturesSample releasePressFeaturesSample = FeatureSamplesService.getReleasePressFeaturesSampleByString(releasePressFeaturesByString, password);
            
            
            FeaturesSample xFeaturesSample = null; 
            FeaturesSample yFeaturesSample = null;
            if (FeaturesService.includeMobileFeatures()) {
                xFeaturesSample = FeatureSamplesService.getCoordFeaturesSampleByString(xFeaturesByString, password);    
                yFeaturesSample = FeatureSamplesService.getCoordFeaturesSampleByString(yFeaturesByString, password);
            }

            List<Double> featuresSample = new ArrayList<Double>();
            featuresSample.addAll(holdFeaturesSample.getFeatures());
            featuresSample.addAll(releasePressFeaturesSample.getFeatures());
            featuresSample.add(meanKeyPressTime);
            if (FeaturesService.includeMobileFeatures()) {
                featuresSample.addAll(xFeaturesSample.getFeatures());
                featuresSample.addAll(yFeaturesSample.getFeatures());    
            }

            isEmptySample = (holdFeaturesSample.isEmpty() && releasePressFeaturesSample.isEmpty());
            
            boolean isEnoughElements = (holdFeaturesSample.definedElementsPercentage() >= holdFeaturesMin
                    //&& releasePressFeaturesSample.definedElementsPercentage() >= releasePressMin
                    );

            logger.debug("Add sample: " + featuresSample);
            if (!isEmptySample && isEnoughElements) {
                samples.add(featuresSample);
            }
            
        }

        return samples;
    }

}