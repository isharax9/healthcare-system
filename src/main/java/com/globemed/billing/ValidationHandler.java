package com.globemed.billing;

/**
 * A concrete handler that performs basic validation on the bill.
 */
public class ValidationHandler implements BillingHandler {
    private BillingHandler next;

    @Override
    public void setNext(BillingHandler next) {
        this.next = next;
    }

    @Override
    public boolean processBill(BillProcessingRequest request) {
        MedicalBill bill = request.getBill(); // Get the bill from the request
        System.out.println("ValidationHandler: Checking bill for patient " + bill.getPatientId());

        // Rule 1: Amount must be greater than zero
        if (bill.getAmount() <= 0) {
            bill.setStatus("Rejected: Invalid Amount");
            bill.addLog("Validation Failed: Bill amount must be positive.");
            System.out.println("Validation Failed: Amount is not positive.");
            return false; // Stop the chain
        }

        // Rule 2: Patient ID must not be empty
        if (bill.getPatientId() == null || bill.getPatientId().trim().isEmpty()) {
            bill.setStatus("Rejected: Missing Patient ID");
            bill.addLog("Validation Failed: Patient ID is required.");
            System.out.println("Validation Failed: Patient ID is missing.");
            return false; // Stop the chain
        }

        // If validation passes
        bill.setStatus("Validated");
        bill.addLog("Bill passed initial validation.");
        System.out.println("Validation successful.");

        // Pass to the next handler if it exists
        if (next != null) {
            return next.processBill(request); // Pass the original request object
        }

        return true; // End of this path in the chain
    }
}