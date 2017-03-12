package com.m1namoto.service;

import java.util.List;

import com.m1namoto.dao.DaoFactory;
import com.m1namoto.domain.User;

public class UsersService {

    /**
     * Finds a user by login
     * @param login
     * @return User or null
     */
    public static User findByLogin(String login) {
        return DaoFactory.getUsersDAO().findByLogin(login);
    }

    /**
     * Finds a user by id
     * @param userId
     * @return User or null
     */
    public static User findById(long userId) {
        return DaoFactory.getUsersDAO().findById(userId);
    }

    /**
     * Returns a list of users
     * @return List of users
     */
    public static List<User> getList() {
    	return (List<User>) DaoFactory.getUsersDAO().findAll();
    }
    
    /**
     * Returns a list of users by type
     * @param type Type of user [regular, admin]
     * @return List of users
     */
    public static List<User> getList(int type) {
        return (List<User>) DaoFactory.getUsersDAO().getList(type);
    }

    /**
     * Saves a user
     * @param user
     * @return Saved user
     */
    public static User save(User user) {
    	return DaoFactory.getUsersDAO().save(user);
    }

    /**
     * Deletes a user with all related data
     * @param user
     */
    public static void del(User user) {
        DaoFactory.getFeaturesDAO().deleteFeatures(user);
        DaoFactory.getSessionsDAO().deleteUserSessions(user);
        DaoFactory.getEventsDAO().deleteUserEvents(user);
    	DaoFactory.getUsersDAO().delete(user);
    }

    /**
     * Deletes all users
     */
    public static void deleteAll() {
        DaoFactory.getUsersDAO().deleteAll();
    }

}
