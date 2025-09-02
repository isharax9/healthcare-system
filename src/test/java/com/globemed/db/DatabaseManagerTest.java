package com.globemed.db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DatabaseManager class.
 * These tests verify environment variable and system property configuration.
 */
class DatabaseManagerTest {

    private String originalUrl;
    private String originalUsername;
    private String originalPassword;

    @BeforeEach
    void setUp() {
        // Store original system properties if they exist
        originalUrl = System.getProperty("db.url");
        originalUsername = System.getProperty("db.username");
        originalPassword = System.getProperty("db.password");
        
        // Close any existing connections
        DatabaseManager.closeConnection();
    }

    @AfterEach
    void tearDown() {
        // Restore original system properties
        if (originalUrl != null) {
            System.setProperty("db.url", originalUrl);
        } else {
            System.clearProperty("db.url");
        }
        
        if (originalUsername != null) {
            System.setProperty("db.username", originalUsername);
        } else {
            System.clearProperty("db.username");
        }
        
        if (originalPassword != null) {
            System.setProperty("db.password", originalPassword);
        } else {
            System.clearProperty("db.password");
        }
        
        // Close connections
        DatabaseManager.closeConnection();
    }

    @Test
    void testDatabaseManagerClassExists() {
        // Test that the DatabaseManager class can be instantiated (via reflection)
        assertDoesNotThrow(() -> {
            Class.forName("com.globemed.db.DatabaseManager");
        }, "DatabaseManager class should exist");
    }

    @Test
    void testGetConnectionWithSystemProperties() {
        // Set test database configuration via system properties
        System.setProperty("db.url", "jdbc:mysql://localhost:3306/test_db");
        System.setProperty("db.username", "test_user");
        System.setProperty("db.password", "test_password");

        // The connection attempt should use the system properties
        // Note: This will fail to connect since we don't have a real database,
        // but it should attempt to use the correct configuration
        assertThrows(SQLException.class, () -> {
            DatabaseManager.getConnection();
        }, "Should attempt to connect with system properties and fail due to no database");
    }

    @Test
    void testTestConnectionMethod() {
        // Set invalid configuration to test the testConnection method
        System.setProperty("db.url", "jdbc:mysql://invalid:3306/invalid_db");
        System.setProperty("db.username", "invalid_user");
        System.setProperty("db.password", "invalid_password");

        // Test connection should return false for invalid configuration
        boolean result = DatabaseManager.testConnection();
        assertFalse(result, "Test connection should return false for invalid database configuration");
    }

    @Test
    void testCloseConnectionDoesNotThrowException() {
        // Test that closing connection doesn't throw exception even when no connection exists
        assertDoesNotThrow(() -> {
            DatabaseManager.closeConnection();
        }, "Closing connection should not throw exception");
    }

    @Test
    void testDefaultConfiguration() {
        // Test that the default configuration is used when no environment variables or system properties are set
        // This test verifies that the class has sensible defaults
        
        // Clear any existing system properties
        System.clearProperty("db.url");
        System.clearProperty("db.username");
        System.clearProperty("db.password");

        // The default configuration should be used (will fail to connect, but should try)
        assertThrows(SQLException.class, () -> {
            DatabaseManager.getConnection();
        }, "Should attempt to connect with default configuration");
    }
}