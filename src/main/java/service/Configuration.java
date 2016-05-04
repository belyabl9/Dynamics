package service;
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
	
	public void addInstance(List<Double> features, long classVal) throws Exception {
		if (features.size() != attributes.size() - 1) {
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
			instancesStr.append(StringUtils.join(inst.getValues(), ',') + "," + inst.getClassValue() + "\n");
		}
		
		String result = title    				+
						attributesSB.toString() +
						classAttr 				+
						"@data\n" 				+
						instancesStr.toString();
		
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration("Dynamics");
		
		conf.addAttribute("feature1");
		conf.addAttribute("feature2");
		List<Integer> allowedValues = new ArrayList<Integer>();
		allowedValues.add(1);
		allowedValues.add(2);
		allowedValues.add(3);
		conf.setClassAttribute("classVal", allowedValues);
		
		List<Double> values1 = new ArrayList<Double>();
		values1.add(1.2);
		values1.add(1.3);
		conf.addInstance(values1, 1);
		
		List<Double> values2 = new ArrayList<Double>();
		values2.add(1.5);
		values2.add(1.6);
		conf.addInstance(values2, 2);
		
		List<Double> values3 = new ArrayList<Double>();
		values3.add(1.9);
		values3.add(2.5);
		conf.addInstance(values3, 3);
		
		System.out.println(conf);
	}
	
}
