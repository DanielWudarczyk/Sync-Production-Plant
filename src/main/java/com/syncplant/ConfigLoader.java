package com.syncplant;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class ConfigLoader {
    public static Map<String, Object> loadConfig() {
        Yaml yaml = new Yaml();
        InputStream inputStream = ConfigLoader.class
                .getClassLoader()
                .getResourceAsStream("config.yaml");

        if (inputStream == null) {
            throw new RuntimeException("config.yaml not found in resources folder");
        }

        return yaml.load(inputStream);
    }
}
