package com.globemed.controller;

import com.globemed.auth.IUser;
import com.globemed.db.SchedulingDAO; // For Doctor CRUD
import com.globemed.db.StaffDAO;
import com.globemed.appointment.Doctor; // Doctor model
import com.globemed.staff.Staff;
import com.globemed.ui.StaffPanel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StaffController {
    private final StaffPanel view;
    private final StaffDAO dao;
    private final SchedulingDAO schedulingDAO; // To manage Doctor entities
    private final IUser currentUser;
    private final JFrame mainFrame;
    private List<Staff> allStaff; // Cache the list of all staff members

    public StaffController(StaffPanel view, JFrame mainFrame, IUser currentUser) {
        this.view = view;
        this.mainFrame = mainFrame;
        this.currentUser = currentUser;
        this.dao = new StaffDAO();
        this.schedulingDAO = new SchedulingDAO(); // Initialize
        initController();
        loadInitialData();
        applyPermissions();
    }

    private void initController() {
        // --- Button Listeners ---
        view.refreshStaffButton.addActionListener(e -> refreshStaffTable());
        view.addButton.addActionListener(e -> addStaff());
        view.updateButton.addActionListener(e -> updateStaff());
        view.deleteButton.addActionListener(e -> deleteStaff());
        view.clearFormButton.addActionListener(e -> clearForm());
        view.findDoctorButton.addActionListener(e -> findDoctorDetails()); // <-- NEW LISTENER

        // --- Table Selection Listener ---
        view.staffTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) { // Only react to final selection changes
                    populateFormFromTable();
                    applyPermissions(); // Re-apply permissions based on selection
                }
            }
        });

        // --- Role ComboBox Listener to toggle doctor fields ---
        view.roleComboBox.addActionListener(e -> applyPermissions());
    }

    private void loadInitialData() {
        refreshStaffTable();
    }

    private void refreshStaffTable() {
        try {
            this.allStaff = dao.getAllStaff();
            view.setStaffTableData(allStaff);
            view.clearForm();
            applyPermissions();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Error loading staff data: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyPermissions() {
        boolean canManageStaff = currentUser.hasPermission("can_manage_staff");
        boolean isStaffSelected = view.staffTable.getSelectedRow() != -1;
        String selectedRole = (String) view.roleComboBox.getSelectedItem();
        boolean isDoctorRoleSelected = "Doctor".equals(selectedRole);

        view.setFormEditable(canManageStaff);
        view.refreshStaffButton.setEnabled(canManageStaff);
        view.clearFormButton.setEnabled(canManageStaff);

        view.addButton.setEnabled(canManageStaff && !isStaffSelected);
        view.updateButton.setEnabled(canManageStaff && isStaffSelected);
        view.deleteButton.setEnabled(canManageStaff && isStaffSelected);

        // Handle field editability
        if (canManageStaff) {
            view.staffIdField.setEditable(false);
            view.usernameField.setEditable(!isStaffSelected);
            view.passwordField.setEditable(true);
            view.roleComboBox.setEnabled(true);

            // Doctor fields editability
            view.doctorLinkIdField.setEditable(isDoctorRoleSelected && !isStaffSelected); // Editable for new Doctor staff
            view.doctorFullNameField.setEditable(isDoctorRoleSelected);
            view.doctorSpecialtyField.setEditable(isDoctorRoleSelected);

            // Find button enabled if Doctor role is selected and Doctor ID field is editable (i.e., not linked yet)
            view.findDoctorButton.setEnabled(isDoctorRoleSelected && view.doctorLinkIdField.isEditable()); // <-- NEW
        } else {
            view.staffIdField.setEditable(false);
            view.usernameField.setEditable(false);
            view.passwordField.setEditable(false);
            view.roleComboBox.setEnabled(false);
            view.doctorLinkIdField.setEditable(false);
            view.doctorFullNameField.setEditable(false);
            view.doctorSpecialtyField.setEditable(false);
            view.findDoctorButton.setEnabled(false); // <-- NEW
        }
    }

    private void populateFormFromTable() {
        Staff selectedStaff = view.getSelectedStaffFromTable(allStaff);
        if (selectedStaff != null) {
            view.staffIdField.setText(String.valueOf(selectedStaff.getStaffId()));
            view.usernameField.setText(selectedStaff.getUsername());
            view.passwordField.setText("");
            view.roleComboBox.setSelectedItem(selectedStaff.getRole());

            if ("Doctor".equals(selectedStaff.getRole())) {
                view.doctorLinkIdField.setText(selectedStaff.getDoctorId() != null ? selectedStaff.getDoctorId() : "");
                // Fetch full doctor details if doctorId is linked
                if (selectedStaff.getDoctorId() != null) {
                    Doctor doctor = schedulingDAO.getDoctorById(selectedStaff.getDoctorId());
                    if (doctor != null) {
                        view.setDoctorDetails(doctor.getFullName(), doctor.getSpecialty()); // <-- Use helper
                        view.setDoctorDetailsEditable(false); // Make them read-only if linked
                    } else {
                        view.setDoctorDetails("[Doctor Profile Missing]", ""); // <-- Use helper
                        view.setDoctorDetailsEditable(true); // Allow editing if profile is missing
                    }
                } else { // Doctor role but no ID linked (e.g., old staff record)
                    view.setDoctorDetails("", ""); // <-- Use helper
                    view.setDoctorDetailsEditable(true); // Allow entering new details
                }
            } else { // Not a Doctor role
                view.setDoctorDetails("", ""); // <-- Use helper
                view.doctorLinkIdField.setText("");
            }
        } else {
            view.clearForm();
        }
    }

    // --- NEW: Find Doctor Details Method ---
    private void findDoctorDetails() {
        String doctorId = view.getDoctorLinkIdText();
        if (doctorId.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter a Doctor ID to find.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Doctor doctor = schedulingDAO.getDoctorById(doctorId);
            if (doctor != null) {
                view.setDoctorDetails(doctor.getFullName(), doctor.getSpecialty());
                view.setDoctorDetailsEditable(false); // Found, so make fields read-only
                JOptionPane.showMessageDialog(view, "Doctor profile found!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                view.setDoctorDetails("", ""); // Clear if not found
                view.setDoctorDetailsEditable(true); // Allow user to fill new details
                JOptionPane.showMessageDialog(view, "Doctor ID not found in the doctor's database. You can create a new profile.", "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Error searching for doctor: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        applyPermissions(); // Re-apply to update button states
    }


    private void addStaff() {
        if (!currentUser.hasPermission("can_manage_staff")) {
            JOptionPane.showMessageDialog(mainFrame,
                    "You do not have permission to add staff members.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = view.getUsernameText();
        String password = view.getPasswordText();
        String role = view.getSelectedRole();
        String doctorLinkId = view.getDoctorLinkIdText();
        String doctorFullName = view.getDoctorFullNameText();
        String doctorSpecialty = view.getDoctorSpecialtyText();


        if (username.isEmpty() || password.isEmpty() || role == null || role.isEmpty()) {
            JOptionPane.showMessageDialog(view,
                    "Please fill in all staff details (username, password, role).",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- Doctor-specific validation ---
        if ("Doctor".equals(role)) {
            if (doctorLinkId.isEmpty() || doctorFullName.isEmpty() || doctorSpecialty.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Please fill in all Doctor details (ID, Name, Specialty).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Check if Doctor ID exists before creating it
            if (schedulingDAO.getDoctorById(doctorLinkId) != null) {
                JOptionPane.showMessageDialog(view, "Doctor ID already exists. Please use a unique Doctor ID or update existing.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            doctorLinkId = null; // Ensure non-doctor roles don't have a doctor ID link
        }

        // --- Rule: Only Admin can add other Admins ---
        if ("Admin".equals(role) && !"Admin".equals(currentUser.getRole())) {
            JOptionPane.showMessageDialog(view,
                    "Only an Administrator can add another Administrator.",
                    "Permission Denied",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // First, create the Doctor record if the role is Doctor and it doesn't exist
            if ("Doctor".equals(role)) {
                Doctor newDoctor = new Doctor(doctorLinkId, doctorFullName, doctorSpecialty);
                boolean doctorSuccess = schedulingDAO.createDoctor(newDoctor);
                if (!doctorSuccess) {
                    JOptionPane.showMessageDialog(view, "Failed to create corresponding Doctor profile.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Then, create the Staff record, linking to doctor if applicable
            Staff newStaff = new Staff(username, password, role, doctorLinkId);
            boolean staffSuccess = dao.createStaff(newStaff);

            if (staffSuccess) {
                JOptionPane.showMessageDialog(view, "Staff member added successfully!");
                refreshStaffTable();
                view.clearForm();
            } else {
                // If staff creation fails after doctor creation, attempt to rollback doctor
                if ("Doctor".equals(role) && schedulingDAO.deleteDoctor(doctorLinkId)) { // Rollback doctor creation
                    System.err.println("Rolled back doctor creation for ID: " + doctorLinkId);
                }
                JOptionPane.showMessageDialog(view,
                        "Failed to add staff member. Username might already exist.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view,
                    "Error adding staff member: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStaff() {
        if (!currentUser.hasPermission("can_manage_staff")) {
            JOptionPane.showMessageDialog(mainFrame,
                    "You do not have permission to update staff members.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Staff selectedStaff = view.getSelectedStaffFromTable(allStaff);
        if (selectedStaff == null) {
            JOptionPane.showMessageDialog(view,
                    "Please select a staff member to update.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = view.getUsernameText();
        String password = view.getPasswordText();
        String role = view.getSelectedRole();
        String doctorLinkId = view.getDoctorLinkIdText(); // From form
        String doctorFullName = view.getDoctorFullNameText();
        String doctorSpecialty = view.getDoctorSpecialtyText();


        if (username.isEmpty() || password.isEmpty() || role == null || role.isEmpty()) {
            JOptionPane.showMessageDialog(view,
                    "Please fill in all staff details (username, password, role).",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- Doctor-specific validation for update ---
        if ("Doctor".equals(role)) {
            if (doctorLinkId.isEmpty() || doctorFullName.isEmpty() || doctorSpecialty.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Please fill in all Doctor details (ID, Name, Specialty).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // If staff was NOT a doctor, but is now, ensure ID is unique for new doctor profile
            if (!"Doctor".equals(selectedStaff.getRole()) && schedulingDAO.getDoctorById(doctorLinkId) != null) {
                JOptionPane.showMessageDialog(view, "Doctor ID already exists for another profile.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // --- Rule: Only Admin can change role to Admin or change an Admin's role ---
        if (("Admin".equals(role) && !"Admin".equals(currentUser.getRole())) ||
                ("Admin".equals(selectedStaff.getRole()) && !"Admin".equals(currentUser.getRole()))) {
            JOptionPane.showMessageDialog(view,
                    "Only an Administrator can update Admin roles or change an Administrator's details.",
                    "Permission Denied",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Prevent an Admin from demoting themselves (if they are the current user)
        if (selectedStaff.getUsername().equals(currentUser.getUsername()) && "Admin".equals(selectedStaff.getRole()) && !"Admin".equals(role)) {
            int confirm = JOptionPane.showConfirmDialog(view,
                    "Are you sure you want to change your OWN role from Admin to " + role + "?\nThis might lock you out of admin functions.",
                    "Confirm Role Change",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        try {
            // Handle Doctor profile update/creation/deletion based on role change
            String oldDoctorId = selectedStaff.getDoctorId();
            String newDoctorId = ("Doctor".equals(role)) ? doctorLinkId : null; // Link if new role is Doctor

            if ("Doctor".equals(role)) { // Target role is Doctor
                Doctor existingDoctorProfile = schedulingDAO.getDoctorById(doctorLinkId);
                if (existingDoctorProfile == null) { // Create new Doctor profile
                    boolean doctorSuccess = schedulingDAO.createDoctor(new Doctor(doctorLinkId, doctorFullName, doctorSpecialty));
                    if (!doctorSuccess) {
                        JOptionPane.showMessageDialog(view, "Failed to create Doctor profile for link " + doctorLinkId + ".", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else { // Update existing Doctor profile
                    boolean doctorSuccess = schedulingDAO.updateDoctor(new Doctor(doctorLinkId, doctorFullName, doctorSpecialty));
                    if (!doctorSuccess) {
                        JOptionPane.showMessageDialog(view, "Failed to update Doctor profile " + doctorLinkId + ".", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            } else if ("Doctor".equals(selectedStaff.getRole()) && !"Doctor".equals(role)) { // Role changed FROM Doctor
                // Ask user if they want to delete the old doctor profile. Default: unlink only.
                int deleteDocConfirm = JOptionPane.showConfirmDialog(view, "Staff member's role is changing FROM Doctor. Do you want to delete the associated Doctor profile '" + oldDoctorId + "'?", "Unlink/Delete Doctor Profile", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (deleteDocConfirm == JOptionPane.YES_OPTION) {
                    boolean doctorDeleted = schedulingDAO.deleteDoctor(oldDoctorId);
                    if (!doctorDeleted) {
                        JOptionPane.showMessageDialog(view, "Failed to delete associated Doctor profile. Unlinking instead.", "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                }
                newDoctorId = null; // Always unlink if role is no longer Doctor
            }

            // Update the Staff record
            Staff updatedStaff = new Staff(selectedStaff.getStaffId(), username, password, role, newDoctorId);
            boolean staffSuccess = dao.updateStaff(updatedStaff);

            if (staffSuccess) {
                JOptionPane.showMessageDialog(view, "Staff member updated successfully!");
                refreshStaffTable();
            } else {
                JOptionPane.showMessageDialog(view,
                        "Failed to update staff member.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view,
                    "Error updating staff member: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteStaff() {
        if (!currentUser.hasPermission("can_manage_staff")) {
            JOptionPane.showMessageDialog(mainFrame,
                    "You do not have permission to delete staff members.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Staff selectedStaff = view.getSelectedStaffFromTable(allStaff);
        if (selectedStaff == null) {
            JOptionPane.showMessageDialog(view,
                    "Please select a staff member to delete.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- Rule: Prevent Admin from deleting themselves ---
        if (selectedStaff.getUsername().equals(currentUser.getUsername())) {
            JOptionPane.showMessageDialog(view,
                    "An Administrator cannot delete their own account.",
                    "Permission Denied",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to delete staff member: " + selectedStaff.getUsername() + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // If staff is a Doctor, confirm deletion of associated Doctor profile too
                if ("Doctor".equals(selectedStaff.getRole()) && selectedStaff.getDoctorId() != null) {
                    int deleteDoctorConfirm = JOptionPane.showConfirmDialog(view,
                            "This staff member is linked to Doctor ID: " + selectedStaff.getDoctorId() + ". Do you also want to DELETE the associated Doctor profile (DANGER: may delete appointments)?",
                            "Confirm Doctor Profile Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (deleteDoctorConfirm == JOptionPane.YES_OPTION) {
                        boolean doctorDeleted = schedulingDAO.deleteDoctor(selectedStaff.getDoctorId());
                        if (!doctorDeleted) {
                            JOptionPane.showMessageDialog(view, "Failed to delete associated Doctor profile. Aborting staff deletion.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } else { // User chose NOT to delete doctor profile, so just unlink staff in the staff table
                        // Update staff to remove doctor_id link first, then delete staff
                        // This update will ensure the FK constraint is satisfied before deleting staff
                        selectedStaff.setDoctorId(null);
                        dao.updateStaff(selectedStaff);
                    }
                }

                boolean success = dao.deleteStaff(selectedStaff.getStaffId());
                if (success) {
                    JOptionPane.showMessageDialog(view, "Staff member deleted successfully!");
                    refreshStaffTable();
                } else {
                    JOptionPane.showMessageDialog(view,
                            "Failed to delete staff member. Ensure no dependencies exist.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(view,
                        "Error deleting staff member: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        view.clearForm();
        view.staffTable.clearSelection();
        applyPermissions();
    }
}