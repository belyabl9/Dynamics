package com.m1namoto.classifier;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class Configuration {
	private String name;

	private List<String> attributes = new ArrayList<String>();
	private List<Integer> allowedClassValues = new ArrayList<Integer>();
	private List<DynamicsInstance> instances = new ArrayList<DynamicsInstance>();

	public Configuration(String name) {
		this.name = name;
	}

	public void addAttribute(String attr) {
		attributes.add(attr);
	}

	public void setClassAttribute(String attr, List<Integer> allowedClassValues) {
		addAttribute(attr);
		this.allowedClassValues = allowedClassValues;
	}

	public List<Integer> getAllowedClassValues() {
		return allowedClassValues;
	}
	
	public void addInstance(List<Double> features, long classVal) throws Exception {
		if (features.size() != attributes.size()) {
			throw new Exception("Features amount is not equal to attributes");
		}
		instances.add(new DynamicsInstance(features, classVal));
	}

	@Override
	public String toString() {
		String title = String.format("@relation %s\n", name);
		
		StringBuilder attributesSB = new StringBuilder();
		for (int i = 0; i < attributes.size() - 1; i++) {
			String attr = String.format("@attribute %s numeric\n", attributes.get(i));
			attributesSB.append(attr);
		}
		
		String classAllowedValues = StringUtils.join(allowedClassValues, ", ");
		String classAttr = "@attribute " + attributes.get(attributes.size() - 1) + " {" + classAllowedValues + "}\n\n";
		
		StringBuilder instancesStr = new StringBuilder();
		for (int i = 0; i < instances.size(); i++) {
			DynamicsInstance inst = instances.get(i);
			List<Double> featuresValues = inst.getValues();
			StringBuilder featuresValuesStr = new StringBuilder();
			for (int j = 0; j < featuresValues.size(); j++) {
			    Double val = featuresValues.get(j);
			    featuresValuesStr.append(val == null ? "?" : val);
			    boolean isLast = (j == (featuresValues.size() - 1));
			    if (!isLast) {
			        featuresValuesStr.append(",");    
			    }
			}
			String sample = featuresValuesStr.toString() + "," + inst.getClassValue() + "\n";
			instancesStr.append(sample);
		}
		
		String result = title    				+
						attributesSB.toString() +
						classAttr 				+
						"@data\n" 				+
						instancesStr.toString();
		
		return result;
	}
	
}
