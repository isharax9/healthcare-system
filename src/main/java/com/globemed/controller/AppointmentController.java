package com.globemed.controller;

import com.globemed.appointment.Appointment;
import com.globemed.appointment.AppointmentScheduler;
import com.globemed.appointment.Doctor;
import com.globemed.auth.IUser;
import com.globemed.db.SchedulingDAO;
import com.globemed.ui.AllAppointmentsDialog;
import com.globemed.ui.AppointmentPanel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
    private final JFrame mainFrame; // For parenting dialogs
    private List<Doctor> currentDoctors; // Keep reference to current doctors list
    private List<Appointment> currentAppointments; // Keep reference to current appointments list

    public AppointmentController(AppointmentPanel view, JFrame mainFrame, IUser currentUser) {
        this.view = view;
        this.mainFrame = mainFrame;
        this.dao = new SchedulingDAO();
        this.scheduler = new AppointmentScheduler();
        this.currentUser = currentUser;
        initController();
        loadInitialData();
        applyPermissions(); // Apply permissions on initial load
    }

    private void initController() {
        // --- Schedule Actions ---
        view.viewScheduleButton.addActionListener(e -> viewSchedule());
        view.bookAppointmentButton.addActionListener(e -> bookNewAppointment());
        view.cancelAppointmentButton.addActionListener(e -> cancelAppointment());
        view.updateAppointmentButton.addActionListener(e -> updateAppointment());
        view.viewAllAppointmentsButton.addActionListener(e -> showAllAppointments());
        view.markAsDoneSelectedButton.addActionListener(e -> markSelectedAppointmentAsDone());

        // --- Doctor CRUD Actions ---
        view.addDoctorButton.addActionListener(e -> addDoctor());
        view.updateDoctorButton.addActionListener(e -> updateDoctor());
        view.deleteDoctorButton.addActionListener(e -> deleteDoctor());
        view.clearDoctorFieldsButton.addActionListener(e -> clearDoctorCrudFields());

        // --- Table Selection Listeners ---
        // Appointments table selection listener
        view.appointmentsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    boolean isSelected = view.appointmentsTable.getSelectedRow() != -1;
                    view.updateAppointmentButton.setEnabled(isSelected && currentUser.hasPermission("can_update_appointment"));
                    view.cancelAppointmentButton.setEnabled(isSelected && currentUser.hasPermission("can_delete_appointment"));
                    view.markAsDoneSelectedButton.setEnabled(isSelected && currentUser.hasPermission("can_mark_appointment_done"));
                }
            }
        });

        // Doctor table selection listener
        view.doctorTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    populateDoctorCrudFields();
                }
            }
        });
    }

    private void loadInitialData() {
        refreshDoctorList(); // Load doctors initially
    }

    // Helper method to refresh the doctor list and clear CRUD fields
    private void refreshDoctorList() {
        currentDoctors = dao.getAllDoctors();
        view.setDoctorList(currentDoctors);
        view.clearDoctorCrudFields(); // Clear CRUD fields after refresh
        view.doctorTable.clearSelection(); // Clear doctor table selection as well
        applyPermissions(); // Re-apply permissions here because the list model changed
    }

    private void applyPermissions() {
        // Appointment related permissions
        view.viewAllAppointmentsButton.setEnabled(currentUser.hasPermission("can_access_appointments"));
        view.bookAppointmentButton.setEnabled(currentUser.hasPermission("can_book_appointment"));

        // These are managed by selection listener for the appointments table
        view.updateAppointmentButton.setEnabled(false);
        view.cancelAppointmentButton.setEnabled(false);
        view.markAsDoneSelectedButton.setEnabled(false);

        // Doctor CRUD Permissions (Admin Only)
        boolean canManageDoctors = currentUser.hasPermission("can_manage_doctors");
        view.setDoctorCrudFieldsEditable(canManageDoctors);
        view.addDoctorButton.setEnabled(canManageDoctors);

        // Update/Delete buttons depend on both admin permission AND a selected doctor
        view.updateDoctorButton.setEnabled(canManageDoctors && view.doctorTable.getSelectedRow() != -1);
        view.deleteDoctorButton.setEnabled(canManageDoctors && view.doctorTable.getSelectedRow() != -1);

        view.clearDoctorFieldsButton.setEnabled(canManageDoctors);
    }

    // --- Doctor CRUD Field Population ---
    private void populateDoctorCrudFields() {
        int selectedRow = view.doctorTable.getSelectedRow();
        if (selectedRow != -1 && currentDoctors != null && selectedRow < currentDoctors.size()) {
            Doctor selectedDoctor = currentDoctors.get(selectedRow);
            view.newDoctorIdField.setText(selectedDoctor.getDoctorId());
            view.newDoctorNameField.setText(selectedDoctor.getFullName());
            view.newDoctorSpecialtyField.setText(selectedDoctor.getSpecialty());
            view.newDoctorIdField.setEditable(false); // ID is not editable once a doctor is selected for update/delete
            applyPermissions(); // Re-apply to enable/disable update/delete buttons correctly
        } else {
            // If selection is cleared, prepare for new doctor entry
            clearDoctorCrudFields();
            view.newDoctorIdField.setEditable(true); // ID becomes editable for new entries
            applyPermissions(); // Re-apply to enable Add/disable Update/Delete
        }
    }

    // --- Doctor CRUD Methods ---
    private void addDoctor() {
        if (!currentUser.hasPermission("can_manage_doctors")) {
            JOptionPane.showMessageDialog(mainFrame, "You do not have permission to add doctors.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String id = view.newDoctorIdField.getText().trim();
        String name = view.newDoctorNameField.getText().trim();
        String specialty = view.newDoctorSpecialtyField.getText().trim();

        if (id.isEmpty() || name.isEmpty() || specialty.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please fill in all doctor fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Doctor newDoctor = new Doctor(id, name, specialty);
        boolean success = dao.createDoctor(newDoctor);

        if (success) {
            JOptionPane.showMessageDialog(view, "Doctor added successfully!");
            refreshDoctorList(); // Refresh the doctor table
        } else {
            JOptionPane.showMessageDialog(view, "Failed to add doctor. ID might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDoctor() {
        if (!currentUser.hasPermission("can_manage_doctors")) {
            JOptionPane.showMessageDialog(mainFrame, "You do not have permission to update doctors.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = view.doctorTable.getSelectedRow();
        if (selectedRow == -1 || currentDoctors == null || selectedRow >= currentDoctors.size()) {
            JOptionPane.showMessageDialog(view, "Please select a doctor to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Doctor selectedDoctor = currentDoctors.get(selectedRow);
        String name = view.newDoctorNameField.getText().trim();
        String specialty = view.newDoctorSpecialtyField.getText().trim();

        if (name.isEmpty() || specialty.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please fill in name and specialty fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create a new Doctor object with the updated details (ID remains the same)
        Doctor updatedDoctor = new Doctor(selectedDoctor.getDoctorId(), name, specialty);
        boolean success = dao.updateDoctor(updatedDoctor);

        if (success) {
            JOptionPane.showMessageDialog(view, "Doctor updated successfully!");
            refreshDoctorList(); // Refresh the doctor table
        } else {
            JOptionPane.showMessageDialog(view, "Failed to update doctor.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteDoctor() {
        if (!currentUser.hasPermission("can_manage_doctors")) {
            JOptionPane.showMessageDialog(mainFrame, "You do not have permission to delete doctors.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = view.doctorTable.getSelectedRow();
        if (selectedRow == -1 || currentDoctors == null || selectedRow >= currentDoctors.size()) {
            JOptionPane.showMessageDialog(view, "Please select a doctor to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Doctor selectedDoctor = currentDoctors.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to delete Doctor " + selectedDoctor.getFullName() + "? This will also delete related appointments if configured via CASCADE DELETE!",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = dao.deleteDoctor(selectedDoctor.getDoctorId());

            if (success) {
                JOptionPane.showMessageDialog(view, "Doctor deleted successfully!");
                refreshDoctorList(); // Refresh the doctor table
            } else {
                JOptionPane.showMessageDialog(view, "Failed to delete doctor. Ensure no active appointments exist if CASCADE DELETE is not configured in your DB.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearDoctorCrudFields() {
        view.clearDoctorCrudFields();
        view.doctorTable.clearSelection(); // Clear selection in the doctor table
        view.newDoctorIdField.setEditable(true); // Make ID editable for new entries
        applyPermissions(); // Re-apply to enable Add/disable Update/Delete
    }

    // --- Appointment Viewing and Booking Methods ---
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
        // No call to viewSchedule() here, as per previous bug fix.
    }

    private void viewSchedule() {
        int selectedRow = view.doctorTable.getSelectedRow();
        if (selectedRow == -1 || currentDoctors == null || selectedRow >= currentDoctors.size()) {
            JOptionPane.showMessageDialog(view, "Please select a doctor first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Doctor selectedDoctor = currentDoctors.get(selectedRow);
        Date selectedDate = (Date) view.dateSpinner.getValue();
        LocalDate localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        currentAppointments = dao.getAppointmentsForDoctorOnDate(selectedDoctor.getDoctorId(), localDate);
        view.setAppointmentsList(currentAppointments);
    }

    private void bookNewAppointment() {
        int selectedRow = view.doctorTable.getSelectedRow();
        if (selectedRow == -1 || currentDoctors == null || selectedRow >= currentDoctors.size()) {
            JOptionPane.showMessageDialog(view, "Please select a doctor first.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Doctor selectedDoctor = currentDoctors.get(selectedRow);
        String patientId = view.patientIdField.getText().trim();
        String reason = view.reasonField.getText().trim();

        if (patientId.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter a Patient ID.", "Validation Error", JOptionPane.ERROR_MESSAGE);
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

        int selectedRow = view.appointmentsTable.getSelectedRow();
        if (selectedRow == -1 || currentAppointments == null || selectedRow >= currentAppointments.size()) {
            JOptionPane.showMessageDialog(view, "Please select an appointment to cancel.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Appointment selectedAppointment = currentAppointments.get(selectedRow);
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

    private void markSelectedAppointmentAsDone() {
        if (!currentUser.hasPermission("can_mark_appointment_done")) {
            JOptionPane.showMessageDialog(view, "You do not have permission to mark appointments as done.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = view.appointmentsTable.getSelectedRow();
        if (selectedRow == -1 || currentAppointments == null || selectedRow >= currentAppointments.size()) {
            JOptionPane.showMessageDialog(view, "Please select an appointment to mark as done.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Appointment selectedAppointment = currentAppointments.get(selectedRow);

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

        int selectedRow = view.appointmentsTable.getSelectedRow();
        if (selectedRow == -1 || currentAppointments == null || selectedRow >= currentAppointments.size()) {
            JOptionPane.showMessageDialog(view, "Please select an appointment to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Appointment selectedAppointment = currentAppointments.get(selectedRow);
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