package com.m1namoto.domain;

import javax.lang.model.element.AnnotationMirror;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.google.gson.annotations.Expose;
import com.m1namoto.service.AnomalyDetection;

@Entity
@Table(name="HoldFeatures")  
@PrimaryKeyJoinColumn(name="feature_id")
@OnDelete(action = OnDeleteAction.CASCADE)
public class HoldFeature extends Feature implements AnomalyDetection {

    public HoldFeature() {}
    
    @Expose
    @Column(name = "code")
    private int code;

    public HoldFeature(double value, int code, User user) {
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
            return String.format("HoldFeature[code=%c; value=%.2f]", code, getValue());
        }
        return String.format("HoldFeature[code=%d; value=%.2f]", code, getValue());
    }
    
}
