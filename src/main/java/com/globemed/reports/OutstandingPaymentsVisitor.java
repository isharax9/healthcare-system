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
    private final Map<String, Double> statusBreakdown = new HashMap<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private double totalOutstanding = 0;
    private int totalOutstandingCount = 0;
    private double totalOriginalAmount = 0;

    @Override
    public void visit(PatientRecord patient) {
        // Initialize for system-wide report
    }

    @Override
    public void visit(Appointment appointment) {
        // Not used for this report
    }

    @Override
    public void visit(MedicalBill bill) {
        double remainingBalance = bill.getRemainingBalance();
        if (remainingBalance > 0) { // Has outstanding amount
            totalOutstanding += remainingBalance;
            totalOriginalAmount += bill.getAmount();
            totalOutstandingCount++;
            outstandingBills.add(new OutstandingBill(bill));

            // Track by status
            String status = bill.getStatus();
            statusBreakdown.put(status, statusBreakdown.getOrDefault(status, 0.0) + remainingBalance);
        }
    }

    @Override
    public String getReport() {
        generateHeader();
        generateSummary();
        generateStatusBreakdown();
        generateDetailedList();
        generateAgeAnalysis();
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
        reportContent.append(String.format("Total Original Amount: $%,.2f\n", totalOriginalAmount));
        reportContent.append(String.format("Average Outstanding per Bill: $%,.2f\n",
                totalOutstandingCount > 0 ? totalOutstanding / totalOutstandingCount : 0));

        double outstandingRate = totalOriginalAmount > 0 ? (totalOutstanding / totalOriginalAmount) * 100 : 0;
        reportContent.append(String.format("Outstanding Rate: %.1f%%\n", outstandingRate));
        reportContent.append("\n");
    }

    private void generateStatusBreakdown() {
        reportContent.append("📋 OUTSTANDING BY STATUS\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        statusBreakdown.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> {
                    double percentage = totalOutstanding > 0 ? (entry.getValue() / totalOutstanding) * 100 : 0;
                    reportContent.append(String.format("%-15s: $%,.2f (%.1f%%)\n",
                            entry.getKey(), entry.getValue(), percentage));
                });
        reportContent.append("\n");
    }

    private void generateDetailedList() {
        reportContent.append("📋 DETAILED OUTSTANDING BILLS\n");
        reportContent.append(repeatString("-", 90)).append("\n");
        reportContent.append(String.format("%-8s | %-12s | %-25s | %-12s | %-12s | %-12s\n",
                "Bill ID", "Patient ID", "Service", "Amount Due", "Status", "Billed Date"));
        reportContent.append(repeatString("-", 90)).append("\n");

        outstandingBills.stream()
                .sorted((b1, b2) -> Double.compare(b2.getAmountDue(), b1.getAmountDue()))
                .forEach(bill -> {
                    reportContent.append(String.format("%-8d | %-12s | %-25s | $%-11.2f | %-12s | %-12s\n",
                            bill.getBillId(),
                            bill.getPatientId(),
                            truncateString(bill.getServiceDescription(), 25),
                            bill.getAmountDue(),
                            bill.getStatus(),
                            bill.getBilledDate()));
                });
        reportContent.append("\n");
    }

    private void generateAgeAnalysis() {
        reportContent.append("⏰ AGING ANALYSIS\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        LocalDate today = LocalDate.now();
        double[] ageCategories = new double[4]; // 0-30, 31-60, 61-90, 90+
        int[] ageCounts = new int[4];
        String[] ageLabels = {"0-30 days", "31-60 days", "61-90 days", "90+ days"};

        outstandingBills.forEach(bill -> {
            long daysSinceBilled = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDate.parse(bill.getBilledDate()), today);

            int categoryIndex;
            if (daysSinceBilled <= 30) categoryIndex = 0;
            else if (daysSinceBilled <= 60) categoryIndex = 1;
            else if (daysSinceBilled <= 90) categoryIndex = 2;
            else categoryIndex = 3;

            ageCategories[categoryIndex] += bill.getAmountDue();
            ageCounts[categoryIndex]++;
        });

        for (int i = 0; i < 4; i++) {
            double percentage = totalOutstanding > 0 ? (ageCategories[i] / totalOutstanding) * 100 : 0;
            reportContent.append(String.format("%-12s: %3d bills | $%8.2f | %5.1f%%\n",
                    ageLabels[i], ageCounts[i], ageCategories[i], percentage));
        }

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
        private final double amountDue;
        private final String status;
        private final String billedDate;

        public OutstandingBill(MedicalBill bill) {
            this.billId = bill.getBillId();
            this.patientId = bill.getPatientId();
            this.serviceDescription = bill.getServiceDescription();
            this.amountDue = bill.getRemainingBalance();
            this.status = bill.getStatus();
            this.billedDate = bill.getBilledDateTime().toLocalDate().toString();
        }

        public int getBillId() { return billId; }
        public String getPatientId() { return patientId; }
        public String getServiceDescription() { return serviceDescription; }
        public double getAmountDue() { return amountDue; }
        public String getStatus() { return status; }
        public String getBilledDate() { return billedDate; }
    }
}