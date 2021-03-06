package com.m1namoto.dao;

public class DaoFactory {

    public static UsersDao getUsersDAO() {
        return new UsersDao(HibernateUtil.getSessionFactory());
    }
    
    public static FeaturesDao getFeaturesDAO() {
        return new FeaturesDao(HibernateUtil.getSessionFactory());
    }

    public static SessionsDao getSessionsDAO() {
        return new SessionsDao(HibernateUtil.getSessionFactory());
    }

}