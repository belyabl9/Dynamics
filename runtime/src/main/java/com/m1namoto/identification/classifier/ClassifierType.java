package com.m1namoto.identification.classifier;

public enum ClassifierType {
	RANDOM_FOREST,
	MLP,
	J48,
	BAYES_NET,
	SVM_ONE_CLASS,
	KNN_TWO_CLASS,
	MLP_TWO_CLASS,
	SMO,
	;

	private static final String UNSUPPORTED_TYPE = "Specified classifier type is not supported.";

	public boolean isOneClass() {
		switch (this) {
			case SVM_ONE_CLASS:
				return true;
			case RANDOM_FOREST:
			case MLP:
			case J48:
			case KNN_TWO_CLASS:
			case BAYES_NET:
			case MLP_TWO_CLASS:
			case SMO:
				return false;
		}
		throw new UnsupportedOperationException(UNSUPPORTED_TYPE);
	}

	public boolean isMultiClass() {
		switch (this) {
			case RANDOM_FOREST:
			case MLP:
			case J48:
				return true;
			case SVM_ONE_CLASS:
			case KNN_TWO_CLASS:
			case BAYES_NET:
			case MLP_TWO_CLASS:
			case SMO:
				return false;
		}
		throw new UnsupportedOperationException(UNSUPPORTED_TYPE);
	}

	public boolean isTwoClass() {
		switch (this) {
			case RANDOM_FOREST:
			case MLP:
			case J48:
			case SVM_ONE_CLASS:
				return false;
			case KNN_TWO_CLASS:
			case BAYES_NET:
			case MLP_TWO_CLASS:
			case SMO:
				return true;
		}
		throw new UnsupportedOperationException(UNSUPPORTED_TYPE);
	}

	public static void assertSize(int expectedItems) {
		assert values().length == expectedItems : "Update the code calling ClassifierType with " + expectedItems + "!";
	}
}