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
    private List<Doctor> currentDoctors; // Cache for the list of all doctors for table interaction
    private List<Appointment> currentAppointments; // Cache for the currently displayed appointments in the table

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
                    populateAppointmentDetailsFields(); // Populate notes/reason when appointment selected
                    boolean isSelected = view.appointmentsTable.getSelectedRow() != -1;
                    view.updateAppointmentButton.setEnabled(isSelected && currentUser.hasPermission("can_update_appointment"));
                    view.cancelAppointmentButton.setEnabled(isSelected && currentUser.hasPermission("can_delete_appointment"));
                    view.markAsDoneSelectedButton.setEnabled(isSelected && currentUser.hasPermission("can_mark_appointment_done"));

                    // Enable notes editing based on selection and if current user is the doctor for that appointment and has permission
                    Appointment selectedAppt = view.getSelectedAppointment(currentAppointments);
                    boolean canEditNotes = isSelected && selectedAppt != null && currentUser.getDoctorId() != null &&
                            currentUser.getDoctorId().equals(selectedAppt.getDoctorId()) &&
                            currentUser.hasPermission("can_add_appointment_notes");
                    view.setDoctorNotesEditable(canEditNotes);
                }
            }
        });

        // Doctor table selection listener
        view.doctorsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
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
        view.setAppointmentsList(List.of()); // Clear appointments on startup
        view.clearAppointmentDetailsFields(); // Clear notes, patient ID, etc.
    }

    // Helper method to refresh the doctor table and clear CRUD fields
    private void refreshDoctorList() {
        currentDoctors = dao.getAllDoctors();
        view.setDoctorList(currentDoctors);
        view.clearDoctorCrudFields();
        view.doctorsTable.clearSelection(); // Clear doctor table selection as well
        applyPermissions(); // Re-apply permissions after refresh
    }

    private void applyPermissions() {
        // Appointment related permissions
        // Doctors only see their own appointments, others see all via 'View All'
        view.viewScheduleButton.setEnabled(currentUser.getDoctorId() != null); // Only doctor can view their own schedule
        view.viewAllAppointmentsButton.setEnabled(currentUser.hasPermission("can_access_appointments") && currentUser.getDoctorId() == null); // Others can view all, doctor views their own schedule
        view.bookAppointmentButton.setEnabled(currentUser.hasPermission("can_book_appointment"));

        // These are managed by selection listener for the appointments table
        view.updateAppointmentButton.setEnabled(false);
        view.cancelAppointmentButton.setEnabled(false);
        view.markAsDoneSelectedButton.setEnabled(false);
        view.setDoctorNotesEditable(false); // Editable only when an appointment is selected and doctor has permission

        // Doctor CRUD Permissions (Admin Only)
        boolean canManageDoctors = currentUser.hasPermission("can_manage_doctors");
        view.setDoctorCrudFieldsEditable(canManageDoctors);
        view.addDoctorButton.setEnabled(canManageDoctors);

        // Update/Delete buttons depend on both admin permission AND a selected doctor
        view.updateDoctorButton.setEnabled(canManageDoctors && view.doctorsTable.getSelectedRow() != -1);
        view.deleteDoctorButton.setEnabled(canManageDoctors && view.doctorsTable.getSelectedRow() != -1);

        view.clearDoctorFieldsButton.setEnabled(canManageDoctors);
    }

    // --- Doctor CRUD Field Population ---
    private void populateDoctorCrudFields() {
        Doctor selectedDoctor = view.getSelectedDoctor(currentDoctors);
        if (selectedDoctor != null) {
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
            refreshDoctorList(); // Refresh the JList of doctors
        } else {
            JOptionPane.showMessageDialog(view, "Failed to add doctor. ID might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDoctor() {
        if (!currentUser.hasPermission("can_manage_doctors")) {
            JOptionPane.showMessageDialog(mainFrame, "You do not have permission to update doctors.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Doctor selectedDoctor = view.getSelectedDoctor(currentDoctors);
        if (selectedDoctor == null) {
            JOptionPane.showMessageDialog(view, "Please select a doctor to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = view.newDoctorNameField.getText().trim();
        String specialty = view.newDoctorSpecialtyField.getText().trim();

        if (name.isEmpty() || specialty.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please fill in name and specialty fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Doctor updatedDoctor = new Doctor(selectedDoctor.getDoctorId(), name, specialty);
        boolean success = dao.updateDoctor(updatedDoctor);

        if (success) {
            JOptionPane.showMessageDialog(view, "Doctor updated successfully!");
            refreshDoctorList(); // Refresh the JList of doctors
        } else {
            JOptionPane.showMessageDialog(view, "Failed to update doctor.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteDoctor() {
        if (!currentUser.hasPermission("can_manage_doctors")) {
            JOptionPane.showMessageDialog(mainFrame, "You do not have permission to delete doctors.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Doctor selectedDoctor = view.getSelectedDoctor(currentDoctors);
        if (selectedDoctor == null) {
            JOptionPane.showMessageDialog(view, "Please select a doctor to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to delete Doctor " + selectedDoctor.getFullName() + "? This will also delete related appointments if configured via CASCADE DELETE!",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = dao.deleteDoctor(selectedDoctor.getDoctorId());

            if (success) {
                JOptionPane.showMessageDialog(view, "Doctor deleted successfully!");
                refreshDoctorList();
            } else {
                JOptionPane.showMessageDialog(view, "Failed to delete doctor. Ensure no active appointments exist if CASCADE DELETE is not configured in your DB.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearDoctorCrudFields() {
        view.clearDoctorCrudFields();
        view.doctorsTable.clearSelection();
        view.newDoctorIdField.setEditable(true);
        applyPermissions();
    }

    // --- Appointment Details Population ---
    private void populateAppointmentDetailsFields() {
        Appointment selectedAppointment = view.getSelectedAppointment(currentAppointments);
        if (selectedAppointment != null) {
            view.patientIdField.setText(selectedAppointment.getPatientId());
            view.reasonField.setText(selectedAppointment.getReason());
            view.timeSpinner.setValue(
                    Date.from(selectedAppointment.getAppointmentDateTime().atZone(ZoneId.systemDefault()).toInstant()));
            view.setDoctorNotesText(selectedAppointment.getDoctorNotes()); // Populate notes field

            boolean canEditNotes = currentUser.getDoctorId() != null &&
                    currentUser.getDoctorId().equals(selectedAppointment.getDoctorId()) &&
                    currentUser.hasPermission("can_add_appointment_notes");
            view.setDoctorNotesEditable(canEditNotes);

        } else {
            view.clearAppointmentDetailsFields(); // Clear all appointment-related fields
        }
    }


    // --- Appointment Viewing and Booking Methods ---
    private void showAllAppointments() {
        // --- MODIFIED: Restrict 'View All Appointments' for Doctors ---
        if (currentUser.getDoctorId() != null) { // Logged in as a doctor
            JOptionPane.showMessageDialog(mainFrame, "Doctors can only view their own schedule.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

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
    }

    private void viewSchedule() {
        // --- MODIFIED: Enforce "My Appointments Only" for Doctors ---
        String doctorIdToView = null;
        if (currentUser.getDoctorId() != null) { // Logged-in user is a doctor
            doctorIdToView = currentUser.getDoctorId();
            // Clear existing selection in the doctor table, as we are implicitly showing THEIR schedule
            view.doctorsTable.clearSelection();
        } else { // Not a doctor, can select any doctor
            Doctor selectedDoctor = view.getSelectedDoctor(currentDoctors);
            if (selectedDoctor == null) {
                JOptionPane.showMessageDialog(view, "Please select a doctor first.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            doctorIdToView = selectedDoctor.getDoctorId();
        }

        Date selectedDate = (Date) view.dateSpinner.getValue();
        LocalDate localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        currentAppointments = dao.getAppointmentsForDoctorOnDate(doctorIdToView, localDate);
        view.setAppointmentsList(currentAppointments);
    }

    private void bookNewAppointment() {
        if (!currentUser.hasPermission("can_book_appointment")) {
            JOptionPane.showMessageDialog(mainFrame, "You do not have permission to book appointments.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- MODIFIED: Ensure logged-in doctor is auto-selected for booking ---
        Doctor selectedDoctor = null;
        if (currentUser.getDoctorId() != null) {
            // Logged-in user is a doctor, auto-assign this doctor
            for (Doctor doc : currentDoctors) {
                if (doc.getDoctorId().equals(currentUser.getDoctorId())) {
                    selectedDoctor = doc;
                    break;
                }
            }
            if (selectedDoctor == null) {
                JOptionPane.showMessageDialog(view, "Your staff account's doctor ID is not linked to an active doctor profile. Please contact admin.", "Configuration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else { // Not a doctor, must select a doctor from the table
            selectedDoctor = view.getSelectedDoctor(currentDoctors);
            if (selectedDoctor == null) {
                JOptionPane.showMessageDialog(view, "Please select a doctor first.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String patientId = view.patientIdField.getText().trim();
        String reason = view.reasonField.getText().trim();
        String doctorNotes = view.getDoctorNotesText(); // Get initial notes from the form

        if (patientId.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter a Patient ID.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Date selectedDate = (Date) view.dateSpinner.getValue();
        Date selectedTime = (Date) view.timeSpinner.getValue();
        LocalDate datePart = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime timePart = selectedTime.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        LocalDateTime requestedDateTime = LocalDateTime.of(datePart, timePart);

        String resultMessage = scheduler.bookAppointment(patientId, selectedDoctor, requestedDateTime, reason, doctorNotes); // Pass doctorNotes
        JOptionPane.showMessageDialog(view, resultMessage, "Booking Status", JOptionPane.INFORMATION_MESSAGE);
        viewSchedule(); // Refresh view
        view.clearAppointmentDetailsFields(); // Clear fields after successful booking
    }

    private void cancelAppointment() {
        if (!currentUser.hasPermission("can_delete_appointment")) {
            JOptionPane.showMessageDialog(mainFrame, "You do not have permission to cancel appointments.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Appointment selectedAppointment = view.getSelectedAppointment(currentAppointments);
        if (selectedAppointment == null) {
            JOptionPane.showMessageDialog(view, "Please select an appointment to cancel.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- NEW: Doctors can only cancel their own appointments ---
        if (currentUser.getDoctorId() != null && !currentUser.getDoctorId().equals(selectedAppointment.getDoctorId())) {
            JOptionPane.showMessageDialog(mainFrame, "You can only cancel your own appointments.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

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
            view.clearAppointmentDetailsFields(); // Clear fields after action
        }
    }

    private void markSelectedAppointmentAsDone() {
        if (!currentUser.hasPermission("can_mark_appointment_done")) {
            JOptionPane.showMessageDialog(mainFrame, "You do not have permission to mark appointments as done.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Appointment selectedAppointment = view.getSelectedAppointment(currentAppointments);
        if (selectedAppointment == null) {
            JOptionPane.showMessageDialog(view, "Please select an appointment to mark as done.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- NEW: Doctors can only mark their own appointments as done ---
        if (currentUser.getDoctorId() != null && !currentUser.getDoctorId().equals(selectedAppointment.getDoctorId())) {
            JOptionPane.showMessageDialog(mainFrame, "You can only mark your own appointments as done.", "Access Denied", JOptionPane.ERROR_MESSAGE);
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
            selectedAppointment.setDoctorNotes(view.getDoctorNotesText()); // Save notes from the area
            boolean success = dao.updateAppointment(selectedAppointment);

            if (success) {
                JOptionPane.showMessageDialog(view, "Appointment marked as Done successfully.");
                viewSchedule();
                view.clearAppointmentDetailsFields();
            } else {
                JOptionPane.showMessageDialog(view, "Failed to update appointment status.", "Error", JOptionPane.ERROR_MESSAGE);
                selectedAppointment.setStatus("Scheduled"); // Revert status in memory if DB update fails
            }
        }
    }

    private void updateAppointment() {
        if (!currentUser.hasPermission("can_update_appointment")) {
            JOptionPane.showMessageDialog(mainFrame, "You do not have permission to update appointments.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Appointment selectedAppointment = view.getSelectedAppointment(currentAppointments);
        if (selectedAppointment == null) {
            JOptionPane.showMessageDialog(view, "Please select an appointment to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- NEW: Doctors can only update their own appointments ---
        if (currentUser.getDoctorId() != null && !currentUser.getDoctorId().equals(selectedAppointment.getDoctorId())) {
            JOptionPane.showMessageDialog(mainFrame, "You can only update your own appointments.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newReason = JOptionPane.showInputDialog(view, "Enter new reason for appointment:", selectedAppointment.getReason());

        if (newReason != null && !newReason.trim().isEmpty()) {
            selectedAppointment.setReason(newReason.trim()); // Update existing object
            selectedAppointment.setDoctorNotes(view.getDoctorNotesText()); // Save notes with update

            boolean success = dao.updateAppointment(selectedAppointment);
            if (success) {
                JOptionPane.showMessageDialog(view, "Appointment updated successfully.");
            } else {
                JOptionPane.showMessageDialog(view, "Failed to update appointment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            viewSchedule();
            view.clearAppointmentDetailsFields();
        }
    }
}