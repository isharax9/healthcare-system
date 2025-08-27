package com.globemed.controller;

import com.globemed.billing.*;
import com.globemed.ui.BillingPanel;
import javax.swing.JOptionPane;

public class BillingController {

    private final BillingPanel view;
    private final BillingHandler billProcessingChain;

    public BillingController(BillingPanel view) {
        this.view = view;
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
        String patientId = view.patientIdField.getText();
        String service = view.serviceField.getText();
        String amountStr = view.amountField.getText();
        String insurance = view.insuranceField.getText();

        // 2. Validate UI input before creating the object
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Invalid amount. Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Create the MedicalBill object
        MedicalBill bill = new MedicalBill(patientId, service, amount, insurance);

        // 4. Start the processing by passing the bill to the first handler
        System.out.println("\n--- Starting New Bill Processing ---");
        billProcessingChain.processBill(bill);
        System.out.println("--- Bill Processing Finished ---");

        // 5. Update the UI with the final results from the processed bill object
        view.setStatus(bill.getStatus());
        view.setLog(bill.getProcessingLog());
    }
}