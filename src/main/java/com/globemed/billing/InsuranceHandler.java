package com.globemed.billing;

/**
 * A concrete handler that processes insurance claims if the patient has insurance.
 */
public class InsuranceHandler implements BillingHandler {
    private BillingHandler next;

    @Override
    public void setNext(BillingHandler next) {
        this.next = next;
    }

    @Override
    public boolean processBill(MedicalBill bill) {
        System.out.println("InsuranceHandler: Checking for insurance on bill for patient " + bill.getPatientId());

        if (bill.hasInsurance()) {
            // Simulate a real-world insurance claim process.
            // Here, we'll assume the insurance covers 80% of the bill.
            double amountCovered = bill.getAmount() * 0.80;
            bill.applyInsurancePayment(amountCovered);

            bill.setStatus("Insurance Approved");
            bill.addLog(String.format("Insurance claim processed for policy %s. Covered: $%.2f",
                    bill.getInsurancePolicyNumber(), amountCovered));
            System.out.println("Insurance processed. Amount covered: " + amountCovered);
        } else {
            // No insurance, so this handler's main job is to just pass it on.
            bill.addLog("No insurance on file. Skipping insurance claim processing.");
            System.out.println("No insurance found. Passing to next handler.");
        }

        // Always pass to the next handler in the chain.
        if (next != null) {
            return next.processBill(bill);
        }

        return true;
    }
}