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

    public List<User> getList() {
        return (List<User>) findAll();
    }
    
    public List<User> getList(int userType) {
        String hql = "FROM User where userType = :userType";
        Session session = getFactory().openSession();
        Query query = session.createQuery(hql);
        query.setString("userType", String.valueOf(userType));
        List<User> users = query.list();
        session.close();
        
        return users;
    }

    public User findByLogin(String login) {
    	if (login.isEmpty()) {
    		return null;
    	}
    	String hql = "FROM User where login = :login";
    	Query query = getFactory().openSession().createQuery(hql);
    	query.setString("login", login);
    	
    	List<User> results = query.list(); 
    	
    	return (results.size() != 0) ? results.get(0) : null;
    }
    
    public User createUser(User user) {
        return save(user);
    }
    
    public void deleteAll() {
        Session session = getFactory().openSession();
        session.getTransaction().begin();
        String hql = "DELETE FROM User";
        Query query = session.createQuery(hql);
        query.executeUpdate();
        session.getTransaction().commit();
        session.close();
    }
}
