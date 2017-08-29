package com.m1namoto.service;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.m1namoto.entity.FeatureType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeatureSelectionService {

    private static final String FEATURES_PROP_NAME = "features";
    private static final String PROP_NOT_SPECIFIED = "'" + FEATURES_PROP_NAME + "' property must be specified.";
    private static final String UNSUPPORTED_FEATURE_TYPE = "Unsupported feature type is specified.";

    private static final Splitter FEATURE_SPLITTER = Splitter.on(";").omitEmptyStrings();

    private static Set<FeatureType> featureTypes;

    private FeatureSelectionService() {}

    private static class LazyHolder {
        static final FeatureSelectionService INSTANCE = new FeatureSelectionService();
    }
    public static FeatureSelectionService getInstance() {
        return LazyHolder.INSTANCE;
    }

    public Set<FeatureType> getFeatureTypes() {
        if (featureTypes == null) {
            synchronized (this) {
                if (featureTypes == null) {
                    Set<FeatureType> featureTypes = new HashSet<>();
                    Optional<String> featuresPropOpt = PropertiesService.getInstance().getStaticPropertyValue(FEATURES_PROP_NAME);
                    if (featuresPropOpt.isPresent()) {
                        List<String> featureStrLst = FEATURE_SPLITTER.splitToList(featuresPropOpt.get());
                        for (String featureStr : featureStrLst) {
                            Optional<FeatureType> featureTypeOpt = Enums.getIfPresent(FeatureType.class, featureStr);
                            if (featureTypeOpt.isPresent()) {
                                featureTypes.add(featureTypeOpt.get());
                            } else {
                                throw new RuntimeException(UNSUPPORTED_FEATURE_TYPE);
                            }
                        }
                        this.featureTypes = featureTypes;
                    } else {
                        throw new RuntimeException(PROP_NOT_SPECIFIED);
                    }
                }
            }
        }
        return featureTypes;
    }

}
