package com.m1namoto.classifier;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;

import com.m1namoto.domain.User;
import com.m1namoto.service.FeaturesService;
import com.m1namoto.service.UsersService;
import com.m1namoto.utils.Utils;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;

public class Classifier {
    final static Logger logger = Logger.getLogger(Classifier.class);

	private static String CONFIGURATION_NAME = "Dynamics";
	private static String ATTR_KEY_PRESS = "keypress";
	private static String ATTR_BETWEEN_KEYS = "betweenKeys";
	private static String ATTR_MEAN_KEYPRESS_TIME = "meanKeypressTime";
	private static String ATTR_X = "x";
	private static String ATTR_Y = "y";
	private static String ATTR_CLASS = "classVal";

	public static enum Classifiers {
	    RANDOM_FOREST, MLP, J48
	};
	
	private User userToCheck;
	//private weka.classifiers.Classifier classifier = new SMO();
	private static weka.classifiers.Classifier classifier = new RandomForest();
	//private weka.classifiers.Classifier classifier = new MultilayerPerceptron();
	//private static weka.classifiers.Classifier classifier = new J48();
	private Instances instances;
	private Configuration configuration;

	public Classifier(User userToCheck) throws Exception {
	    logger.debug("Create classifier");
	    this.userToCheck = userToCheck;
	    configuration = createConfiguration();
	    instances = createInstances(configuration.toString());
	    classifier.buildClassifier(instances);
	    evaluateClassifier();
	}
	
    public Classifier(String password) throws Exception {
        logger.debug("Create classifier");
        configuration = createConfiguration(password);
        instances = createInstances(configuration.toString());
        classifier.buildClassifier(instances);
    }
    
    public static void setClassifier(Classifiers classifierType) {
        switch (classifierType) {
            case J48:
                classifier = new J48();
                logger.info("Classifier was changed to " + classifierType);
                break;
            case MLP:
                classifier = new MultilayerPerceptron();
                logger.info("Classifier was changed to " + classifierType);
                break;
            case RANDOM_FOREST:
                classifier = new RandomForest();
                logger.info("Classifier was changed to " + classifierType);
                break;
        }
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }

	public String evaluateClassifier() throws Exception {
        Evaluation eval = new Evaluation(instances);
        eval.evaluateModel(classifier, instances);
        logger.debug(eval.errorRate());
        logger.debug(eval.toSummaryString());
        return eval.toSummaryString();
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
	    String password = userToCheck.getPassword();
	    return createConfiguration(password);
	}
	
	public Configuration createConfiguration(String password) throws Exception {
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
        conf.addAttribute(ATTR_MEAN_KEYPRESS_TIME);
        
        if (FeaturesService.includeMobileFeatures()) {
            for (int i = 0; i < password.length(); i++) {
                logger.debug("Add attribute definition: " + ATTR_X + Character.toUpperCase(password.charAt(i)) + i);
                conf.addAttribute(ATTR_X + Character.toUpperCase(password.charAt(i)) + i);
            }
            for (int i = 0; i < password.length(); i++) {
                logger.debug("Add attribute definition: " + ATTR_Y + Character.toUpperCase(password.charAt(i)) + i);
                conf.addAttribute(ATTR_Y + Character.toUpperCase(password.charAt(i)) + i);
            }
        }

		List<User> users = UsersService.getList(User.USER_TYPE_REGULAR);
		List<Integer> allowedValues = new ArrayList<Integer>();

        logger.debug("Prepare user features");
		for (User user : users) {
		    logger.debug("User id: " + user.getId());
		    List<List<Double>> featuresSamples = null;
		    boolean isUserToCheck = userToCheck != null ? userToCheck.getId() == user.getId() : false; 
		    featuresSamples = user.getSamples(password, isUserToCheck);    

            for (List<Double> sample : featuresSamples) {
                logger.debug("Add sample: " + sample);
                conf.addInstance(sample, user.getId());
            }

            logger.debug("Add user to the list of allowed users");
		    allowedValues.add((int) user.getId());
		}
		conf.setClassAttribute(ATTR_CLASS, allowedValues);

		logger.info("Created classifier configuration (.arff):\n" + conf);

		return conf;
	}

	public ClassificationResult getClassForInstance(DynamicsInstance inst) throws Exception {
		int n = inst.getValues().size() + 1;
		List<Double> values = inst.getValues();
	    Instance instance = new Instance(n);
		
	    for (int i = 0; i < n - 1; i++) {
	        instance.setValue(i, values.get(i));    
	    }
	    instance.setDataset(instances);

	    double[] distr = classifier.distributionForInstance(instance);

	    List<Integer> classValues = configuration.getAllowedClassValues();
	    double probability = distr[classValues.indexOf((int)userToCheck.getId())];
	    
	    logger.info("Predicted probability=" + probability);
	    ClassificationResult result = new ClassificationResult(probability);
		
		return result;
	}
	
    public static Instances readInstancesFromFile(String filename) throws IOException {
        String content = Utils.readFile(filename, Charset.defaultCharset());

        return new Instances(new StringReader(content));
    }

}
