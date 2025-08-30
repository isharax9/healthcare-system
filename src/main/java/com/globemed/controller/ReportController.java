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
import com.globemed.utils.TextReportPrinter; // <-- NEW IMPORT

import javax.swing.*;
import java.util.List;

public class ReportController {
    private final ReportPanel view;
    private final PatientDAO patientDAO;
    private final SchedulingDAO schedulingDAO;
    private final BillingDAO billingDAO;
    private final JFrame mainFrame;
    private final IUser currentUser;
    private PatientRecord currentPatient;
    private String lastGeneratedReportTitle; // <-- NEW: To store title for printing
    private String lastGeneratedReportContent; // <-- NEW: To store content for printing


    public ReportController(ReportPanel view, JFrame mainFrame, IUser currentUser) {
        this.view = view;
        this.mainFrame = mainFrame;
        this.currentUser = currentUser;
        this.patientDAO = new PatientDAO();
        this.schedulingDAO = new SchedulingDAO();
        this.billingDAO = new BillingDAO();
        initController();
    }

    private void initController() {
        view.findPatientButton.addActionListener(e -> findPatient());
        view.generateReportButton.addActionListener(e -> generateReport());
        view.printReportButton.addActionListener(e -> printReport()); // <-- NEW LISTENER
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
            view.printReportButton.setEnabled(false); // Disable print until report is generated
            view.reportArea.setText(""); // Clear previous report
            lastGeneratedReportContent = null; // Clear cached content
            lastGeneratedReportTitle = null; // Clear cached title
        } else {
            view.patientFoundLabel.setText("Status: Patient not found.");
            view.reportTypeComboBox.setEnabled(false);
            view.generateReportButton.setEnabled(false);
            view.printReportButton.setEnabled(false); // Disable print
            view.reportArea.setText("");
            currentPatient = null;
            lastGeneratedReportContent = null;
            lastGeneratedReportTitle = null;
        }
    }

    private void generateReport() {
        if (currentPatient == null) {
            JOptionPane.showMessageDialog(view, "Please find and load a patient first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ReportVisitor visitor;
        String selectedReportType = (String) view.reportTypeComboBox.getSelectedItem();

        if ("Financial Report".equals(selectedReportType)) {
            visitor = new FinancialReportVisitor();
        } else {
            visitor = new PatientSummaryReportVisitor();
        }

        List<Appointment> appointments = schedulingDAO.getAppointmentsByPatientId(currentPatient.getPatientId());
        List<MedicalBill> bills = billingDAO.getBillsByPatientId(currentPatient.getPatientId());

        currentPatient.accept(visitor);

        for (Appointment appointment : appointments) {
            appointment.accept(visitor);
        }

        for (MedicalBill bill : bills) {
            bill.accept(visitor);
        }

        lastGeneratedReportContent = visitor.getReport(); // Store content
        lastGeneratedReportTitle = selectedReportType; // Store title
        view.reportArea.setText(lastGeneratedReportContent);
        view.printReportButton.setEnabled(true); // Enable print button after generation
    }

    // --- NEW: Print Report Method ---
    private void printReport() {
        if (lastGeneratedReportContent == null || currentPatient == null) {
            JOptionPane.showMessageDialog(view, "No report has been generated yet.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Use the TextReportPrinter utility to generate the PDF
        String filename = TextReportPrinter.printTextReport(
                lastGeneratedReportTitle,
                lastGeneratedReportContent,
                currentPatient.getPatientId()
        );

        if (filename != null) {
            JOptionPane.showMessageDialog(view, "Report PDF generated: " + filename, "Print Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(view, "Failed to generate report PDF.", "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}