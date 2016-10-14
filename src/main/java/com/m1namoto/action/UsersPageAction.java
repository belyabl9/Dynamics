package com.m1namoto.action;

import java.util.List;

import com.m1namoto.domain.User;
import com.m1namoto.page.UsersPageData;
import com.m1namoto.utils.Const;

public class UsersPageAction extends Action {

    @Override
    protected ActionResult execute() {
        UsersPageData usersData = new UsersPageData();
        List<User> users = com.m1namoto.service.Users.getList();
        usersData.setUsers(users);
        
        return createShowPageResult(Const.ViewURIs.USERS, usersData);
    }

}
