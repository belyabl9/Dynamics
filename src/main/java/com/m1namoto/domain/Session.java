package com.m1namoto.domain;

import java.util.ArrayList;
import java.util.List;

import com.m1namoto.service.Features;

public class Session {

    private String name;
    private User user;
    private List<Event> events = new ArrayList<Event>();

	public Session(String name, List<Event> events, User user) {
		this.name = name;
	    this.events = events;
	    this.user = user;
	}
	
	public Session() {}
	
	public void addEvent(Event event) {
		events.add(event);
	}
	
	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public List<HoldFeature> getHoldFeatures() {
        return Features.getHoldFeatures(events);
    }
    
    public List<ReleasePressFeature> getReleasePressFeatures() {
        return Features.getReleasePressFeatures(events);
    }

    public List<Feature> getFeatures() {
        List<Feature> features = new ArrayList<Feature>();
        features.addAll(getHoldFeatures());
        features.addAll(getReleasePressFeatures());
        
        return features;
    }

	public String toString() {
		StringBuilder sb = new StringBuilder("Session " + name + "\n[");
		
		for (Event event : events) {
		    sb.append(event + "\n");
		}
		
		return sb.toString();
	}

}
