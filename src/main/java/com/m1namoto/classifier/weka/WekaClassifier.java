package com.m1namoto.classifier.weka;

import com.m1namoto.api.ClassificationResult;
import com.m1namoto.api.IClassifier;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.User;
import com.m1namoto.service.FeatureExtractorService;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class WekaClassifier implements IClassifier {
	private final static Logger logger = Logger.getLogger(WekaClassifier.class);

	private static final FeatureExtractorService FEATURE_EXTRACTOR = FeatureExtractorService.getInstance();

	public enum Type {
	    RANDOM_FOREST,
		MLP,
		J48,
		;
	}

	private Classifier classifier = new RandomForest();
	private final Instances instances;
	private final Configuration configuration;

	public WekaClassifier(@NotNull Type type, @NotNull Configuration configuration) throws Exception {
	    this.classifier = createClassifierInstance(type);
	    this.configuration = configuration;
	    this.instances = createInstances(configuration.toString());
	    classifier.buildClassifier(instances);
	}

	@Override
	public ClassificationResult classify(@NotNull List<Event> events, @NotNull User authUser) {
		if (events.isEmpty()) {
			throw new IllegalArgumentException("Can not classify based on empty list of specified events.");
		}

		DynamicsInstance instance = new DynamicsInstance(FEATURE_EXTRACTOR.getFeatureValues(events, authUser));
		try {
			return getClassForInstance(instance, authUser.getId());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public WekaClassifier(@NotNull Configuration configuration) throws Exception {
		this(Type.J48, configuration);
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
	 * Returns a classification result for passed instance.
	 * Classification result contains information about similarity percentage to the expected class
	 * @return Classification result for the passed instance
	 * @throws Exception
	 */
	public ClassificationResult getClassForInstance(@NotNull DynamicsInstance inst, long authUserId) throws Exception {
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
		double probability = distr[classValues.indexOf((int) authUserId)];

		logger.debug("Predicted probability=" + probability);

		return new ClassificationResult(probability);
	}

	private static Classifier createClassifierInstance(@NotNull Type classifierType) {
		switch (classifierType) {
			case RANDOM_FOREST:
				return new RandomForest();
			case J48:
				return new J48();
			case MLP:
				return new MultilayerPerceptron();
			default:
				throw new UnsupportedOperationException("Specified classifier is not supported.");
		}
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

}
