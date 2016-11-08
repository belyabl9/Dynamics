package com.m1namoto.action;

import java.util.List;

import com.m1namoto.domain.Session;
import com.m1namoto.domain.User;
import com.m1namoto.page.UserSessionsPageData;
import com.m1namoto.service.SessionsService;
import com.m1namoto.service.UsersService;
import com.m1namoto.utils.Const;

public class UserSessionsAction extends Action {
    @Override
    protected ActionResult execute() {
        String userId = getRequestParamValue("userId");
        User user = UsersService.findById(Long.parseLong(userId));
        List<Session> sessions = SessionsService.getUserSessions(user);
        
        System.out.println("Sessions Count: " + sessions.size());
        UserSessionsPageData data = new UserSessionsPageData(sessions, user);
        
        return createShowPageResult(Const.ViewURIs.USER_SESSIONS, data);
    }
}
