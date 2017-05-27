package com.m1namoto.action;

import com.m1namoto.domain.User;
import com.m1namoto.page.PageData;
import com.m1namoto.service.UserService;

public class DelUserAction extends Action {

    @Override
    protected ActionResult execute() throws Exception {
        String userId = getRequestParamValue("id");
        User user = new User();
        user.setId(Long.valueOf(userId));
        UserService.remove(user);

        return createAjaxResult(new PageData());
    }

}
