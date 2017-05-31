package com.m1namoto.action;

import com.google.common.base.Optional;
import com.m1namoto.classifier.Classifier;
import com.m1namoto.domain.User;
import com.m1namoto.page.EvalClassifierPageData;
import com.m1namoto.service.UserService;

public class EvalClassifierAction extends Action {

    private static final String USER_ID_WAS_NOT_PASSED = "User ID was not passed";
    private static final String INVALID_USER_ID = "Can not parse user id";

    @Override
    protected ActionResult execute() throws Exception {
        Optional<String> userIdOpt = getRequestParamValue("userId");
        if (!userIdOpt.isPresent()) {
            throw new RuntimeException(USER_ID_WAS_NOT_PASSED);
        }

        int userId;
        try {
            userId = Integer.parseInt(userIdOpt.get());
        } catch (Exception e) {
            throw new RuntimeException(INVALID_USER_ID + ": " + userIdOpt.get());
        }

        Optional<User> userOpt = UserService.findById(userId);
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
