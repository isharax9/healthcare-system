package com.globemed.auth;

public class DoctorRole extends UserRoleDecorator {
    public DoctorRole(IUser user) {
        super(user);
    }

    @Override
    public boolean hasPermission(String permission) {
        // Doctors can access patient and appointment tabs.
        if (    "can_access_patients".equals(permission) ||
                "can_access_appointments".equals(permission)) {
            return true;
        }
        // For any other permission, check the wrapped object.
        return super.hasPermission(permission);
    }
}