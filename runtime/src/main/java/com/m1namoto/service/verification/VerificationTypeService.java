package com.m1namoto.service.verification;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.m1namoto.service.PropertiesService;

public class VerificationTypeService {

    private static final String VERIFICATION_TYPE_PROP_NAME = "verification_type";
    private static final String PROP_NOT_SPECIFIED = "'" + VERIFICATION_TYPE_PROP_NAME + "' property must be specified.";
    private static final String UNSUPPORTED_VERIFICATION_TYPE = "Unsupported verification type is specified.";

    private static VerificationType verificationType;

    private VerificationTypeService() {}

    private static class LazyHolder {
        static final VerificationTypeService INSTANCE = new VerificationTypeService();
    }
    public static VerificationTypeService getInstance() {
        return VerificationTypeService.LazyHolder.INSTANCE;
    }

    public VerificationType getVerificationType() {
        if (verificationType == null) {
            synchronized (this) {
                if (verificationType == null) {
                    Optional<String> verificationTypePropOpt = PropertiesService.getInstance().getStaticPropertyValue(VERIFICATION_TYPE_PROP_NAME);
                    if (verificationTypePropOpt.isPresent()) {
                        Optional<VerificationType> verificationTypeOpt = Enums.getIfPresent(VerificationType.class, verificationTypePropOpt.get());
                        if (verificationTypeOpt.isPresent()) {
                            verificationType = verificationTypeOpt.get();
                        } else {
                            throw new RuntimeException(UNSUPPORTED_VERIFICATION_TYPE);
                        }
                    } else {
                        throw new RuntimeException(PROP_NOT_SPECIFIED);
                    }
                }
            }
        }

        return verificationType;
    }

}
