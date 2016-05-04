package service;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import domain.Event;
import domain.Session;
import domain.User;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instance;
import weka.core.Instances;

public class NeuralNetwork {

	private MultilayerPerceptron perceptron;
	
	public NeuralNetwork() throws Exception {
		perceptron = createPerceptron();
		perceptron.buildClassifier( createInstances(createConfiguration().toString()) );
	}
	
	private MultilayerPerceptron createPerceptron() {
		MultilayerPerceptron mlp = new MultilayerPerceptron();

		//Setting Parameters
		mlp.setLearningRate(0.1);
		mlp.setMomentum(0.2);
		mlp.setTrainingTime(2000);
		mlp.setHiddenLayers("3");

		return mlp;
	}
	
	private Instances createInstances(String input) {
		try {
			Instances instances = new Instances(new StringReader(input));
			instances.setClassIndex(instances.numAttributes() - 1);
			return instances;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Configuration createConfiguration() throws Exception {
		Configuration conf = new Configuration("Dynamics");
		
		conf.addAttribute("keypress");
		conf.addAttribute("betweenKeys");
		
		List<User> users = Users.getList();
		List<Integer> allowedValues = new ArrayList<Integer>();
		for (User user : users) {
			allowedValues.add((int) user.getId());
		}
		
		conf.setClassAttribute("classVal", allowedValues);
		
		Map<Long, List<Session>> sessionsPerUser = Sessions.getSessionsPerUser();
		for (Long userId : sessionsPerUser.keySet()) {
			System.out.println("User ID: " + userId);
			List<Session> userSessions = sessionsPerUser.get(userId);
			System.out.println("Sessions Amount: " + userSessions.size());
			for (Session session : userSessions) {
				List<Event> sessionEvents = session.getEvents();
				double meanKeyTime = Features.getMeanKeyTime(sessionEvents);
				double meanTimeBetweenKeys = Features.getMeanTimeBetweenKeys(sessionEvents);
				System.out.printf("%f,%f,%d\n", meanKeyTime, meanTimeBetweenKeys, userId);
				
				List<Double> featureValues = new ArrayList<Double>();
				featureValues.add(meanKeyTime);
				featureValues.add(meanTimeBetweenKeys);
				conf.addInstance(featureValues, userId);
			}
		}

		return conf;
	}
	
	public int getClassForInstance(DynamicsInstance inst) throws Exception {
		Instance instance = new Instance(3);
		instance.setValue(0, inst.getValues().get(0));
		instance.setValue(1, inst.getValues().get(1));

		double clsLabel = perceptron.classifyInstance(instance);

		return Integer.parseInt(instance.classAttribute().value((int)clsLabel));
	}
	
	public static void main(String[] args) throws Exception {
		NeuralNetwork network = new NeuralNetwork();
		
		List<Double> lst = new ArrayList<Double>();
		lst.add(7.0);
		lst.add(8.0);
		DynamicsInstance instance = new DynamicsInstance(lst);
		
		System.out.println( network.getClassForInstance(instance) );
		
	}
	
}
