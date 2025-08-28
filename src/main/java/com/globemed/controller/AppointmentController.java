package com.globemed.controller;

import com.globemed.appointment.Appointment;
import com.globemed.appointment.AppointmentScheduler;
import com.globemed.appointment.Doctor;
import com.globemed.auth.IUser;
import com.globemed.db.SchedulingDAO;
import com.globemed.ui.AllAppointmentsDialog;
import com.globemed.ui.AppointmentPanel;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class AppointmentController {
    private final AppointmentPanel view;
    private final SchedulingDAO dao;
    private final AppointmentScheduler scheduler;
    private final IUser currentUser;
    private final JFrame mainFrame;

    public AppointmentController(AppointmentPanel view, JFrame mainFrame, IUser currentUser) {
        this.view = view;
        this.mainFrame = mainFrame;
        this.dao = new SchedulingDAO();
        this.scheduler = new AppointmentScheduler();
        this.currentUser = currentUser;
        initController();
        loadInitialData();
        applyPermissions();
    }

    private void initController() {
        view.viewScheduleButton.addActionListener(e -> viewSchedule());
        view.bookAppointmentButton.addActionListener(e -> bookNewAppointment());
        view.cancelAppointmentButton.addActionListener(e -> cancelAppointment());
        view.updateAppointmentButton.addActionListener(e -> updateAppointment());
        view.viewAllAppointmentsButton.addActionListener(e -> showAllAppointments());
        view.markAsDoneSelectedButton.addActionListener(e -> markSelectedAppointmentAsDone()); // <-- NEW LISTENER

        view.appointmentsList.addListSelectionListener(e -> {
            boolean isSelected = view.appointmentsList.getSelectedValue() != null;
            view.updateAppointmentButton.setEnabled(isSelected && currentUser.hasPermission("can_update_appointment")); // Enable update based on permission
            view.cancelAppointmentButton.setEnabled(isSelected && currentUser.hasPermission("can_delete_appointment"));
            view.markAsDoneSelectedButton.setEnabled(isSelected && currentUser.hasPermission("can_mark_appointment_done")); // <-- NEW PERMISSION CHECK
        });
    }

    private void loadInitialData() {
        view.setDoctorList(dao.getAllDoctors());
    }

    private void applyPermissions() {
        view.viewAllAppointmentsButton.setEnabled(currentUser.hasPermission("can_access_appointments"));
        view.bookAppointmentButton.setEnabled(currentUser.hasPermission("can_book_appointment"));
        // The enable/disable for update/cancel/mark as done on selected items is handled by the ListSelectionListener.
        // We set their initial state here (disabled) and let the listener take over when items are selected.
        view.updateAppointmentButton.setEnabled(false);
        view.cancelAppointmentButton.setEnabled(false);
        view.markAsDoneSelectedButton.setEnabled(false);
    }

    private void showAllAppointments() {
        List<Appointment> allAppointments = dao.getAllAppointments();

        if (allAppointments.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "No appointments found in the database.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        AllAppointmentsDialog dialog = new AllAppointmentsDialog(
                mainFrame,
                allAppointments,
                currentUser.hasPermission("can_mark_appointment_done")
        );
        dialog.setVisible(true);
        viewSchedule();
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
        viewSchedule();
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
            viewSchedule();
        }
    }

    // --- NEW METHOD: Mark Selected Appointment as Done ---
    private void markSelectedAppointmentAsDone() {
        if (!currentUser.hasPermission("can_mark_appointment_done")) {
            JOptionPane.showMessageDialog(view, "You do not have permission to mark appointments as done.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Appointment selectedAppointment = view.appointmentsList.getSelectedValue();
        if (selectedAppointment == null) {
            JOptionPane.showMessageDialog(view, "Please select an appointment to mark as done.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Done".equalsIgnoreCase(selectedAppointment.getStatus())) {
            JOptionPane.showMessageDialog(view, "Appointment is already marked as Done.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(view,
                "Mark appointment #" + selectedAppointment.getAppointmentId() + " for Patient " + selectedAppointment.getPatientId() + " as Done?",
                "Confirm Status Change", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            selectedAppointment.setStatus("Done");
            boolean success = dao.updateAppointment(selectedAppointment);

            if (success) {
                JOptionPane.showMessageDialog(view, "Appointment marked as Done successfully.");
                viewSchedule(); // Refresh the current schedule view
            } else {
                JOptionPane.showMessageDialog(view, "Failed to update appointment status.", "Error", JOptionPane.ERROR_MESSAGE);
                selectedAppointment.setStatus("Scheduled"); // Revert status in memory if DB update fails
            }
        }
    }

    private void updateAppointment() {
        if (!currentUser.hasPermission("can_update_appointment")) {
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
            updatedAppointment.setStatus(selectedAppointment.getStatus());

            boolean success = dao.updateAppointment(updatedAppointment);
            if (success) {
                JOptionPane.showMessageDialog(view, "Appointment updated successfully.");
            } else {
                JOptionPane.showMessageDialog(view, "Failed to update appointment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            viewSchedule();
        }
    }
}