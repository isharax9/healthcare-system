package com.globemed.auth;

public class NurseRole extends UserRoleDecorator {
    public NurseRole(IUser user) {
        super(user);
    }

    @Override
    public boolean hasPermission(String permission) {
        // Nurses can also access patient and appointment tabs.
        // (In a real system, they might have fewer editing rights, but for now, this is fine).
        if (    "can_access_patients".equals(permission) ||
                "can_access_appointments".equals(permission) ||
                "can_generate_reports".equals(permission))  {
            return true;
        }
        // Nurses DO NOT have permissions like:
        // "can_delete_patient"
        // "can_delete_appointment"
        // "can_delete_bill"
        // "can_mark_appointment_done"
        // "can_update_appointment"
        // "can_access_billing"

        return super.hasPermission(permission);
    }
}