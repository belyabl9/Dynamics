package com.m1namoto.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;

import com.m1namoto.servlets.Auth;

public class PropertiesService {
    private final static Logger logger = Logger.getLogger(Auth.class);
    private final static String staticPropFileName = "config.properties";
    
    private static Properties getStaticProperties() {
        InputStream inputStream = null;
        Properties prop = null;
        try {
            prop = new Properties();
            inputStream = PropertiesService.class.getClassLoader().getResourceAsStream(staticPropFileName);
            
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("Property file '" + staticPropFileName + "' not found in the classpath");
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
    
    public static String getStaticPropertyValue(String propName) {
        Properties properties = getStaticProperties();
        return properties.getProperty(propName);
    }
    
    private static Configuration dynamicConfiguration;

    public static String getDynamicPropertyValue(String propName) {
        if (dynamicConfiguration == null) {
            loadDynamicConfiguration();
        }

        return dynamicConfiguration.getString(propName);
    }
    
    public static Map<String, String> getDynamicsPropertyValues() {
        if (dynamicConfiguration == null) {
            loadDynamicConfiguration();
        }
        
        Map<String, String> properties = new HashMap<String, String>();
        Iterator<String> keys = dynamicConfiguration.getKeys();
        while(keys.hasNext()){
            String key = (String) keys.next();
            String value = dynamicConfiguration.getString(key);
            properties.put(key, value);
        }
        
        return properties;
    }
    
    public static void setDynamicPropertyValues(Map<String, String> properties) {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
            new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
            .configure(params.properties()
            .setFile(new File(getStaticPropertyValue("app_settings_path"))));
        
        Configuration conf = null;
        try {
            conf = builder.getConfiguration();
            for (String propName : properties.keySet()) {
                conf.setProperty(propName, properties.get(propName));
            }
            builder.save();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

        dynamicConfiguration = conf;
    }

    public static void reloadDynamicConfiguration() {
        loadDynamicConfiguration();
    }
    
    private static void loadDynamicConfiguration() {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
            new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
            .configure(params.properties()
            .setFile(new File(getStaticPropertyValue("app_settings_path"))));

        try {
            dynamicConfiguration = builder.getConfiguration();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

}
