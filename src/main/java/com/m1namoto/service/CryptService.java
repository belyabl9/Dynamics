package com.m1namoto.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;

public class CryptService {

    @NotNull
    public static String cryptPassword(@NotNull String password) {
        if (password.isEmpty()) {
            throw new IllegalArgumentException("Password must be non-empty.");
        }
        return DigestUtils.sha1Hex(password);
    }

}
