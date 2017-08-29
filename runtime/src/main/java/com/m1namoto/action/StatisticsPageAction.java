package com.m1namoto.action;

import com.m1namoto.domain.User;
import com.m1namoto.page.UsersPageData;
import com.m1namoto.service.UserService;
import com.m1namoto.utils.Const;

import java.util.List;

public class StatisticsPageAction extends Action {
    
    @Override
    protected ActionResult execute() throws Exception {
        UsersPageData data = new UsersPageData();
        List<User> users = UserService.getInstance().getList(User.Type.REGULAR);
        data.setUsers(users);

        return createShowPageResult(Const.ViewURIs.STATISTICS, data);
    }

}
