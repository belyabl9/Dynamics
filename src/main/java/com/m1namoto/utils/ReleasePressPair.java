package com.m1namoto.utils;

public class ReleasePressPair {
    private int releaseCode;
    private int pressCode;
    
    public ReleasePressPair(int releaseCode, int pressCode) {
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
        ReleasePressPair other = (ReleasePressPair) obj;
        if (pressCode != other.pressCode)
            return false;
        if (releaseCode != other.releaseCode)
            return false;
        return true;
    }
    
    
    
}
