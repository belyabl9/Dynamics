package com.m1namoto.dao;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import com.m1namoto.service.PropertiesService;

public class HibernateUtil {
    private static SessionFactory sessionFactory;
    private static DatabaseConfigs dbConfig = DatabaseConfigs.valueOf(PropertiesService.getInstance().getStaticPropertyValue("active_db_config").get());

    private static final String MAIN_DB_CONFIG_FILE = "hibernate.cfg.xml";
    private static final String TEST_DB_CONFIG_FILE = "hibernate.cfg.test.xml";
    
    public enum DatabaseConfigs {
        MAIN(MAIN_DB_CONFIG_FILE),
        TEST(TEST_DB_CONFIG_FILE);
        
        private String stringValue;
        
        DatabaseConfigs(String stringValue) {
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
            StandardServiceRegistryBuilder standardServiceRegistryBuilder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
            sessionFactory = configuration.buildSessionFactory(standardServiceRegistryBuilder.build());
        }
        return sessionFactory;
    }

}