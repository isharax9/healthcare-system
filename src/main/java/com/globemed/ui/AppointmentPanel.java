package com.globemed.ui;

import com.globemed.appointment.Appointment;
import com.globemed.appointment.Doctor;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class AppointmentPanel extends JPanel {

    // --- Components ---
    // Make these public so the controller can access them

    // Doctor selection
    public final JList<Doctor> doctorList = new JList<>(); // <-- FIX

    // Date selection
    public final JSpinner dateSpinner = new JSpinner(new SpinnerDateModel()); // <-- FIX

    // Display existing appointments
    public final JList<String> appointmentsList = new JList<>(); // <-- FIX

    // New appointment booking
    public final JTextField patientIdField = new JTextField(10); // <-- FIX
    public final JSpinner timeSpinner = new JSpinner(new SpinnerDateModel()); // <-- FIX
    public final JTextField reasonField = new JTextField(20); // <-- FIX
    public final JButton bookAppointmentButton = new JButton("Book Appointment"); // <-- FIX

    // Status display
    public final JLabel statusLabel = new JLabel("Please select a doctor and date to view their schedule."); // <-- FIX

    // The rest of the file remains exactly the same.
    // ... constructor and methods ...
    public AppointmentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- 1. West Panel: Doctor List ---
        JPanel doctorPanel = new JPanel(new BorderLayout());
        doctorPanel.setBorder(new TitledBorder("Select Doctor"));
        doctorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        doctorPanel.add(new JScrollPane(doctorList), BorderLayout.CENTER);
        add(doctorPanel, BorderLayout.WEST);

        // --- 2. Center Panel: Main content ---
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        add(centerPanel, BorderLayout.CENTER);

        // --- 2a. North of Center: Date Selection ---
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.setBorder(new TitledBorder("Select Date"));
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new java.util.Date()); // Set to today's date
        datePanel.add(dateSpinner);
        centerPanel.add(datePanel, BorderLayout.NORTH);

        // --- 2b. Center of Center: Existing Appointments ---
        JPanel schedulePanel = new JPanel(new BorderLayout());
        schedulePanel.setBorder(new TitledBorder("Scheduled Appointments for Selected Date"));
        schedulePanel.add(new JScrollPane(appointmentsList), BorderLayout.CENTER);
        centerPanel.add(schedulePanel, BorderLayout.CENTER);

        // --- 2c. South of Center: Booking Form ---
        JPanel bookingPanel = new JPanel(new GridBagLayout());
        bookingPanel.setBorder(new TitledBorder("Book a New Appointment"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Configure time spinner for time only
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

        // --- 3. South Panel: Status Label ---
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }

    // ... rest of the methods are unchanged ...
    public void setDoctorList(java.util.List<Doctor> doctors) {
        DefaultListModel<Doctor> model = new DefaultListModel<>();
        for (Doctor doctor : doctors) {
            model.addElement(doctor);
        }
        doctorList.setModel(model);
    }

    public void setAppointmentsList(java.util.List<Appointment> appointments) {
        DefaultListModel<String> model = new DefaultListModel<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        if (appointments.isEmpty()) {
            model.addElement("No appointments scheduled for this day.");
        } else {
            for (Appointment appt : appointments) {
                String display = String.format("%s - Patient: %s, Reason: %s",
                        appt.getAppointmentDateTime().format(formatter),
                        appt.getPatientId(),
                        appt.getReason()
                );
                model.addElement(display);
            }
        }
        appointmentsList.setModel(model);
    }
}