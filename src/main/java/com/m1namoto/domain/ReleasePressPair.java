package com.m1namoto.domain;

public class ReleasePressPair {
    private final int releaseCode;
    private final int pressCode;
    
    public ReleasePressPair(int releaseCode, int pressCode) {
        this.releaseCode = releaseCode;
        this.pressCode = pressCode;
    }

    public int getReleaseCode() {
        return releaseCode;
    }

    public int getPressCode() {
        return pressCode;
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
        ReleasePressPair other = (ReleasePressPair) obj;
        if (pressCode != other.pressCode)
            return false;
        if (releaseCode != other.releaseCode)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ReleasePressPair{" +
                "releaseCode=" + releaseCode +
                ", pressCode=" + pressCode +
                '}';
    }
}
