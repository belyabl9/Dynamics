package com.m1namoto.service;

import java.util.List;

import com.m1namoto.dao.DaoFactory;
import com.m1namoto.domain.User;

public class Users {

    public static User findByLogin(String login) {
        return DaoFactory.getUsersDAO().findByLogin(login);
    }

    public static User findById(long userId) {
        return DaoFactory.getUsersDAO().findById(userId);
    }

    public static List<User> getList() {
    	return (List<User>) DaoFactory.getUsersDAO().findAll();
    }
    
    public static List<User> getList(int type) {
        return (List<User>) DaoFactory.getUsersDAO().getList(type);
    }

    public static User save(User user) {
    	return DaoFactory.getUsersDAO().save(user);
    }

    public static void del(User user) {
        DaoFactory.getEventsDAO().deleteUserEvents(user);
        DaoFactory.getFeaturesDAO().deleteFeatures(user);
    	DaoFactory.getUsersDAO().delete(user);
    }

}
