package com.globemed.controller;

// Add these imports
import com.globemed.billing.BillProcessingRequest;
import com.globemed.db.InsuranceDAO;
import com.globemed.insurance.InsurancePlan;
import com.globemed.patient.PatientRecord;
import com.globemed.utils.BillPrinter;
import com.globemed.billing.*;
import com.globemed.db.BillingDAO;
import com.globemed.db.PatientDAO;
import com.globemed.ui.BillingPanel;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Vector;

public class BillingController {
    private final BillingPanel view;
    private final BillingDAO billingDAO;
    private final PatientDAO patientDAO;
    private final InsuranceDAO insuranceDAO; // <-- ADDED THIS FIELD
    private final BillingHandler billProcessingChain;
    private List<MedicalBill> currentBills; // To hold the search results

    public BillingController(BillingPanel view) {
        this.view = view;
        this.billingDAO = new BillingDAO();
        this.patientDAO = new PatientDAO();
        this.insuranceDAO = new InsuranceDAO(); // <-- INITIALIZED IT
        this.billProcessingChain = setupChain();
        initController();
    }

    private BillingHandler setupChain() {
        BillingHandler validationHandler = new ValidationHandler();
        BillingHandler insuranceHandler = new InsuranceHandler();
        BillingHandler finalBillingHandler = new FinalBillingHandler();
        validationHandler.setNext(insuranceHandler);
        insuranceHandler.setNext(finalBillingHandler);
        return validationHandler;
    }

    private void initController() {
        view.processBillButton.addActionListener(e -> processBill());
        view.searchBillsButton.addActionListener(e -> searchBills());
        view.deleteBillButton.addActionListener(e -> deleteBill());
        view.printBillButton.addActionListener(e -> printBill());
        view.viewLogButton.addActionListener(e -> viewLog()); // Listener was already here, confirming it stays

        view.billsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = view.billsTable.getSelectedRow() != -1;
            view.deleteBillButton.setEnabled(rowSelected);
            view.printBillButton.setEnabled(rowSelected);
            view.viewLogButton.setEnabled(rowSelected); // Line was already here, confirming it stays
        });
    }

    private void searchBills() {
        String patientId = view.searchPatientIdField.getText().trim();
        if (patientId.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter a Patient ID to search.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentBills = billingDAO.getBillsByPatientId(patientId);

        String[] columnNames = {"Bill ID", "Service", "Original Amount", "Final Amount", "Status"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override // This makes the table cells not editable
            public boolean isCellEditable(int row, int column) { return false; }
        };

        for (MedicalBill bill : currentBills) {
            Vector<Object> row = new Vector<>();
            row.add(bill.getBillId());
            row.add(bill.getServiceDescription());
            row.add(String.format("%.2f", bill.getAmount()));
            row.add(String.format("%.2f", bill.getFinalAmount()));
            row.add(bill.getStatus());
            model.addRow(row);
        }
        view.billsTable.setModel(model);
    }

    private void deleteBill() {
        int selectedRow = view.billsTable.getSelectedRow();
        if (selectedRow == -1) return;

        MedicalBill selectedBill = currentBills.get(selectedRow);
        int choice = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to delete Bill #" + selectedBill.getBillId() + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            boolean success = billingDAO.deleteBill(selectedBill.getBillId());
            if (success) {
                JOptionPane.showMessageDialog(view, "Bill deleted successfully.");
                searchBills(); // Refresh the table
            } else {
                JOptionPane.showMessageDialog(view, "Failed to delete the bill.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- REPLACED the printBill method with this new version ---
    private void printBill() {
        int selectedRow = view.billsTable.getSelectedRow();
        if (selectedRow == -1) return;

        MedicalBill selectedBill = currentBills.get(selectedRow);
        PatientRecord patient = patientDAO.getPatientById(selectedBill.getPatientId());

        if (patient != null) {
            // 1. Fetch the list of all insurance plans
            List<InsurancePlan> allPlans = insuranceDAO.getAllPlans();

            // 2. Pass the list to the printer
            BillPrinter.printBill(selectedBill, patient, allPlans);

            JOptionPane.showMessageDialog(view, "Bill PDF has been generated in the project folder.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(view, "Could not find patient data to generate the bill.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewLog() {
        int selectedRow = view.billsTable.getSelectedRow();
        if (selectedRow == -1) return;

        MedicalBill selectedBillStub = currentBills.get(selectedRow);
        MedicalBill fullBill = billingDAO.getBillById(selectedBillStub.getBillId());

        if (fullBill != null) {
            JTextArea textArea = new JTextArea(20, 50);
            textArea.setText(fullBill.getProcessingLog());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);

            JOptionPane.showMessageDialog(view,
                    scrollPane,
                    "Processing Log for Bill #" + fullBill.getBillId(),
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(view, "Could not retrieve full details for this bill.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processBill() {
        String patientId = view.createPatientIdField.getText().trim();
        PatientRecord patient = patientDAO.getPatientById(patientId);

        if (patient == null) {
            JOptionPane.showMessageDialog(view, "Patient with ID '" + patientId + "' not found.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String service = view.serviceField.getText();
        String amountStr = view.amountField.getText();

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Invalid amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        MedicalBill bill = new MedicalBill(patientId, service, amount);
        BillProcessingRequest request = new BillProcessingRequest(bill, patient);

        billProcessingChain.processBill(request);

        JOptionPane.showMessageDialog(view, "Processing complete.\nFinal Status: " + bill.getStatus(), "Processing Result", JOptionPane.INFORMATION_MESSAGE);

        if (patientId.equals(view.searchPatientIdField.getText().trim())) {
            searchBills();
        }
    }
}