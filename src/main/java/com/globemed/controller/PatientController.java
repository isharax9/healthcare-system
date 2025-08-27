package com.globemed.controller;

import com.globemed.db.PatientDAO;
import com.globemed.patient.PatientRecord;
import com.globemed.patient.RecordHistory;
import com.globemed.ui.PatientPanel;

import javax.swing.*;

public class PatientController {
    private final PatientPanel view;
    private final PatientDAO dao;
    private PatientRecord currentPatient;
    private RecordHistory recordHistory;
    private boolean isNewPatientMode = false;

    public PatientController(PatientPanel view) {
        this.view = view;
        this.dao = new PatientDAO();
        initController();
    }

    private void initController() {
        view.searchButton.addActionListener(e -> searchPatient());
        view.newButton.addActionListener(e -> prepareNewPatient());
        view.editButton.addActionListener(e -> editPatient());
        view.saveButton.addActionListener(e -> savePatient());
        view.deleteButton.addActionListener(e -> deletePatient());
        view.undoButton.addActionListener(e -> undoChanges());
    }

    private void searchPatient() {
        isNewPatientMode = false; // Important: Exit "new patient" mode on search
        String patientId = view.getSearchId();
        if (patientId == null || patientId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter a Patient ID.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        currentPatient = dao.getPatientById(patientId);

        if (currentPatient != null) {
            recordHistory = new RecordHistory(currentPatient);
            displayPatientData();
            view.setFieldsEditable(false);
        } else {
            JOptionPane.showMessageDialog(view, "Patient not found.", "Error", JOptionPane.ERROR_MESSAGE);
            view.clearFields();
            view.editButton.setEnabled(false);
        }
    }

    private void editPatient() {
        // Save the state *before* any edits are made
        recordHistory.save();
        view.setFieldsEditable(true);
    }


    // Replace the old savePatient method with this one

    private void savePatient() {
        // Validate that Patient ID is not empty for new records
        if (isNewPatientMode && view.getPatientId().getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Patient ID cannot be empty for a new record.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (isNewPatientMode) {
            // --- CREATE LOGIC ---
            PatientRecord newPatient = new PatientRecord(view.getPatientId().getText(), view.getPatientName());
            newPatient.setMedicalHistory(view.getMedicalHistory());
            newPatient.setTreatmentPlans(view.getTreatmentPlans());

            boolean success = dao.createPatient(newPatient);
            if (success) {
                JOptionPane.showMessageDialog(view, "Patient created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                currentPatient = newPatient;
                isNewPatientMode = false;
                view.setFieldsEditable(false);
                view.setPatientIdEditable(false);
                displayPatientData(); // Display the newly created patient
            } else {
                JOptionPane.showMessageDialog(view, "Failed to create patient. The ID might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // --- UPDATE LOGIC (existing logic) ---
            currentPatient.setName(view.getPatientName());
            currentPatient.setMedicalHistory(view.getMedicalHistory());
            currentPatient.setTreatmentPlans(view.getTreatmentPlans());

            boolean success = dao.updatePatient(currentPatient);
            if (success) {
                JOptionPane.showMessageDialog(view, "Patient record updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                view.setFieldsEditable(false);
            } else {
                JOptionPane.showMessageDialog(view, "Failed to update patient record.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Add these new methods inside PatientController.java

    private void prepareNewPatient() {
        isNewPatientMode = true;
        currentPatient = null;
        recordHistory = null;

        view.clearFields();
        view.setFieldsEditable(true);
        view.setPatientIdEditable(true); // Allow user to enter a new ID
        view.editButton.setEnabled(false);
        view.deleteButton.setEnabled(false);
        view.undoButton.setEnabled(false);
    }

    private void deletePatient() {
        if (currentPatient == null) return; // Nothing to delete

        int response = JOptionPane.showConfirmDialog(
                view,
                "Are you sure you want to delete patient " + currentPatient.getName() + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION) {
            boolean success = dao.deletePatient(currentPatient.getPatientId());
            if (success) {
                JOptionPane.showMessageDialog(view, "Patient deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                view.clearFields();
                view.setFieldsEditable(false);
            } else {
                JOptionPane.showMessageDialog(view, "Failed to delete patient.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void undoChanges() {
        recordHistory.undo();
        displayPatientData(); // Refresh the view with the restored data
    }

    private void displayPatientData() {
        view.setPatientId(currentPatient.getPatientId());
        view.setPatientName(currentPatient.getName());
        view.setMedicalHistory(currentPatient.getMedicalHistory());
        view.setTreatmentPlans(currentPatient.getTreatmentPlans());
    }

}