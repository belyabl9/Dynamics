package service;

import java.util.List;

import dao.DaoFactory;
import domain.User;

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
    
    public static User save(User user) {
    	return DaoFactory.getUsersDAO().save(user);
    }
    
    public static void del(User user) {
    	DaoFactory.getUsersDAO().delete(user);
    }

}
