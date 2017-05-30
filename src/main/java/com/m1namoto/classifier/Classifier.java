package com.m1namoto.classifier;

import com.m1namoto.domain.FeaturesSample;
import com.m1namoto.domain.User;
import com.m1namoto.service.UserService;
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
	private final static Logger logger = Logger.getLogger(Classifier.class);

	private static final String NOT_ENOUGH_USERS = "Can not create configuration if there are less than two users.";
	private static final String NOT_ENOUGH_COLLECTED_DYNAMICS = "Not enough keystroke dynamics has been collected to classify.";

	private static String CONFIGURATION_NAME = "Dynamics";

	private static String ATTR_KEY_PRESS = "keypress";
	private static String ATTR_BETWEEN_KEYS = "betweenKeys";
	private static String ATTR_MEAN_KEYPRESS_TIME = "meanKeypressTime";
	private static String ATTR_CLASS = "classVal";

	public enum Type {
	    RANDOM_FOREST,
		MLP,
		J48,
		;
	}

	private final User user;
	private weka.classifiers.Classifier classifier = new RandomForest();
	private final Instances instances;
	private final Configuration configuration;

	public Classifier(@NotNull Type type, @NotNull User user) throws Exception {
	    this.user = user;
	    this.classifier = createClassifierInstance(type);
	    configuration = createConfiguration(user.getPassword());
	    instances = createInstances(configuration.toString());
	    classifier.buildClassifier(instances);
	}

	public Classifier(@NotNull User user) throws Exception {
		this(Type.J48, user);
	}

    public static weka.classifiers.Classifier createClassifierInstance(@NotNull Type classifierType) {
        switch (classifierType) {
            case J48:
                return new J48();
            case MLP:
                return new MultilayerPerceptron();
            case RANDOM_FOREST:
                return new RandomForest();
			default:
				throw new UnsupportedOperationException("Specified classifier is not supported.");
        }
    }

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
	 * @param input configuration in .arff format
	 * @throws IOException
	 */
	private Instances createInstances(@NotNull String input) throws IOException {
		Instances instances = new Instances(new StringReader(input));
		instances.setClassIndex(instances.numAttributes() - 1);

		return instances;
	}

	/**
	 * Creates a configuration in .arff format for passed string (password).
	 * Only features for symbols which exist in passed string are considered
	 * @param password
	 * @return Configuration in .arff format
	 * @throws Exception
	 */
	public Configuration createConfiguration(@NotNull String password) throws Exception {
	    logger.debug("Create configuration " + CONFIGURATION_NAME);
        Configuration.Builder confBuilder = new Configuration.Builder().name(CONFIGURATION_NAME);

		for (int i = 0; i < password.length(); i++) {
            confBuilder.attribute(ATTR_KEY_PRESS + (i + 1));
		}
        for (int i = 1; i < password.length(); i++) {
            confBuilder.attribute(ATTR_BETWEEN_KEYS + i);
        }
        confBuilder.attribute(ATTR_MEAN_KEYPRESS_TIME);

		List<User> users = UserService.getList(User.Type.REGULAR);
		if (users.size() < 2) {
			throw new Exception(NOT_ENOUGH_USERS);
		}
		List<Integer> allowedValues = new ArrayList<>();
		for (User user : users) {
		    boolean isUserToCheck = this.user.getId() == user.getId();
			List<FeaturesSample> featuresSamples = user.getSamples(password, isUserToCheck);

            for (FeaturesSample sample : featuresSamples) {
                confBuilder.instance(sample.getFeatures(), user.getId());
            }
            if (!featuresSamples.isEmpty()) {
				allowedValues.add((int) user.getId());
			}
		}
		if (allowedValues.isEmpty()) {
			throw new Exception(NOT_ENOUGH_COLLECTED_DYNAMICS);
		}
        confBuilder.classAttribute(ATTR_CLASS, allowedValues);

        Configuration conf = confBuilder.build();
		logger.debug("Created classifier configuration (.arff):" + System.lineSeparator() + conf);

		return conf;
	}

	/**
	 * Returns a classification result for passed instance.
	 * Classification result contains information about similarity percentage to the expected class
	 * @param inst
	 * @return Classification result for the passed instance
	 * @throws Exception
	 */
	public ClassificationResult getClassForInstance(@NotNull DynamicsInstance inst) throws Exception {
		int instanceElementsNum = inst.getValues().size() + 1;
		List<Double> values = inst.getValues();
	    Instance instance = new Instance(instanceElementsNum);

	    for (int i = 0; i < instanceElementsNum - 1; i++) {
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

	    logger.debug("Predicted probability=" + probability);

		return new ClassificationResult(probability);
	}

}
