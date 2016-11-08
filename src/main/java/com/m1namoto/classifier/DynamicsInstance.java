package com.m1namoto.classifier;

import java.util.List;

public class DynamicsInstance {
	private List<Double> values;
	private long classValue;
	
    public DynamicsInstance(List<Double> values) {
        this.values = values; 
    }
    
	public DynamicsInstance(List<Double> values, long classValue) {
		this(values);
		this.classValue = classValue;
	}
	
	public void setValues(List<Double> values) {
		this.values = values;
	}
	
	public List<Double> getValues() {
		return values;
	}

	public long getClassValue() {
		return classValue;
	}
	
}
