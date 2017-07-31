package com.m1namoto.action;

import java.util.List;

import com.google.common.base.Optional;
import com.m1namoto.domain.Session;
import com.m1namoto.domain.User;
import com.m1namoto.page.UserSessionsPageData;
import com.m1namoto.service.SessionService;
import com.m1namoto.service.UserService;
import com.m1namoto.utils.Const;

public class UserSessionsAction extends Action {

    private static final String USER_ID_MUST_BE_SPECIFIED = "User ID must be specified.";
    private static final String INVALID_USER_ID = "Can not parse user ID.";
    private static final String USER_NOT_FOUND = "Can not find a user with specified id.";

    @Override
    protected ActionResult execute() {
        Optional<String> userIdOpt = getRequestParamValue("userId");
        if (!userIdOpt.isPresent()) {
            throw new RuntimeException(USER_ID_MUST_BE_SPECIFIED);
        }
        Optional<User> userOpt;
        try {
            userOpt = UserService.getInstance().findById(Long.valueOf(userIdOpt.get()));
        } catch (Exception e) {
            throw new RuntimeException(INVALID_USER_ID);
        }
        if (!userOpt.isPresent()) {
            throw new RuntimeException(USER_NOT_FOUND);
        }
        List<Session> sessions = SessionService.getList(userOpt.get());
        UserSessionsPageData data = new UserSessionsPageData(sessions, userOpt.get());

        return createShowPageResult(Const.ViewURIs.USER_SESSIONS, data);
    }
}
