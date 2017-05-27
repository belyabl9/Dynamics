package com.m1namoto.classifier;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Abstraction of configuration in .arff format.</p>
 *
 * <pre>
 *  Example:
 * {@literal @}RELATION iris

 * {@literal @}ATTRIBUTE sepallength  NUMERIC
 * {@literal @}ATTRIBUTE sepalwidth   NUMERIC
 * {@literal @}ATTRIBUTE petallength  NUMERIC
 * {@literal @}ATTRIBUTE petalwidth   NUMERIC
 * {@literal @}ATTRIBUTE class        {1,2,3}
 *
 * {@literal @}DATA
 *  5.1,3.5,1.4,0.2,1
 *  4.9,3.0,1.4,0.2,1
 *  4.7,3.2,1.3,0.2,2
 *  4.6,3.1,1.5,0.2,3
 *
 * </pre>
 */
public class Configuration {

	private static final String WRONG_NUMBER_OF_FEATURES = "Number of features is not equal to attributes";
	private static final String CONFIGURATION_NAME_MUST_BE_SPECIFIED = "Configuration name must be specified.";

	static class Builder {
		private String name;
		private List<String> attributes = new ArrayList<>();
		private List<Integer> allowedClassValues = new ArrayList<>();
		private List<DynamicsInstance> instances = new ArrayList<>();

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

		public Builder instance(@NotNull List<Double> features, long classVal) {
			if (features.size() != attributes.size()) {
				throw new IllegalArgumentException(WRONG_NUMBER_OF_FEATURES);
			}
			instances.add(new DynamicsInstance(features, Optional.of(classVal)));
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

	// cached, lazy-init
	private String text;

	public Configuration(@NotNull String name,
						 @NotNull List<String> attributes,
						 @NotNull List<Integer> allowedClassValues,
						 @NotNull List<DynamicsInstance> instances) {
		if (name.isEmpty()) {
			throw new IllegalArgumentException(CONFIGURATION_NAME_MUST_BE_SPECIFIED);
		}

		this.name = name;
		this.attributes = ImmutableList.copyOf(attributes);
		this.allowedClassValues = ImmutableList.copyOf(allowedClassValues);
		this.instances = ImmutableList.copyOf(instances);
	}

	public List<Integer> getAllowedClassValues() {
		return allowedClassValues;
	}

	@NotNull
	public String prepareText() {
		String title = String.format("@relation %s", name);

		List<String> attributesLst = new ArrayList<>();
		for (int i = 0; i < attributes.size() - 1; i++) {
			attributesLst.add(String.format("@attribute %s numeric", attributes.get(i)));
		}

		String classAllowedValues = StringUtils.join(allowedClassValues, ", ");
		String classAttr = "@attribute " + attributes.get(attributes.size() - 1) + " {" + classAllowedValues + "}";

		List<String> instancesLst = new ArrayList<>();
		for (DynamicsInstance instance : instances) {
			List<String> sampleValues = new ArrayList<>();
			for (Double val : instance.getValues()) {
				sampleValues.add(val != null ? val.toString() : "?");
			}
			sampleValues.add(instance.getClassValue().get().toString());
			String joined = StringUtils.join(sampleValues, ",");
			instancesLst.add(joined);
		}
		String attributes = StringUtils.join(attributesLst, System.lineSeparator());
		String instances  = StringUtils.join(instancesLst, System.lineSeparator());

		return  title      + System.lineSeparator()	+
				attributes + System.lineSeparator() +
				classAttr  + System.lineSeparator() + System.lineSeparator() +
				"@data"    + System.lineSeparator() +
				instances;
	}

	@Override
	public String toString() {
		if (text == null) {
			text = prepareText();
		}
		return text;
	}
	
}
