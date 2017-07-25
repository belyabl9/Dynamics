package com.m1namoto.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;

public class PasswordService {

    private static final String PASSWORD_NOT_SPECIFIED = "Password must be non-empty.";

    private PasswordService() {}

    private static class LazyHolder {
        static final PasswordService INSTANCE = new PasswordService();
    }
    public static PasswordService getInstance() {
        return LazyHolder.INSTANCE;
    }

    @NotNull
    public String makeHash(@NotNull String password) {
        if (password.isEmpty()) {
            throw new IllegalArgumentException(PASSWORD_NOT_SPECIFIED);
        }
        return DigestUtils.sha1Hex(password);
    }

}
