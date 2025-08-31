package com.globemed.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection manager for the GlobeMed Healthcare System.
 * Provides centralized database connection management with MySQL.
 */
public class DatabaseManager {
    // --- CONFIGURE YOUR DATABASE CONNECTION HERE ---
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/globemed_db";
    private static final String USERNAME = "root"; // change to your MySQL username
    private static final String PASSWORD = "your_password"; // change to your MySQL password

    private static Connection connection;

    // Private constructor to prevent instantiation
    private DatabaseManager() {}

    /**
     * Gets a connection to the database.
     * @return A database connection object.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // This line is optional for modern JDBC drivers but good practice
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
                System.out.println("Database connection successful!");
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found.");
                throw new SQLException("JDBC Driver not found", e);
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
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Failed to close the database connection: " + e.getMessage());
            }
        }
    }
}