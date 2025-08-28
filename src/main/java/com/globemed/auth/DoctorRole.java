package com.globemed.auth;

public class DoctorRole extends UserRoleDecorator {
    public DoctorRole(IUser user) {
        super(user);
    }

    @Override
    public boolean hasPermission(String permission) {
        if (    "can_access_appointments".equals(permission) ||
                "can_generate_reports".equals(permission) || // Already added this for reports
                "can_mark_appointment_done".equals(permission) || // <-- NEW: Doctors can mark appointments as done
                "can_update_appointment".equals(permission) ) { // <-- NEW: Doctors can update appointment details
            return true;
        }
        return super.hasPermission(permission);
    }
}