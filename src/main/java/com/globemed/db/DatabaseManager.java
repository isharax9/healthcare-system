package com.globemed.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseManager handles database connections with environment variable support.
 * This allows for flexible configuration across different environments.
 */
public class DatabaseManager {
    // Default values that can be overridden by environment variables
    private static final String DEFAULT_JDBC_URL = "jdbc:mysql://localhost:3306/globemed_db";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "NewPassword123!";
    
    // Environment variable names
    private static final String ENV_DB_URL = "DB_URL";
    private static final String ENV_DB_USERNAME = "DB_USERNAME";
    private static final String ENV_DB_PASSWORD = "DB_PASSWORD";
    
    private static Connection connection;

    // Private constructor to prevent instantiation
    private DatabaseManager() {}

    /**
     * Gets the database URL from environment variables or default.
     * @return Database URL
     */
    private static String getDatabaseUrl() {
        String url = System.getenv(ENV_DB_URL);
        if (url == null || url.trim().isEmpty()) {
            url = System.getProperty("db.url", DEFAULT_JDBC_URL);
        }
        return url;
    }

    /**
     * Gets the database username from environment variables or default.
     * @return Database username
     */
    private static String getDatabaseUsername() {
        String username = System.getenv(ENV_DB_USERNAME);
        if (username == null || username.trim().isEmpty()) {
            username = System.getProperty("db.username", DEFAULT_USERNAME);
        }
        return username;
    }

    /**
     * Gets the database password from environment variables or default.
     * @return Database password
     */
    private static String getDatabasePassword() {
        String password = System.getenv(ENV_DB_PASSWORD);
        if (password == null || password.trim().isEmpty()) {
            password = System.getProperty("db.password", DEFAULT_PASSWORD);
        }
        return password;
    }

    /**
     * Gets a connection to the database.
     * Supports configuration via environment variables:
     * - DB_URL: Database URL (default: jdbc:mysql://localhost:3306/globemed_db)
     * - DB_USERNAME: Database username (default: root)
     * - DB_PASSWORD: Database password (default: NewPassword123!)
     * 
     * @return A database connection object.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                String url = getDatabaseUrl();
                String username = getDatabaseUsername();
                String password = getDatabasePassword();
                
                System.out.println("Connecting to database: " + url);
                System.out.println("Username: " + username);
                
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("Database connection successful!");
                
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found.");
                throw new SQLException("JDBC Driver not found", e);
            } catch (SQLException e) {
                System.err.println("Failed to connect to database: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    /**
     * Closes the database connection if it's open.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Failed to close the database connection: " + e.getMessage());
            }
        }
    }

    /**
     * Tests the database connection without storing it.
     * Useful for validating configuration.
     * 
     * @return true if connection test is successful, false otherwise
     */
    public static boolean testConnection() {
        try {
            String url = getDatabaseUrl();
            String username = getDatabaseUsername();
            String password = getDatabasePassword();
            
            Connection testConnection = DriverManager.getConnection(url, username, password);
            testConnection.close();
            return true;
        } catch (Exception e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}