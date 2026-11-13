package com.vfms.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages JDBC Database Connectivity and credentials.
 * Credentials are NEVER hardcoded in source files.
 * Reads from db.properties or system environment variables.
 */
public class DatabaseConfig {

    private static final String PROPERTIES_FILE_PATH = "resources/db.properties";
    private static final String FALLBACK_PROPERTIES_PATH = "db.properties";

    private static String host = "localhost";
    private static int port = 3306;
    private static String dbName = "virtual_file_system";
    private static String user = "root";
    private static String password = "";
    private static boolean useSSL = false;
    private static boolean allowPublicKeyRetrieval = true;
    private static String serverTimezone = "UTC";

    private static String lastErrorMessage = "";
    private static boolean lastConnectionSuccessful = false;

    static {
        loadConfiguration();
    }

    /**
     * Loads configuration from file or environment variables.
     */
    public static synchronized void loadConfiguration() {
        Properties props = new Properties();
        File propFile = new File(PROPERTIES_FILE_PATH);
        if (!propFile.exists()) {
            propFile = new File(FALLBACK_PROPERTIES_PATH);
        }

        if (propFile.exists()) {
            try (InputStream in = new FileInputStream(propFile)) {
                props.load(in);
            } catch (Exception e) {
                System.err.println("Notice: Could not read properties file: " + e.getMessage());
            }
        }

        // Apply properties with defaults
        host = getEnvOrProp("DB_HOST", props.getProperty("db.host", host));
        try {
            port = Integer.parseInt(getEnvOrProp("DB_PORT", props.getProperty("db.port", String.valueOf(port))));
        } catch (NumberFormatException ignored) {}

        dbName = getEnvOrProp("DB_NAME", props.getProperty("db.name", dbName));
        user = getEnvOrProp("DB_USER", props.getProperty("db.user", user));
        password = getEnvOrProp("DB_PASSWORD", props.getProperty("db.password", password));

        useSSL = Boolean.parseBoolean(props.getProperty("db.useSSL", "false"));
        allowPublicKeyRetrieval = Boolean.parseBoolean(props.getProperty("db.allowPublicKeyRetrieval", "true"));
        serverTimezone = props.getProperty("db.serverTimezone", "UTC");
    }

    private static String getEnvOrProp(String envKey, String fallback) {
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }
        return fallback != null ? fallback.trim() : "";
    }

    /**
     * Builds the MySQL JDBC URL.
     */
    public static String buildJdbcUrl(String h, int p, String db) {
        return String.format(
            "jdbc:mysql://%s:%d/%s?createDatabaseIfNotExist=true&useSSL=%b&allowPublicKeyRetrieval=%b&serverTimezone=%s",
            h, p, db, useSSL, allowPublicKeyRetrieval, serverTimezone
        );
    }

    /**
     * Gets an active database Connection.
     * Throws SQLException if connection cannot be established.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Ensure modern MySQL JDBC driver class is registered
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // Fallback for older driver class name if present
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ignored) {}
        }

        String url = buildJdbcUrl(host, port, dbName);
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            lastConnectionSuccessful = true;
            lastErrorMessage = "";
            return conn;
        } catch (SQLException e) {
            lastConnectionSuccessful = false;
            lastErrorMessage = e.getMessage();
            throw e;
        }
    }

    /**
     * Tests a database connection without changing active global state.
     */
    public static boolean testConnection(String h, int p, String db, String u, String pass) {
        String url = buildJdbcUrl(h, p, db);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {}

        try (Connection conn = DriverManager.getConnection(url, u, pass)) {
            lastConnectionSuccessful = true;
            lastErrorMessage = "";
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            lastConnectionSuccessful = false;
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    /**
     * Saves updated configuration to the properties file and updates in-memory variables.
     */
    public static synchronized boolean saveConfiguration(String h, int p, String db, String u, String pass) {
        host = h.trim();
        port = p;
        dbName = db.trim();
        user = u.trim();
        password = pass != null ? pass : "";

        Properties props = new Properties();
        props.setProperty("db.host", host);
        props.setProperty("db.port", String.valueOf(port));
        props.setProperty("db.name", dbName);
        props.setProperty("db.user", user);
        props.setProperty("db.password", password);
        props.setProperty("db.useSSL", String.valueOf(useSSL));
        props.setProperty("db.allowPublicKeyRetrieval", String.valueOf(allowPublicKeyRetrieval));
        props.setProperty("db.serverTimezone", serverTimezone);

        File targetFile = new File(PROPERTIES_FILE_PATH);
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (OutputStream out = new FileOutputStream(targetFile)) {
            props.store(out, "Updated via Virtual File Management System GUI");
            return true;
        } catch (Exception e) {
            lastErrorMessage = "Failed to save db.properties: " + e.getMessage();
            return false;
        }
    }

    // Getters
    public static String getHost() { return host; }
    public static int getPort() { return port; }
    public static String getDbName() { return dbName; }
    public static String getUser() { return user; }
    public static String getPassword() { return password; }
    public static String getLastErrorMessage() { return lastErrorMessage; }
    public static boolean isLastConnectionSuccessful() { return lastConnectionSuccessful; }
}

