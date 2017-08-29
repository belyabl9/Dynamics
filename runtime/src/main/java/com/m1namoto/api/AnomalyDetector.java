package com.m1namoto.api;

import com.google.common.base.Optional;
import com.m1namoto.domain.User;
import com.m1namoto.entity.FeatureType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface AnomalyDetector {

    boolean isAnomaly(@NotNull Map<FeatureType, List<Double>> testFeatureValuesMap,
                      @NotNull User authUser,
                      @NotNull Optional<Double> maxAllowedDistanceOpt
    );

}
