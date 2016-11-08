package com.m1namoto.dao;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import com.m1namoto.utils.PropertiesService;

public class HibernateUtil {
    private static SessionFactory sessionFactory;
    private static DatabaseConfigs dbConfig = DatabaseConfigs.valueOf(PropertiesService.getPropertyValue("active_db_config"));

    public static enum DatabaseConfigs {
        MAIN("hibernate.cfg.xml"), TEST("hibernate.cfg.test.xml");
        private String stringValue;
        private DatabaseConfigs(String stringValue) {
            this.stringValue = stringValue;
        }
        public String toString() {
            return stringValue;
        }
    };
    
    public static void setDb(DatabaseConfigs config) {
        dbConfig = config;
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            Configuration configuration = new Configuration();
            configuration.configure(dbConfig.toString());
            StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
            sessionFactory = configuration.buildSessionFactory(ssrb.build());
        }
        return sessionFactory;
    }

}