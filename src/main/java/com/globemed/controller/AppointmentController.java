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
import java.util.Objects;
import java.util.stream.Collectors; // Import for filtering

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
        view.updateAppointmentButton.addActionListener(e -> updateAppointmentReason()); // UPDATED: Now specifically for reason updates
        view.viewAllAppointmentsButton.addActionListener(e -> showAllAppointments());
        view.markAsDoneSelectedButton.addActionListener(e -> markSelectedAppointmentAsDone());
        view.updatePrescriptionButton.addActionListener(e -> updatePrescription()); // NEW: Update Prescription action

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
        // MODIFIED: Appointments table selection listener with smart button state logic
        view.appointmentsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    // Only load doctor notes for selected appointment (not patient ID, reason, time)
                    loadDoctorNotesOnly();

                    // MODIFIED: Smart button state logic based on appointment status
                    updateButtonStatesBasedOnSelection();
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

    // UPDATED: Enhanced button state logic for appointment reason updates
    private void updateButtonStatesBasedOnSelection() {
        boolean isSelected = view.appointmentsTable.getSelectedRow() != -1;
        Appointment selectedAppt = view.getSelectedAppointment(currentAppointments);

        if (!isSelected || selectedAppt == null) {
            // No appointment selected - disable all buttons
            view.updateAppointmentButton.setEnabled(false);
            view.cancelAppointmentButton.setEnabled(false);
            view.markAsDoneSelectedButton.setEnabled(false);
            view.updatePrescriptionButton.setEnabled(false);
            view.setDoctorNotesEditable(false);
            return;
        }

        String status = selectedAppt.getStatus();
        boolean isOwner = currentUser.getDoctorId() != null && currentUser.getDoctorId().equals(selectedAppt.getDoctorId());
        boolean isDoctor = currentUser.getDoctorId() != null;
        boolean isNurseOrAdmin = !isDoctor; // Non-doctors are nurses/admins

        System.out.println("DEBUG - updateButtonStatesBasedOnSelection (Updated 2025-08-30 19:48:09):"); // DEBUG
        System.out.println("  Selected appointment status: " + status); // DEBUG
        System.out.println("  User is owner: " + isOwner); // DEBUG
        System.out.println("  User is doctor: " + isDoctor); // DEBUG
        System.out.println("  User is nurse/admin: " + isNurseOrAdmin); // DEBUG
        System.out.println("  User doctorId: " + currentUser.getDoctorId()); // DEBUG
        System.out.println("  Appointment doctorId: " + selectedAppt.getDoctorId()); // DEBUG

        // UPDATED: Update Appointment Reason - Nurses and Admins can update, but only for "Scheduled" appointments
        boolean canUpdateReason = currentUser.hasPermission("can_update_appointment_reason") &&
                isNurseOrAdmin && // Only nurses/admins (not doctors)
                "Scheduled".equalsIgnoreCase(status); // Only if status is "Scheduled"
        view.updateAppointmentButton.setEnabled(canUpdateReason);

        // Cancel Appointment - NOT available for "Done" appointments, available for others
        boolean canCancel = currentUser.hasPermission("can_cancel_appointment") &&
                !"Done".equalsIgnoreCase(status) &&
                !"Canceled".equalsIgnoreCase(status) &&
                (isOwner || isNurseOrAdmin); // Doctors can cancel own, nurses/admins can cancel any
        view.cancelAppointmentButton.setEnabled(canCancel);

        // Mark as Done - NOT available for "Canceled" appointments, available for others
        boolean canMarkDone = currentUser.hasPermission("can_mark_appointment_done") &&
                !"Canceled".equalsIgnoreCase(status) &&
                !"Done".equalsIgnoreCase(status) &&
                (isOwner || isNurseOrAdmin); // Doctors can mark own, nurses/admins can mark any
        view.markAsDoneSelectedButton.setEnabled(canMarkDone);

        // Update Prescription - only for doctors who own the appointment and have permission
        boolean canUpdatePrescription = currentUser.hasPermission("can_add_appointment_notes") &&
                isOwner && // Must be the doctor who owns the appointment
                isDoctor && // Must be a doctor
                !"Canceled".equalsIgnoreCase(status); // Can't update prescription for canceled appointments
        view.updatePrescriptionButton.setEnabled(canUpdatePrescription);

        // Enable notes editing based on selection and permissions
        boolean canEditNotes = isOwner && currentUser.hasPermission("can_add_appointment_notes");
        view.setDoctorNotesEditable(canEditNotes);

        System.out.println("  canUpdateReason: " + canUpdateReason); // DEBUG
        System.out.println("  canCancel: " + canCancel); // DEBUG
        System.out.println("  canMarkDone: " + canMarkDone); // DEBUG
        System.out.println("  canUpdatePrescription: " + canUpdatePrescription); // DEBUG
    }

    // FIXED: Specific method for updating appointment reason (Nurses and Admins only)
    private void updateAppointmentReason() {
        if (!currentUser.hasPermission("can_update_appointment_reason")) {
            JOptionPane.showMessageDialog(mainFrame,
                    "You do not have permission to update appointment reasons.\nContact system administrator for access.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Appointment selectedAppointment = view.getSelectedAppointment(currentAppointments);
        if (selectedAppointment == null) {
            JOptionPane.showMessageDialog(view,
                    "Please select an appointment to update its reason.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if user is a doctor (doctors should not update appointment reasons)
        if (currentUser.getDoctorId() != null) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Doctors cannot update appointment reasons.\nThis function is available for nurses and administrators only.",
                    "Access Restricted", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check appointment status - only "Scheduled" appointments can be updated
        if (!"Scheduled".equalsIgnoreCase(selectedAppointment.getStatus())) {
            JOptionPane.showMessageDialog(view,
                    "Only scheduled appointments can have their reason updated.\nCurrent status: " + selectedAppointment.getStatus(),
                    "Invalid Status", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create input dialog for reason update
        JTextArea reasonTextArea = new JTextArea(4, 30);
        reasonTextArea.setText(selectedAppointment.getReason());
        reasonTextArea.setLineWrap(true);
        reasonTextArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(reasonTextArea);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Update appointment reason for Patient " + selectedAppointment.getPatientId() + ":"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel infoLabel = new JLabel("<html><i>Appointment ID: " + selectedAppointment.getAppointmentId() +
                " | Doctor: " + selectedAppointment.getDoctorId() +
                " | Date: " + selectedAppointment.getAppointmentDateTime().toLocalDate() + "</i></html>");
        panel.add(infoLabel, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(view, panel,
                "Update Appointment Reason - " + currentUser.getUsername(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newReason = reasonTextArea.getText().trim();

            if (newReason.isEmpty()) {
                JOptionPane.showMessageDialog(view,
                        "Appointment reason cannot be empty.\nPlease enter a valid reason.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Confirm the update
            int confirmResult = JOptionPane.showConfirmDialog(view,
                    "Confirm appointment reason update?\n\n" +
                            "Appointment ID: " + selectedAppointment.getAppointmentId() + "\n" +
                            "Patient: " + selectedAppointment.getPatientId() + "\n" +
                            "Old Reason: " + selectedAppointment.getReason() + "\n" +
                            "New Reason: " + newReason + "\n\n" +
                            "Updated by: " + currentUser.getUsername() + " (2025-08-30 19:48:09 UTC)",
                    "Confirm Reason Update", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirmResult == JOptionPane.YES_OPTION) {
                String oldReason = selectedAppointment.getReason();

                // Use the enhanced method from Appointment class for safer updating
                boolean updateSuccess = selectedAppointment.updateReasonIfAllowed(newReason, currentUser.getUsername());

                if (!updateSuccess) {
                    JOptionPane.showMessageDialog(view,
                            "Failed to update appointment reason.\nAppointment may not be in a valid state for updates.",
                            "Update Validation Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Save to database
                boolean dbSuccess = dao.updateAppointment(selectedAppointment);

                if (dbSuccess) {
                    JOptionPane.showMessageDialog(view,
                            "Appointment reason updated successfully!\n\n" +
                                    "Appointment ID: " + selectedAppointment.getAppointmentId() + "\n" +
                                    "Patient: " + selectedAppointment.getPatientId() + "\n" +
                                    "Old Reason: " + oldReason + "\n" +
                                    "New Reason: " + newReason + "\n" +
                                    "Updated by: " + currentUser.getUsername() + "\n" +
                                    "Timestamp: 2025-08-30 19:48:09 UTC",
                            "Update Successful", JOptionPane.INFORMATION_MESSAGE);

                    // Refresh the schedule to show updated reason
                    viewSchedule();

                    // FIXED: Keep the appointment selected to show the updated reason
                    SwingUtilities.invokeLater(() -> {
                        for (int i = 0; i < currentAppointments.size(); i++) {
                            if (currentAppointments.get(i).getAppointmentId() == selectedAppointment.getAppointmentId()) { // FIXED: Proper comparison
                                view.appointmentsTable.setRowSelectionInterval(i, i);
                                break;
                            }
                        }
                    });

                } else {
                    JOptionPane.showMessageDialog(view,
                            "Failed to save appointment reason to database.\nPlease try again or contact system administrator.",
                            "Database Update Failed", JOptionPane.ERROR_MESSAGE);

                    // Revert the reason change
                    selectedAppointment.setReason(oldReason);
                }
            }
        }
    }

    // NEW: Update Prescription functionality
    private void updatePrescription() {
        if (!currentUser.hasPermission("can_add_appointment_notes")) {
            JOptionPane.showMessageDialog(mainFrame, "You do not have permission to update prescriptions.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Appointment selectedAppointment = view.getSelectedAppointment(currentAppointments);
        if (selectedAppointment == null) {
            JOptionPane.showMessageDialog(view, "Please select an appointment to update prescription.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Only doctors can update their own appointment prescriptions
        if (currentUser.getDoctorId() == null || !currentUser.getDoctorId().equals(selectedAppointment.getDoctorId())) {
            JOptionPane.showMessageDialog(mainFrame, "You can only update prescriptions for your own appointments.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if appointment is canceled
        if ("Canceled".equalsIgnoreCase(selectedAppointment.getStatus())) {
            JOptionPane.showMessageDialog(view, "Cannot update prescription for canceled appointments.", "Invalid Status", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get current prescription/notes from the text area
        String currentNotes = view.getDoctorNotesText();

        // Show input dialog with current notes
        JTextArea textArea = new JTextArea(8, 40);
        textArea.setText(currentNotes);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);

        int result = JOptionPane.showConfirmDialog(view, scrollPane,
                "Update Prescription/Notes for Patient " + selectedAppointment.getPatientId() + " - " + currentUser.getUsername(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newNotes = textArea.getText().trim();

            // Update the appointment with new notes
            selectedAppointment.setDoctorNotes(newNotes);
            selectedAppointment.updateLastModified(currentUser.getUsername());
            boolean success = dao.updateAppointment(selectedAppointment);

            if (success) {
                // Update the UI
                view.setDoctorNotesText(newNotes);
                JOptionPane.showMessageDialog(view,
                        "Prescription/Notes updated successfully by " + currentUser.getUsername() +
                                " at " + LocalDateTime.now(),
                        "Update Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(view, "Failed to update prescription/notes.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Method to load only doctor notes when appointment is selected (no auto-filling)
    private void loadDoctorNotesOnly() {
        Appointment selectedAppointment = view.getSelectedAppointment(currentAppointments);
        if (selectedAppointment != null) {
            // Only load doctor notes, don't fill other fields
            view.setDoctorNotesText(selectedAppointment.getDoctorNotes());
        } else {
            // Clear only doctor notes
            view.setDoctorNotesText("");
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
        view.updatePrescriptionButton.setEnabled(false); // NEW: Initially disabled
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
            JOptionPane.showMessageDialog(view, "Doctor added successfully by " + currentUser.getUsername() + "!");
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
            JOptionPane.showMessageDialog(view, "Doctor updated successfully by " + currentUser.getUsername() + "!");
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
                "Are you sure you want to delete Doctor " + selectedDoctor.getFullName() + "?\n" +
                        "This will also delete related appointments if configured via CASCADE DELETE!\n" +
                        "Action performed by: " + currentUser.getUsername(),
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = dao.deleteDoctor(selectedDoctor.getDoctorId());

            if (success) {
                JOptionPane.showMessageDialog(view, "Doctor deleted successfully by " + currentUser.getUsername() + "!");
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
            // MODIFIED: Filter out "Canceled" appointments for doctors
            appointments = appointments.stream()
                    .filter(apt -> !"Canceled".equalsIgnoreCase(apt.getStatus()))
                    .collect(Collectors.toList());
        } else {
            appointments = dao.getAllAppointments();
            // For nurses/admins, show all appointments including canceled ones
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

        // MODIFIED: Filter out "Canceled" appointments for doctors only
        if (currentUser.getDoctorId() != null) {
            currentAppointments = currentAppointments.stream()
                    .filter(apt -> !"Canceled".equalsIgnoreCase(apt.getStatus()))
                    .collect(Collectors.toList());
            System.out.println("DEBUG - Filtering canceled appointments for doctor. Remaining: " + currentAppointments.size()); // DEBUG
        } else {
            System.out.println("DEBUG - Not filtering appointments for non-doctor user. Total: " + currentAppointments.size()); // DEBUG
        }

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
        JOptionPane.showMessageDialog(view, resultMessage + "\nBooked by: " + currentUser.getUsername(), "Booking Status", JOptionPane.INFORMATION_MESSAGE);
        viewSchedule();
        view.clearBookingFormFields();
    }

    // Cancel appointment functionality - now updates status to "Canceled" instead of deleting
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

        // MODIFIED: Check if appointment is already canceled or done
        if ("Canceled".equalsIgnoreCase(selectedAppointment.getStatus())) {
            JOptionPane.showMessageDialog(view, "This appointment is already canceled.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if ("Done".equalsIgnoreCase(selectedAppointment.getStatus())) {
            JOptionPane.showMessageDialog(view, "Cannot cancel a completed appointment.", "Invalid Status", JOptionPane.WARNING_MESSAGE);
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
                        "This will change the status to 'Canceled'.\n" +
                        "Action by: " + currentUser.getUsername(),
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            // Update status to "Canceled" instead of deleting
            selectedAppointment.setStatus("Canceled");
            selectedAppointment.updateLastModified(currentUser.getUsername());
            boolean success = dao.updateAppointment(selectedAppointment);

            if (success) {
                JOptionPane.showMessageDialog(view,
                        "Appointment canceled successfully by " + currentUser.getUsername() +
                                ". Status updated to 'Canceled' at 2025-08-30 19:48:09 UTC.",
                        "Cancellation Successful", JOptionPane.INFORMATION_MESSAGE);
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

        // MODIFIED: Check if appointment is already done or canceled
        if ("Done".equalsIgnoreCase(selectedAppointment.getStatus())) {
            JOptionPane.showMessageDialog(view, "Appointment is already marked as Done.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if ("Canceled".equalsIgnoreCase(selectedAppointment.getStatus())) {
            JOptionPane.showMessageDialog(view, "Cannot mark a canceled appointment as done.", "Invalid Status", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(view,
                "Mark appointment #" + selectedAppointment.getAppointmentId() +
                        " for Patient " + selectedAppointment.getPatientId() + " as Done?\n" +
                        "Action by: " + currentUser.getUsername(),
                "Confirm Status Change", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            selectedAppointment.setStatus("Done");
            selectedAppointment.setDoctorNotes(view.getDoctorNotesText());
            selectedAppointment.updateLastModified(currentUser.getUsername());
            boolean success = dao.updateAppointment(selectedAppointment);

            if (success) {
                JOptionPane.showMessageDialog(view,
                        "Appointment marked as Done successfully by " + currentUser.getUsername() +
                                " at 2025-08-30 19:48:09 UTC.");
                viewSchedule();
                view.clearAppointmentDetailsFields();
            } else {
                JOptionPane.showMessageDialog(view, "Failed to update appointment status.", "Error", JOptionPane.ERROR_MESSAGE);
                selectedAppointment.setStatus("Scheduled");
            }
        }
    }
}