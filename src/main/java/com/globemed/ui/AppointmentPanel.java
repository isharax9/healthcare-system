package com.globemed.ui;

import com.globemed.appointment.Appointment;
import com.globemed.appointment.Doctor;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List; // Ensure java.util.List is imported

public class AppointmentPanel extends JPanel {

    // --- Components ---
    public final JList<Doctor> doctorList = new JList<>();
    public final JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
    public final JButton viewScheduleButton = new JButton("View Schedule"); // New button
    public final JList<Appointment> appointmentsList = new JList<>(); // Changed to hold Appointment objects
    public final JTextField patientIdField = new JTextField(10);
    public final JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
    public final JTextField reasonField = new JTextField(20);
    public final JButton bookAppointmentButton = new JButton("Book Appointment");
    public final JButton updateAppointmentButton = new JButton("Update Selected"); // New button
    public final JButton cancelAppointmentButton = new JButton("Cancel Selected"); // New button

    public AppointmentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- West Panel: Doctor List ---
        JPanel doctorPanel = new JPanel(new BorderLayout());
        doctorPanel.setBorder(new TitledBorder("1. Select Doctor"));
        doctorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        doctorPanel.add(new JScrollPane(doctorList), BorderLayout.CENTER);
        add(doctorPanel, BorderLayout.WEST);

        // --- Center Panel: Main content ---
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        add(centerPanel, BorderLayout.CENTER);

        // --- Date Selection & Action Panel ---
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.setBorder(new TitledBorder("2. Select Date & View"));
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new java.util.Date());
        datePanel.add(dateSpinner);
        datePanel.add(viewScheduleButton);
        centerPanel.add(datePanel, BorderLayout.NORTH);

        // --- Schedule Display Panel with action buttons ---
        JPanel schedulePanel = new JPanel(new BorderLayout(0, 5));
        schedulePanel.setBorder(new TitledBorder("3. Schedule & Actions"));
        schedulePanel.add(new JScrollPane(appointmentsList), BorderLayout.CENTER);
        // Custom renderer to display appointment info nicely
        appointmentsList.setCellRenderer(new AppointmentListRenderer());

        JPanel scheduleActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        scheduleActions.add(updateAppointmentButton);
        scheduleActions.add(cancelAppointmentButton);
        schedulePanel.add(scheduleActions, BorderLayout.SOUTH);
        centerPanel.add(schedulePanel, BorderLayout.CENTER);

        // --- Booking Form ---
        JPanel bookingPanel = new JPanel(new GridBagLayout());
        bookingPanel.setBorder(new TitledBorder("4. Book a New Appointment"));
        // (GridBagLayout code is the same as before)
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

        // Initial state
        updateAppointmentButton.setEnabled(false);
        cancelAppointmentButton.setEnabled(false);
    }

    public void setDoctorList(List<Doctor> doctors) {
        DefaultListModel<Doctor> model = new DefaultListModel<>();
        doctors.forEach(model::addElement);
        doctorList.setModel(model);
    }

    public void setAppointmentsList(List<Appointment> appointments) {
        DefaultListModel<Appointment> model = new DefaultListModel<>();
        if (appointments.isEmpty()) {
            // Handle display for an empty list if needed, or JList shows up empty
        } else {
            appointments.forEach(model::addElement);
        }
        appointmentsList.setModel(model);
    }

    // Custom renderer class to make the JList<Appointment> display readable text
    private static class AppointmentListRenderer extends DefaultListCellRenderer {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Appointment) {
                Appointment appt = (Appointment) value;
                setText(String.format("%s - Patient: %s, Reason: %s",
                        appt.getAppointmentDateTime().format(formatter),
                        appt.getPatientId(),
                        appt.getReason()));
            }
            return this;
        }
    }
}