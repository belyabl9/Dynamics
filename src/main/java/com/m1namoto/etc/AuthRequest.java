package com.m1namoto.etc;

public class AuthRequest {
    private final String login;
    private final String password;
    private final String stat;

    public AuthRequest(String login, String password, String stat) {
        this.login = login;
        this.password = password;
        this.stat = stat;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getStat() {
        return stat;
    }

    @Override
    public String toString() {
        return "AuthRequest{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", stat='" + stat + '\'' +
                '}';
    }
}
