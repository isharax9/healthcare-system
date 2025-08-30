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
import java.util.Objects; // Import for Objects.equals()

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

        // --- Spinner Change Listeners (for date/time increments) ---
        view.dateSpinner.addChangeListener(e -> {
            viewSchedule(); // Refresh schedule when date changes
        });

        // --- Table Selection Listeners ---
        // MODIFIED: Appointments table selection listener - REMOVED auto-filling behavior
        view.appointmentsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    // REMOVED: populateAppointmentDetailsFields(); - This was causing auto-filling

                    // Only load doctor notes for selected appointment (not patient ID, reason, time)
                    loadDoctorNotesOnly();

                    boolean isSelected = view.appointmentsTable.getSelectedRow() != -1;
                    view.updateAppointmentButton.setEnabled(isSelected && currentUser.hasPermission("can_update_appointment"));

                    // MODIFIED: Cancel appointment permission - allow nurses and others with can_cancel_appointment permission
                    view.cancelAppointmentButton.setEnabled(isSelected && currentUser.hasPermission("can_cancel_appointment"));

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
                    // Re-evaluate 'View Schedule' button for Admins/Nurses if a doctor is selected
                    applyPermissions();
                    // When doctor selection changes, clear current appointments
                    view.setAppointmentsList(List.of());
                    view.clearAppointmentDetailsFields();
                }
            }
        });
    }

    // NEW: Method to load only doctor notes when appointment is selected (no auto-filling)
    private void loadDoctorNotesOnly() {
        Appointment selectedAppointment = view.getSelectedAppointment(currentAppointments);
        if (selectedAppointment != null) {
            // Only load doctor notes, don't fill other fields
            view.setDoctorNotesText(selectedAppointment.getDoctorNotes());

            boolean canEditNotes = currentUser.getDoctorId() != null &&
                    currentUser.getDoctorId().equals(selectedAppointment.getDoctorId()) &&
                    currentUser.hasPermission("can_add_appointment_notes");
            view.setDoctorNotesEditable(canEditNotes);
        } else {
            // Clear only doctor notes
            view.setDoctorNotesText("");
            view.setDoctorNotesEditable(false);
        }
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

        // --- Automatically select a logged-in doctor if applicable ---
        if (currentUser.getDoctorId() != null) {
            selectDoctorRow(currentUser.getDoctorId());
            viewSchedule(); // Automatically show their schedule
        }
    }

    // --- Helper: Selects and highlights a doctor's row in the table ---
    private void selectDoctorRow(String doctorId) {
        for (int i = 0; i < currentDoctors.size(); i++) {
            if (Objects.equals(currentDoctors.get(i).getDoctorId(), doctorId)) {
                view.doctorsTable.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    private void applyPermissions() {
        // --- Appointment related permissions ---
        view.viewScheduleButton.setEnabled(currentUser.getDoctorId() != null || (view.doctorsTable.getSelectedRow() != -1 && currentUser.hasPermission("can_access_appointments")));
        view.viewAllAppointmentsButton.setEnabled(currentUser.hasPermission("can_access_appointments"));
        view.bookAppointmentButton.setEnabled(currentUser.hasPermission("can_book_appointment"));

        // These are managed by selection listener for the appointments table
        view.updateAppointmentButton.setEnabled(false);
        view.cancelAppointmentButton.setEnabled(false);
        view.markAsDoneSelectedButton.setEnabled(false);
        view.setDoctorNotesEditable(false);

        // Doctor CRUD Permissions (Admin Only)
        boolean canManageDoctors = currentUser.hasPermission("can_manage_doctors");
        view.setDoctorCrudFieldsEditable(canManageDoctors);
        view.addDoctorButton.setEnabled(canManageDoctors);
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
            view.newDoctorIdField.setEditable(false);
            applyPermissions();
        } else {
            clearDoctorCrudFields();
            view.newDoctorIdField.setEditable(true);
            applyPermissions();
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
            refreshDoctorList();
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
            refreshDoctorList();
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

    // --- REMOVED: populateAppointmentDetailsFields() method - this was causing auto-filling

    // --- Appointment Viewing and Booking Methods ---
    private void showAllAppointments() {
        String doctorIdToView = null;
        String infoMessage = "";

        if (currentUser.getDoctorId() != null) {
            doctorIdToView = currentUser.getDoctorId();
            infoMessage = "Displaying all your appointments.";
            view.doctorsTable.clearSelection();
            selectDoctorRow(doctorIdToView);
        } else {
            infoMessage = "Displaying all appointments.";
            view.doctorsTable.clearSelection();
        }

        List<Appointment> appointments;
        if (doctorIdToView != null) {
            appointments = dao.getAppointmentsByDoctorId(doctorIdToView);
        } else {
            appointments = dao.getAllAppointments();
        }

        if (appointments.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "No appointments found.", "Information", JOptionPane.INFORMATION_MESSAGE);
            view.setAppointmentsList(List.of());
            return;
        }

        AllAppointmentsDialog dialog = new AllAppointmentsDialog(
                mainFrame,
                appointments,
                currentUser.hasPermission("can_mark_appointment_done")
        );
        dialog.setVisible(true);
    }

    private void viewSchedule() {
        String doctorIdToView = null;
        Doctor selectedDoctor = view.getSelectedDoctor(currentDoctors);

        if (currentUser.getDoctorId() != null) {
            if (selectedDoctor != null && !currentUser.getDoctorId().equals(selectedDoctor.getDoctorId())) {
                JOptionPane.showMessageDialog(mainFrame, "You can't view other doctors' appointments. Please contact a Nurse or Admin.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                view.doctorsTable.clearSelection();
                view.setAppointmentsList(List.of());
                view.clearAppointmentDetailsFields();
                return;
            }
            doctorIdToView = currentUser.getDoctorId();
            selectDoctorRow(doctorIdToView);
        } else {
            if (selectedDoctor == null) {
                JOptionPane.showMessageDialog(view, "Please select a doctor first.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            doctorIdToView = selectedDoctor.getDoctorId();
        }

        Date selectedDate = (Date) view.dateSpinner.getValue();
        LocalDate localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        currentAppointments = dao.getAppointmentsForDoctorOnDate(doctorIdToView, localDate);
        if (currentAppointments.isEmpty()) {
            view.setAppointmentsList(List.of());
        } else {
            view.setAppointmentsList(currentAppointments);
        }
    }

    private void bookNewAppointment() {
        if (!currentUser.hasPermission("can_book_appointment")) {
            JOptionPane.showMessageDialog(mainFrame, "You do not have permission to book appointments.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Doctor selectedDoctor = null;
        if (currentUser.getDoctorId() != null) {
            for (Doctor doc : currentDoctors) {
                if (doc.getDoctorId().equals(currentUser.getDoctorId())) {
                    selectedDoctor = doc;
                    break;
                }
            }
            if (selectedDoctor == null) {
                JOptionPane.showMessageDialog(mainFrame, "Your staff account's doctor ID is not linked to an active doctor profile. Please contact admin.", "Configuration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            selectDoctorRow(currentUser.getDoctorId());
        } else {
            selectedDoctor = view.getSelectedDoctor(currentDoctors);
            if (selectedDoctor == null) {
                JOptionPane.showMessageDialog(view, "Please select a doctor first.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String patientId = view.patientIdField.getText().trim();
        String reason = view.reasonField.getText().trim();
        String doctorNotes = view.getDoctorNotesText();

        if (patientId.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter a Patient ID.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Date selectedDate = (Date) view.dateSpinner.getValue();
        Date selectedTime = (Date) view.timeSpinner.getValue();
        LocalDate datePart = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime timePart = selectedTime.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        LocalDateTime requestedDateTime = LocalDateTime.of(datePart, timePart);

        String resultMessage = scheduler.bookAppointment(patientId, selectedDoctor, requestedDateTime, reason, doctorNotes);
        JOptionPane.showMessageDialog(view, resultMessage, "Booking Status", JOptionPane.INFORMATION_MESSAGE);
        viewSchedule();
        view.clearBookingFormFields(); // MODIFIED: Use new method that only clears booking form
    }

    // MODIFIED: Cancel appointment functionality - now updates status to "Canceled" instead of deleting
    private void cancelAppointment() {
        if (!currentUser.hasPermission("can_cancel_appointment")) {
            JOptionPane.showMessageDialog(mainFrame, "You do not have permission to cancel appointments.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Appointment selectedAppointment = view.getSelectedAppointment(currentAppointments);
        if (selectedAppointment == null) {
            JOptionPane.showMessageDialog(view, "Please select an appointment to cancel.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if appointment is already canceled
        if ("Canceled".equalsIgnoreCase(selectedAppointment.getStatus())) {
            JOptionPane.showMessageDialog(view, "This appointment is already canceled.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // If user is a doctor, they can only cancel their own appointments
        if (currentUser.getDoctorId() != null && !currentUser.getDoctorId().equals(selectedAppointment.getDoctorId())) {
            JOptionPane.showMessageDialog(mainFrame, "You can only cancel your own appointments.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to cancel appointment #" + selectedAppointment.getAppointmentId() +
                        " for Patient " + selectedAppointment.getPatientId() + "?\n" +
                        "This will change the status to 'Canceled'.",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            // Update status to "Canceled" instead of deleting
            selectedAppointment.setStatus("Canceled");
            boolean success = dao.updateAppointment(selectedAppointment);

            if (success) {
                JOptionPane.showMessageDialog(view, "Appointment canceled successfully. Status updated to 'Canceled'.", "Success", JOptionPane.INFORMATION_MESSAGE);
                viewSchedule(); // Refresh the appointments view
                view.clearAppointmentDetailsFields();
            } else {
                JOptionPane.showMessageDialog(view, "Failed to cancel appointment.", "Error", JOptionPane.ERROR_MESSAGE);
                // Revert status change if update failed
                selectedAppointment.setStatus("Scheduled");
            }
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
            selectedAppointment.setDoctorNotes(view.getDoctorNotesText());
            boolean success = dao.updateAppointment(selectedAppointment);

            if (success) {
                JOptionPane.showMessageDialog(view, "Appointment marked as Done successfully.");
                viewSchedule();
                view.clearAppointmentDetailsFields();
            } else {
                JOptionPane.showMessageDialog(view, "Failed to update appointment status.", "Error", JOptionPane.ERROR_MESSAGE);
                selectedAppointment.setStatus("Scheduled");
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

        if (currentUser.getDoctorId() != null && !currentUser.getDoctorId().equals(selectedAppointment.getDoctorId())) {
            JOptionPane.showMessageDialog(mainFrame, "You can only update your own appointments.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newReason = JOptionPane.showInputDialog(view, "Enter new reason for appointment:", selectedAppointment.getReason());

        if (newReason != null && !newReason.trim().isEmpty()) {
            selectedAppointment.setReason(newReason.trim());
            selectedAppointment.setDoctorNotes(view.getDoctorNotesText());

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