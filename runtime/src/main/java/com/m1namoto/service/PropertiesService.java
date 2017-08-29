package com.m1namoto.service;

import com.google.common.base.Optional;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class PropertiesService {
    private final static Logger logger = Logger.getLogger(PropertiesService.class);

    private static final String STATIC_PROP_FILE_NAME = "config.properties";
    private static final String PROPERTY_FILE_NOT_FOUND = "Property file '" + STATIC_PROP_FILE_NAME + "' not found in the classpath";
    private static final String APP_SETTINGS_PATH_PARAM = "app_settings_path";

    private static final String APP_PATH_NOT_SPECIFIED = "Property with application settings path is not specified.";

    private static final List<String> DYNAMIC_OPTIONS = Arrays.asList("save_requests", "update_template", "learning_rate", "threshold");

    private Properties staticProperties;
    private Configuration dynamicPropertiesConfiguration;

    private PropertiesService() {}

    private static class LazyHolder {
        static final PropertiesService INSTANCE = new PropertiesService();
    }
    public static PropertiesService getInstance() {
        return LazyHolder.INSTANCE;
    }

    private Properties getStaticProperties() {
        if (staticProperties == null) {
            synchronized (this) {
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

    public Optional<String> getStaticPropertyValue(String propName) {
        return Optional.fromNullable(getStaticProperties().getProperty(propName));
    }
    
    public Configuration getDynamicPropertyConfiguration() {
        if (dynamicPropertiesConfiguration == null){
            synchronized (this){
                if (dynamicPropertiesConfiguration == null) {
                    dynamicPropertiesConfiguration = loadDynamicConfiguration();
                }
            }
        }
        return dynamicPropertiesConfiguration;
    }

    public Map<String, String> getDynamicPropertyValues() {
        return makeDynamicPropertiesMap();
    }

    public Optional<String> getDynamicPropertyValue(String propName) {
        return Optional.fromNullable(getDynamicPropertyValues().get(propName));
    }

    public void saveDynamicConfiguration(@NotNull Map<String, String> values) {
        if (dynamicPropertiesConfiguration == null) {
            dynamicPropertiesConfiguration = getDynamicPropertyConfiguration();
        }
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = entry.getKey();
            if (!DYNAMIC_OPTIONS.contains(key)) {
                continue;
            }
            dynamicPropertiesConfiguration.setProperty(key, entry.getValue());
        }
    }

    @NotNull
    private Map<String, String> makeDynamicPropertiesMap() {
        Map<String, String> configurationMap = new HashMap<>();
        Iterator<String> keys = getDynamicPropertyConfiguration().getKeys();
        while(keys.hasNext()){
            String key = keys.next();
            String value = dynamicPropertiesConfiguration.getString(key);
            configurationMap.put(key, value);
        }
        return configurationMap;
    }

    @NotNull
    private Configuration loadDynamicConfiguration() {
        Optional<String> appSettingsPropOpt = getStaticPropertyValue(APP_SETTINGS_PATH_PARAM);
        if (!appSettingsPropOpt.isPresent()) {
            throw new RuntimeException(APP_PATH_NOT_SPECIFIED);
        }

        File file = new File(appSettingsPropOpt.get());
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
            new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(params.fileBased().setFile(file));
        builder.setAutoSave(true);

        try {
            return builder.getConfiguration();
        } catch (ConfigurationException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

}
