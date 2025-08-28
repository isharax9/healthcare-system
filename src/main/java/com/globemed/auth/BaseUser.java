package com.globemed.auth;

/**
 * The Concrete Component. A base user object that represents a
 * logged-in user with basic information but no special permissions.
 */
public class BaseUser implements IUser {
    private final String username;
    private final String role;

    public BaseUser(String username, String role) {
        this.username = username;
        this.role = role;
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
    public boolean hasPermission(String permission) {
        // A base user has no permissions by default.
        return false;
    }
}