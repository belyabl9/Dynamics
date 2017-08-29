package com.m1namoto.service.verification;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.m1namoto.entity.AuthenticationType;
import com.m1namoto.service.PropertiesService;

public class AuthenticationTypeService {

    private static final String AUTH_TYPE_PROP_NAME = "auth_type";
    private static final String PROP_NOT_SPECIFIED = "'" + AUTH_TYPE_PROP_NAME + "' property must be specified.";
    private static final String UNSUPPORTED_AUTH_TYPE = "Unsupported authentication type is specified.";

    private static AuthenticationType authType;

    private AuthenticationTypeService() {}

    private static class LazyHolder {
        static final AuthenticationTypeService INSTANCE = new AuthenticationTypeService();
    }
    public static AuthenticationTypeService getInstance() {
        return AuthenticationTypeService.LazyHolder.INSTANCE;
    }

    public AuthenticationType getAuthenticationType() {
        if (authType == null) {
            synchronized (this) {
                if (authType == null) {
                    Optional<String> authTypePropOpt = PropertiesService.getInstance().getStaticPropertyValue(AUTH_TYPE_PROP_NAME);
                    if (authTypePropOpt.isPresent()) {
                        Optional<AuthenticationType> authTypeOpt = Enums.getIfPresent(AuthenticationType.class, authTypePropOpt.get());
                        if (authTypeOpt.isPresent()) {
                            authType = authTypeOpt.get();
                        } else {
                            throw new RuntimeException(UNSUPPORTED_AUTH_TYPE);
                        }
                    } else {
                        throw new RuntimeException(PROP_NOT_SPECIFIED);
                    }
                }
            }
        }
        return authType;
    }

}
