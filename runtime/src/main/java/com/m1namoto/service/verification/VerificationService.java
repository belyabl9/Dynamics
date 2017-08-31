package com.m1namoto.service.verification;

import com.google.common.base.Optional;
import com.m1namoto.api.AnomalyDetector;
import com.m1namoto.domain.User;
import com.m1namoto.entity.FeatureType;
import com.m1namoto.entity.UserTemplate;
import com.m1namoto.service.FeatureSelectionService;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Distance is computed for every feature type separately and after that final decision is computed.
 */
public class VerificationService implements AnomalyDetector {

    private static final Logger logger = Logger.getLogger(VerificationService.class);

    private static final UserTemplateService USER_TEMPLATE_SERVICE = UserTemplateService.getInstance();
    private static final DistanceCalcService DISTANCE_CALC_SERVICE = DistanceCalcService.getInstance();

    private static final String NOT_SUPPORTED_VERIFICATION_TYPE = "Specified verification type is not supported.";

    @NotNull
    private final VerificationType verificationType;

    public VerificationService(@NotNull VerificationType verificationType) {
        this.verificationType = verificationType;
    }

    @Override
    public boolean isAnomaly(@NotNull Map<FeatureType, List<Double>> testFeatureValuesMap,
                             @NotNull User authUser,
                             @NotNull Optional<Double> maxAllowedDistanceOpt) {
        Set<FeatureType> featureTypes = FeatureSelectionService.getInstance().getFeatureTypes();

        UserTemplate template = USER_TEMPLATE_SERVICE.getTemplate(authUser);
        double holdScore = 0;
        double releasePressScore = 0;
        double pressPressScore = 0;

        switch (verificationType) {
            case DTW:
                if (featureTypes.contains(FeatureType.HOLD)) {
                    holdScore = DISTANCE_CALC_SERVICE.dtw(
                            template.getMeanVector(FeatureType.HOLD),
                            testFeatureValuesMap.get(FeatureType.HOLD)
                    );
                }
                if (featureTypes.contains(FeatureType.RELEASE_PRESS)) {
                    releasePressScore = DISTANCE_CALC_SERVICE.dtw(
                            template.getMeanVector(FeatureType.RELEASE_PRESS),
                            testFeatureValuesMap.get(FeatureType.RELEASE_PRESS)
                    );
                }
                if (featureTypes.contains(FeatureType.PRESS_PRESS)) {
                    pressPressScore = DISTANCE_CALC_SERVICE.dtw(
                            template.getMeanVector(FeatureType.PRESS_PRESS),
                            testFeatureValuesMap.get(FeatureType.PRESS_PRESS)
                    );
                }
                break;
            case MANHATTAN:
                if (featureTypes.contains(FeatureType.HOLD)) {
                    holdScore = DISTANCE_CALC_SERVICE.manhattan(
                            template.getMeanVector(FeatureType.HOLD),
                            testFeatureValuesMap.get(FeatureType.HOLD)
                    );
                }
                if (featureTypes.contains(FeatureType.RELEASE_PRESS)) {
                    releasePressScore = DISTANCE_CALC_SERVICE.manhattan(
                            template.getMeanVector(FeatureType.RELEASE_PRESS),
                            testFeatureValuesMap.get(FeatureType.RELEASE_PRESS)
                    );
                }
                if (featureTypes.contains(FeatureType.PRESS_PRESS)) {
                    pressPressScore = DISTANCE_CALC_SERVICE.manhattan(
                            template.getMeanVector(FeatureType.PRESS_PRESS),
                            testFeatureValuesMap.get(FeatureType.PRESS_PRESS)
                    );
                }
                break;
            case MANHATTAN_SCALED:
                if (featureTypes.contains(FeatureType.HOLD)) {
                    holdScore = DISTANCE_CALC_SERVICE.manhattanScaled(
                            template.getMeanVector(FeatureType.HOLD),
                            testFeatureValuesMap.get(FeatureType.HOLD),
                            template.getMeanAbsDeviationVector(FeatureType.HOLD)
                    );
                }
                if (featureTypes.contains(FeatureType.RELEASE_PRESS)) {
                    releasePressScore = DISTANCE_CALC_SERVICE.manhattanScaled(
                            template.getMeanVector(FeatureType.RELEASE_PRESS),
                            testFeatureValuesMap.get(FeatureType.RELEASE_PRESS),
                            template.getMeanAbsDeviationVector(FeatureType.RELEASE_PRESS)
                    );
                }
                if (featureTypes.contains(FeatureType.PRESS_PRESS)) {
                    pressPressScore = DISTANCE_CALC_SERVICE.manhattanScaled(
                            template.getMeanVector(FeatureType.PRESS_PRESS),
                            testFeatureValuesMap.get(FeatureType.PRESS_PRESS),
                            template.getMeanAbsDeviationVector(FeatureType.PRESS_PRESS)
                    );
                }
                break;
            case MAHANABOLIS:
                if (featureTypes.contains(FeatureType.HOLD)) {
                    holdScore = DISTANCE_CALC_SERVICE.mahanabolis(
                            template.getMeanVector(FeatureType.HOLD),
                            testFeatureValuesMap.get(FeatureType.HOLD),
                            template.getMeanAbsDeviationVector(FeatureType.HOLD)
                    );
                }
                if (featureTypes.contains(FeatureType.RELEASE_PRESS)) {
                    releasePressScore = DISTANCE_CALC_SERVICE.mahanabolis(
                            template.getMeanVector(FeatureType.RELEASE_PRESS),
                            testFeatureValuesMap.get(FeatureType.RELEASE_PRESS),
                            template.getMeanAbsDeviationVector(FeatureType.RELEASE_PRESS)
                    );
                }
                if (featureTypes.contains(FeatureType.PRESS_PRESS)) {
                    pressPressScore = DISTANCE_CALC_SERVICE.mahanabolis(
                            template.getMeanVector(FeatureType.PRESS_PRESS),
                            testFeatureValuesMap.get(FeatureType.PRESS_PRESS),
                            template.getMeanAbsDeviationVector(FeatureType.PRESS_PRESS)
                    );
                }
                break;
            default:
                throw new UnsupportedOperationException(NOT_SUPPORTED_VERIFICATION_TYPE);
        }

        boolean holdMatches = false;
        boolean releasePressMatches = false;
        boolean pressPressMatches = false;
        if (featureTypes.contains(FeatureType.HOLD)) {
            double maxAllowedDistance = maxAllowedDistanceOpt.isPresent() ? maxAllowedDistanceOpt.get() : template.getThreshold(FeatureType.HOLD);
//            logger.info(holdScore + " vs " + maxAllowedDistance);
            holdMatches = holdScore <= maxAllowedDistance;
        }
        if (featureTypes.contains(FeatureType.RELEASE_PRESS)) {
            double maxAllowedDistance = maxAllowedDistanceOpt.isPresent() ? maxAllowedDistanceOpt.get() : template.getThreshold(FeatureType.RELEASE_PRESS);
//            logger.info(releasePressScore + " vs " + maxAllowedDistance);
            releasePressMatches = releasePressScore <= maxAllowedDistance;
        }
        if (featureTypes.contains(FeatureType.PRESS_PRESS)) {
            double maxAllowedDistance = maxAllowedDistanceOpt.isPresent() ? maxAllowedDistanceOpt.get() : template.getThreshold(FeatureType.PRESS_PRESS);
//            logger.info(pressPressScore + " vs " + maxAllowedDistance);
            pressPressMatches = pressPressScore <= maxAllowedDistance;
        }

        if (!holdMatches) {
            return true;
        }

        return !releasePressMatches && !pressPressMatches;
    }

}
