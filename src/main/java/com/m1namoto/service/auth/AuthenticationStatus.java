package com.m1namoto.service.auth;

public enum AuthenticationStatus {
    SUCCESS,
    FIRST_TRUSTED_ATTEMPTS,

    WRONG_PASSWORD,
    EMPTY_LOGIN_OR_PASSWORD,
    CAN_NOT_FIND_USER,
    DYNAMICS_NOT_PASSED,
    ADMIN_ACCESS,
    FAIL,
    ;

    public static void assertSize(int expectedItems) {
        assert values().length == expectedItems : "Update the code calling AuthenticationStatus with " + expectedItems + "!";
    }
}
