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
        String sql = "SELECT staff_id, username, password_hash, role FROM staff WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Staff(
                        rs.getInt("staff_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role")
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
        String sql = "SELECT staff_id, username, password_hash, role FROM staff WHERE staff_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Staff(
                        rs.getInt("staff_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role")
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
        String sql = "SELECT staff_id, username, password_hash, role FROM staff ORDER BY username";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                staffList.add(new Staff(
                        rs.getInt("staff_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role")
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
        // Prevent creating staff with an existing username
        if (getStaffByUsername(staff.getUsername()) != null) {
            System.err.println("Error: Staff with username '" + staff.getUsername() + "' already exists.");
            return false;
        }
        String sql = "INSERT INTO staff (username, password_hash, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, staff.getUsername());
            pstmt.setString(2, staff.getPasswordHash()); // Remember, in a real app, hash this!
            pstmt.setString(3, staff.getRole());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        staff.setStaffId(generatedKeys.getInt(1)); // Set the auto-generated ID back to the object
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
        String sql = "UPDATE staff SET username = ?, password_hash = ?, role = ? WHERE staff_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, staff.getUsername());
            pstmt.setString(2, staff.getPasswordHash()); // Update password hash as well
            pstmt.setString(3, staff.getRole());
            pstmt.setInt(4, staff.getStaffId());
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