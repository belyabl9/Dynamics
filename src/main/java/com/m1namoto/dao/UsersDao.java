package com.m1namoto.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.m1namoto.domain.User;

public class UsersDao extends GenericDAO<User> {

    public UsersDao(SessionFactory factory) {
        super(User.class, factory);
    }

    /**
     * Returns a list of users
     * @return List of users
     */
    public List<User> getList() {
        return (List<User>) findAll();
    }
    
    /**
     * Returns a list of users by type
     * @param userType - type of user [regular, admin]
     * @return List of users
     */
    @SuppressWarnings("unchecked")
    public List<User> getList(int userType) {
        String hql = "FROM User where userType = :userType";
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(hql);
        query.setString("userType", String.valueOf(userType));
        List<User> users = query.list();
        
        return users;
    }

    /**
     * Returns a user with specified login
     * @param login
     * @return User or null
     */
    @SuppressWarnings("unchecked")
    public User findByLogin(String login) {
    	if (login.isEmpty()) {
    		return null;
    	}
    	String hql = "FROM User where login = :login";
    	Query query = getFactory().getCurrentSession().createQuery(hql);
    	query.setString("login", login);
    	
    	List<User> results = query.list(); 
    	
    	return (results.size() != 0) ? results.get(0) : null;
    }
    
    /**
     * Creates a user
     * @param user
     * @return Created user
     */
    public User createUser(User user) {
        return save(user);
    }
    
    /**
     * Deletes all users
     */
    public void deleteAll() {
        Session session = getFactory().getCurrentSession();
        String hql = "DELETE FROM User";
        Query query = session.createQuery(hql);
        query.executeUpdate();
    }
}
