package com.m1namoto.domain;

import com.google.gson.annotations.Expose;
import com.m1namoto.features.FeatureExtractor;
import com.m1namoto.service.FeatureSampleService;
import com.m1namoto.service.FeatureService;
import com.m1namoto.service.SessionService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "Users")
public class User extends DomainSuperClass implements Serializable {
    private static final long serialVersionUID = 1L;

    final static Logger logger = Logger.getLogger(User.class);

    private final static int ORIGIN_HOLD_FEATURES_THRESHOLD = 100;
    private final static int OTHER_HOLD_FEATURES_THRESHOLD = 80;

    public enum Type {
        ADMIN(0),
        REGULAR(1),
        ;

        private final int value;

        Type(int value) {
            this.value = value;
        }

        public static Type fromInt(int value) {
            for (Type type : values()) {
                if (type.getValue() == value) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unsupported user type.");
        }

        public int getValue() {
            return value;
        }
    }

    @Expose
	@Column(name = "name")
	private String name;

    @Expose
	@Column(name = "login", unique = true)
    private String login;
    
    @Column(name = "password")
    private String password;

    @Column(name = "userType", nullable=false)
    @Enumerated(EnumType.ORDINAL)
    private Type userType;

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

    public Type getUserType() {
        return userType;
    }

    public void setUserType(Type userType) {
        this.userType = userType;
    }

    public boolean isNew() {
//        return getId() == 0;
        return true;
    }

	public double getMeanKeypressTime() {
        List<HoldFeature> holdFeatures = FeatureService.getHoldFeatures(this);
        if (holdFeatures.isEmpty()) {
            return 0d;
        }

        double meanSum = 0d;
        for (Feature feature : holdFeatures) {
            meanSum += feature.getValue();
        }

        return meanSum / holdFeatures.size();
	}

	@Nullable
    public Map<Integer, List<Double>> getHoldFeaturesByString(@NotNull String password) {
        Map<Integer, List<Double>> userFeaturesByString = new HashMap<>();
        long userId = this.getId();
        Map<Integer, List<HoldFeature>> userHoldFeaturesPerCode = FeatureService.getUserHoldFeaturesMap().get(userId);

        if (userHoldFeaturesPerCode == null) {
            return null;
        }

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
    public Map<ReleasePressPair, List<Double>> getReleasePressFeaturesByString(String password) {
        Map<ReleasePressPair, List<Double>> userFeaturesByString = new HashMap<>();
        long userId = this.getId();
        Map<ReleasePressPair, List<ReleasePressFeature>> userReleasePressFeaturesPerCode = FeatureService.getUserReleasePressFeaturesMap().get(userId);

        if (userReleasePressFeaturesPerCode == null) {
            return null;
        }
        
        char[] passwordCharacters = password.toCharArray();
        for (int i = 1; i < passwordCharacters.length; i++) {
            char pressCode = passwordCharacters[i],
                 releaseCode = passwordCharacters[i-1];
            ReleasePressPair codePair = new ReleasePressPair(releaseCode, pressCode);

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

    public List<FeaturesSample> getSamples(@NotNull String password, boolean fullSample) {
        List<FeaturesSample> samples = new ArrayList<>();
        double meanKeyPressTime = getMeanKeypressTime();
        
        Map<Integer, List<Double>> holdFeaturesByString = getHoldFeaturesByString(password);
        Map<ReleasePressPair, List<Double>> releasePressFeaturesByString = getReleasePressFeaturesByString(password);
        
        final int holdFeaturesMin = fullSample ? ORIGIN_HOLD_FEATURES_THRESHOLD : OTHER_HOLD_FEATURES_THRESHOLD;

        boolean isFullSampleEmpty = false;
        while (!isFullSampleEmpty) {
            FeaturesSample holdFeaturesSample = FeatureSampleService.getHoldFeaturesSampleByString(holdFeaturesByString, password);
            FeaturesSample releasePressFeaturesSample = FeatureSampleService.getReleasePressFeaturesSampleByString(releasePressFeaturesByString, password);

            List<Double> fullFeatureSampleLst = new ArrayList<>();
            fullFeatureSampleLst.addAll(holdFeaturesSample.getFeatures());
            fullFeatureSampleLst.addAll(releasePressFeaturesSample.getFeatures());
            fullFeatureSampleLst.add(meanKeyPressTime);

            boolean isHoldFeatureSampleEmpty  = holdFeaturesSample == null         || holdFeaturesSample.isEmpty();
            boolean isReleasePressSampleEmpty = releasePressFeaturesSample == null || releasePressFeaturesSample.isEmpty();
            isFullSampleEmpty = isHoldFeatureSampleEmpty && isReleasePressSampleEmpty;
            if (!isFullSampleEmpty) {
                boolean isEnoughElements = holdFeaturesSample.definedElementsPercentage() >= holdFeaturesMin;
                if (isEnoughElements) {
                    samples.add(new FeaturesSample(fullFeatureSampleLst));
                }
            }
        }

        return samples;
    }

}