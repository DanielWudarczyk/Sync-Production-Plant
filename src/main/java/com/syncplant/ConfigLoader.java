package com.syncplant;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class ConfigLoader {
    public static Map<String, Object> loadConfig() {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream("config.yaml")) {
            if (inputStream == null) {
                throw new RuntimeException("Config file not found!");
            }
            return yaml.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Error loading config", e);
        }
    }
}
