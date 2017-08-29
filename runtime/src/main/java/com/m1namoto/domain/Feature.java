package com.m1namoto.domain;

import com.google.gson.annotations.Expose;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "Features")
@Inheritance(strategy=InheritanceType.JOINED)
@OnDelete(action = OnDeleteAction.CASCADE)
public abstract class Feature extends DomainSuperClass implements Serializable {
    private static final long serialVersionUID = 1L;

    @Expose
    @Column(name = "value")
    protected double value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    public Feature() {}
    
    public Feature(double value, @NotNull User user) {
        this.value = value;
        this.user = user;
    }
    
    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
    
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
