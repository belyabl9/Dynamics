package com.m1namoto.domain;

public class Event {
    public static String ACTION_PRESS = "press";
    public static String ACTION_RELEASE = "release";
    
    private int code;
    private String action;
    private long time;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Event{" +
                "code=" + code +
                ", char='" + (char) code + '\'' +
                ", action='" + action + '\'' +
                ", time=" + time +
                '}';
    }
}
