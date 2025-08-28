package com.globemed.ui;

import com.globemed.staff.Staff;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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
        staffIdField.setEditable(false); // Staff ID is usually auto-generated/read-only
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

        // CRUD Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearFormButton);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; // Span two columns for buttons
        crudPanel.add(buttonPanel, gbc);

        add(crudPanel, BorderLayout.SOUTH);

        // Initial state
        setFormEditable(false); // Initially not editable
        addButton.setEnabled(false);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
        clearFormButton.setEnabled(false);
        // Refresh button is always enabled for those with access
        refreshStaffButton.setEnabled(false);
    }

    // --- Helper Methods for Controller Interaction ---

    public void setStaffTableData(List<Staff> staffList) {
        String[] columnNames = {"ID", "Username", "Role"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        for (Staff staff : staffList) {
            model.addRow(new Object[]{staff.getStaffId(), staff.getUsername(), staff.getRole()});
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
    }

    public void clearForm() {
        staffIdField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        roleComboBox.setSelectedIndex(0); // Default to Doctor or first role
        staffTable.clearSelection();
    }

    // Getters for form fields
    public String getStaffIdText() { return staffIdField.getText().trim(); }
    public String getUsernameText() { return usernameField.getText().trim(); }
    public String getPasswordText() { return new String(passwordField.getPassword()); }
    public String getSelectedRole() { return (String) roleComboBox.getSelectedItem(); }
}