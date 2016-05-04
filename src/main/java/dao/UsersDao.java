package dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;

import domain.User;

public class UsersDao extends GenericDAO<User> {

    public UsersDao(SessionFactory factory) {
        super(User.class, factory);
    }

    public List<User> getList() {
        return (List<User>) findAll();
    }

    public User findByLogin(String login) {
    	if (login.isEmpty()) {
    		return null;
    	}
    	String hql = "FROM User where login = :login";
    	Query query = getFactory().openSession().createQuery(hql).setString("login", login);
    	
    	List<User> results = query.list(); 
    	
    	return (results.size() != 0) ? results.get(0) : null;
    }
    
    public User createUser(User user) {
        return save(user);
    }
}
