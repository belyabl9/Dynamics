package com.m1namoto.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.google.gson.annotations.Expose;
import com.m1namoto.anomalyDetection.AnomalyDetection;

@Entity
@Table(name="HoldFeatures")  
@PrimaryKeyJoinColumn(name="feature_id")
@OnDelete(action = OnDeleteAction.CASCADE)
public class HoldFeature extends Feature implements AnomalyDetection {
    private static final long serialVersionUID = 1L;

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
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + code;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HoldFeature other = (HoldFeature) obj;
        if (code != other.code)
            return false;
        if (value != other.value)
            return false;
        return true;
    }
}
