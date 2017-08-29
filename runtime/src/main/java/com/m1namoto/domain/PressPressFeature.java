package com.m1namoto.domain;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name="PressPressFeatures")
@PrimaryKeyJoinColumn(name="feature_id")
@OnDelete(action = OnDeleteAction.CASCADE)
public class PressPressFeature extends Feature {
    private static final long serialVersionUID = 1L;

    @Column(name = "firstPressCode")
    private int firstPressCode;

    @Column(name = "secondPressCode")
    private int secondPressCode;

    public PressPressFeature() {}

    public PressPressFeature(double value, int firstPressCode, int secondPressCode, User user) {
        super(value, user);
        this.firstPressCode = firstPressCode;
        this.secondPressCode = secondPressCode;
    }

    public int getFirstPressCode() {
        return firstPressCode;
    }

    public void setFirstPressCode(int firstPressCode) {
        this.firstPressCode = firstPressCode;
    }

    public int getSecondPressCode() {
        return secondPressCode;
    }

    public void setSecondPressCode(int secondPressCode) {
        this.secondPressCode = secondPressCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PressPressFeature that = (PressPressFeature) o;

        if (firstPressCode != that.firstPressCode) return false;
        return secondPressCode == that.secondPressCode;
    }

    @Override
    public int hashCode() {
        int result = firstPressCode;
        result = 31 * result + secondPressCode;
        return result;
    }

    @Override
    public String toString() {
        if (firstPressCode > 0 && secondPressCode > 0) {
            return String.format("ReleasePressFeature[releaseCode=%c; pressCode=%c; value=%.2f]", firstPressCode, secondPressCode, getValue());
        }
        return String.format("ReleasePressFeature[releaseCode=%d; pressCode=%d; value=%.2f]", firstPressCode, secondPressCode, getValue());
    }
}