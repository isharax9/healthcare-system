package com.globemed.ui;

import com.globemed.appointment.Appointment;
import com.globemed.appointment.Doctor;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar; // Import Calendar
import java.util.Date;     // Import Date
import java.util.List;

public class AppointmentPanel extends JPanel {

    // --- Components ---
    public final JTable doctorsTable = new JTable();
    public final DefaultTableModel doctorTableModel = new DefaultTableModel();
    // MODIFIED: Make fields final and assign in constructor
    public final JSpinner dateSpinner; // Initialized in constructor
    public final JButton viewScheduleButton = new JButton("View Selected Day Appointments"); // <-- MODIFIED TEXT
    public final JButton viewAllAppointmentsButton = new JButton("View All Appointments");
    public final JTable appointmentsTable = new JTable();
    public final DefaultTableModel appointmentsTableModel = new DefaultTableModel();
    public final JTextField patientIdField = new JTextField(10);
    // MODIFIED: Make fields final and assign in constructor
    public final JSpinner timeSpinner; // Initialized in constructor
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

    // --- Doctor Notes Components ---
    public final JTextArea doctorNotesArea = new JTextArea(5, 40);
    public final JScrollPane doctorNotesScrollPane;

    public AppointmentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initializeTableModels(); // Initialize table models

        // --- Initialize Date Spinner ---
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_YEAR);
        dateSpinner = new JSpinner(dateModel); // Assign to final field
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);

        // --- Initialize Time Spinner ---
        SpinnerDateModel timeModel = new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE);
        timeSpinner = new JSpinner(timeModel); // Assign to final field
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);


        doctorNotesScrollPane = new JScrollPane(doctorNotesArea);

        // --- West Panel: Doctor List & CRUD ---
        JPanel doctorManagementPanel = new JPanel(new BorderLayout());
        JPanel doctorListPanel = new JPanel(new BorderLayout());
        doctorListPanel.setBorder(new TitledBorder("1. Select Doctor"));

        doctorsTable.setModel(doctorTableModel);
        doctorsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        doctorsTable.setRowSelectionAllowed(true);
        doctorsTable.setColumnSelectionAllowed(false);
        doctorsTable.getTableHeader().setReorderingAllowed(false);
        doctorsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        doctorsTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        doctorsTable.getColumnModel().getColumn(2).setPreferredWidth(100);

        JScrollPane doctorScrollPane = new JScrollPane(doctorsTable);
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
        // JSpinner dateSpinner setup is now handled at the top
        dateSelectionSubPanel.add(dateSpinner);
        dateSelectionSubPanel.add(viewScheduleButton);
        dateAndActionPanel.add(dateSelectionSubPanel);
        dateAndActionPanel.add(viewAllAppointmentsButton);
        centerPanel.add(dateAndActionPanel, BorderLayout.NORTH);

        // --- Schedule Display Panel with action buttons ---
        JPanel schedulePanel = new JPanel(new BorderLayout(0, 5));
        schedulePanel.setBorder(new TitledBorder("3. Scheduled Appointments for Selected Date"));

        appointmentsTable.setModel(appointmentsTableModel);
        appointmentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentsTable.setRowSelectionAllowed(true);
        appointmentsTable.setColumnSelectionAllowed(false);
        appointmentsTable.getTableHeader().setReorderingAllowed(false);
        appointmentsTable.setDefaultRenderer(LocalDateTime.class, new LocalDateTimeRenderer()); // Set custom renderer

        appointmentsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        appointmentsTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        appointmentsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        appointmentsTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        appointmentsTable.getColumnModel().getColumn(4).setPreferredWidth(200);
        appointmentsTable.getColumnModel().getColumn(5).setPreferredWidth(100);

        JScrollPane appointmentsScrollPane = new JScrollPane(appointmentsTable);
        schedulePanel.add(appointmentsScrollPane, BorderLayout.CENTER);

        // --- Panel to hold Schedule Actions and Doctor Notes ---
        JPanel bottomSchedulePanel = new JPanel(new BorderLayout(0, 5));
        JPanel scheduleActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        scheduleActions.add(updateAppointmentButton);
        scheduleActions.add(markAsDoneSelectedButton);
        scheduleActions.add(cancelAppointmentButton);
        bottomSchedulePanel.add(scheduleActions, BorderLayout.NORTH);

        // Doctor Notes / Prescription area
        JPanel doctorNotesPanel = new JPanel(new BorderLayout());
        doctorNotesPanel.setBorder(new TitledBorder("Doctor Notes / Prescription (Doctors Only)")); // <-- MODIFIED LABEL TEXT
        doctorNotesArea.setLineWrap(true);
        doctorNotesArea.setWrapStyleWord(true);
        doctorNotesPanel.add(doctorNotesScrollPane, BorderLayout.CENTER);
        bottomSchedulePanel.add(doctorNotesPanel, BorderLayout.CENTER);

        schedulePanel.add(bottomSchedulePanel, BorderLayout.SOUTH);
        centerPanel.add(schedulePanel, BorderLayout.CENTER);

        // --- Booking Form ---
        JPanel bookingPanel = new JPanel(new GridBagLayout());
        bookingPanel.setBorder(new TitledBorder("4. Book a New Appointment"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // JSpinner timeSpinner setup is now handled at the top
        gbc.gridx = 0; gbc.gridy = 0; bookingPanel.add(new JLabel("Patient ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; bookingPanel.add(patientIdField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; bookingPanel.add(new JLabel("Time (HH:mm):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; bookingPanel.add(timeSpinner, gbc);
        gbc.gridx = 2; gbc.gridy = 0; bookingPanel.add(new JLabel("Reason:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.gridwidth = 2; bookingPanel.add(reasonField, gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.gridwidth = 2; bookingPanel.add(bookAppointmentButton, gbc);

        centerPanel.add(bookingPanel, BorderLayout.SOUTH);

        // --- Initial states ---
        setDoctorCrudFieldsEditable(false);
        addDoctorButton.setEnabled(false);
        updateDoctorButton.setEnabled(false);
        deleteDoctorButton.setEnabled(false);
        clearDoctorFieldsButton.setEnabled(false);

        updateAppointmentButton.setEnabled(false);
        cancelAppointmentButton.setEnabled(false);
        viewAllAppointmentsButton.setEnabled(false);
        markAsDoneSelectedButton.setEnabled(false);
        doctorNotesArea.setEditable(false);
        clearAppointmentDetailsFields();
    }

    private void initializeTableModels() {
        doctorTableModel.setColumnIdentifiers(new String[]{"Doctor ID", "Name", "Specialty"});
        appointmentsTableModel.setColumnIdentifiers(new String[]{"ID", "Date/Time", "Patient ID", "Doctor ID", "Reason", "Status"});
    }

    public void setDoctorCrudFieldsEditable(boolean editable) {
        newDoctorIdField.setEditable(editable);
        newDoctorNameField.setEditable(editable);
        newDoctorSpecialtyField.setEditable(editable);
    }

    public void clearDoctorCrudFields() {
        newDoctorIdField.setText("");
        newDoctorNameField.setText("");
        newDoctorSpecialtyField.setText("");
    }

    public void clearAppointmentDetailsFields() {
        patientIdField.setText("");
        reasonField.setText("");
        doctorNotesArea.setText("");
    }

    public void setDoctorList(List<Doctor> doctors) {
        doctorTableModel.setRowCount(0);
        for (Doctor doctor : doctors) {
            doctorTableModel.addRow(new Object[]{
                    doctor.getDoctorId(),
                    doctor.getFullName(),
                    doctor.getSpecialty()
            });
        }
    }

    public void setAppointmentsList(List<Appointment> appointments) {
        appointmentsTableModel.setRowCount(0);

        if (appointments == null || appointments.isEmpty()) {
            appointmentsTableModel.addRow(new Object[]{"", "", "", "", "No appointments found.", ""});
            appointmentsTable.setEnabled(false);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"); // This line is not used here but kept for context.
            for (Appointment appointment : appointments) {
                appointmentsTableModel.addRow(new Object[]{
                        appointment.getAppointmentId(),
                        appointment.getAppointmentDateTime(), // Pass LocalDateTime object directly
                        appointment.getPatientId(),
                        appointment.getDoctorId(),
                        appointment.getReason(),
                        appointment.getStatus()
                });
            }
            appointmentsTable.setEnabled(true);
        }
        appointmentsTable.repaint();
    }

    public Doctor getSelectedDoctor(List<Doctor> doctors) {
        int selectedRow = doctorsTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < doctors.size()) {
            return doctors.get(selectedRow);
        }
        return null;
    }

    public Appointment getSelectedAppointment(List<Appointment> appointments) {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < appointments.size()) {
            if (appointmentsTable.getValueAt(selectedRow, 4).equals("No appointments found.")) {
                return null;
            }
            return appointments.get(selectedRow);
        }
        return null;
    }

    public String getSelectedDoctorId() {
        int selectedRow = doctorsTable.getSelectedRow();
        if (selectedRow >= 0) {
            return (String) doctorTableModel.getValueAt(selectedRow, 0);
        }
        return null;
    }

    public Integer getSelectedAppointmentId() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow >= 0) {
            if (appointmentsTable.getValueAt(selectedRow, 4).equals("No appointments found.")) {
                return null;
            }
            Object value = appointmentsTableModel.getValueAt(selectedRow, 0);
            if (value instanceof Integer) {
                return (Integer) value;
            }
        }
        return null;
    }

    public String getDoctorNotesText() {
        return doctorNotesArea.getText().trim();
    }

    public void setDoctorNotesText(String notes) {
        doctorNotesArea.setText(notes != null ? notes : "");
    }

    public void setDoctorNotesEditable(boolean editable) {
        doctorNotesArea.setEditable(editable);
        doctorNotesArea.setBackground(editable ? Color.WHITE : UIManager.getColor("Panel.background"));
    }

    // --- Custom Renderer for LocalDateTime in JTable ---
    private static class LocalDateTimeRenderer extends DefaultTableCellRenderer {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof LocalDateTime) {
                value = ((LocalDateTime) value).format(formatter);
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}