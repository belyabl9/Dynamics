package com.m1namoto.action;

import java.util.List;

import com.m1namoto.domain.User;
import com.m1namoto.page.UsersPageData;
import com.m1namoto.service.UserService;
import com.m1namoto.utils.Const;

public class StatisticsPageAction extends Action {
    
    @Override
    protected ActionResult execute() throws Exception {
        UsersPageData data = new UsersPageData();
        List<User> users = UserService.getList(User.Type.REGULAR);
        data.setUsers(users);

        return createShowPageResult(Const.ViewURIs.STATISTICS, data);
    }

}
