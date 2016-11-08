package com.m1namoto.action;

import com.m1namoto.classifier.Classifier;
import com.m1namoto.page.EvalClassifierPageData;

public class EvalClassifierAction extends Action {

    @Override
    protected ActionResult execute() throws Exception {
        String password = getRequestParamValue("password");
        Classifier classifier = new Classifier(password);
        String evalResults = classifier.evaluateClassifier();
        String configuration = classifier.getConfiguration().toString();
        EvalClassifierPageData data = new EvalClassifierPageData(evalResults, configuration);
        return createAjaxResult(data);
    }

}
