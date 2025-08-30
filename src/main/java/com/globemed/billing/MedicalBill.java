package com.globemed.billing;

import com.globemed.insurance.InsurancePlan; // Import this
import com.globemed.reports.ReportVisitor; // Add import
import com.globemed.reports.Visitable;   // Add import
import java.time.LocalDateTime; // NEW IMPORT

public class MedicalBill implements Visitable { // Implement Visitable
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
    private LocalDateTime billedDateTime; // NEW FIELD
    private double amountPaid; // NEW FIELD - total amount paid (including insurance and patient payments)

    // Original constructor
    public MedicalBill(String patientId, String serviceDescription, double amount) {
        this.patientId = patientId;
        this.serviceDescription = serviceDescription;
        this.amount = amount;
        this.status = "New";
        this.processingLog = new StringBuilder("Bill created.\n");
        this.billedDateTime = LocalDateTime.now(); // Set current date/time
        this.amountPaid = 0.0; // Initialize to 0
        this.finalAmount = amount; // Initialize to original amount
    }

    // NEW CONSTRUCTOR for database loading
    public MedicalBill(int billId, String patientId, String serviceDescription, double amount,
                       String status, String processingLog, double finalAmount,
                       LocalDateTime billedDateTime, double amountPaid) {
        this.billId = billId;
        this.patientId = patientId;
        this.serviceDescription = serviceDescription;
        this.amount = amount;
        this.status = status;
        this.processingLog = new StringBuilder(processingLog != null ? processingLog : "");
        this.finalAmount = finalAmount;
        this.billedDateTime = billedDateTime;
        this.amountPaid = amountPaid;
    }

    // --- Getters ---
    public int getBillId() { return billId; }
    public String getPatientId() { return patientId; }
    public String getServiceDescription() { return serviceDescription; }
    public double getAmount() { return amount; }
    public InsurancePlan getAppliedInsurancePlan() { return appliedInsurancePlan; }
    public String getStatus() { return status; }
    public String getProcessingLog() { return processingLog.toString(); }
    public double getRemainingBalance() { return finalAmount - amountPaid; }
    public double getFinalAmount() { return finalAmount; }
    public LocalDateTime getBilledDateTime() { return billedDateTime; } // NEW GETTER
    public double getAmountPaid() { return amountPaid; } // NEW GETTER

    // Legacy getter for backward compatibility
    public double getAmountPaidByInsurance() { return amountPaidByInsurance; }

    // --- Setters and Modifiers ---
    public void setBillId(int billId) { this.billId = billId; }
    public void setStatus(String status) { this.status = status; }
    public void addLog(String logEntry) { this.processingLog.append("- ").append(logEntry).append("\n"); }
    public void applyInsurancePayment(double amount) {
        this.amountPaidByInsurance += amount;
        this.amountPaid += amount; // Update total amount paid
    }
    public void setProcessingLog(String log) { this.processingLog = new StringBuilder(log != null ? log : ""); }
    public void setAppliedInsurancePlan(InsurancePlan plan) { this.appliedInsurancePlan = plan; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }
    public void setBilledDateTime(LocalDateTime billedDateTime) { this.billedDateTime = billedDateTime; } // NEW SETTER
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; } // NEW SETTER

    // NEW METHOD: Add patient payment
    public void addPatientPayment(double amount) {
        this.amountPaid += amount;
        addLog("Patient payment of $" + amount + " received.");
    }

    // ADD THIS METHOD
    @Override
    public void accept(ReportVisitor visitor) {
        visitor.visit(this);
    }
}