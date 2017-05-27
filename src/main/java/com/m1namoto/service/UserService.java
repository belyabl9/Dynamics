package com.m1namoto.service;

import java.util.List;

import com.google.common.base.Optional;
import com.m1namoto.dao.DaoFactory;
import com.m1namoto.domain.User;
import org.jetbrains.annotations.NotNull;

public class UserService {

    private static final String LOGIN_CAN_NOT_BE_EMPTY = "Login can not be empty.";
    private static final String INVALID_USER_ID = "User id must be >= 0.";

    public static Optional<User> findByLogin(@NotNull String login) {
        if (login.isEmpty()) {
            throw new IllegalArgumentException(LOGIN_CAN_NOT_BE_EMPTY);
        }
        return DaoFactory.getUsersDAO().findByLogin(login);
    }

    /**
     * Finds a user by id
     */
    public static Optional<User> findById(long userId) {
        if (userId < 0) {
            throw new IllegalArgumentException(INVALID_USER_ID);
        }
        return Optional.fromNullable(DaoFactory.getUsersDAO().findById(userId));
    }

    /**
     * Returns a list of users
     */
    public static List<User> getList() {
    	return (List<User>) DaoFactory.getUsersDAO().findAll();
    }
    
    /**
     * Returns a list of users by type
     * @param type Type of user [regular, admin]
     */
    public static List<User> getList(@NotNull User.Type type) {
        return DaoFactory.getUsersDAO().getList(type);
    }

    public static User save(@NotNull User user) {
    	return DaoFactory.getUsersDAO().save(user);
    }

    /**
     * Removes a user with all related data
     */
    public static void remove(@NotNull User user) {
        DaoFactory.getFeaturesDAO().removeAll(user);
        DaoFactory.getSessionsDAO().removeAll(user);
        DaoFactory.getEventsDAO().removeAll(user);
    	DaoFactory.getUsersDAO().delete(user);
    }

    /**
     * Removes all users
     */
    public static void removeAll() {
        DaoFactory.getUsersDAO().removeAll();
    }

}
