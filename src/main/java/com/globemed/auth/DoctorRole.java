package com.globemed.auth;

public class DoctorRole extends UserRoleDecorator {
    public DoctorRole(IUser user) {
        super(user);
    }

    @Override
    public boolean hasPermission(String permission) {
        if (    "can_access_appointments".equals(permission) ||
                "can_generate_reports".equals(permission) ||
                "can_mark_appointment_done".equals(permission) ||
                "can_update_appointment".equals(permission) ||
                "can_add_appointment_notes".equals(permission) ) { // <-- NEW: Doctors can add/edit notes
            return true;
        }
        return super.hasPermission(permission);
    }
}