package com.m1namoto.page;

import java.util.List;

import com.m1namoto.domain.User;

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