package com.m1namoto.entity;

import com.google.common.base.Optional;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * <p>Classification instance that contains a list of feature values and a class value.</p>
 *
 * <p>Class value may be absent for instances which are going to be classified</p>
 *
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
		this.values = Collections.unmodifiableList(values);
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
