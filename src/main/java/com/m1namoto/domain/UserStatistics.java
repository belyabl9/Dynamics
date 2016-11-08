package com.m1namoto.domain;

import java.util.List;

public class UserStatistics {
	private User user;
	private List<HoldFeature> features;
	
	public UserStatistics(User user, List<HoldFeature> features) {
	    this.user = user;
	    this.features = features;
	}

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<HoldFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<HoldFeature> features) {
        this.features = features;
    }
	
	
}
