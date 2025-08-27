package com.globemed.controller;

import com.globemed.billing.*;
import com.globemed.ui.BillingPanel;
import javax.swing.JOptionPane;
import com.globemed.db.PatientDAO;

public class BillingController {

    private final BillingPanel view;
    private final BillingHandler billProcessingChain;
    private final PatientDAO patientDAO;

    public BillingController(BillingPanel view) {
        this.view = view;
        this.patientDAO = new PatientDAO();
        // This is where we assemble our Chain of Responsibility
        this.billProcessingChain = setupChain();
        initController();
    }

    /**
     * Creates and links all the handlers in the chain.
     * @return The first handler in the chain.
     */
    private BillingHandler setupChain() {
        // 1. Create the handlers
        BillingHandler validationHandler = new ValidationHandler();
        BillingHandler insuranceHandler = new InsuranceHandler();
        BillingHandler finalBillingHandler = new FinalBillingHandler();

        // 2. Link them together in the desired order
        validationHandler.setNext(insuranceHandler);
        insuranceHandler.setNext(finalBillingHandler);

        // 3. Return the head of the chain
        return validationHandler;
    }

    /**
     * Attaches the listener to the button.
     */
    private void initController() {
        view.processBillButton.addActionListener(e -> processBill());
    }

    /**
     * The main action method. Gathers data, creates a bill, and starts the chain.
     */
    private void processBill() {
        // 1. Get data from the view
        String patientId = view.patientIdField.getText().trim();
        // ... (rest of the data fetching is the same)
        String service = view.serviceField.getText();
        String amountStr = view.amountField.getText();
        String insurance = view.insuranceField.getText();

        // 2. NEW: Pre-validation Step
        if (patientDAO.getPatientById(patientId) == null) {
            JOptionPane.showMessageDialog(view, "Patient with ID '" + patientId + "' not found.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return; // Stop immediately
        }

        // 3. Validate UI input (same as before)
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Invalid amount. Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 4. Create the MedicalBill object (same as before)
        MedicalBill bill = new MedicalBill(patientId, service, amount, insurance);

        // 5. Start the processing (same as before)
        System.out.println("\n--- Starting New Bill Processing ---");
        billProcessingChain.processBill(bill);
        System.out.println("--- Bill Processing Finished ---");

        // 6. Update the UI (same as before)
        view.setStatus(bill.getStatus());
        view.setLog(bill.getProcessingLog());
    }
}