package com.globemed.controller;

import com.globemed.appointment.Appointment;
import com.globemed.auth.IUser;
import com.globemed.billing.MedicalBill;
import com.globemed.db.BillingDAO;
import com.globemed.db.PatientDAO;
import com.globemed.db.SchedulingDAO;
import com.globemed.patient.PatientRecord;
import com.globemed.reports.FinancialReportVisitor;
import com.globemed.reports.PatientSummaryReportVisitor;
import com.globemed.reports.ReportVisitor;
import com.globemed.ui.ReportPanel;

import javax.swing.*;
import java.util.List;

public class ReportController {
    private final ReportPanel view;
    private final PatientDAO patientDAO;
    private final SchedulingDAO schedulingDAO;
    private final BillingDAO billingDAO;
    private final JFrame mainFrame; // <-- Ensure this field exists
    private final IUser currentUser;
    private PatientRecord currentPatient;

    // --- THIS IS THE CORRECTED CONSTRUCTOR ---
    public ReportController(ReportPanel view, JFrame mainFrame, IUser currentUser) { // ADDED 'mainFrame' and 'currentUser'
        this.view = view;
        this.mainFrame = mainFrame; // <-- Initialize the new field
        this.currentUser = currentUser; // <-- Initialize the new field
        this.patientDAO = new PatientDAO();
        this.schedulingDAO = new SchedulingDAO();
        this.billingDAO = new BillingDAO();
        initController();
    }

    private void initController() {
        view.findPatientButton.addActionListener(e -> findPatient());
        view.generateReportButton.addActionListener(e -> generateReport());
    }

    private void findPatient() {
        String patientId = view.patientIdField.getText().trim();
        if (patientId.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter a Patient ID.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentPatient = patientDAO.getPatientById(patientId);

        if (currentPatient != null) {
            view.patientFoundLabel.setText("Status: Loaded " + currentPatient.getName());
            view.reportTypeComboBox.setEnabled(true);
            view.generateReportButton.setEnabled(true);
        } else {
            view.patientFoundLabel.setText("Status: Patient not found.");
            view.reportTypeComboBox.setEnabled(false);
            view.generateReportButton.setEnabled(false);
            currentPatient = null;
        }
    }

private void generateReport() {
    if (currentPatient == null) {
        JOptionPane.showMessageDialog(view, "Please find and load a patient first.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // 1. Instantiate the correct visitor
    ReportVisitor visitor;
    String selectedReport = (String) view.reportTypeComboBox.getSelectedItem();
    if ("Financial Report".equals(selectedReport)) {
        visitor = new FinancialReportVisitor();
    } else {
        visitor = new PatientSummaryReportVisitor();
    }

    // 2. Fetch all related data for the patient using the CORRECT DAO methods
    List<Appointment> appointments = schedulingDAO.getAppointmentsByPatientId(currentPatient.getPatientId());
    List<MedicalBill> bills = billingDAO.getBillsByPatientId(currentPatient.getPatientId());
    
    // 3. Visit each element in the object structure
    currentPatient.accept(visitor);
    
    for (Appointment appointment : appointments) {
        appointment.accept(visitor);
    }
    
    for (MedicalBill bill : bills) {
        bill.accept(visitor);
    }
    
    // 4. Get the final report and display it
    view.reportArea.setText(visitor.getReport());
}
}