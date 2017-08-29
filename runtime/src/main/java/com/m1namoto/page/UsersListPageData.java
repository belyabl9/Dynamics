package com.m1namoto.page;

import com.m1namoto.domain.User;

import java.util.List;

public class UsersListPageData extends PageData {
    List<User> users;
    
    public UsersListPageData(List<User> users) {
        this.users = users;
    }
}
