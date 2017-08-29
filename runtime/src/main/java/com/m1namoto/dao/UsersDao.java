package com.m1namoto.dao;

import com.google.common.base.Optional;
import com.m1namoto.domain.User;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UsersDao extends GenericDAO<User> {

    private static final String USER_LIST_BY_TYPE_QUERY = "FROM User WHERE userType = :userType";
    private static final String FIND_BY_LOGIN_QUERY = "FROM User WHERE login = :login";
    private static final String DELETE_USERS_QUERY = "DELETE FROM User";
    private static final String DELETE_USER_QUERY = "DELETE FROM User WHERE id = :id";

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
        query.setInteger("userType",userType.getValue());
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

    public void remove(long id) {
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(DELETE_USER_QUERY);
        query.setLong("id", (int) id);
        query.executeUpdate();
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
