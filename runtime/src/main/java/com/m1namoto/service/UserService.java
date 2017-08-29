package com.m1namoto.service;

import com.google.common.base.Optional;
import com.m1namoto.dao.DaoFactory;
import com.m1namoto.domain.User;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UserService {

    private final String LOGIN_CAN_NOT_BE_EMPTY = "Login can not be empty.";
    private final String INVALID_USER_ID = "User id must be >= 0.";

    private UserService() {}

    private static class LazyHolder {
        static final UserService INSTANCE = new UserService();
    }
    public static UserService getInstance() {
        return LazyHolder.INSTANCE;
    }
    
    public Optional<User> findByLogin(@NotNull String login) {
        if (login.isEmpty()) {
            throw new IllegalArgumentException(LOGIN_CAN_NOT_BE_EMPTY);
        }
        return DaoFactory.getUsersDAO().findByLogin(login);
    }

    /**
     * Finds a user by id
     */
    public Optional<User> findById(long userId) {
        if (userId < 0) {
            throw new IllegalArgumentException(INVALID_USER_ID);
        }
        return Optional.fromNullable(DaoFactory.getUsersDAO().findById(userId));
    }

    /**
     * Returns a list of users
     */
    public List<User> getList() {
    	return (List<User>) DaoFactory.getUsersDAO().findAll();
    }
    
    /**
     * Returns a list of users by type
     * @param type Type of user [regular, admin]
     */
    public List<User> getList(@NotNull User.Type type) {
        return DaoFactory.getUsersDAO().getList(type);
    }

    public User save(@NotNull User user) {
    	return DaoFactory.getUsersDAO().save(user);
    }

    public void incrementAuthCounter(@NotNull String login) {
        Optional<User> userOpt = findByLogin(login);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setAuthenticatedCnt(user.getAuthenticatedCnt() + 1);
            save(user);
        }
    }

    /**
     * Removes a user with all related data
     */
    public void remove(@NotNull User user) {
        DaoFactory.getFeaturesDAO().removeAll(user);
        DaoFactory.getSessionsDAO().removeAll(user);
    	DaoFactory.getUsersDAO().remove(user.getId());
    }

    /**
     * Removes all users
     */
    public void removeAll() {
        DaoFactory.getUsersDAO().removeAll();
    }

}
