package com.globemed.controller;

import com.globemed.db.PatientDAO;
import com.globemed.patient.PatientRecord;
import com.globemed.patient.RecordHistory;
import com.globemed.ui.PatientPanel;

import javax.swing.*;
import java.awt.*;

public class PatientController {
    private final PatientPanel view;
    private final PatientDAO dao;
    private PatientRecord currentPatient;
    private RecordHistory recordHistory;

    public PatientController(PatientPanel view) {
        this.view = view;
        this.dao = new PatientDAO();
        initController();
    }

    private void initController() {
        view.searchButton.addActionListener(e -> searchPatient());
        view.editButton.addActionListener(e -> editPatient());
        view.saveButton.addActionListener(e -> savePatient());
        view.undoButton.addActionListener(e -> undoChanges());
    }

    private void searchPatient() {
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


    private void savePatient() {
        // Update the model from the view
        currentPatient.setName(view.getPatientName());

        // Use the new setters to REPLACE the old lists with the new ones from the view
        currentPatient.setMedicalHistory(view.getMedicalHistory());
        currentPatient.setTreatmentPlans(view.getTreatmentPlans());

        // Persist to a database
        boolean success = dao.updatePatient(currentPatient);
        if (success) {
            JOptionPane.showMessageDialog(view, "Patient record updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            view.setFieldsEditable(false);
        } else {
            JOptionPane.showMessageDialog(view, "Failed to update patient record.", "Error", JOptionPane.ERROR_MESSAGE);
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