package marketplace.config;

import java.io.*;
import java.util.*;

public class ConfigLoader {
    private static final Properties properties = new Properties();

    static {
        try (var stream = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}
