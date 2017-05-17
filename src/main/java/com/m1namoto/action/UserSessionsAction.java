package com.m1namoto.action;

import java.util.List;

import com.google.common.base.Optional;
import com.m1namoto.domain.Session;
import com.m1namoto.domain.User;
import com.m1namoto.page.UserSessionsPageData;
import com.m1namoto.service.SessionService;
import com.m1namoto.service.UsersService;
import com.m1namoto.utils.Const;

public class UserSessionsAction extends Action {
    @Override
    protected ActionResult execute() {
        String userId = getRequestParamValue("userId");
        Optional<User> userOpt = UsersService.findById(Long.parseLong(userId));
        if (!userOpt.isPresent()) {
            return null;
        }
        List<Session> sessions = SessionService.getUserSessions(userOpt.get());
        UserSessionsPageData data = new UserSessionsPageData(sessions, userOpt.get());

        return createShowPageResult(Const.ViewURIs.USER_SESSIONS, data);
    }
}
