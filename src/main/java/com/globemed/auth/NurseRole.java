package com.globemed.auth;

public class NurseRole extends UserRoleDecorator {
    public NurseRole(IUser user) {
        super(user);
    }

    @Override
    public boolean hasPermission(String permission) {
        // Nurses can also access patient and appointment tabs.
        // (In a real system, they might have fewer editing rights, but for now, this is fine).
        if ("can_access_patients".equals(permission) ||
                "can_access_appointments".equals(permission)) {
            return true;
        }
        return super.hasPermission(permission);
    }
}