package com.m1namoto.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.google.gson.annotations.SerializedName;

@Entity
@Table(name = "Events")
public class Event extends DomainSuperClass implements Serializable {
    private static final long serialVersionUID = 1L;

    public static String ACTION_PRESS = "press";
    public static String ACTION_RELEASE = "release";
    
    @Column(name = "session")
    private String session;

    @Column(name = "code")
    @SerializedName("entity")
    private int code;
    
    @Column(name = "action")
    private String action;

    @Column(name = "time")
    private long time;
    
    @Column(name = "orientation")
    private String orientation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

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
    
    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public String toString() {
        char symbol = (char) code;
        return String.format("Event[action=%s;code=%c;char=%c;time=%d]", action, code, symbol, time);
    }
    
}
