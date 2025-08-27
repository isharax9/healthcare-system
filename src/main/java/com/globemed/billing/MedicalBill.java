package com.globemed.billing;

public class MedicalBill {
    private int billId;
    private final String patientId;
    private final String serviceDescription;
    private final double amount;
    private String insurancePolicyNumber; // Can be null

    // These fields will be modified by the handlers
    private String status;
    private StringBuilder processingLog;
    private double amountPaidByInsurance = 0.0;
    private double amountPaidByPatient = 0.0;

    public MedicalBill(String patientId, String serviceDescription, double amount, String insurancePolicyNumber) {
        this.patientId = patientId;
        this.serviceDescription = serviceDescription;
        this.amount = amount;
        this.insurancePolicyNumber = insurancePolicyNumber;
        this.status = "New";
        this.processingLog = new StringBuilder("Bill created.\n");
    }

    // --- Getters ---
    public int getBillId() { return billId; }
    public String getPatientId() { return patientId; }
    public double getAmount() { return amount; }
    public String getInsurancePolicyNumber() { return insurancePolicyNumber; }
    public String getStatus() { return status; }
    public String getProcessingLog() { return processingLog.toString(); }
    public boolean hasInsurance() {
        return insurancePolicyNumber != null && !insurancePolicyNumber.trim().isEmpty();
    }
    public double getRemainingBalance() {
        return amount - amountPaidByInsurance - amountPaidByPatient;
    }

    // --- Setters and Modifiers used by Handlers ---
    public void setBillId(int billId) { this.billId = billId; }
    public void setStatus(String status) { this.status = status; }
    public void addLog(String logEntry) {
        this.processingLog.append("- ").append(logEntry).append("\n");
    }
    public void applyInsurancePayment(double amount) {
        this.amountPaidByInsurance += amount;
    }
    public void applyPatientPayment(double amount) {
        this.amountPaidByPatient += amount;
    }

    @Override
    public String toString() {
        return String.format("Bill #%d for %s (%.2f) - Status: %s",
                billId, patientId, amount, status);
    }

    public String getServiceDescription() {
        return "";
    }
}