package com.m1namoto.action;

import com.google.common.base.Optional;
import com.m1namoto.classifier.Classifier;
import com.m1namoto.domain.User;
import com.m1namoto.page.EvalClassifierPageData;
import com.m1namoto.service.UserService;

public class EvalClassifierAction extends Action {

    @Override
    protected ActionResult execute() throws Exception {
        String id = getRequestParamValue("id");
        Optional<User> userOpt = UserService.findById(Integer.parseInt(id));
        if (userOpt.isPresent()) {
            Classifier classifier = new Classifier(userOpt.get());
            String evalResults = classifier.evaluateClassifier();
            String configuration = classifier.getConfiguration().toString();
            EvalClassifierPageData data = new EvalClassifierPageData(evalResults, configuration);
            return createAjaxResult(data);
        }
        return createAjaxResult(null);
    }

}
