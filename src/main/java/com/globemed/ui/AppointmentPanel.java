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
    public final JButton viewScheduleButton = new JButton("View Schedule");
    public final JButton viewAllAppointmentsButton = new JButton("View All Appointments");
    public final JList<Appointment> appointmentsList = new JList<>();
    public final JTextField patientIdField = new JTextField(10);
    public final JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
    public final JTextField reasonField = new JTextField(20);
    public final JButton bookAppointmentButton = new JButton("Book Appointment");
    public final JButton updateAppointmentButton = new JButton("Update Selected");
    public final JButton cancelAppointmentButton = new JButton("Cancel Selected");
    public final JButton markAsDoneSelectedButton = new JButton("Mark as Done");

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
        schedulePanel.add(new JScrollPane(appointmentsList), BorderLayout.CENTER);
        appointmentsList.setCellRenderer(new AppointmentListRenderer());

        JPanel scheduleActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        scheduleActions.add(updateAppointmentButton);
        scheduleActions.add(markAsDoneSelectedButton); // <-- ADD THE NEW BUTTON HERE
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

        // Initial state
        updateAppointmentButton.setEnabled(false);
        cancelAppointmentButton.setEnabled(false);
        viewAllAppointmentsButton.setEnabled(false);
        markAsDoneSelectedButton.setEnabled(false); // <-- Initially disabled
    }

    public void setDoctorList(List<Doctor> doctors) {
        DefaultListModel<Doctor> model = new DefaultListModel<>();
        doctors.forEach(model::addElement);
        doctorList.setModel(model);
    }

    public void setAppointmentsList(List<Appointment> appointments) {
        DefaultListModel<Appointment> model = new DefaultListModel<>();
        if (appointments.isEmpty()) {
            // No need to add a specific message for empty list here, JList will just be empty
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
                setText(String.format("%s - Patient: %s, Reason: %s - Status: %s",
                        appt.getAppointmentDateTime().format(formatter),
                        appt.getPatientId(),
                        appt.getReason(),
                        appt.getStatus()
                ));
            }
            return this;
        }
    }
}