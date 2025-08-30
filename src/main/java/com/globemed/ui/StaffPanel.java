package com.globemed.ui;

import com.globemed.staff.Staff;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

public class StaffPanel extends JPanel {

    // --- Components for Staff List ---
    public final JTable staffTable = new JTable();
    public final JButton refreshStaffButton = new JButton("Refresh List");

    // --- Components for Staff CRUD Form ---
    public final JTextField staffIdField = new JTextField(5); // Staff ID (read-only for existing)
    public final JTextField usernameField = new JTextField(20);
    public final JPasswordField passwordField = new JPasswordField(20);
    public final JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"Doctor", "Nurse", "Admin"});

    // --- Doctor-specific fields ---
    private final JLabel doctorLinkIdLabel = new JLabel("Doctor ID (Link):");
    public final JTextField doctorLinkIdField = new JTextField(10);
    public final JButton findDoctorButton = new JButton("Find"); // <-- NEW BUTTON
    private final JLabel doctorFullNameLabel = new JLabel("Doctor Full Name:");
    public final JTextField doctorFullNameField = new JTextField(20);
    private final JLabel doctorSpecialtyLabel = new JLabel("Doctor Specialty:");
    public final JTextField doctorSpecialtyField = new JTextField(20);
    private final JPanel doctorFieldsPanel = new JPanel(new GridBagLayout()); // Panel to hold doctor-specific fields


    public final JButton addButton = new JButton("Add New Staff");
    public final JButton updateButton = new JButton("Update Staff");
    public final JButton deleteButton = new JButton("Delete Staff");
    public final JButton clearFormButton = new JButton("Clear Form");

    public StaffPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Top Panel: Refresh Button ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(refreshStaffButton);
        add(topPanel, BorderLayout.NORTH);

        // --- Center Panel: Staff Table ---
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new TitledBorder("All Staff Members"));
        staffTable.setFillsViewportHeight(true);
        staffIdField.setEditable(false);
        tablePanel.add(new JScrollPane(staffTable), BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        // --- Bottom Panel: CRUD Form ---
        JPanel crudPanel = new JPanel(new GridBagLayout());
        crudPanel.setBorder(new TitledBorder("Staff Details (Admin Only)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; crudPanel.add(new JLabel("Staff ID:"), gbc);
        gbc.gridx = 1; crudPanel.add(staffIdField, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; crudPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; crudPanel.add(usernameField, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; crudPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; crudPanel.add(passwordField, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; crudPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; crudPanel.add(roleComboBox, gbc);
        row++;

        // --- Doctor-specific fields panel setup ---
        GridBagConstraints gbcDoctor = new GridBagConstraints();
        gbcDoctor.insets = new Insets(3, 5, 3, 5);
        gbcDoctor.anchor = GridBagConstraints.WEST;
        gbcDoctor.fill = GridBagConstraints.HORIZONTAL;

        // Doctor ID (Link) and Find Button
        gbcDoctor.gridx = 0; gbcDoctor.gridy = 0; doctorFieldsPanel.add(doctorLinkIdLabel, gbcDoctor);
        gbcDoctor.gridx = 1; gbcDoctor.weightx = 1.0; doctorFieldsPanel.add(doctorLinkIdField, gbcDoctor);
        gbcDoctor.gridx = 2; gbcDoctor.weightx = 0.0; gbcDoctor.fill = GridBagConstraints.NONE; doctorFieldsPanel.add(findDoctorButton, gbcDoctor); // <-- ADDED BUTTON

        // Doctor Full Name
        gbcDoctor.gridx = 0; gbcDoctor.gridy = 1; gbcDoctor.gridwidth = 1; doctorFieldsPanel.add(doctorFullNameLabel, gbcDoctor);
        gbcDoctor.gridx = 1; gbcDoctor.gridwidth = 2; gbcDoctor.fill = GridBagConstraints.HORIZONTAL; doctorFieldsPanel.add(doctorFullNameField, gbcDoctor);

        // Doctor Specialty
        gbcDoctor.gridx = 0; gbcDoctor.gridy = 2; gbcDoctor.gridwidth = 1; doctorFieldsPanel.add(doctorSpecialtyLabel, gbcDoctor);
        gbcDoctor.gridx = 1; gbcDoctor.gridwidth = 2; gbcDoctor.fill = GridBagConstraints.HORIZONTAL; doctorFieldsPanel.add(doctorSpecialtyField, gbcDoctor);

        // Add the doctor-specific fields panel to the main CRUD panel
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        crudPanel.add(doctorFieldsPanel, gbc);
        row++;

        // CRUD Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearFormButton);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        crudPanel.add(buttonPanel, gbc);

        add(crudPanel, BorderLayout.SOUTH);

        // --- Action Listener for Role ComboBox ---
        roleComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleDoctorFieldsVisibility((String) roleComboBox.getSelectedItem());
            }
        });

        // Initial state
        setFormEditable(false);
        addButton.setEnabled(false);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
        clearFormButton.setEnabled(false);
        refreshStaffButton.setEnabled(false);

        toggleDoctorFieldsVisibility(null); // Hide doctor fields initially
        findDoctorButton.setEnabled(false); // <-- NEW: Disable Find button initially
    }

    // --- Helper Methods for Controller Interaction ---

    public void setStaffTableData(List<Staff> staffList) {
        String[] columnNames = {"ID", "Username", "Role", "Doctor ID"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        for (Staff staff : staffList) {
            model.addRow(new Object[]{staff.getStaffId(), staff.getUsername(), staff.getRole(), staff.getDoctorId()});
        }
        staffTable.setModel(model);
        staffTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        staffTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    public Staff getSelectedStaffFromTable(List<Staff> allStaff) {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow != -1 && allStaff != null && selectedRow < allStaff.size()) {
            return allStaff.get(selectedRow);
        }
        return null;
    }

    public void setFormEditable(boolean editable) {
        usernameField.setEditable(editable);
        passwordField.setEditable(editable);
        roleComboBox.setEnabled(editable);
        staffIdField.setEditable(false); // ID is read-only

        boolean isDoctorRole = "Doctor".equals(roleComboBox.getSelectedItem());

        // Doctor fields editability controlled by role and general form editability
        doctorLinkIdField.setEditable(editable && isDoctorRole);
        doctorFullNameField.setEditable(editable && isDoctorRole);
        doctorSpecialtyField.setEditable(editable && isDoctorRole);

        // Find button is enabled if Doctor role is selected AND doctorLinkIdField is editable
        findDoctorButton.setEnabled(editable && isDoctorRole && doctorLinkIdField.isEditable()); // <-- NEW: Find button depends on role and editability
    }

    public void clearForm() {
        staffIdField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        roleComboBox.setSelectedIndex(0);
        staffTable.clearSelection();

        doctorLinkIdField.setText("");
        doctorFullNameField.setText("");
        doctorSpecialtyField.setText("");
        toggleDoctorFieldsVisibility(null); // Hide doctor fields when form is cleared
    }

    // Getters for form fields
    public String getStaffIdText() { return staffIdField.getText().trim(); }
    public String getUsernameText() { return usernameField.getText().trim(); }
    public String getPasswordText() { return new String(passwordField.getPassword()); }
    public String getSelectedRole() { return (String) roleComboBox.getSelectedItem(); }
    public String getDoctorLinkIdText() { return doctorLinkIdField.getText().trim(); }
    public String getDoctorFullNameText() { return doctorFullNameField.getText().trim(); }
    public String getDoctorSpecialtyText() { return doctorSpecialtyField.getText().trim(); }

    // --- Method to toggle visibility/editability of doctor-specific fields ---
    private void toggleDoctorFieldsVisibility(String selectedRole) {
        boolean showDoctorFields = "Doctor".equals(selectedRole);

        doctorFieldsPanel.setVisible(showDoctorFields);
        doctorLinkIdLabel.setVisible(showDoctorFields);
        doctorLinkIdField.setVisible(showDoctorFields);
        doctorFullNameLabel.setVisible(showDoctorFields);
        doctorFullNameField.setVisible(showDoctorFields);
        doctorSpecialtyLabel.setVisible(showDoctorFields);
        doctorSpecialtyField.setVisible(showDoctorFields);
        findDoctorButton.setVisible(showDoctorFields); // <-- NEW: Toggle visibility of Find button

        // Re-validate and re-layout the panel
        revalidate();
        repaint();
    }

    // --- NEW: Method to set doctor full name and specialty fields (used by controller) ---
    public void setDoctorDetails(String fullName, String specialty) {
        doctorFullNameField.setText(fullName);
        doctorSpecialtyField.setText(specialty);
    }

    // --- NEW: Method to enable/disable doctor full name and specialty fields ---
    public void setDoctorDetailsEditable(boolean editable) {
        doctorFullNameField.setEditable(editable);
        doctorSpecialtyField.setEditable(editable);
    }
}