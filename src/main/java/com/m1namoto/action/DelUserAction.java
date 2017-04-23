package com.m1namoto.action;

import com.m1namoto.domain.User;
import com.m1namoto.page.PageData;
import com.m1namoto.service.UsersService;

public class DelUserAction extends Action {

    @Override
    protected ActionResult execute() throws Exception {
        String userId = getRequestParamValue("id");
        User user = new User();
        user.setId(Long.valueOf(userId));
        UsersService.remove(user);

        return createAjaxResult(new PageData());
    }

}
