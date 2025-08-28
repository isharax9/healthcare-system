package com.globemed.auth;

public class AdminRole extends UserRoleDecorator {
    public AdminRole(IUser user) {
        super(user);
    }

    @Override
    public boolean hasPermission(String permission) {
        // Admins have all permissions.
        return true;
    }
}