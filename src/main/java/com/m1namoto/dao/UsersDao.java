package com.m1namoto.dao;

import java.util.List;

import com.google.common.base.Optional;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.m1namoto.domain.User;
import org.jetbrains.annotations.NotNull;

public class UsersDao extends GenericDAO<User> {

    private static final String USER_LIST_BY_TYPE_QUERY = "FROM User WHERE userType = :userType";
    private static final String FIND_BY_LOGIN_QUERY = "FROM User WHERE login = :login";
    public static final String DELETE_USERS_QUERY = "DELETE FROM User";

    public UsersDao(@NotNull SessionFactory factory) {
        super(User.class, factory);
    }

    /**
     * Returns a list of users
     */
    public List<User> getList() {
        return (List<User>) findAll();
    }
    
    /**
     * Returns a list of users by type
     * @param userType - type of user [regular, admin]
     */
    public List<User> getList(@NotNull User.Type userType) {
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(USER_LIST_BY_TYPE_QUERY);
        query.setString("userType", String.valueOf(userType));
        List<User> users = query.list();
        
        return users;
    }

    /**
     * Returns a user with specified login
     */
    public Optional<User> findByLogin(@NotNull String login) {
    	if (login.isEmpty()) {
    		return Optional.absent();
    	}
    	Query query = getFactory().getCurrentSession().createQuery(FIND_BY_LOGIN_QUERY);
    	query.setString("login", login);
    	
    	List<User> results = query.list(); 
    	if (results.isEmpty()) {
    	    return Optional.absent();
        }

    	return Optional.of(results.get(0));
    }
    
    /**
     * Creates a user
     */
    public User createUser(@NotNull User user) {
        return save(user);
    }
    
    /**
     * Deletes all users
     */
    public void removeAll() {
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(DELETE_USERS_QUERY);
        query.executeUpdate();
    }
}
