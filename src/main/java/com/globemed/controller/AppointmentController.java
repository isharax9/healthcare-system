package com.globemed.controller;

import com.globemed.appointment.Appointment;
import com.globemed.appointment.AppointmentScheduler;
import com.globemed.appointment.Doctor;
import com.globemed.auth.IUser;
import com.globemed.db.SchedulingDAO;
import com.globemed.ui.AllAppointmentsDialog; // <-- NEW IMPORT
import com.globemed.ui.AppointmentPanel;

import javax.swing.*;
import java.awt.*; // Make sure this is imported for JFrame
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
    private final IUser currentUser;
    private final JFrame mainFrame; // For parenting dialogs

    public AppointmentController(AppointmentPanel view, JFrame mainFrame, IUser currentUser) {
        this.view = view;
        this.mainFrame = mainFrame;
        this.dao = new SchedulingDAO();
        this.scheduler = new AppointmentScheduler();
        this.currentUser = currentUser;
        initController();
        loadInitialData();
        applyPermissions(); // <-- NEW CALL
    }

    private void initController() {
        view.viewScheduleButton.addActionListener(e -> viewSchedule());
        view.bookAppointmentButton.addActionListener(e -> bookNewAppointment());
        view.cancelAppointmentButton.addActionListener(e -> cancelAppointment());
        view.updateAppointmentButton.addActionListener(e -> updateAppointment());
        view.viewAllAppointmentsButton.addActionListener(e -> showAllAppointments()); // <-- NEW LISTENER

        view.appointmentsList.addListSelectionListener(e -> {
            boolean isSelected = view.appointmentsList.getSelectedValue() != null;
            view.updateAppointmentButton.setEnabled(isSelected);
            view.cancelAppointmentButton.setEnabled(isSelected && currentUser.hasPermission("can_delete_appointment"));
        });
    }

    private void loadInitialData() {
        view.setDoctorList(dao.getAllDoctors());
    }

    // --- NEW METHOD: Apply Permissions ---
    private void applyPermissions() {
        // Doctors and Nurses can view all appointments
        view.viewAllAppointmentsButton.setEnabled(currentUser.hasPermission("can_access_appointments"));

        // Only doctors can potentially update status (e.g., mark as done)
        // For now, we enable update for all who can access appointment,
        // but specific status changes would need further checks in the dialog or here.
        // The "Mark as Done" button in AllAppointmentsDialog will handle its own permissions.
    }

    // --- NEW METHOD: Show All Appointments ---
    private void showAllAppointments() {
        // Fetch all appointments
        List<Appointment> allAppointments = dao.getAllAppointments();

        if (allAppointments.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "No appointments found in the database.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Pass the permission for marking as done to the dialog
        AllAppointmentsDialog dialog = new AllAppointmentsDialog(
                mainFrame,
                allAppointments,
                currentUser.hasPermission("can_mark_appointment_done") // Pass this permission
        );
        dialog.setVisible(true);
        viewSchedule(); // Refresh the current schedule view after dialog closes, in case a status was changed
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

    private void cancelAppointment() {
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
        // In a real app, update might involve changing doctor/time, which is complex.
        // For simplicity, we just allow updating the reason for now, but a full update dialog would be needed.
        if (!currentUser.hasPermission("can_update_appointment")) { // New permission for updating
            JOptionPane.showMessageDialog(view, "You do not have permission to update appointments.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Appointment selectedAppointment = view.appointmentsList.getSelectedValue();
        if (selectedAppointment == null) return;

        String newReason = JOptionPane.showInputDialog(view, "Enter new reason for appointment:", selectedAppointment.getReason());

        if (newReason != null && !newReason.trim().isEmpty()) {
            Appointment updatedAppointment = new Appointment(
                    selectedAppointment.getPatientId(),
                    selectedAppointment.getDoctorId(),
                    selectedAppointment.getAppointmentDateTime(),
                    newReason.trim()
            );
            updatedAppointment.setAppointmentId(selectedAppointment.getAppointmentId());
            updatedAppointment.setStatus(selectedAppointment.getStatus()); // Keep original status unless explicitly changed

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