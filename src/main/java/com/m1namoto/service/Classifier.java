package com.m1namoto.service;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.m1namoto.domain.User;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;

public class Classifier {
    final static Logger logger = Logger.getLogger(Classifier.class);

	private static String CONFIGURATION_NAME = "Dynamics";
	private static String ATTR_KEY_PRESS = "keypress";
	private static String ATTR_BETWEEN_KEYS = "betweenKeys";
	private static String ATTR_CLASS = "classVal";

	private String password;
	private RandomForest classifier = new RandomForest();
	private Instances instances;

	public Classifier(String password) throws Exception {
	    logger.debug("Create classifier");
	    this.password = password;
	    instances = createInstances(createConfiguration().toString());
	    classifier.buildClassifier(instances);
	    evaluateClassifier();
	}

	private void evaluateClassifier() throws Exception {
        Evaluation eval = new Evaluation(instances);
        eval.evaluateModel(classifier, instances);
        logger.debug(eval.errorRate());
        logger.debug(eval.toSummaryString());
	}
	
	private Instances createInstances(String input) {
		try {
		    logger.debug("Create instances");
			Instances instances = new Instances(new StringReader(input));
			instances.setClassIndex(instances.numAttributes() - 1);

			return instances;
		} catch (IOException e) {
			logger.error("Can not create configuration instances: ", e);
			return null;
		}
	}

	public Configuration createConfiguration() throws Exception {
	    logger.debug("Create configuration " + CONFIGURATION_NAME);
	    Configuration conf = new Configuration(CONFIGURATION_NAME);

		for (int i = 0; i < password.length(); i++) {
		    logger.debug("Add attribute definition: " + ATTR_KEY_PRESS + (i + 1));
		    conf.addAttribute(ATTR_KEY_PRESS + (i + 1));
		}
		
        for (int i = 1; i < password.length(); i++) {
            logger.debug("Add attribute definition: " + ATTR_BETWEEN_KEYS + i);
            conf.addAttribute(ATTR_BETWEEN_KEYS + i);
        }
		
		List<User> users = Users.getList(User.USER_TYPE_REGULAR);
		List<Integer> allowedValues = new ArrayList<Integer>();

        logger.debug("Prepare user features");
		for (User user : users) {
		    logger.debug("User id: " + user.getId());
		    List<List<Double>> featuresSamples = Features.getUserSamples(user, password);
            for (List<Double> sample : featuresSamples) {
                logger.debug("Add sample: " + sample);
                conf.addInstance(sample, user.getId());
            }

            logger.debug("Add user to the list of allowed users");
		    allowedValues.add((int) user.getId());
		}
		conf.setClassAttribute(ATTR_CLASS, allowedValues);

		logger.debug("Created classifier configuration (.arff):\n" + conf);

		return conf;
	}

	public int getClassForInstance(DynamicsInstance inst) throws Exception {
		int n = inst.getValues().size() + 1;
		List<Double> values = inst.getValues();
	    Instance instance = new Instance(n);
		
	    for (int i = 0; i < n - 1; i++) {
	        instance.setValue(i, values.get(i));    
	    }
	    instance.setDataset(instances);
	    
	    double clsLabel = classifier.classifyInstance(instance);
		
		return Integer.parseInt(instance.classAttribute().value((int)clsLabel));
	}

}
