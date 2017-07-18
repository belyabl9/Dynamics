package com.m1namoto.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.m1namoto.anomaly.AnomalyDetection;

@Entity
@Table(name="ReleasePressFeatures")  
@PrimaryKeyJoinColumn(name="feature_id")
@OnDelete(action = OnDeleteAction.CASCADE)
public class ReleasePressFeature extends Feature implements AnomalyDetection {
    private static final long serialVersionUID = 1L;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + pressCode;
        result = prime * result + releaseCode;
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
        ReleasePressFeature other = (ReleasePressFeature) obj;
        if (pressCode != other.pressCode)
            return false;
        if (releaseCode != other.releaseCode)
            return false;
        if (value != other.value)
            return false;
        return true;
    }

    public String toString() {
        if (releaseCode > 0 && pressCode > 0) {
            return String.format("ReleasePressFeature[releaseCode=%c; pressCode=%c; value=%.2f]", releaseCode, pressCode, getValue());
        }
        return String.format("ReleasePressFeature[releaseCode=%d; pressCode=%d; value=%.2f]", releaseCode, pressCode, getValue());
    }
}
