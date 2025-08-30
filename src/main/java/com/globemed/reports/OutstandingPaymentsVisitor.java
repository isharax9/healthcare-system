package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class OutstandingPaymentsVisitor implements ReportVisitor {
    private final StringBuilder reportContent = new StringBuilder();
    private final List<OutstandingBill> outstandingBills = new ArrayList<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private double totalOutstanding = 0;
    private int totalOutstandingCount = 0;

    @Override
    public void visit(PatientRecord patient) {
        // Store patient info for later use
    }

    @Override
    public void visit(Appointment appointment) {
        // Not used for this report
    }

    @Override
    public void visit(MedicalBill bill) {
        if (bill.getFinalAmount() > 0) { // Outstanding amount
            totalOutstanding += bill.getFinalAmount();
            totalOutstandingCount++;
            outstandingBills.add(new OutstandingBill(bill));
        }
    }

    @Override
    public String getReport() {
        generateHeader();
        generateSummary();
        generateDetailedList();
        return reportContent.toString();
    }

    private void generateHeader() {
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("    OUTSTANDING PAYMENTS REPORT\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("Generated: ").append(LocalDate.now().format(dateFormatter)).append("\n\n");
    }

    private void generateSummary() {
        reportContent.append("📊 OUTSTANDING PAYMENTS SUMMARY\n");
        reportContent.append(repeatString("-", 50)).append("\n");
        reportContent.append(String.format("Total Outstanding Bills: %d\n", totalOutstandingCount));
        reportContent.append(String.format("Total Outstanding Amount: $%,.2f\n", totalOutstanding));
        reportContent.append(String.format("Average Outstanding per Bill: $%,.2f\n",
                totalOutstandingCount > 0 ? totalOutstanding / totalOutstandingCount : 0));
        reportContent.append("\n");
    }

    private void generateDetailedList() {
        reportContent.append("📋 DETAILED OUTSTANDING BILLS\n");
        reportContent.append(repeatString("-", 80)).append("\n");
        reportContent.append(String.format("%-10s | %-15s | %-30s | %-15s\n",
                "Bill ID", "Patient ID", "Service", "Amount Due"));
        reportContent.append(repeatString("-", 80)).append("\n");

        outstandingBills.stream()
                .sorted((b1, b2) -> Double.compare(b2.getAmount(), b1.getAmount()))
                .forEach(bill -> {
                    reportContent.append(String.format("%-10d | %-15s | %-30s | $%-14.2f\n",
                            bill.getBillId(),
                            bill.getPatientId(),
                            truncateString(bill.getServiceDescription(), 30),
                            bill.getAmount()));
                });

        reportContent.append("\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("End of Outstanding Payments Report\n");
        reportContent.append(repeatString("=", 80)).append("\n");
    }

    private String repeatString(String str, int count) {
        return str.repeat(count);
    }

    private String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    private static class OutstandingBill {
        private final int billId;
        private final String patientId;
        private final String serviceDescription;
        private final double amount;

        public OutstandingBill(MedicalBill bill) {
            this.billId = bill.getBillId();
            this.patientId = bill.getPatientId();
            this.serviceDescription = bill.getServiceDescription();
            this.amount = bill.getFinalAmount();
        }

        public int getBillId() { return billId; }
        public String getPatientId() { return patientId; }
        public String getServiceDescription() { return serviceDescription; }
        public double getAmount() { return amount; }
    }
}