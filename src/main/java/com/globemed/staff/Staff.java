package com.globemed.staff;

/**
 * Represents a staff member in the GlobeMed system.
 */
public class Staff {
    private int staffId;
    private String username;
    private String passwordHash;
    private String role;
    private String doctorId; // <-- NEW FIELD: Link to a Doctor record (can be null)

    // Constructor for creating new staff (ID is auto-generated)
    public Staff(String username, String passwordHash, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.doctorId = null; // Default to null for new staff
    }

    // Constructor for creating new staff with doctor ID
    public Staff(String username, String passwordHash, String role, String doctorId) { // <-- NEW CONSTRUCTOR
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.doctorId = doctorId;
    }

    // Constructor for existing staff (ID is known)
    public Staff(int staffId, String username, String passwordHash, String role, String doctorId) { // <-- MODIFIED CONSTRUCTOR
        this.staffId = staffId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.doctorId = doctorId; // Initialize new field
    }

    // Getters
    public int getStaffId() { return staffId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public String getDoctorId() { return doctorId; } // <-- NEW GETTER

    // Setters (for updating existing staff details)
    public void setStaffId(int staffId) { this.staffId = staffId; }
    public void setUsername(String username) { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setRole(String role) { this.role = role; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; } // <-- NEW SETTER

    @Override
    public String toString() {
        return String.format("ID: %d | Username: %s | Role: %s %s",
                staffId, username, role, (doctorId != null ? " (Doctor ID: " + doctorId + ")" : ""));
    }
}