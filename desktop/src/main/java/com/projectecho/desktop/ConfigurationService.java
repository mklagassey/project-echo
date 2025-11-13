package com.projectecho.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationService {

    private final Properties properties = new Properties();

    public ConfigurationService() {
        try (InputStream input = getClass().getResourceAsStream("/config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}