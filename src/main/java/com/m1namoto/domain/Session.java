package com.m1namoto.domain;

import com.google.common.collect.ImmutableList;
import com.m1namoto.features.FeatureExtractor;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Sessions")
public class Session extends DomainSuperClass implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String SESSION_NAME_MUST_BE_SPECIFIED = "Session name must be specified.";

    @Column(name = "date")
    @Type(type="timestamp")
    private Date date = new Date();

    @Transient
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy="session", fetch=FetchType.LAZY)
    @Cascade({CascadeType.DELETE})
    private List<Feature> features = new ArrayList<>();

    public Session() {}

    public Session(@NotNull String name, @NotNull User user) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException(SESSION_NAME_MUST_BE_SPECIFIED);
        }
		this.name = name;
	    this.user = user;
	}

	public List<Feature> getFeatures() {
	    return features;
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

    @Override
    public String toString() {
        return "Session{" +
                "date=" + date +
                ", name='" + name + '\'' +
                ", user=" + user +
                ", features=" + features +
                '}';
    }
}
