package com.m1namoto.classifier;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Abstraction for classification instance
 * which has a list of feature values and a class value
 */
public class DynamicsInstance {
	private final List<Double> values;
	private long classValue;
	
    public DynamicsInstance(@NotNull List<Double> values) {
        this.values = ImmutableList.copyOf(values);
    }
    
	public DynamicsInstance(List<Double> values, long classValue) {
		this(values);
		this.classValue = classValue;
	}
	
	public List<Double> getValues() {
		return values;
	}

	public long getClassValue() {
		return classValue;
	}
	
}
