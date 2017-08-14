package com.m1namoto.action;

import com.google.common.base.Optional;
import com.m1namoto.classifier.weka.WekaClassifier;
import com.m1namoto.page.EvalClassifierPageData;
import com.m1namoto.service.weka.ConfigurationService;

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

        WekaClassifier classifier = new WekaClassifier(ConfigurationService.getInstance().create(passwordOpt.get(), Optional.<Long>absent()));
        String evalResults = classifier.evaluateClassifier();
        String configuration = classifier.getConfiguration().toString();
        EvalClassifierPageData data = new EvalClassifierPageData(evalResults, configuration);

        return createAjaxResult(data);
    }

}
