package com.m1namoto.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import com.m1namoto.domain.User;
import com.m1namoto.service.Features;

public class DaoFactory {

    private static SessionFactory factory;

    private static SessionFactory getSessionFactory() {
        if (factory == null) {
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");
            StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
            factory = configuration.buildSessionFactory(ssrb.build());
        }
        return factory;
    }

    private Session createSession() {
        return getSessionFactory().openSession();
    }

    public static EventsDao getEventsDAO() {
        return new EventsDao(getSessionFactory());
    }
    
    public static UsersDao getUsersDAO() {
        return new UsersDao(getSessionFactory());
    }
    
    public static FeaturesDao getFeaturesDAO() {
        return new FeaturesDao(getSessionFactory());
    }
    
    public static void main(String[] args) {
        User user = new User();
        user.setId(2);
        Features.deleteFeatures(user);
    }
}