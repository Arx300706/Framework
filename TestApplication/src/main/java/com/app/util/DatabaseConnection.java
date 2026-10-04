package com.app.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    private static final String DEFAULT_DB_PATH = System.getProperty("user.home") + "/testApplicationDb";
    private static final String DEFAULT_URL = "jdbc:h2:file:" + DEFAULT_DB_PATH + ";AUTO_SERVER=TRUE";
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String url = readConfig("app.db.url", "APP_DB_URL", DEFAULT_URL);
        String user = readConfig("app.db.user", "APP_DB_USER", DEFAULT_USER);
        String password = readConfig("app.db.password", "APP_DB_PASSWORD", DEFAULT_PASSWORD);
        loadDriver(url);
        return DriverManager.getConnection(url, user, password);
    }

    private static void loadDriver(String url) throws SQLException {
        String driverClassName = null;

        if (url.startsWith("jdbc:h2:")) {
            driverClassName = "org.h2.Driver";
        } else if (url.startsWith("jdbc:postgresql:")) {
            driverClassName = "org.postgresql.Driver";
        }

        if (driverClassName == null) {
            return;
        }

        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC introuvable: " + driverClassName, e);
        }
    }

    private static String readConfig(String propertyKey, String envKey, String defaultValue) {
        String propertyValue = System.getProperty(propertyKey);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue;
        }

        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        return defaultValue;
    }
}
