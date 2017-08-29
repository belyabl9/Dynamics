package com.m1namoto.action;

import com.m1namoto.domain.User;
import com.m1namoto.page.UsersPageData;
import com.m1namoto.service.UserService;
import com.m1namoto.utils.Const;

import java.util.List;

public class UsersPageAction extends Action {

    @Override
    protected ActionResult execute() {
        UsersPageData usersData = new UsersPageData();
        List<User> users = UserService.getInstance().getList();
        usersData.setUsers(users);
        
        return createShowPageResult(Const.ViewURIs.USERS, usersData);
    }

}
