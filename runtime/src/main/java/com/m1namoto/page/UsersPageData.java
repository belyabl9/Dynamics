package com.m1namoto.page;

import com.m1namoto.domain.User;

import java.util.List;

public class UsersPageData extends PageData {
    private List<User> users;
    public UsersPageData() {
        super();
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
    
}