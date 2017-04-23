package com.m1namoto.classifier;

import com.m1namoto.domain.User;
import com.m1namoto.service.UsersService;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class Classifier {
    final static Logger logger = Logger.getLogger(Classifier.class);

	private static String CONFIGURATION_NAME = "Dynamics";
	private static String ATTR_KEY_PRESS = "keypress";
	private static String ATTR_BETWEEN_KEYS = "betweenKeys";
	private static String ATTR_MEAN_KEYPRESS_TIME = "meanKeypressTime";
	private static String ATTR_X = "x";
	private static String ATTR_Y = "y";
	private static String ATTR_CLASS = "classVal";

	public enum Type {
	    RANDOM_FOREST, MLP, J48
	}

	private User user;
	private static weka.classifiers.Classifier classifier = new RandomForest();
	private Instances instances;
	private Configuration configuration;

	public Classifier(@NotNull User user) throws Exception {
	    this.user = user;
	    configuration = createConfiguration();
	    instances = createInstances(configuration.toString());
	    classifier.buildClassifier(instances);
	    evaluateClassifier();
	}
	
    public Classifier(String password) throws Exception {
        configuration = createConfiguration(password);
        instances = createInstances(configuration.toString());
        classifier.buildClassifier(instances);
    }
    
    public static void setClassifier(Type classifierType) {
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
    
    /**
     * Returns current configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Evaluates classifier
     * @return String with classifier evaluation information
     * @throws Exception
     */
	public String evaluateClassifier() throws Exception {
        Evaluation eval = new Evaluation(instances);
        eval.evaluateModel(classifier, instances);
        logger.debug(eval.errorRate());
        logger.debug(eval.toSummaryString());
        return eval.toSummaryString();
	}
	
	/**
	 * Creates Weka instances from the passed configuration
	 * @param input - passed configuration in .arff format
	 * @throws IOException
	 */
	private Instances createInstances(String input) throws IOException {
		Instances instances = new Instances(new StringReader(input));
		instances.setClassIndex(instances.numAttributes() - 1);

		return instances;
	}

	private Configuration createConfiguration() throws Exception {
		String password = user.getPassword();
		return createConfiguration(password);
	}

	/**
	 * Creates a configuration in .arff format for passed string (password).
	 * Only features for symbols which exist in passed string are considered
	 * @param password
	 * @return Configuration in .arff format
	 * @throws Exception
	 */
	public Configuration createConfiguration(String password) throws Exception {
	    logger.debug("Create configuration " + CONFIGURATION_NAME);
        Configuration.Builder confBuilder = new Configuration.Builder().name(CONFIGURATION_NAME);

		for (int i = 0; i < password.length(); i++) {
            confBuilder.attribute(ATTR_KEY_PRESS + (i + 1));
		}

        for (int i = 1; i < password.length(); i++) {
            confBuilder.attribute(ATTR_BETWEEN_KEYS + i);
        }
        confBuilder.attribute(ATTR_MEAN_KEYPRESS_TIME);

		List<User> users = UsersService.getList(User.Type.REGULAR);
		List<Integer> allowedValues = new ArrayList<Integer>();

		for (User user : users) {
		    List<List<Double>> featuresSamples = null;
		    boolean isUserToCheck = this.user != null ? this.user.getId() == user.getId() : false;
		    featuresSamples = user.getSamples(user.getPassword(), isUserToCheck);

            for (List<Double> sample : featuresSamples) {
                confBuilder.instance(sample, user.getId());
            }
		    allowedValues.add((int) user.getId());
		}
        confBuilder.classAttribute(ATTR_CLASS, allowedValues);

        Configuration conf = confBuilder.build();

		logger.info("Created classifier configuration (.arff):\n" + conf);

		return conf;
	}

	/**
	 * Returns a classification result for passed instance.
	 * Classification result contains information
	 * about similarity percentage to the expected class
	 * @param inst
	 * @return Classification result for the passed instance
	 * @throws Exception
	 */
	public ClassificationResult getClassForInstance(DynamicsInstance inst) throws Exception {
		int n = inst.getValues().size() + 1;
		List<Double> values = inst.getValues();
	    Instance instance = new Instance(n);

	    for (int i = 0; i < n - 1; i++) {
	    	if (values.get(i) == null) {
	    		instance.setValue(i, Instance.missingValue());
	    	} else {
	    		instance.setValue(i, values.get(i));
	    	}
	    }
	    instance.setDataset(instances);

	    double[] distr = classifier.distributionForInstance(instance);

	    List<Integer> classValues = configuration.getAllowedClassValues();
	    double probability = distr[classValues.indexOf((int) user.getId())];

	    logger.info("Predicted probability=" + probability);
	    ClassificationResult result = new ClassificationResult(probability);

		return result;
	}

}
