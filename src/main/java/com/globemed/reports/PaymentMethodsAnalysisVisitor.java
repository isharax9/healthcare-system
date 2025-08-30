package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PaymentMethodsAnalysisVisitor implements ReportVisitor {
    private final StringBuilder reportContent = new StringBuilder();
    private final Map<String, PaymentMethodData> paymentMethods = new HashMap<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private double totalPayments = 0;
    private int totalTransactions = 0;

    @Override
    public void visit(PatientRecord patient) {
        if (reportContent.length() == 0) {
            reportContent.append(repeatString("=", 80)).append("\n");
            reportContent.append("    PAYMENT METHODS ANALYSIS REPORT\n");
            reportContent.append(repeatString("=", 80)).append("\n");
            reportContent.append("Generated: ").append(LocalDate.now().format(dateFormatter)).append("\n\n");
        }
    }

    @Override
    public void visit(Appointment appointment) {
        // Not used for this report
    }

    @Override
    public void visit(MedicalBill bill) {
        totalTransactions++;
        double paidAmount = bill.getAmount() - bill.getFinalAmount();
        totalPayments += paidAmount;

        // Determine payment method (placeholder logic)
        String paymentMethod = determinePaymentMethod(bill);

        PaymentMethodData data = paymentMethods.getOrDefault(paymentMethod, new PaymentMethodData(paymentMethod));
        data.addPayment(paidAmount);
        paymentMethods.put(paymentMethod, data);
    }

    private String determinePaymentMethod(MedicalBill bill) {
        // Placeholder logic - enhance based on your MedicalBill structure
        // This could check actual payment method fields
        String patientId = bill.getPatientId();
        if (patientId.contains("INS")) return "Insurance";
        if (bill.getFinalAmount() == 0) return "Cash";
        if (Math.random() > 0.5) return "Credit Card";
        return "Bank Transfer";
    }

    @Override
    public String getReport() {
        generateOverview();
        generateMethodBreakdown();
        generateTrendAnalysis();
        generateRecommendations();
        return reportContent.toString();
    }

    private void generateOverview() {
        reportContent.append("💳 PAYMENT METHODS OVERVIEW\n");
        reportContent.append(repeatString("-", 50)).append("\n");
        reportContent.append(String.format("Total Payment Methods: %d\n", paymentMethods.size()));
        reportContent.append(String.format("Total Transactions: %d\n", totalTransactions));
        reportContent.append(String.format("Total Payments Processed: $%,.2f\n", totalPayments));
        reportContent.append(String.format("Average Transaction: $%,.2f\n",
                totalTransactions > 0 ? totalPayments / totalTransactions : 0));
        reportContent.append("\n");
    }

    private void generateMethodBreakdown() {
        reportContent.append("📊 PAYMENT METHOD BREAKDOWN\n");
        reportContent.append(repeatString("-", 80)).append("\n");
        reportContent.append(String.format("%-20s | %-12s | %-15s | %-12s | %-10s\n",
                "Payment Method", "Transactions", "Total Amount", "Average", "% of Total"));
        reportContent.append(repeatString("-", 80)).append("\n");

        paymentMethods.values().stream()
                .sorted((p1, p2) -> Double.compare(p2.getTotalAmount(), p1.getTotalAmount()))
                .forEach(method -> {
                    double percentage = totalPayments > 0 ? (method.getTotalAmount() / totalPayments) * 100 : 0;
                    reportContent.append(String.format("%-20s | %-12d | $%-14.2f | $%-11.2f | %7.1f%%\n",
                            method.getMethodName(),
                            method.getTransactionCount(),
                            method.getTotalAmount(),
                            method.getAverageAmount(),
                            percentage));
                });
        reportContent.append("\n");
    }

    private void generateTrendAnalysis() {
        reportContent.append("📈 PAYMENT METHOD PREFERENCES\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        PaymentMethodData mostPopular = paymentMethods.values().stream()
                .max(Comparator.comparing(PaymentMethodData::getTransactionCount))
                .orElse(null);

        PaymentMethodData highestValue = paymentMethods.values().stream()
                .max(Comparator.comparing(PaymentMethodData::getTotalAmount))
                .orElse(null);

        PaymentMethodData highestAverage = paymentMethods.values().stream()
                .max(Comparator.comparing(PaymentMethodData::getAverageAmount))
                .orElse(null);

        if (mostPopular != null) {
            reportContent.append(String.format("🥇 Most Popular: %s (%d transactions)\n",
                    mostPopular.getMethodName(), mostPopular.getTransactionCount()));
        }
        if (highestValue != null) {
            reportContent.append(String.format("💰 Highest Volume: %s ($%,.2f)\n",
                    highestValue.getMethodName(), highestValue.getTotalAmount()));
        }
        if (highestAverage != null) {
            reportContent.append(String.format("📊 Highest Average: %s ($%.2f/transaction)\n",
                    highestAverage.getMethodName(), highestAverage.getAverageAmount()));
        }
        reportContent.append("\n");
    }

    private void generateRecommendations() {
        reportContent.append("💡 PAYMENT OPTIMIZATION RECOMMENDATIONS\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        // Find least used payment method
        PaymentMethodData leastUsed = paymentMethods.values().stream()
                .min(Comparator.comparing(PaymentMethodData::getTransactionCount))
                .orElse(null);

        if (leastUsed != null && leastUsed.getTransactionCount() > 0) {
            reportContent.append(String.format("• Consider promoting %s (currently only %.1f%% of transactions)\n",
                    leastUsed.getMethodName(),
                    totalTransactions > 0 ? (double) leastUsed.getTransactionCount() / totalTransactions * 100 : 0));
        }

        reportContent.append("• Implement contactless payment options\n");
        reportContent.append("• Offer payment method incentives for preferred methods\n");
        reportContent.append("• Monitor transaction fees by payment method\n");
        reportContent.append("• Consider digital wallet integration\n");

        reportContent.append("\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("End of Payment Methods Analysis Report\n");
        reportContent.append(repeatString("=", 80)).append("\n");
    }

    private String repeatString(String str, int count) {
        return str.repeat(count);
    }

    private static class PaymentMethodData {
        private final String methodName;
        private double totalAmount = 0;
        private int transactionCount = 0;

        public PaymentMethodData(String methodName) {
            this.methodName = methodName;
        }

        public void addPayment(double amount) {
            totalAmount += amount;
            transactionCount++;
        }

        public String getMethodName() { return methodName; }
        public double getTotalAmount() { return totalAmount; }
        public int getTransactionCount() { return transactionCount; }
        public double getAverageAmount() {
            return transactionCount > 0 ? totalAmount / transactionCount : 0;
        }
    }
}