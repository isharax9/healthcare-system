package com.globemed.controller;

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
    private final BillingHandler billProcessingChain;
    private List<MedicalBill> currentBills; // To hold the search results

    public BillingController(BillingPanel view) {
        this.view = view;
        this.billingDAO = new BillingDAO();
        this.patientDAO = new PatientDAO();
        this.billProcessingChain = setupChain();
        initController();
    }

    private BillingHandler setupChain() {
        // ... (this method is unchanged)
        BillingHandler validationHandler = new ValidationHandler();
        BillingHandler insuranceHandler = new InsuranceHandler();
        BillingHandler finalBillingHandler = new FinalBillingHandler();
        validationHandler.setNext(insuranceHandler);
        insuranceHandler.setNext(finalBillingHandler);
        return validationHandler;
    }

    private void initController() {
        // Wire up all the buttons
        view.processBillButton.addActionListener(e -> processBill());
        view.searchBillsButton.addActionListener(e -> searchBills());
        view.deleteBillButton.addActionListener(e -> deleteBill());
        view.viewLogButton.addActionListener(e -> viewLog());

        // Add a listener to the table to enable/disable buttons
        view.billsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = view.billsTable.getSelectedRow() != -1;
            view.deleteBillButton.setEnabled(rowSelected);
            view.viewLogButton.setEnabled(rowSelected);
        });
    }

    private void searchBills() {
        String patientId = view.searchPatientIdField.getText().trim();
        if (patientId.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter a Patient ID to search.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentBills = billingDAO.getBillsByPatientId(patientId);

        // Populate the JTable
        String[] columnNames = {"Bill ID", "Service", "Amount", "Status"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override // This makes the table cells not editable
            public boolean isCellEditable(int row, int column) { return false; }
        };

        for (MedicalBill bill : currentBills) {
            Vector<Object> row = new Vector<>();
            row.add(bill.getBillId());
            row.add(bill.getServiceDescription());
            row.add(String.format("%.2f", bill.getAmount()));
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

    private void viewLog() {
        int selectedRow = view.billsTable.getSelectedRow();
        if (selectedRow == -1) return;

        // This is a simple implementation. In a real app, you might re-fetch the
        // full object from the DB to get the most up-to-date log.
        MedicalBill selectedBill = currentBills.get(selectedRow);

        // The log isn't currently loaded in searchBills, so we must refetch.
        // Let's add a getBillById method to the DAO.
        // For now, we'll just show a message.
        JOptionPane.showMessageDialog(view,
                "Log viewing feature requires fetching full bill details.\n" +
                        "Status: " + selectedBill.getStatus(),
                "Processing Info for Bill #" + selectedBill.getBillId(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void processBill() {
        // Note the change to use the 'createPatientIdField'
        String patientId = view.createPatientIdField.getText().trim();
        if (patientDAO.getPatientById(patientId) == null) {
            JOptionPane.showMessageDialog(view, "Patient with ID '" + patientId + "' not found.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String service = view.serviceField.getText();
        String amountStr = view.amountField.getText();
        String insurance = view.insuranceField.getText();

        double amount;
        try { amount = Double.parseDouble(amountStr); }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Invalid amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        MedicalBill bill = new MedicalBill(patientId, service, amount, insurance);
        billProcessingChain.processBill(bill);

        JOptionPane.showMessageDialog(view, "Processing complete.\nFinal Status: " + bill.getStatus(), "Processing Result", JOptionPane.INFORMATION_MESSAGE);

        // If the new bill is for the patient we are currently viewing, refresh the list
        if (patientId.equals(view.searchPatientIdField.getText().trim())) {
            searchBills();
        }
    }
}