package com.globemed.auth;

public class NurseRole extends UserRoleDecorator {
    public NurseRole(IUser user) {
        super(user);
    }

    @Override
    public boolean hasPermission(String permission) {
        if (    "can_access_patients".equals(permission) ||
                "can_access_appointments".equals(permission) ||
                "can_generate_reports".equals(permission) ||
                "can_book_appointment".equals(permission) ) {
            return true;
        }
        // Nurses DO NOT have permissions like:
        // "can_delete_patient"
        // "can_delete_appointment"
        // "can_delete_bill"
        // "can_mark_appointment_done"
        // "can_update_appointment"
        // "can_access_billing"
        // "can_view_all_patients"

        return super.hasPermission(permission);
    }
}