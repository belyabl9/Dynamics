package com.m1namoto.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name="YFeatures")  
@PrimaryKeyJoinColumn(name="feature_id")
@OnDelete(action = OnDeleteAction.CASCADE)
public class YFeature extends Feature {
    private static final long serialVersionUID = 1L;

    public YFeature() {}
    
    @Column(name = "code")
    private int code;

    public YFeature(double value, int code, User user) {
        super(value, user);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String toString() {
        if (code > 0) {
            return String.format("YFeature[code=%c; value=%.2f]", code, getValue());
        }
        return String.format("YFeature[code=%d; value=%.2f]", code, getValue());
    }
}