package com.m1namoto.action;

import java.util.List;

import com.m1namoto.domain.User;
import com.m1namoto.page.UsersListPageData;
import com.m1namoto.service.UserService;

public class GetUsersList extends Action {

    @Override
    protected ActionResult execute() throws Exception {
        List<User> users = UserService.getList();
        UsersListPageData data = new UsersListPageData(users);
        
        return createAjaxResult(data);
    }

}
