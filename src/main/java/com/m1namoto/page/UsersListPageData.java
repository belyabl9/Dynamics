package com.m1namoto.page;

import java.util.List;

import com.m1namoto.domain.User;

public class UsersListPageData extends PageData {
    List<User> users;
    
    public UsersListPageData(List<User> users) {
        this.users = users;
    }
}
