package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;

/**
 * A Concrete Visitor that generates a financial-only report,
 * calculating total amounts billed and paid.
 */
public class FinancialReportVisitor implements ReportVisitor {
    private final StringBuilder reportContent = new StringBuilder();
    private double totalBilled = 0;
    private double totalFinalAmount = 0;

    @Override
    public void visit(PatientRecord patient) {
        // This visitor only cares about financials, so it starts the report here.
        reportContent.append("--- Financial Report for Patient: ")
                .append(patient.getName())
                .append(" (").append(patient.getPatientId()).append(") ---\n\n");
        reportContent.append(String.format("%-10s | %-30s | %-15s | %-15s\n",
                "Bill ID", "Service", "Original Amt", "Final Amt"));
        reportContent.append("-".repeat(75)).append("\n");
    }

    @Override
    public void visit(Appointment appointment) {
        // This visitor is not interested in appointments, so it does nothing.
    }

    @Override
    public void visit(MedicalBill bill) {
        // This visitor's main job is to process bills.
        totalBilled += bill.getAmount();
        totalFinalAmount += bill.getFinalAmount();
        reportContent.append(String.format("%-10d | %-30s | $%-14.2f | $%-14.2f\n",
                bill.getBillId(),
                bill.getServiceDescription(),
                bill.getAmount(),
                bill.getFinalAmount()
        ));
    }

    @Override
    public String getReport() {
        reportContent.append("-".repeat(75)).append("\n");
        reportContent.append(String.format("Total Original Amount Billed: $%.2f\n", totalBilled));
        reportContent.append(String.format("Total Final Amount Due: $%.2f\n", totalFinalAmount));
        return reportContent.toString();
    }
}