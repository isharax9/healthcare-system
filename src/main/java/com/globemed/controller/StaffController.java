package com.globemed.controller;

import com.globemed.auth.IUser;
import com.globemed.db.StaffDAO;
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
    private final IUser currentUser;
    private final JFrame mainFrame;
    private List<Staff> allStaff; // Cache the list of all staff members

    public StaffController(StaffPanel view, JFrame mainFrame, IUser currentUser) {
        this.view = view;
        this.mainFrame = mainFrame;
        this.currentUser = currentUser;
        this.dao = new StaffDAO();
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
    }

    private void loadInitialData() {
        refreshStaffTable();
    }

    private void refreshStaffTable() {
        try {
            this.allStaff = dao.getAllStaff();
            view.setStaffTableData(allStaff);
            view.clearForm(); // Clear the form after refreshing the table
            applyPermissions(); // Re-apply permissions after refresh
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

        view.setFormEditable(canManageStaff);
        view.refreshStaffButton.setEnabled(canManageStaff);
        view.clearFormButton.setEnabled(canManageStaff);

        view.addButton.setEnabled(canManageStaff && !isStaffSelected);
        view.updateButton.setEnabled(canManageStaff && isStaffSelected);
        view.deleteButton.setEnabled(canManageStaff && isStaffSelected);

        // Handle field editability
        if (canManageStaff) {
            // ID field is always read-only (auto-generated or for display only)
            view.staffIdField.setEditable(false);
            // Username editable for new entries, read-only for updates
            view.usernameField.setEditable(!isStaffSelected);
        } else {
            view.staffIdField.setEditable(false);
            view.usernameField.setEditable(false);
        }
    }

    private void populateFormFromTable() {
        Staff selectedStaff = view.getSelectedStaffFromTable(allStaff);
        if (selectedStaff != null) {
            // FIX: Convert int to String for setText
            view.staffIdField.setText(String.valueOf(selectedStaff.getStaffId()));
            view.usernameField.setText(selectedStaff.getUsername());
            view.passwordField.setText(""); // Never display password
            view.roleComboBox.setSelectedItem(selectedStaff.getRole());
        } else {
            view.clearForm();
        }
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

        if (username.isEmpty() || password.isEmpty() || role == null || role.isEmpty()) {
            JOptionPane.showMessageDialog(view,
                    "Please fill in all staff details (username, password, role).",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
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
            // New Staff constructor now includes doctorId (though it's null by default here)
            Staff newStaff = new Staff(username, password, role);
            boolean success = dao.createStaff(newStaff);

            if (success) {
                JOptionPane.showMessageDialog(view, "Staff member added successfully!");
                refreshStaffTable();
                view.clearForm();
            } else {
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

        String username = view.usernameField.getText().trim();
        String password = view.getPasswordText();
        String role = view.getSelectedRole();

        if (username.isEmpty() || password.isEmpty() || role == null || role.isEmpty()) {
            JOptionPane.showMessageDialog(view,
                    "Please fill in all staff details (username, password, role).",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
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

        // Prevent an Admin from demoting themselves
        if (selectedStaff.getUsername().equals(currentUser.getUsername()) && !"Admin".equals(role)) {
            int confirm = JOptionPane.showConfirmDialog(view,
                    "Are you sure you want to change your OWN role from Admin to " + role + "?\nThis might lock you out of admin functions.",
                    "Confirm Role Change",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        try {
            // Pass the original doctorId if staff is not changing roles
            Staff updatedStaff = new Staff(selectedStaff.getStaffId(), username, password, role, selectedStaff.getDoctorId());
            boolean success = dao.updateStaff(updatedStaff);

            if (success) {
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
        view.staffTable.clearSelection(); // Clear selection in the table
        applyPermissions(); // Re-apply to enable Add / disable Update/Delete
    }
}