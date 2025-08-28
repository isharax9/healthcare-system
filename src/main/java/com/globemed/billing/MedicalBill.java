package com.globemed.billing;

import com.globemed.insurance.InsurancePlan; // Import this

public class MedicalBill {
    private int billId;
    private final String patientId;
    private final String serviceDescription;
    private final double amount;

    // This will be populated by the InsuranceHandler
    private InsurancePlan appliedInsurancePlan;

    private String status;
    private StringBuilder processingLog;
    private double amountPaidByInsurance = 0.0;
    private double finalAmount; // New field

    // Constructor no longer needs insurancePolicyNumber
    public MedicalBill(String patientId, String serviceDescription, double amount) {
        this.patientId = patientId;
        this.serviceDescription = serviceDescription;
        this.amount = amount;
        this.status = "New";
        this.processingLog = new StringBuilder("Bill created.\n");
    }

    // --- Getters ---
    public int getBillId() { return billId; }
    public String getPatientId() { return patientId; }
    public double getAmount() { return amount; }
    public InsurancePlan getAppliedInsurancePlan() { return appliedInsurancePlan; }
    public String getStatus() { return status; }
    public String getProcessingLog() { return processingLog.toString(); }
    public double getRemainingBalance() { return amount - amountPaidByInsurance; }
    public double getFinalAmount() { return finalAmount; }

    // --- Setters and Modifiers ---
    public void setBillId(int billId) { this.billId = billId; }
    public void setStatus(String status) { this.status = status; }
    public void addLog(String logEntry) { this.processingLog.append("- ").append(logEntry).append("\n"); }
    public void applyInsurancePayment(double amount) { this.amountPaidByInsurance += amount; }
    public void setProcessingLog(String log) { this.processingLog = new StringBuilder(log); }
    public void setAppliedInsurancePlan(InsurancePlan plan) { this.appliedInsurancePlan = plan; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }

    public String getServiceDescription() {
        return serviceDescription;
    }
}