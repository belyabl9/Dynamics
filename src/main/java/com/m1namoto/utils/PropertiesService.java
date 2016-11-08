package com.m1namoto.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.m1namoto.servlets.Auth;

public class PropertiesService {
    private final static Logger logger = Logger.getLogger(Auth.class);
    private final static String propFileName = "config.properties";
    
    private static Properties getProperties() {
        InputStream inputStream = null;
        Properties prop = null;
        try {
            prop = new Properties();
            inputStream = PropertiesService.class.getClassLoader().getResourceAsStream(propFileName);
            
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("Property file '" + propFileName + "' not found in the classpath");
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error(e);
                return null;
            }
        }

        return prop;
    }
    
    public static String getPropertyValue(String propName) {
        Properties properties = getProperties();
        return properties.getProperty(propName);
    }
    
    public static void main(String[] args) {
        System.out.println( getPropertyValue("saved_requests_path") );
    }

}
