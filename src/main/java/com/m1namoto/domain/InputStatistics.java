package com.m1namoto.domain;

import java.util.List;

public class InputStatistics {
    private List<Event> password;
    private List<Event> additional;

    public InputStatistics() {}

    public List<Event> getPassword() {
        return password;
    }

    public void setPassword(List<Event> password) {
        this.password = password;
    }

    public List<Event> getAdditional() {
        return additional;
    }

    public void setAdditional(List<Event> additional) {
        this.additional = additional;
    }

    @Override
    public String toString() {
        return "InputStatistics{" +
                "password=" + password +
                ", additional=" + additional +
                '}';
    }
}
