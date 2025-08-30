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
        // --- MODIFIED: Select doctor_id from staff table ---
        String sql = "SELECT role, doctor_id FROM staff WHERE username = ? AND password_hash = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                String doctorId = rs.getString("doctor_id"); // <-- NEW: Fetch doctor_id

                // Check if doctor_id was NULL in the database
                if (rs.wasNull()) {
                    doctorId = null;
                }

                System.out.println("Login successful for user: " + username + " with role: " + role + (doctorId != null ? " (Doctor ID: " + doctorId + ")" : ""));
                // --- MODIFIED: Pass doctorId to decorateUser ---
                return decorateUser(username, role, doctorId);
            }

        } catch (SQLException e) {
            System.err.println("Database error during login: " + e.getMessage());
        }

        System.out.println("Login failed for user: " + username);
        return null;
    }

    /**
     * Factory method to construct the appropriate decorated user object.
     * @param username The username of the logged-in user.
     * @param role The role of the logged-in user.
     * @param doctorId The associated doctor ID, or null if not a doctor.
     * @return The fully decorated IUser object.
     */
    // --- MODIFIED: New parameter for doctorId ---
    private IUser decorateUser(String username, String role, String doctorId) {
        // --- MODIFIED: Pass doctorId to BaseUser constructor ---
        IUser user = new BaseUser(username, role, doctorId);

        switch (role) {
            case "Doctor":
                return new DoctorRole(user);
            case "Nurse":
                return new NurseRole(user);
            case "Admin":
                return new AdminRole(user);
            default:
                return user;
        }
    }
}