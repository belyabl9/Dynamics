package com.m1namoto.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.m1namoto.service.AnomalyDetection;

@Entity
@Table(name="ReleasePressFeatures")  
@PrimaryKeyJoinColumn(name="feature_id")
@OnDelete(action = OnDeleteAction.CASCADE)
public class ReleasePressFeature extends Feature implements AnomalyDetection {

    @Column(name = "releaseCode")
    private int releaseCode;

    @Column(name = "pressCode")
    private int pressCode;

    public ReleasePressFeature() {}
    
    public ReleasePressFeature(double value, int releaseCode, int pressCode, User user) {
        super(value, user);
        this.releaseCode = releaseCode;
        this.pressCode = pressCode;
    }
    
    public int getReleaseCode() {
        return releaseCode;
    }

    public void setReleaseCode(int releaseCode) {
        this.releaseCode = releaseCode;
    }

    public int getPressCode() {
        return pressCode;
    }

    public void setPressCode(int pressCode) {
        this.pressCode = pressCode;
    }
    
    public String toString() {
        if (releaseCode > 0 && pressCode > 0) {
            return String.format("ReleasePressFeature[releaseCode=%c; pressCode=%c; value=%.2f]", releaseCode, pressCode, getValue());
        }
        return String.format("ReleasePressFeature[releaseCode=%d; pressCode=%d; value=%.2f]", releaseCode, pressCode, getValue());
    }

}
