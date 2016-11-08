package com.m1namoto.etc;

public class AuthRequest {
    private String login;
    private String password;
    private String stat;

    private boolean isTest = true;
    private double threshold;

    public AuthRequest(String login, String password, String stat) {
        this.login = login;
        this.password = password;
        this.stat = stat;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public boolean isTest() {
        return isTest;
    }

    public void setTest(boolean isTest) {
        this.isTest = isTest;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
    
}
