package com.globemed.controller;

import com.globemed.billing.BillProcessingRequest;
import com.globemed.billing.BillingHandler;
import com.globemed.billing.FinalBillingHandler;
import com.globemed.billing.InsuranceHandler;
import com.globemed.billing.MedicalBill;
import com.globemed.billing.ValidationHandler;
import com.globemed.db.BillingDAO;
import com.globemed.db.InsuranceDAO;
import com.globemed.db.PatientDAO;
import com.globemed.insurance.InsurancePlan;
import com.globemed.patient.PatientRecord;
import com.globemed.ui.BillingPanel;
import com.globemed.utils.BillPrinter;
import com.globemed.auth.IUser;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BillingController {
    private final BillingPanel view;
    private final BillingDAO billingDAO;
    private final PatientDAO patientDAO;
    private final InsuranceDAO insuranceDAO;
    private final BillingHandler billProcessingChain;
    private final IUser currentUser;
    private final JFrame mainFrame;
    private List<MedicalBill> currentBills; // To hold the search results

    public BillingController(BillingPanel view, JFrame mainFrame, IUser currentUser) {
        this.view = view;
        this.mainFrame = mainFrame;
        this.billingDAO = new BillingDAO();
        this.patientDAO = new PatientDAO();
        this.insuranceDAO = new InsuranceDAO();
        this.currentUser = currentUser;
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
        view.viewLogButton.addActionListener(e -> viewLog());
        view.payNowButton.addActionListener(e -> {
            System.out.println("Pay Now button clicked!"); // DEBUG
            payNow();
        });

        view.billsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
    }

    private void updateButtonStates() {
        boolean rowSelected = view.billsTable.getSelectedRow() != -1;
        MedicalBill selectedBill = null;

        System.out.println("updateButtonStates - rowSelected: " + rowSelected); // DEBUG

        if (currentBills != null && !currentBills.isEmpty() && rowSelected) {
            selectedBill = view.getSelectedBillFromTable(currentBills);
            System.out.println("Selected bill: " + (selectedBill != null ? selectedBill.getBillId() : "null")); // DEBUG
        }

        // Update button states
        view.deleteBillButton.setEnabled(rowSelected && selectedBill != null && currentUser.hasPermission("can_delete_bill"));
        view.printBillButton.setEnabled(rowSelected && selectedBill != null);
        view.viewLogButton.setEnabled(rowSelected && selectedBill != null);

        // Debug Pay Now button enabling logic with CORRECT calculation
        boolean hasPermission = currentUser.hasPermission("can_process_payments");
        boolean hasBalance = false;

        if (selectedBill != null) {
            // FIXED: Use correct remaining balance calculation
            double correctRemainingBalance = (selectedBill.getAmount() - selectedBill.getInsurancePaidAmount()) - selectedBill.getAmountPaid();
            hasBalance = correctRemainingBalance > 0.01;

            System.out.println("Pay Now button logic:"); // DEBUG
            System.out.println("  rowSelected: " + rowSelected); // DEBUG
            System.out.println("  selectedBill != null: " + true); // DEBUG
            System.out.println("  hasPermission (can_process_payments): " + hasPermission); // DEBUG
            System.out.println("  correctRemainingBalance: " + correctRemainingBalance); // DEBUG
            System.out.println("  hasBalance > 0.01: " + hasBalance); // DEBUG
        }

        boolean canPay = rowSelected && selectedBill != null && hasBalance && hasPermission;
        System.out.println("  Final canPay: " + canPay); // DEBUG

        view.payNowButton.setEnabled(canPay);
    }

    private void searchBills() {
        String patientId = view.searchPatientIdField.getText().trim();
        if (patientId.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter a Patient ID to search.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentBills = billingDAO.getBillsByPatientId(patientId);
        System.out.println("Found " + currentBills.size() + " bills for patient " + patientId); // DEBUG

        // ADDED: Fix insurance amounts for existing bills that don't have it calculated
        for (MedicalBill bill : currentBills) {
            if (bill.getInsurancePaidAmount() == 0.0 && bill.getAmount() > bill.getFinalAmount()) {
                // Calculate missing insurance payment
                double insurancePayment = bill.getAmount() - bill.getFinalAmount();
                bill.setInsurancePaidAmount(insurancePayment);
                System.out.println("Fixed insurance amount for bill " + bill.getBillId() + ": $" + insurancePayment);

                // Update in database
                billingDAO.updateAmountPaidAndStatus(
                        bill.getBillId(),
                        bill.getAmountPaid(),
                        insurancePayment,
                        bill.getStatus()
                );
            }
        }

        // Use the new method from BillingPanel to set the data
        view.setBillsTableData(currentBills);

        if (currentBills.isEmpty()) {
            JOptionPane.showMessageDialog(view, "No bills found for Patient ID: " + patientId, "Search Results", JOptionPane.INFORMATION_MESSAGE);
        }

        // Force button state update after search
        updateButtonStates();
    }

    private void deleteBill() {
        if (!currentUser.hasPermission("can_delete_bill")) {
            JOptionPane.showMessageDialog(mainFrame, "You do not have permission to delete bills.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        MedicalBill selectedBill = view.getSelectedBillFromTable(currentBills);
        if (selectedBill == null) {
            JOptionPane.showMessageDialog(view, "Please select a bill to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to delete Bill #" + selectedBill.getBillId() + "?\n" +
                        "Service: " + selectedBill.getServiceDescription() + "\n" +
                        "Amount: $" + String.format("%.2f", selectedBill.getFinalAmount()),
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

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

    private void printBill() {
        MedicalBill selectedBill = view.getSelectedBillFromTable(currentBills);
        if (selectedBill == null) {
            JOptionPane.showMessageDialog(view, "Please select a bill to print.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PatientRecord patient = patientDAO.getPatientById(selectedBill.getPatientId());

        if (patient != null) {
            List<InsurancePlan> allPlans = insuranceDAO.getAllPlans();
            BillPrinter.printBill(selectedBill, patient, allPlans);
            JOptionPane.showMessageDialog(view, "Bill PDF has been generated in the project folder.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(view, "Could not find patient data to generate the bill.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewLog() {
        MedicalBill selectedBill = view.getSelectedBillFromTable(currentBills);
        if (selectedBill == null) {
            JOptionPane.showMessageDialog(view, "Please select a bill to view its log.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the full bill details from database to ensure we have the complete log
        MedicalBill fullBill = billingDAO.getBillById(selectedBill.getBillId());

        if (fullBill != null) {
            JTextArea textArea = new JTextArea(20, 50);
            textArea.setText(fullBill.getProcessingLog());
            textArea.setEditable(false);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            JScrollPane scrollPane = new JScrollPane(textArea);

            JOptionPane.showMessageDialog(view,
                    scrollPane,
                    "Processing Log for Bill #" + fullBill.getBillId() + " - " + fullBill.getServiceDescription(),
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(view, "Could not retrieve full details for this bill.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Handle payment processing - FIXED VERSION
    private void payNow() {
        System.out.println("payNow() method called"); // DEBUG

        MedicalBill selectedBill = view.getSelectedBillFromTable(currentBills);
        if (selectedBill == null) {
            System.out.println("No bill selected"); // DEBUG
            JOptionPane.showMessageDialog(view, "Please select a bill to make a payment.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        System.out.println("Selected bill ID: " + selectedBill.getBillId()); // DEBUG

        if (!currentUser.hasPermission("can_process_payments")) {
            System.out.println("No permission to process payments"); // DEBUG
            JOptionPane.showMessageDialog(mainFrame, "You do not have permission to process payments.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // FIXED: Calculate correct amounts instead of using wrong database values
        double originalAmount = selectedBill.getAmount();
        double insurancePaid = selectedBill.getInsurancePaidAmount();
        double patientPaid = selectedBill.getAmountPaid();
        double correctFinalAmount = originalAmount - insurancePaid;  // What patient should owe
        double correctRemainingBalance = correctFinalAmount - patientPaid;  // What patient still owes

        System.out.println("Payment calculation:"); // DEBUG
        System.out.println("  originalAmount: " + originalAmount);
        System.out.println("  insurancePaid: " + insurancePaid);
        System.out.println("  patientPaid: " + patientPaid);
        System.out.println("  correctFinalAmount: " + correctFinalAmount);
        System.out.println("  correctRemainingBalance: " + correctRemainingBalance);

        if (correctRemainingBalance <= 0.01) {
            JOptionPane.showMessageDialog(view, "This bill is already fully paid.", "Payment Status", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // FIXED: Show payment dialog with correct values
        String paymentAmountStr = JOptionPane.showInputDialog(view,
                "Bill #" + selectedBill.getBillId() + " - " + selectedBill.getServiceDescription() + "\n" +
                        "Original Amount: $" + String.format("%.2f", originalAmount) + "\n" +
                        "Insurance Paid: $" + String.format("%.2f", insurancePaid) + "\n" +
                        "Final Amount (Patient Owes): $" + String.format("%.2f", correctFinalAmount) + "\n" +
                        "Patient Paid: $" + String.format("%.2f", patientPaid) + "\n" +
                        "Remaining Balance: $" + String.format("%.2f", correctRemainingBalance) + "\n\n" +
                        "Enter payment amount:",
                "Process Payment",
                JOptionPane.QUESTION_MESSAGE);

        if (paymentAmountStr != null && !paymentAmountStr.trim().isEmpty()) {
            try {
                double paymentAmount = Double.parseDouble(paymentAmountStr.trim());

                if (paymentAmount <= 0) {
                    JOptionPane.showMessageDialog(view, "Payment amount must be greater than 0.", "Invalid Amount", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (paymentAmount > correctRemainingBalance) {
                    int choice = JOptionPane.showConfirmDialog(view,
                            "Payment amount ($" + String.format("%.2f", paymentAmount) + ") exceeds remaining balance ($" + String.format("%.2f", correctRemainingBalance) + ").\n" +
                                    "This will result in an overpayment. Continue?",
                            "Overpayment Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                    if (choice != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                // Process the payment (patient payment)
                double newAmountPaid = patientPaid + paymentAmount;
                double newTotalPaid = newAmountPaid + insurancePaid;
                String newStatus = (newAmountPaid >= correctFinalAmount) ? "Paid" : "Partially Paid";

                System.out.println("Processing payment: " + paymentAmount); // DEBUG
                System.out.println("New patient amount paid: " + newAmountPaid); // DEBUG
                System.out.println("New total paid: " + newTotalPaid); // DEBUG
                System.out.println("New status: " + newStatus); // DEBUG

                boolean success = billingDAO.updateAmountPaidAndStatus(
                        selectedBill.getBillId(),
                        newAmountPaid,
                        insurancePaid,  // Keep existing insurance payment
                        newStatus
                );

                if (success) {
                    JOptionPane.showMessageDialog(view,
                            "Payment processed successfully!\n" +
                                    "Payment Amount: $" + String.format("%.2f", paymentAmount) + "\n" +
                                    "New Patient Total: $" + String.format("%.2f", newAmountPaid) + "\n" +
                                    "Insurance Paid: $" + String.format("%.2f", insurancePaid) + "\n" +
                                    "Total Paid: $" + String.format("%.2f", newTotalPaid) + "\n" +
                                    "New Remaining: $" + String.format("%.2f", Math.max(0, correctFinalAmount - newAmountPaid)) + "\n" +
                                    "New Status: " + newStatus,
                            "Payment Successful", JOptionPane.INFORMATION_MESSAGE);

                    searchBills(); // Refresh the table to show updated amounts
                } else {
                    JOptionPane.showMessageDialog(view, "Failed to process payment. Please try again.", "Payment Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(view, "Invalid payment amount. Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void processBill() {
        if (!currentUser.hasPermission("can_create_bill")) {
            JOptionPane.showMessageDialog(mainFrame, "You do not have permission to create bills.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String patientId = view.createPatientIdField.getText().trim();
        String service = view.serviceField.getText().trim();
        String amountStr = view.amountField.getText().trim();

        // Validation
        if (patientId.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter a Patient ID.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (service.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter a service description.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter an amount.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        PatientRecord patient = patientDAO.getPatientById(patientId);
        if (patient == null) {
            JOptionPane.showMessageDialog(view, "Patient with ID '" + patientId + "' not found.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(view, "Amount must be greater than 0.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Invalid amount. Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create and process the bill
        MedicalBill bill = new MedicalBill(patientId, service, amount);
        BillProcessingRequest request = new BillProcessingRequest(bill, patient);

        try {
            billProcessingChain.processBill(request);

            // ADDED: Calculate and set insurance payment after processing
            if (bill.getAmount() > bill.getFinalAmount()) {
                double insurancePayment = bill.getAmount() - bill.getFinalAmount();
                bill.setInsurancePaidAmount(insurancePayment);
                System.out.println("Calculated insurance payment: $" + insurancePayment);
            }

            // Save the processed bill to database
            int billId = billingDAO.saveBill(bill);
            if (billId > 0) {
                bill.setBillId(billId);
                JOptionPane.showMessageDialog(view,
                        "Bill processed and saved successfully!\n" +
                                "Bill ID: " + billId + "\n" +
                                "Final Status: " + bill.getStatus() + "\n" +
                                "Original Amount: $" + String.format("%.2f", bill.getAmount()) + "\n" +
                                "Insurance Paid: $" + String.format("%.2f", bill.getInsurancePaidAmount()) + "\n" +
                                "Final Amount (Patient Owes): $" + String.format("%.2f", bill.getFinalAmount()),
                        "Processing Complete", JOptionPane.INFORMATION_MESSAGE);

                // Clear the form
                view.clearCreateBillForm();

                // Refresh search results if showing bills for the same patient
                if (patientId.equals(view.searchPatientIdField.getText().trim())) {
                    searchBills();
                }
            } else {
                JOptionPane.showMessageDialog(view, "Bill processed but failed to save to database.", "Save Error", JOptionPane.WARNING_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view,
                    "Error processing bill: " + ex.getMessage(),
                    "Processing Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}