package com.globemed.billing;

import com.globemed.insurance.InsurancePlan;
import com.globemed.reports.ReportVisitor;
import com.globemed.reports.Visitable;
import java.time.LocalDateTime;

public class MedicalBill implements Visitable {
    private int billId;
    private final String patientId;
    private final String serviceDescription;
    private final double amount;

    // This will be populated by the InsuranceHandler
    private InsurancePlan appliedInsurancePlan;

    private String status;
    private StringBuilder processingLog;
    private double amountPaidByInsurance = 0.0; // Legacy field for backward compatibility
    private double finalAmount;
    private LocalDateTime billedDateTime;
    private double amountPaid; // Total amount paid by patient
    private double insurancePaidAmount; // Amount paid by insurance

    // Original constructor
    public MedicalBill(String patientId, String serviceDescription, double amount) {
        this.patientId = patientId;
        this.serviceDescription = serviceDescription;
        this.amount = amount;
        this.status = "New";
        this.processingLog = new StringBuilder("Bill created.\n");
        this.billedDateTime = LocalDateTime.now();
        this.amountPaid = 0.0;
        this.insurancePaidAmount = 0.0;
        this.finalAmount = amount;
    }

    // Constructor for database loading with insurance paid amount
    public MedicalBill(int billId, String patientId, String serviceDescription, double amount,
                       String status, String processingLog, double finalAmount,
                       LocalDateTime billedDateTime, double amountPaid, double insurancePaidAmount) {
        this.billId = billId;
        this.patientId = patientId;
        this.serviceDescription = serviceDescription;
        this.amount = amount;
        this.status = status;
        this.processingLog = new StringBuilder(processingLog != null ? processingLog : "");
        this.finalAmount = finalAmount;
        this.billedDateTime = billedDateTime;
        this.amountPaid = amountPaid;
        this.insurancePaidAmount = insurancePaidAmount;
        this.amountPaidByInsurance = insurancePaidAmount; // Keep legacy field in sync
    }

    // Legacy constructor for backward compatibility (without insurance paid amount)
    public MedicalBill(int billId, String patientId, String serviceDescription, double amount,
                       String status, String processingLog, double finalAmount,
                       LocalDateTime billedDateTime, double amountPaid) {
        this(billId, patientId, serviceDescription, amount, status, processingLog,
                finalAmount, billedDateTime, amountPaid, 0.0);
    }

    // --- Getters ---
    public int getBillId() {
        return billId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public double getAmount() {
        return amount;
    }

    public InsurancePlan getAppliedInsurancePlan() {
        return appliedInsurancePlan;
    }

    public String getStatus() {
        return status;
    }

    public String getProcessingLog() {
        return processingLog.toString();
    }

    public double getFinalAmount() {
        return finalAmount;
    }

    public LocalDateTime getBilledDateTime() {
        return billedDateTime;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public double getInsurancePaidAmount() {
        return insurancePaidAmount;
    }

    // CORRECTED: Remaining balance is what patient still owes
    public double getRemainingBalance() {
        // finalAmount is what patient owes after insurance
        // amountPaid is what patient has already paid
        // So remaining = finalAmount - amountPaid
        double remaining = finalAmount - amountPaid;
        return Math.max(0, remaining); // Don't return negative values
    }

    // Legacy getter for backward compatibility
    public double getAmountPaidByInsurance() {
        return insurancePaidAmount;
    }

    // Additional helper methods for reporting
    public double getTotalCollected() {
        return amountPaid + insurancePaidAmount;
    }

    public boolean isFullyPaid() {
        return getRemainingBalance() == 0 && getTotalCollected() > 0;
    }

    public boolean hasInsuranceCoverage() {
        return insurancePaidAmount > 0 || appliedInsurancePlan != null;
    }

    public boolean hasPatientPayment() {
        return amountPaid > 0;
    }

    public double getDiscountAmount() {
        // Discount is the difference between original amount and what's owed (final amount and collected)
        double totalAccountedFor = finalAmount + getTotalCollected();
        return Math.max(0, amount - totalAccountedFor);
    }

    // --- Setters and Modifiers ---
    public void setBillId(int billId) {
        this.billId = billId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void addLog(String logEntry) {
        this.processingLog.append("- ").append(logEntry).append("\n");
    }

    public void applyInsurancePayment(double amount) {
        this.insurancePaidAmount += amount;
        this.amountPaidByInsurance = this.insurancePaidAmount; // Keep legacy field in sync
        addLog("Insurance payment of $" + amount + " applied.");
    }

    public void setProcessingLog(String log) {
        this.processingLog = new StringBuilder(log != null ? log : "");
    }

    public void setAppliedInsurancePlan(InsurancePlan plan) {
        this.appliedInsurancePlan = plan;
    }

    public void setFinalAmount(double finalAmount) {
        this.finalAmount = finalAmount;
    }

    public void setBilledDateTime(LocalDateTime billedDateTime) {
        this.billedDateTime = billedDateTime;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public void setInsurancePaidAmount(double insurancePaidAmount) {
        this.insurancePaidAmount = insurancePaidAmount;
        this.amountPaidByInsurance = insurancePaidAmount; // Keep legacy field in sync
    }

    // Add patient payment
    public void addPatientPayment(double amount) {
        this.amountPaid += amount;
        addLog("Patient payment of $" + amount + " received.");
    }

    // Update status based on payment state
    public void updateStatusBasedOnPayments() {
        if (isFullyPaid()) {
            setStatus("Paid");
        } else if (getTotalCollected() > 0) {
            setStatus("Partial");
        } else if ("New".equals(status)) {
            setStatus("Pending");
        }
    }

    @Override
    public void accept(ReportVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("MedicalBill{billId=%d, patientId='%s', service='%s', amount=%.2f, " +
                        "amountPaid=%.2f, insurancePaid=%.2f, remaining=%.2f, status='%s'}",
                billId, patientId, serviceDescription, amount, amountPaid,
                insurancePaidAmount, getRemainingBalance(), status);
    }
}