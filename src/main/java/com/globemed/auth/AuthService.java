package com.globemed.auth;

import com.globemed.db.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This service handles user authentication and role decoration.
 */
public class AuthService {

    /**
     * Attempts to log in a user with the given credentials.
     * @param username The username to check.
     * @param password The password to check.
     * @return A decorated IUser object on success, or null on failure.
     */
    public IUser login(String username, String password) {
        String sql = "SELECT role FROM staff WHERE username = ? AND password_hash = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password); // In a real app, you'd hash the input password first.

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Login successful, now decorate the user based on their role.
                String role = rs.getString("role");
                System.out.println("Login successful for user: " + username + " with role: " + role);
                return decorateUser(username, role);
            }

        } catch (SQLException e) {
            System.err.println("Database error during login: " + e.getMessage());
        }

        System.out.println("Login failed for user: " + username);
        return null; // Login failed
    }

    /**
     * Factory method to construct the appropriate decorated user object.
     * This is where the Decorator pattern is assembled.
     * @param username The username of the logged-in user.
     * @param role The role of the logged-in user.
     * @return The fully decorated IUser object.
     */
    private IUser decorateUser(String username, String role) {
        // Start with a base user object
        IUser user = new BaseUser(username, role);

        // Wrap it with the appropriate decorator based on the role string
        switch (role) {
            case "Doctor":
                return new DoctorRole(user);
            case "Nurse":
                return new NurseRole(user);
            case "Admin":
                return new AdminRole(user);
            default:
                // If the role from the DB is unknown, return the base user with no permissions.
                return user;
        }
    }
}