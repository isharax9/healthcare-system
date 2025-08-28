package com.globemed.billing;

import com.globemed.db.BillingDAO;

/**
 * The final handler in the chain. It determines the final status
 * and saves the processed bill to the database.
 */
public class FinalBillingHandler implements BillingHandler {
    private BillingHandler next; // Will be null, as this is the last handler
    private final BillingDAO billingDAO;

    public FinalBillingHandler() {
        this.billingDAO = new BillingDAO();
    }

    @Override
    public void setNext(BillingHandler next) {
        // This is the end of the chain, so next is usually null.
        this.next = next;
    }

    @Override
    public boolean processBill(BillProcessingRequest request) {
        MedicalBill bill = request.getBill();
        System.out.println("FinalBillingHandler: Finalizing and saving bill for patient " + bill.getPatientId());

        double remainingBalance = bill.getRemainingBalance();

        bill.setFinalAmount(remainingBalance); // <-- SET THE FINAL AMOUNT

        if (remainingBalance <= 0) {
            bill.setStatus("Closed - Fully Paid");
            bill.addLog("Bill is fully paid. No remaining balance.");
        } else {
            // In a real system, you might process a co-pay or send an invoice here.
            // For now, we'll just mark it as pending patient payment.
            bill.setStatus("Closed - Pending Patient Payment");
            bill.addLog(String.format("Final balance of $%.2f due from patient.", remainingBalance));
        }

        System.out.println("Final bill status: " + bill.getStatus() + ". Saving to database...");

        // Save the final state of the bill to the database
        int billId = billingDAO.saveBill(bill);
        if (billId != -1) {
            bill.setBillId(billId); // Update the object with its new ID from the DB
            bill.addLog("Bill successfully saved to database with ID: " + billId);
            System.out.println("Successfully saved bill with ID: " + billId);
        } else {
            bill.setStatus("Error - Failed to Save");
            bill.addLog("CRITICAL ERROR: Failed to save the processed bill to the database.");
            System.err.println("Failed to save bill.");
            return false; // Indicate failure
        }

        // This is the end of the chain.
        return true;
    }
}