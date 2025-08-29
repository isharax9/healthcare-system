package com.globemed.auth;

/**
 * The Concrete Component. A base user object that represents a
 * logged-in user with basic information but no special permissions.
 */
public class BaseUser implements IUser {
    private final String username;
    private final String role;
    private final String doctorId; // <-- NEW FIELD

    // MODIFIED CONSTRUCTOR
    public BaseUser(String username, String role, String doctorId) {
        this.username = username;
        this.role = role;
        this.doctorId = doctorId; // Initialize new field
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getRole() {
        return this.role;
    }

    @Override
    public String getDoctorId() { // <-- NEW GETTER IMPLEMENTATION
        return this.doctorId;
    }

    @Override
    public boolean hasPermission(String permission) {
        return false;
    }
}