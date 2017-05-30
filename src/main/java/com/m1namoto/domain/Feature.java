package com.m1namoto.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.google.gson.annotations.Expose;
import org.jetbrains.annotations.NotNull;

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
