package com.m1namoto.identification.classifier.weka;

import com.m1namoto.api.ClassificationResult;
import com.m1namoto.api.IClassifier;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.User;
import com.m1namoto.entity.DynamicsInstance;
import com.m1namoto.identification.classifier.ClassifierType;
import com.m1namoto.service.FeatureExtractorService;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class WekaClassifier implements IClassifier {
	private final static Logger logger = Logger.getLogger(WekaClassifier.class);

	static {
		libsvm.svm.svm_set_print_string_function(new libsvm.svm_print_interface() {
			@Override
			public void print(String s) {
			} // Disables svm output
		});
	}

	private static final FeatureExtractorService FEATURE_EXTRACTOR = FeatureExtractorService.getInstance();
	private static final String UNSUPPORTED_CLASSIFIER = "Specified classifier is not supported.";

	private Classifier classifier;
	private final Instances instances;
	private final Configuration configuration;

    @NotNull
    private ClassifierType classifierType;

	public WekaClassifier(@NotNull ClassifierType classifierType, @NotNull Configuration configuration) throws Exception {
	    this.classifierType = classifierType;
	    this.classifier = createClassifierInstance(classifierType);
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
		double probability;
		if (classifierType.isMultiClass()) {
		    probability = distr[classValues.indexOf((int)authUserId)];
        } else {
		    probability = distr[0];
        }

		logger.debug("Predicted probability=" + probability);

		return new ClassificationResult(probability);
	}

	private static Classifier createClassifierInstance(@NotNull ClassifierType classifierType) {
		ClassifierType.assertSize(8);
		switch (classifierType) {
			case RANDOM_FOREST:
				RandomForest randomForest = new RandomForest();
				randomForest.setNumTrees(100);
				return randomForest;
			case J48:
				return new J48();
			case MLP:
			case MLP_TWO_CLASS:
				MultilayerPerceptron multilayerPerceptron = new MultilayerPerceptron();
				multilayerPerceptron.setHiddenLayers("50");
				multilayerPerceptron.setTrainingTime(3000);
                multilayerPerceptron.setLearningRate(0.1d);
                multilayerPerceptron.setMomentum(0.1);

				return multilayerPerceptron;
			case SMO:
				SMO smo = new SMO();
				smo.setKernel(new RBFKernel());

				return smo;
			case SVM_ONE_CLASS:
				LibSVM libSVM = new LibSVM();
				libSVM.setSVMType(new SelectedTag(LibSVM.SVMTYPE_ONE_CLASS_SVM, LibSVM.TAGS_SVMTYPE));
//				libSVM.setNu(0.15);
//				libSVM.setGamma(Math.pow(2, -6));
//				libSVM.setCost(Math.pow(2, -5));

				return libSVM;
			case KNN_TWO_CLASS:
				IBk iBk = new IBk();
				iBk.setKNN(4);
				return iBk;
			case BAYES_NET:
				return new BayesNet();
			default:
				throw new UnsupportedOperationException(UNSUPPORTED_CLASSIFIER);
		}
	}

	/**
	 * Creates Weka instances from the passed configuration
	 * @param input configuration in .arff format
	 * @throws IOException
	 */
	private Instances createInstances(@NotNull String input) throws Exception {
		Instances instances = new Instances(new StringReader(input));
		instances.setClassIndex(instances.numAttributes() - 1);

//        int maxCorrect = 0;
//		for (int time = 500; time < 5000; time *= 2) {
//            ((MultilayerPerceptron) classifier).setTrainingTime(time);
//            ((MultilayerPerceptron) classifier).setLearningRate(0.1);
//            for (double layers = 30; layers < 50; layers++) {
//                ((MultilayerPerceptron) classifier).setHiddenLayers(String.valueOf(layers));
//                classifier.buildClassifier(instances);
//                Evaluation eval = new Evaluation(instances);
//                eval.evaluateModel(classifier, instances);
//                if (eval.correct() > maxCorrect) {
//                    System.out.println("Layers " + layers + " Time " + time + " ; correct " + eval.correct());
//                    maxCorrect = (int) eval.correct();
//                }
//            }
//        }

		// Attribute selection
//		AttributeSelection filter = new AttributeSelection();  // package weka.filters.supervised.attribute!
//		CfsSubsetEval eval = new CfsSubsetEval();
//		GreedyStepwise search = new GreedyStepwise();
//		search.setSearchBackwards(true);
//		filter.setEvaluator(eval);
//		filter.setSearch(search);
//		filter.setInputFormat(instances);
//		// generate new data
//
//		System.out.println("Before attr. selection: " + instances.numAttributes());
//		System.out.println("Before attr. selection: " + instances.numInstances());
//
//		Instances filteredInstances = Filter.useFilter(instances, filter);
//
//		System.out.println("After attr. selection: " + filteredInstances.numAttributes());
//		System.out.println("After attr. selection: " + filteredInstances.numInstances());
//
//		return filteredInstances;

		return instances;
	}

}
