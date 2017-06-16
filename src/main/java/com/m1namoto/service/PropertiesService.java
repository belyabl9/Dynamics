package com.m1namoto.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.google.common.base.Optional;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class PropertiesService {
    private final static Logger logger = Logger.getLogger(PropertiesService.class);

    private static final String STATIC_PROP_FILE_NAME = "config.properties";
    private static final String PROPERTY_FILE_NOT_FOUND = "Property file '" + STATIC_PROP_FILE_NAME + "' not found in the classpath";
    private static final String APP_SETTINGS_PATH_PARAM = "app_settings_path";

    private static final String APP_PATH_NOT_SPECIFIED = "Property with application settings path is not specified.";

    private static final List<String> DYNAMIC_OPTIONS = Arrays.asList("save_requests", "update_template", "learning_rate", "threshold");

    private static Properties staticProperties;
    private static Map<String, String> dynamicProperties;

    private static Properties getStaticProperties() {
        if (staticProperties == null) {
            synchronized (Properties.class) {
                if (staticProperties == null) {
                    InputStream inputStream = null;
                    try {
                        staticProperties = new Properties();
                        inputStream = PropertiesService.class.getClassLoader().getResourceAsStream(STATIC_PROP_FILE_NAME);
                        if (inputStream != null) {
                            staticProperties.load(inputStream);
                        } else {
                            throw new FileNotFoundException(PROPERTY_FILE_NOT_FOUND);
                        }
                    } catch (Exception e) {
                        logger.error(e);
                        throw new RuntimeException(e);
                    } finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            logger.error(e);
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }

        return staticProperties;
    }

    public static Optional<String> getStaticPropertyValue(String propName) {
        return Optional.fromNullable(getStaticProperties().getProperty(propName));
    }
    
    public static Map<String, String> getDynamicPropertyValues() {
        if (dynamicProperties == null){
            synchronized (PropertiesService.class){
                if (dynamicProperties == null) {
                    Map<String, String> configurationMap = makeDynamicPropertiesMap(loadDynamicConfiguration());
                    dynamicProperties = configurationMap;
                }
            }
        }
        return dynamicProperties;
    }

    public static void setDynamicPropertyValues(@NotNull Map<String, String> propertiesMap) {
        Map<String, String> dynamicPropertyValues = getDynamicPropertyValues();
        for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
            if (DYNAMIC_OPTIONS.contains(entry.getKey())) {
                dynamicPropertyValues.put(entry.getKey(), entry.getValue());
            }
        }
        dynamicProperties = dynamicPropertyValues;
    }

    @NotNull
    private static Map<String, String> makeDynamicPropertiesMap(@NotNull Configuration configuration) {
        Map<String, String> configurationMap = new HashMap<>();
        Iterator<String> keys = configuration.getKeys();
        while(keys.hasNext()){
            String key = keys.next();
            String value = configuration.getString(key);
            configurationMap.put(key, value);
        }
        return configurationMap;
    }

    public static Optional<String> getDynamicPropertyValue(String propName) {
        return Optional.fromNullable(getDynamicPropertyValues().get(propName));
    }

    public static void reloadDynamicConfiguration() {
        dynamicProperties = makeDynamicPropertiesMap(loadDynamicConfiguration());
    }
    
    private static Configuration loadDynamicConfiguration() {
        Optional<String> appSettingsPropOpt = getStaticPropertyValue(APP_SETTINGS_PATH_PARAM);
        if (!appSettingsPropOpt.isPresent()) {
            throw new RuntimeException(APP_PATH_NOT_SPECIFIED);
        }

        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
            new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(
                        params.properties()
                        .setFile(new File(appSettingsPropOpt.get()))
                );

        try {
            return builder.getConfiguration();
        } catch (ConfigurationException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

}
