package service;

import java.util.ArrayList;
import java.util.List;

public class DynamicsInstance {

	private List<Double> values = new ArrayList<Double>();

	private long classValue;
	
	public DynamicsInstance(List<Double> values, long classValue) {
		this.values = values;
		this.classValue = classValue;
	}
	
	public DynamicsInstance(List<Double> values) {
		this.values = values;
	}
	
	public List<Double> getValues() {
		return values;
	}

	public long getClassValue() {
		return classValue;
	}
	
}
