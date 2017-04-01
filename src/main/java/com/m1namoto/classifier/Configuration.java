package com.m1namoto.classifier;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstraction of configuration in .arff format
 */
public class Configuration {

	static class Builder {
		private String name;
		private List<String> attributes = new ArrayList<String>();
		private List<Integer> allowedClassValues = new ArrayList<Integer>();
		private List<DynamicsInstance> instances = new ArrayList<DynamicsInstance>();

		public Builder name(@NotNull String name) {
			this.name = name;
			return this;
		}

		public Builder attribute(@NotNull String attr) {
			attributes.add(attr);
			return this;
		}

		public Builder classAttribute(@NotNull String attr, @NotNull List<Integer> allowedClassValues) {
			attribute(attr);
			this.allowedClassValues = allowedClassValues;
			return this;
		}

		public Builder instance(@NotNull List<Double> features, long classVal) throws Exception {
			if (features.size() != attributes.size()) {
				throw new IllegalArgumentException("Features amount is not equal to attributes");
			}
			instances.add(new DynamicsInstance(features, classVal));
			return this;
		}

		public Configuration build() {
			return new Configuration(name, attributes, allowedClassValues, instances);
		}

	}

	private final String name;

	private final List<String> attributes;
	private final List<Integer> allowedClassValues;
	private final List<DynamicsInstance> instances;

	private final String text;

	public Configuration(@NotNull String name,
						 @NotNull List<String> attributes,
						 @NotNull List<Integer> allowedClassValues,
						 @NotNull List<DynamicsInstance> instances) {
		this.name = name;
		this.attributes = ImmutableList.copyOf(attributes);
		this.allowedClassValues = ImmutableList.copyOf(allowedClassValues);
		this.instances = ImmutableList.copyOf(instances);

		this.text = text();
	}

	public List<Integer> getAllowedClassValues() {
		return allowedClassValues;
	}

	public String text() {
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

	@Override
	public String toString() {
		// cached because class is immutable
		return text;
	}
	
}
