package com.m1namoto.domain;

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

    @Column(name = "date")
    @Type(type="timestamp")
    private Date date = new Date();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy="session", fetch=FetchType.LAZY)
    @Cascade({CascadeType.DELETE})
    private List<Feature> features = new ArrayList<>();

    public Session() {}

    public Session(@NotNull User user) {
	    this.user = user;
	}

	public List<Feature> getFeatures() {
	    return features;
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
                ", user=" + user +
                ", features=" + features +
                '}';
    }
}
