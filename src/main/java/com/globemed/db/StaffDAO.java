package com.globemed.db;

import com.globemed.staff.Staff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    /**
     * Retrieves a staff member by their username.
     * @param username The username of the staff member.
     * @return Staff object if found, null otherwise.
     */
    public Staff getStaffByUsername(String username) {
        // --- MODIFIED: Include doctor_id in SELECT ---
        String sql = "SELECT staff_id, username, password_hash, role, doctor_id FROM staff WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Staff(
                        rs.getInt("staff_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getString("doctor_id") // --- NEW: Pass doctor_id to constructor ---
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching staff by username: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a staff member by their ID.
     * @param staffId The ID of the staff member.
     * @return Staff object if found, null otherwise.
     */
    public Staff getStaffById(int staffId) {
        // --- MODIFIED: Include doctor_id in SELECT ---
        String sql = "SELECT staff_id, username, password_hash, role, doctor_id FROM staff WHERE staff_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Staff(
                        rs.getInt("staff_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getString("doctor_id") // --- NEW: Pass doctor_id to constructor ---
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching staff by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all staff members.
     * @return A list of all Staff objects.
     */
    public List<Staff> getAllStaff() {
        List<Staff> staffList = new ArrayList<>();
        // --- MODIFIED: Include doctor_id in SELECT ---
        String sql = "SELECT staff_id, username, password_hash, role, doctor_id FROM staff ORDER BY username";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                staffList.add(new Staff(
                        rs.getInt("staff_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getString("doctor_id") // --- NEW: Pass doctor_id to constructor ---
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all staff: " + e.getMessage());
        }
        return staffList;
    }

    /**
     * Creates a new staff member record in the database.
     * @param staff The Staff object to create.
     * @return true if creation was successful, false otherwise.
     */
    public boolean createStaff(Staff staff) {
        if (getStaffByUsername(staff.getUsername()) != null) {
            System.err.println("Error: Staff with username '" + staff.getUsername() + "' already exists.");
            return false;
        }
        // --- MODIFIED: Include doctor_id in INSERT statement ---
        String sql = "INSERT INTO staff (username, password_hash, role, doctor_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, staff.getUsername());
            pstmt.setString(2, staff.getPasswordHash());
            pstmt.setString(3, staff.getRole());

            // --- NEW: Set doctor_id, handling null ---
            if (staff.getDoctorId() != null && !staff.getDoctorId().isEmpty()) {
                pstmt.setString(4, staff.getDoctorId());
            } else {
                pstmt.setNull(4, java.sql.Types.VARCHAR);
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        staff.setStaffId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating staff: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates an existing staff member record in the database.
     * @param staff The Staff object with updated details.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateStaff(Staff staff) {
        // --- MODIFIED: Include doctor_id in UPDATE statement ---
        String sql = "UPDATE staff SET username = ?, password_hash = ?, role = ?, doctor_id = ? WHERE staff_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, staff.getUsername());
            pstmt.setString(2, staff.getPasswordHash());
            pstmt.setString(3, staff.getRole());

            // --- NEW: Set doctor_id, handling null ---
            if (staff.getDoctorId() != null && !staff.getDoctorId().isEmpty()) {
                pstmt.setString(4, staff.getDoctorId());
            } else {
                pstmt.setNull(4, java.sql.Types.VARCHAR);
            }
            pstmt.setInt(5, staff.getStaffId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating staff: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a staff member record from the database.
     * @param staffId The ID of the staff member to delete.
     * @return true if deletion was successful, false otherwise.
     */
    public boolean deleteStaff(int staffId) {
        String sql = "DELETE FROM staff WHERE staff_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting staff: " + e.getMessage());
        }
        return false;
    }
}