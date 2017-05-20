package com.m1namoto.classifier;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Abstraction for classification instance
 * which has a list of feature values and a class value
 */
public class DynamicsInstance {
	@NotNull
	private final List<Double> values;
	@NotNull
	private Optional<Long> classValue;
	
    public DynamicsInstance(@NotNull List<Double> values) {
        this(values, Optional.<Long>absent());
    }
    
	public DynamicsInstance(@NotNull List<Double> values,
							@NotNull Optional<Long> classValue) {
		this.values = ImmutableList.copyOf(values);
		this.classValue = classValue;
	}

	@NotNull
	public List<Double> getValues() {
		return values;
	}

	@NotNull
	public Optional<Long> getClassValue() {
		return classValue;
	}
	
}
