package com.m1namoto.domain;

public class KeyCodePair {
    private final int firstCode;
    private final int secondCode;
    
    public KeyCodePair(int firstCode, int secondCode) {
        this.firstCode = firstCode;
        this.secondCode = secondCode;
    }

    public int getFirstCode() {
        return firstCode;
    }

    public int getSecondCode() {
        return secondCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyCodePair that = (KeyCodePair) o;

        if (firstCode != that.firstCode) return false;
        return secondCode == that.secondCode;
    }

    @Override
    public int hashCode() {
        int result = firstCode;
        result = 31 * result + secondCode;
        return result;
    }

    @Override
    public String toString() {
        return "KeyCodePair{" +
                "firstCode=" + firstCode +
                ", secondCode=" + secondCode +
                '}';
    }
}
