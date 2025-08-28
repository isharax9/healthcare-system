package com.globemed.ui;

import com.globemed.appointment.Appointment;
import com.globemed.appointment.Doctor;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AppointmentPanel extends JPanel {

    // --- Components ---
    public final JTable doctorTable = new JTable();
    public final DefaultTableModel doctorTableModel = new DefaultTableModel();
    public final JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
    public final JButton viewScheduleButton = new JButton("View Schedule");
    public final JButton viewAllAppointmentsButton = new JButton("View All Appointments");
    public final JTable appointmentsTable = new JTable();
    public final DefaultTableModel appointmentsTableModel = new DefaultTableModel();
    public final JTextField patientIdField = new JTextField(10);
    public final JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
    public final JTextField reasonField = new JTextField(20);
    public final JButton bookAppointmentButton = new JButton("Book Appointment");
    public final JButton updateAppointmentButton = new JButton("Update Selected");
    public final JButton cancelAppointmentButton = new JButton("Cancel Selected");
    public final JButton markAsDoneSelectedButton = new JButton("Mark Selected as Done");

    // --- Doctor CRUD Components ---
    public final JTextField newDoctorIdField = new JTextField(10);
    public final JTextField newDoctorNameField = new JTextField(15);
    public final JTextField newDoctorSpecialtyField = new JTextField(15);
    public final JButton addDoctorButton = new JButton("Add Doctor");
    public final JButton updateDoctorButton = new JButton("Update Doctor");
    public final JButton deleteDoctorButton = new JButton("Delete Doctor");
    public final JButton clearDoctorFieldsButton = new JButton("Clear Fields");

    public AppointmentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Initialize table models
        initializeTableModels();

        // --- West Panel: Doctor List & CRUD ---
        JPanel doctorManagementPanel = new JPanel(new BorderLayout());

        JPanel doctorListPanel = new JPanel(new BorderLayout());
        doctorListPanel.setBorder(new TitledBorder("1. Select Doctor"));

        // Setup doctor table
        doctorTable.setModel(doctorTableModel);
        doctorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        doctorTable.setRowSelectionAllowed(true);
        doctorTable.setColumnSelectionAllowed(false);
        doctorTable.getTableHeader().setReorderingAllowed(false);

        // Set preferred column widths for doctor table
        doctorTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        doctorTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Name
        doctorTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Specialty

        JScrollPane doctorScrollPane = new JScrollPane(doctorTable);
        doctorScrollPane.setPreferredSize(new Dimension(300, 200));
        doctorListPanel.add(doctorScrollPane, BorderLayout.CENTER);

        doctorManagementPanel.add(doctorListPanel, BorderLayout.CENTER);

        // --- Doctor CRUD Panel (South of doctor list) ---
        JPanel doctorCrudPanel = new JPanel(new GridBagLayout());
        doctorCrudPanel.setBorder(new TitledBorder("Doctor Management (Admin Only)"));
        GridBagConstraints gbcCrud = new GridBagConstraints();
        gbcCrud.insets = new Insets(3, 3, 3, 3);
        gbcCrud.anchor = GridBagConstraints.WEST;
        gbcCrud.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbcCrud.gridx = 0; gbcCrud.gridy = row; doctorCrudPanel.add(new JLabel("ID:"), gbcCrud);
        gbcCrud.gridx = 1; doctorCrudPanel.add(newDoctorIdField, gbcCrud);
        row++;
        gbcCrud.gridx = 0; gbcCrud.gridy = row; doctorCrudPanel.add(new JLabel("Name:"), gbcCrud);
        gbcCrud.gridx = 1; doctorCrudPanel.add(newDoctorNameField, gbcCrud);
        row++;
        gbcCrud.gridx = 0; gbcCrud.gridy = row; doctorCrudPanel.add(new JLabel("Specialty:"), gbcCrud);
        gbcCrud.gridx = 1; doctorCrudPanel.add(newDoctorSpecialtyField, gbcCrud);
        row++;

        JPanel crudButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        crudButtonsPanel.add(addDoctorButton);
        crudButtonsPanel.add(updateDoctorButton);
        crudButtonsPanel.add(deleteDoctorButton);
        crudButtonsPanel.add(clearDoctorFieldsButton);

        gbcCrud.gridx = 0; gbcCrud.gridy = row; gbcCrud.gridwidth = 2;
        doctorCrudPanel.add(crudButtonsPanel, gbcCrud);

        doctorManagementPanel.add(doctorCrudPanel, BorderLayout.SOUTH);
        add(doctorManagementPanel, BorderLayout.WEST);

        // --- Center Panel: Main content ---
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        add(centerPanel, BorderLayout.CENTER);

        // --- Date Selection & Action Panel ---
        JPanel dateAndActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dateAndActionPanel.setBorder(new TitledBorder("2. Schedule Actions"));

        JPanel dateSelectionSubPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dateSelectionSubPanel.add(new JLabel("Date:"));
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new java.util.Date());
        dateSelectionSubPanel.add(dateSpinner);
        dateSelectionSubPanel.add(viewScheduleButton);

        dateAndActionPanel.add(dateSelectionSubPanel);
        dateAndActionPanel.add(viewAllAppointmentsButton);

        centerPanel.add(dateAndActionPanel, BorderLayout.NORTH);

        // --- Schedule Display Panel with action buttons ---
        JPanel schedulePanel = new JPanel(new BorderLayout(0, 5));
        schedulePanel.setBorder(new TitledBorder("3. Scheduled Appointments for Selected Date"));

        // Setup appointments table
        appointmentsTable.setModel(appointmentsTableModel);
        appointmentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentsTable.setRowSelectionAllowed(true);
        appointmentsTable.setColumnSelectionAllowed(false);
        appointmentsTable.getTableHeader().setReorderingAllowed(false);

        // Set preferred column widths for appointments table
        appointmentsTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        appointmentsTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Date/Time
        appointmentsTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Patient ID
        appointmentsTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Doctor ID
        appointmentsTable.getColumnModel().getColumn(4).setPreferredWidth(200); // Reason
        appointmentsTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Status

        JScrollPane appointmentsScrollPane = new JScrollPane(appointmentsTable);
        schedulePanel.add(appointmentsScrollPane, BorderLayout.CENTER);

        JPanel scheduleActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        scheduleActions.add(updateAppointmentButton);
        scheduleActions.add(markAsDoneSelectedButton);
        scheduleActions.add(cancelAppointmentButton);
        schedulePanel.add(scheduleActions, BorderLayout.SOUTH);
        centerPanel.add(schedulePanel, BorderLayout.CENTER);

        // --- Booking Form ---
        JPanel bookingPanel = new JPanel(new GridBagLayout());
        bookingPanel.setBorder(new TitledBorder("4. Book a New Appointment"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        gbc.gridx = 0; gbc.gridy = 0; bookingPanel.add(new JLabel("Patient ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; bookingPanel.add(patientIdField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; bookingPanel.add(new JLabel("Time (HH:mm):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; bookingPanel.add(timeSpinner, gbc);
        gbc.gridx = 2; gbc.gridy = 0; bookingPanel.add(new JLabel("Reason:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.gridwidth = 2; bookingPanel.add(reasonField, gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.gridwidth = 2; bookingPanel.add(bookAppointmentButton, gbc);
        centerPanel.add(bookingPanel, BorderLayout.SOUTH);

        // --- Initial state for Doctor CRUD buttons ---
        setDoctorCrudFieldsEditable(false);
        addDoctorButton.setEnabled(false);
        updateDoctorButton.setEnabled(false);
        deleteDoctorButton.setEnabled(false);
        clearDoctorFieldsButton.setEnabled(false);

        // Existing initial state for appointment actions
        updateAppointmentButton.setEnabled(false);
        cancelAppointmentButton.setEnabled(false);
        viewAllAppointmentsButton.setEnabled(false);
        markAsDoneSelectedButton.setEnabled(false);
    }

    private void initializeTableModels() {
        // Initialize doctor table model
        doctorTableModel.setColumnIdentifiers(new String[]{"Doctor ID", "Name", "Specialty"});

        // Initialize appointments table model
        appointmentsTableModel.setColumnIdentifiers(new String[]{"ID", "Date/Time", "Patient ID", "Doctor ID", "Reason", "Status"});
    }

    // --- Helper method to manage doctor CRUD field editability ---
    public void setDoctorCrudFieldsEditable(boolean editable) {
        newDoctorIdField.setEditable(editable);
        newDoctorNameField.setEditable(editable);
        newDoctorSpecialtyField.setEditable(editable);
    }

    // --- Helper method to clear doctor CRUD fields ---
    public void clearDoctorCrudFields() {
        newDoctorIdField.setText("");
        newDoctorNameField.setText("");
        newDoctorSpecialtyField.setText("");
    }

    public void setDoctorList(List<Doctor> doctors) {
        doctorTableModel.setRowCount(0); // Clear existing data
        for (Doctor doctor : doctors) {
            doctorTableModel.addRow(new Object[]{
                    doctor.getDoctorId(),
                    doctor.getFullName(),
                    doctor.getSpecialty()
            });
        }
    }

    public void setAppointmentsList(List<Appointment> appointments) {
        appointmentsTableModel.setRowCount(0); // Clear existing data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Appointment appointment : appointments) {
            appointmentsTableModel.addRow(new Object[]{
                    appointment.getAppointmentId(),
                    appointment.getAppointmentDateTime().format(formatter),
                    appointment.getPatientId(),
                    appointment.getDoctorId(),
                    appointment.getReason(),
                    appointment.getStatus()
            });
        }
    }

    // Helper method to get selected doctor
    public Doctor getSelectedDoctor(List<Doctor> doctors) {
        int selectedRow = doctorTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < doctors.size()) {
            return doctors.get(selectedRow);
        }
        return null;
    }

    // Helper method to get selected appointment
    public Appointment getSelectedAppointment(List<Appointment> appointments) {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < appointments.size()) {
            return appointments.get(selectedRow);
        }
        return null;
    }

    // Helper method to get selected doctor from table
    public String getSelectedDoctorId() {
        int selectedRow = doctorTable.getSelectedRow();
        if (selectedRow >= 0) {
            return (String) doctorTableModel.getValueAt(selectedRow, 0);
        }
        return null;
    }

    // Helper method to get selected appointment ID from table
    public Integer getSelectedAppointmentId() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow >= 0) {
            return (Integer) appointmentsTableModel.getValueAt(selectedRow, 0);
        }
        return null;
    }
}