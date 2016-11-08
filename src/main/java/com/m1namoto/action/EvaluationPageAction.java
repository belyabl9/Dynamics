package com.m1namoto.action;

import com.m1namoto.utils.Const;

public class EvaluationPageAction extends Action {

    @Override
    protected ActionResult execute() throws Exception {
        return createShowPageResult(Const.ViewURIs.EVAL_CLASSIFIER, null);
    }

}
