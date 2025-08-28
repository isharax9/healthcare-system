package com.globemed.controller;

import com.globemed.appointment.Appointment;
import com.globemed.appointment.AppointmentScheduler;
import com.globemed.appointment.Doctor;
import com.globemed.auth.IUser; // <-- Add import
import com.globemed.db.SchedulingDAO;
import com.globemed.ui.AppointmentPanel;

import javax.swing.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class AppointmentController {
    private final AppointmentPanel view;
    private final SchedulingDAO dao;
    private final AppointmentScheduler scheduler; // The Mediator
    private final IUser currentUser; // <-- ADD THIS FIELD
    private final JFrame mainFrame;

    // --- THIS IS THE CORRECTED CONSTRUCTOR ---
    public AppointmentController(AppointmentPanel view, JFrame mainFrame, IUser currentUser) { // ADDED 'mainFrame'
        this.view = view;
        this.mainFrame = mainFrame; // <-- Initialize the new field
        this.dao = new SchedulingDAO();
        this.scheduler = new AppointmentScheduler();
        this.currentUser = currentUser;
        initController();
        loadInitialData();
    }

    private void initController() {
        // Explicit action to view schedule
        view.viewScheduleButton.addActionListener(e -> viewSchedule());

        // CRUD actions
        view.bookAppointmentButton.addActionListener(e -> bookNewAppointment());
        view.cancelAppointmentButton.addActionListener(e -> cancelAppointment());
        view.updateAppointmentButton.addActionListener(e -> updateAppointment());

        // MODIFY the selection listener
        view.appointmentsList.addListSelectionListener(e -> {
            boolean isSelected = view.appointmentsList.getSelectedValue() != null;
            view.updateAppointmentButton.setEnabled(isSelected);
            // Apply permission check to the cancel button
            view.cancelAppointmentButton.setEnabled(isSelected && currentUser.hasPermission("can_delete_appointment"));
        });
    }

    private void loadInitialData() {
        view.setDoctorList(dao.getAllDoctors());
    }

    private void viewSchedule() {
        Doctor selectedDoctor = view.doctorList.getSelectedValue();
        if (selectedDoctor == null) {
            JOptionPane.showMessageDialog(view, "Please select a doctor first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Date selectedDate = (Date) view.dateSpinner.getValue();
        LocalDate localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        List<Appointment> appointments = dao.getAppointmentsForDoctorOnDate(selectedDoctor.getDoctorId(), localDate);
        view.setAppointmentsList(appointments);
    }

    private void bookNewAppointment() {
        // ... (This logic is the same as before)
        Doctor selectedDoctor = view.doctorList.getSelectedValue();
        String patientId = view.patientIdField.getText().trim();
        String reason = view.reasonField.getText().trim();

        if (selectedDoctor == null || patientId.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please select a doctor and enter a Patient ID.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Date selectedDate = (Date) view.dateSpinner.getValue();
        Date selectedTime = (Date) view.timeSpinner.getValue();
        LocalDate datePart = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime timePart = selectedTime.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        LocalDateTime requestedDateTime = LocalDateTime.of(datePart, timePart);

        String resultMessage = scheduler.bookAppointment(patientId, selectedDoctor, requestedDateTime, reason);
        JOptionPane.showMessageDialog(view, resultMessage, "Booking Status", JOptionPane.INFORMATION_MESSAGE);
        viewSchedule(); // Refresh view
    }

    // MODIFY the cancelAppointment method
    private void cancelAppointment() {
        // Add a permission check at the very beginning
        if (!currentUser.hasPermission("can_delete_appointment")) {
            JOptionPane.showMessageDialog(view, "You do not have permission to cancel appointments.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Appointment selectedAppointment = view.appointmentsList.getSelectedValue();
        if (selectedAppointment == null) return;

        int choice = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to cancel this appointment?",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            boolean success = dao.deleteAppointment(selectedAppointment.getAppointmentId());
            if (success) {
                JOptionPane.showMessageDialog(view, "Appointment canceled successfully.");
            } else {
                JOptionPane.showMessageDialog(view, "Failed to cancel appointment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            viewSchedule(); // Refresh view
        }
    }

    private void updateAppointment() {
        Appointment selectedAppointment = view.appointmentsList.getSelectedValue();
        if (selectedAppointment == null) return;

        // For simplicity, we'll just allow updating the reason via a dialog
        String newReason = JOptionPane.showInputDialog(view, "Enter new reason for appointment:", selectedAppointment.getReason());

        if (newReason != null && !newReason.trim().isEmpty()) {
            // In a real app, you'd create a more complex dialog to change time, etc.
            // Here we just update the reason on the existing appointment object
            Appointment updatedAppointment = new Appointment(
                    selectedAppointment.getPatientId(),
                    selectedAppointment.getDoctorId(),
                    selectedAppointment.getAppointmentDateTime(),
                    newReason.trim()
            );
            updatedAppointment.setAppointmentId(selectedAppointment.getAppointmentId());
            updatedAppointment.setStatus("Updated"); // Optionally update status

            boolean success = dao.updateAppointment(updatedAppointment);
            if (success) {
                JOptionPane.showMessageDialog(view, "Appointment updated successfully.");
            } else {
                JOptionPane.showMessageDialog(view, "Failed to update appointment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            viewSchedule(); // Refresh view
        }
    }
}