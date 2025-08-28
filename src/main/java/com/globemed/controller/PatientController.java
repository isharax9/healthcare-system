package com.globemed.controller;

import com.globemed.auth.IUser;
import com.globemed.db.InsuranceDAO;
import com.globemed.db.PatientDAO;
import com.globemed.insurance.InsurancePlan;
import com.globemed.patient.PatientRecord;
import com.globemed.patient.RecordHistory;
import com.globemed.ui.AllPatientsDialog;
import com.globemed.ui.PatientPanel;

import javax.swing.*;
import java.util.List;

public class PatientController {
    private final PatientPanel view;
    private final PatientDAO dao;
    private final InsuranceDAO insuranceDAO;
    private final IUser currentUser;
    private final JFrame mainFrame; // For parenting dialogs
    private PatientRecord currentPatient;
    private RecordHistory recordHistory;
    private boolean isNewPatientMode = false;

    public PatientController(PatientPanel view, JFrame mainFrame, IUser currentUser) {
        this.view = view;
        this.mainFrame = mainFrame;
        this.currentUser = currentUser;
        this.dao = new PatientDAO();
        this.insuranceDAO = new InsuranceDAO();
        initController();
        loadInitialData();
    }

    private void loadInitialData() {
        List<InsurancePlan> plans = insuranceDAO.getAllPlans();
        view.setInsurancePlans(plans);
    }

    private void initController() {
        view.searchButton.addActionListener(e -> searchPatient());
        view.newButton.addActionListener(e -> prepareNewPatient());
        view.editButton.addActionListener(e -> editPatient());
        view.saveButton.addActionListener(e -> savePatient());
        view.deleteButton.addActionListener(e -> deletePatient());
        view.undoButton.addActionListener(e -> undoChanges());
        view.viewAllButton.addActionListener(e -> showAllPatients());
    }

    private void showAllPatients() {
        List<PatientRecord> allPatients = dao.getAllPatients();
        if (allPatients.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "No patients found in the database.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        AllPatientsDialog dialog = new AllPatientsDialog(mainFrame, allPatients);
        dialog.setVisible(true);
    }

    private void searchPatient() {
        isNewPatientMode = false;
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
            view.insurancePlanComboBox.setEnabled(false);
            view.editButton.setEnabled(true);
            view.deleteButton.setEnabled(currentUser.hasPermission("can_delete_patient"));
        } else {
            JOptionPane.showMessageDialog(view, "Patient not found.", "Error", JOptionPane.ERROR_MESSAGE);
            view.clearFields();
            view.editButton.setEnabled(false);
            view.deleteButton.setEnabled(false);
        }
    }

    private void prepareNewPatient() {
        isNewPatientMode = true;
        currentPatient = null;
        recordHistory = null;
        view.clearFields();
        view.setFieldsEditable(true);
        view.setPatientIdEditable(true);
        view.editButton.setEnabled(false);
        view.deleteButton.setEnabled(false);
        view.undoButton.setEnabled(false);
    }

    private void editPatient() {
        recordHistory.save();
        view.setFieldsEditable(true);
    }

    private void savePatient() {
        if (isNewPatientMode && view.getPatientId().getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Patient ID cannot be empty for a new record.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (isNewPatientMode) {
            PatientRecord newPatient = new PatientRecord(view.getPatientId().getText(), view.getPatientName());
            newPatient.setMedicalHistory(view.getMedicalHistory());
            newPatient.setTreatmentPlans(view.getTreatmentPlans());
            newPatient.setInsurancePlan(view.getSelectedInsurancePlan());

            boolean success = dao.createPatient(newPatient);
            if (success) {
                JOptionPane.showMessageDialog(view, "Patient created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                currentPatient = newPatient;
                isNewPatientMode = false;
                view.setFieldsEditable(false);
                view.setPatientIdEditable(false);
                displayPatientData();
            } else {
                JOptionPane.showMessageDialog(view, "Failed to create patient. The ID might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            currentPatient.setName(view.getPatientName());
            currentPatient.setMedicalHistory(view.getMedicalHistory());
            currentPatient.setTreatmentPlans(view.getTreatmentPlans());
            currentPatient.setInsurancePlan(view.getSelectedInsurancePlan());

            boolean success = dao.updatePatient(currentPatient);
            if (success) {
                JOptionPane.showMessageDialog(view, "Patient record updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                view.setFieldsEditable(false);
            } else {
                JOptionPane.showMessageDialog(view, "Failed to update patient record.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deletePatient() {
        if (!currentUser.hasPermission("can_delete_patient")) {
            JOptionPane.showMessageDialog(view, "You do not have permission to delete patient records.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (currentPatient == null) return;

        int response = JOptionPane.showConfirmDialog(view, "Are you sure you want to delete patient " + currentPatient.getName() + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

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
        if (recordHistory != null) {
            recordHistory.undo();
            displayPatientData();
        }
    }

    private void displayPatientData() {
        view.setPatientId(currentPatient.getPatientId());
        view.setPatientName(currentPatient.getName());
        view.setMedicalHistory(currentPatient.getMedicalHistory());
        view.setTreatmentPlans(currentPatient.getTreatmentPlans());
        view.setSelectedInsurancePlan(currentPatient.getInsurancePlan());
    }
}