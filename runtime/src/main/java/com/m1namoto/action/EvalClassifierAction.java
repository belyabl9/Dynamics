package com.m1namoto.action;

import com.google.common.base.Optional;
import com.m1namoto.identification.classifier.ClassifierType;
import com.m1namoto.identification.classifier.weka.WekaClassifier;
import com.m1namoto.page.EvalClassifierPageData;
import com.m1namoto.service.weka.configuration.ConfigurationService;

/**
 * It works only for Weka classifier
 */
public class EvalClassifierAction extends Action {

    private static final String PASSWORD_MUST_BE_SPECIFIED = "Password for classifier evaluation must be specified.";

    @Override
    protected ActionResult execute() throws Exception {
        Optional<String> passwordOpt = getRequestParamValue("password");
        if (!passwordOpt.isPresent()) {
            throw new IllegalArgumentException(PASSWORD_MUST_BE_SPECIFIED);
        }

        // classifier type is hardcoded for now. dropdown with all types may be added to WI in future
        WekaClassifier classifier = new WekaClassifier(ClassifierType.RANDOM_FOREST, ConfigurationService.getInstance().forEvaluation(passwordOpt.get()));
        String evalResults = classifier.evaluateClassifier();
        String configuration = classifier.getConfiguration().toString();
        EvalClassifierPageData data = new EvalClassifierPageData(evalResults, configuration);

        return createAjaxResult(data);
    }

}
