package com.m1namoto.action;

import com.m1namoto.service.SessionService;
import com.m1namoto.utils.Const;

public class DeleteSessionAction extends Action {

    @Override
    protected ActionResult execute() throws Exception {
        String userId = getRequestParamValue("userId").get();
        String sessionId = getRequestParamValue("sessionId").get();
        SessionService.remove(Long.parseLong(sessionId));

        return createRedirectResult(Const.ActionURIs.USER_SESSIONS_PAGE + "?userId=" + userId);
    }

}
