package com.globemed.billing;

import com.globemed.insurance.InsurancePlan;
import com.globemed.patient.PatientRecord;

public class InsuranceHandler implements BillingHandler {
    private BillingHandler next;

    @Override
    public void setNext(BillingHandler next) { this.next = next; }

    @Override
    public boolean processBill(BillProcessingRequest request) {
        MedicalBill bill = request.getBill();
        PatientRecord patient = request.getPatient();
        InsurancePlan plan = patient.getInsurancePlan();

        System.out.println("InsuranceHandler: Checking patient " + patient.getPatientId());

        if (plan != null) {
            bill.setAppliedInsurancePlan(plan); // Store the plan on the bill
            double coveragePercent = plan.getCoveragePercent();
            double amountToCover = bill.getAmount() * (coveragePercent / 100.0);

            bill.applyInsurancePayment(amountToCover);
            bill.setStatus("Insurance Processed");
            bill.addLog(String.format("Insurance claim processed for policy %s (%.0f%%). Covered: $%.2f",
                    plan.getPlanName(), coveragePercent, amountToCover));
            System.out.println("Insurance processed for " + plan.getPlanName());
        } else {
            bill.addLog("No insurance on file. Skipping claim processing.");
            System.out.println("No insurance found.");
        }

        if (next != null) {
            return next.processBill(request);
        }
        return true;
    }
}