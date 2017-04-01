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

public class PropertiesService {
    private final static Logger logger = Logger.getLogger(PropertiesService.class);

    private final static PropertiesService INSTANCE = new PropertiesService();
    private PropertiesService() {}

    private final static String STATIC_PROP_FILE_NAME = "config.properties";
    private final static String APP_SETTINGS_PATH_PARAM = "app_settings_path";
    
    private static Configuration dynamicConfiguration;

    public static PropertiesService getInstance() {
        return INSTANCE;
    }

    private Properties getStaticProperties() {
        InputStream inputStream = null;
        Properties prop = null;
        try {
            prop = new Properties();
            inputStream = PropertiesService.class.getClassLoader().getResourceAsStream(STATIC_PROP_FILE_NAME);
            
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("Property file '" + STATIC_PROP_FILE_NAME + "' not found in the classpath");
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
    
    public String getStaticPropertyValue(String propName) {
        return getStaticProperties().getProperty(propName);
    }
    
    public String getDynamicPropertyValue(String propName) {
        if (dynamicConfiguration == null) {
            loadDynamicConfiguration();
        }
        return dynamicConfiguration.getString(propName);
    }
    
    public Map<String, String> getDynamicsPropertyValues() {
        if (dynamicConfiguration == null) {
            loadDynamicConfiguration();
        }
        
        Map<String, String> properties = new HashMap<String, String>();
        Iterator<String> keys = dynamicConfiguration.getKeys();
        while(keys.hasNext()){
            String key = keys.next();
            String value = dynamicConfiguration.getString(key);
            properties.put(key, value);
        }
        
        return properties;
    }
    
    public void setDynamicPropertyValues(Map<String, String> properties) {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
            new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
            .configure(params.properties()
            .setFile(new File(getStaticPropertyValue(APP_SETTINGS_PATH_PARAM))));

        Configuration conf = null;
        try {
            conf = builder.getConfiguration();
            for (String propName : properties.keySet()) {
                conf.setProperty(propName, properties.get(propName));
            }
            builder.save();
        } catch (ConfigurationException e) {
            logger.error(e);
        }

        dynamicConfiguration = conf;
    }

    public void reloadDynamicConfiguration() {
        loadDynamicConfiguration();
    }
    
    private void loadDynamicConfiguration() {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
            new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
            .configure(params.properties()
            .setFile(new File(getStaticPropertyValue(APP_SETTINGS_PATH_PARAM))));

        try {
            dynamicConfiguration = builder.getConfiguration();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

}
