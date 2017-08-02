package com.m1namoto.service.auth;

import com.google.common.base.Optional;
import org.jetbrains.annotations.NotNull;

public class AuthenticationResult {
    private final boolean success;
    private final Optional<Double> threshold;
    private final AuthenticationStatus status;

    public AuthenticationResult(boolean success, @NotNull AuthenticationStatus status, @NotNull Optional<Double> threshold) {
        this.success = success;
        this.status = status;
        this.threshold = threshold;
    }

    public AuthenticationResult(boolean success, @NotNull AuthenticationStatus status) {
        this(success, status, Optional.<Double>absent());
    }

    public AuthenticationResult(boolean success) {
        this(success, success ? AuthenticationStatus.SUCCESS : AuthenticationStatus.FAIL, Optional.<Double>absent());
    }

    public boolean isSuccess() {
        return success;
    }

    public AuthenticationStatus getStatus() {
        return status;
    }

    public Optional<Double> getThreshold() {
        return threshold;
    }

    @Override
    public String toString() {
        return "AuthenticationResult{" +
                "success=" + success +
                ", threshold=" + threshold +
                ", status=" + status +
                '}';
    }
}
