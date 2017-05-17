package com.m1namoto.domain;

import com.m1namoto.features.FeatureExtractor;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Sessions")
public class Session extends DomainSuperClass implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "date")
    @Type(type="timestamp")
    private Date date = new Date();

    @Transient
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Transient
    private List<Event> events = new ArrayList<Event>();

    @OneToMany(mappedBy="session", fetch=FetchType.LAZY)
    @Cascade({CascadeType.DELETE})
    private List<Feature> features = new ArrayList<Feature>();

    public Session(String name, List<Event> events, User user) {
		this.name = name;
	    this.events = events;
	    this.user = user;
	}
	
	public Session() {}
	
	public List<Feature> getFeatures() {
	    return features;
	}
	
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

    public Date getDate() {
        return date;
    }
    
    public List<HoldFeature> getHoldFeaturesFromEvents() {
        return FeatureExtractor.getInstance().getHoldFeatures(events);
    }
    
    public List<ReleasePressFeature> getReleasePressFeaturesFromEvents() {
        return FeatureExtractor.getInstance().getReleasePressFeatures(events);
    }

    public List<Feature> getFeaturesFromEvents() {
        List<Feature> features = new ArrayList<>();
        features.addAll(getHoldFeaturesFromEvents());
        features.addAll(getReleasePressFeaturesFromEvents());
        
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
