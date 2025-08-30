package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Patient Payment History Visitor - Fixed to match actual database schema
 * Provides detailed payment history for individual patients
 */
public class PatientPaymentHistoryVisitor implements ReportVisitor {
    private final StringBuilder reportContent = new StringBuilder();
    private final List<PaymentRecord> paymentHistory = new ArrayList<>();
    private final Map<String, Double> servicePayments = new HashMap<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String patientName = "";
    private String patientId = "";
    private double totalBilled = 0;
    private double totalPatientPaid = 0;
    private double totalInsurancePaid = 0;
    private double totalCollected = 0;
    private double totalOutstanding = 0;
    private int fullyPaidBills = 0;
    private int partiallyPaidBills = 0;
    private int unpaidBills = 0;

    @Override
    public void visit(PatientRecord patient) {
        this.patientName = patient.getName();
        this.patientId = patient.getPatientId();

        reportContent.append(repeatString("=", 90)).append("\n");
        reportContent.append("    PATIENT PAYMENT HISTORY REPORT\n");
        reportContent.append(repeatString("=", 90)).append("\n\n");
    }

    @Override
    public void visit(Appointment appointment) {
        // Could track appointment dates for payment correlation if needed
    }

    @Override
    public void visit(MedicalBill bill) {
        totalBilled += bill.getAmount();
        totalPatientPaid += bill.getAmountPaid();
        totalInsurancePaid += bill.getInsurancePaidAmount();
        totalCollected = totalPatientPaid + totalInsurancePaid;
        totalOutstanding += bill.getRemainingBalance();

        // Categorize payment status
        if (bill.isFullyPaid()) {
            fullyPaidBills++;
        } else if (bill.getTotalCollected() > 0) {
            partiallyPaidBills++;
        } else {
            unpaidBills++;
        }

        // Track payments by service type
        String service = bill.getServiceDescription();
        double serviceCollected = bill.getTotalCollected();
        servicePayments.put(service, servicePayments.getOrDefault(service, 0.0) + serviceCollected);

        paymentHistory.add(new PaymentRecord(bill));
    }

    @Override
    public String getReport() {
        generatePaymentSummary();
        generatePaymentStatusBreakdown();
        generateDetailedPaymentHistory();
        generateServicePaymentAnalysis();
        generatePaymentPattern();
        generateRecommendations();
        return reportContent.toString();
    }

    private void generatePaymentSummary() {
        reportContent.append("💰 PAYMENT SUMMARY\n");
        reportContent.append(repeatString("-", 60)).append("\n");
        reportContent.append(String.format("Total Bills: %d\n", paymentHistory.size()));
        reportContent.append(String.format("Total Amount Billed: $%,.2f\n", totalBilled));
        reportContent.append(String.format("Total Patient Payments: $%,.2f\n", totalPatientPaid));
        reportContent.append(String.format("Total Insurance Payments: $%,.2f\n", totalInsurancePaid));
        reportContent.append(String.format("Total Amount Collected: $%,.2f\n", totalCollected));
        reportContent.append(String.format("Total Outstanding Balance: $%,.2f\n", totalOutstanding));

        double collectionRate = totalBilled > 0 ? (totalCollected / totalBilled) * 100 : 0;
        reportContent.append(String.format("Overall Collection Rate: %.1f%%\n", collectionRate));

        if (totalCollected > 0) {
            double patientPaymentRatio = (totalPatientPaid / totalCollected) * 100;
            double insurancePaymentRatio = (totalInsurancePaid / totalCollected) * 100;
            reportContent.append(String.format("Patient Payment Ratio: %.1f%%\n", patientPaymentRatio));
            reportContent.append(String.format("Insurance Payment Ratio: %.1f%%\n", insurancePaymentRatio));
        }
        reportContent.append("\n");
    }

    private void generatePaymentStatusBreakdown() {
        reportContent.append("📊 PAYMENT STATUS BREAKDOWN\n");
        reportContent.append(repeatString("-", 60)).append("\n");

        int totalBills = paymentHistory.size();
        reportContent.append(String.format("Fully Paid Bills: %d (%.1f%%)\n",
                fullyPaidBills, totalBills > 0 ? (double) fullyPaidBills / totalBills * 100 : 0));
        reportContent.append(String.format("Partially Paid Bills: %d (%.1f%%)\n",
                partiallyPaidBills, totalBills > 0 ? (double) partiallyPaidBills / totalBills * 100 : 0));
        reportContent.append(String.format("Unpaid Bills: %d (%.1f%%)\n",
                unpaidBills, totalBills > 0 ? (double) unpaidBills / totalBills * 100 : 0));

        // Payment performance indicator
        if (fullyPaidBills > totalBills * 0.8) {
            reportContent.append("🟢 Excellent payment history\n");
        } else if (fullyPaidBills > totalBills * 0.6) {
            reportContent.append("🟡 Good payment history\n");
        } else {
            reportContent.append("🔴 Payment history needs attention\n");
        }
        reportContent.append("\n");
    }

    private void generateDetailedPaymentHistory() {
        reportContent.append("📋 DETAILED PAYMENT HISTORY\n");
        reportContent.append(repeatString("-", 120)).append("\n");
        reportContent.append(String.format("%-8s | %-25s | %-12s | %-12s | %-12s | %-12s | %-12s | %-15s | %-12s\n",
                "Bill ID", "Service", "Billed", "Patient Paid", "Insurance", "Collected", "Outstanding", "Billed Date", "Status"));
        reportContent.append(repeatString("-", 120)).append("\n");

        // Sort by billing date (most recent first)
        paymentHistory.stream()
                .sorted((r1, r2) -> r2.getBilledDate().compareTo(r1.getBilledDate()))
                .forEach(record -> {
                    reportContent.append(String.format("%-8d | %-25s | $%-11.2f | $%-11.2f | $%-11.2f | $%-11.2f | $%-11.2f | %-15s | %-12s\n",
                            record.getBillId(),
                            truncateString(record.getServiceDescription(), 25),
                            record.getBilledAmount(),
                            record.getPatientPaid(),
                            record.getInsurancePaid(),
                            record.getTotalCollected(),
                            record.getOutstanding(),
                            record.getBilledDate(),
                            record.getPaymentStatus()));
                });
        reportContent.append("\n");
    }

    private void generateServicePaymentAnalysis() {
        reportContent.append("🏥 SERVICE PAYMENT ANALYSIS\n");
        reportContent.append(repeatString("-", 70)).append("\n");
        reportContent.append(String.format("%-35s | %-15s | %-15s\n",
                "Service Type", "Amount Paid", "% of Total"));
        reportContent.append(repeatString("-", 70)).append("\n");

        servicePayments.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> {
                    double percentage = totalCollected > 0 ? (entry.getValue() / totalCollected) * 100 : 0;
                    reportContent.append(String.format("%-35s | $%-14.2f | %13.1f%%\n",
                            truncateString(entry.getKey(), 35), entry.getValue(), percentage));
                });
        reportContent.append("\n");
    }

    private void generatePaymentPattern() {
        reportContent.append("📈 PAYMENT PATTERN ANALYSIS\n");
        reportContent.append(repeatString("-", 60)).append("\n");

        // Calculate averages
        double avgBillAmount = paymentHistory.size() > 0 ? totalBilled / paymentHistory.size() : 0;
        double avgPatientPayment = paymentHistory.size() > 0 ? totalPatientPaid / paymentHistory.size() : 0;
        double avgInsurancePayment = paymentHistory.size() > 0 ? totalInsurancePaid / paymentHistory.size() : 0;

        reportContent.append(String.format("Average Bill Amount: $%.2f\n", avgBillAmount));
        reportContent.append(String.format("Average Patient Payment: $%.2f\n", avgPatientPayment));
        reportContent.append(String.format("Average Insurance Payment: $%.2f\n", avgInsurancePayment));

        // Find largest and smallest bills
        Optional<PaymentRecord> largestBill = paymentHistory.stream()
                .max(Comparator.comparing(PaymentRecord::getBilledAmount));
        Optional<PaymentRecord> smallestBill = paymentHistory.stream()
                .min(Comparator.comparing(PaymentRecord::getBilledAmount));

        if (largestBill.isPresent()) {
            PaymentRecord largest = largestBill.get();
            reportContent.append(String.format("Largest Bill: $%.2f (%s) - %s\n",
                    largest.getBilledAmount(), largest.getServiceDescription(), largest.getPaymentStatus()));
        }

        if (smallestBill.isPresent()) {
            PaymentRecord smallest = smallestBill.get();
            reportContent.append(String.format("Smallest Bill: $%.2f (%s) - %s\n",
                    smallest.getBilledAmount(), smallest.getServiceDescription(), smallest.getPaymentStatus()));
        }

        // Payment timing analysis
        long billsWithImmediatePayment = paymentHistory.stream()
                .filter(r -> r.getTotalCollected() > 0)
                .count();

        if (billsWithImmediatePayment > 0) {
            double immediatePaymentRate = ((double) billsWithImmediatePayment / paymentHistory.size()) * 100;
            reportContent.append(String.format("Bills with Payments: %.1f%% (%d/%d)\n",
                    immediatePaymentRate, billsWithImmediatePayment, paymentHistory.size()));
        }
        reportContent.append("\n");
    }

    private void generateRecommendations() {
        reportContent.append("💡 PAYMENT RECOMMENDATIONS\n");
        reportContent.append(repeatString("-", 60)).append("\n");

        double outstandingRate = totalBilled > 0 ? (totalOutstanding / totalBilled) * 100 : 0;

        if (outstandingRate > 30) {
            reportContent.append("🔴 HIGH OUTSTANDING BALANCE:\n");
            reportContent.append("  • Consider setting up a payment plan\n");
            reportContent.append("  • Offer financial counseling services\n");
            reportContent.append("  • Review insurance coverage options\n");
        } else if (outstandingRate > 15) {
            reportContent.append("🟡 MODERATE OUTSTANDING BALANCE:\n");
            reportContent.append("  • Send payment reminders\n");
            reportContent.append("  • Offer online payment options\n");
        } else {
            reportContent.append("🟢 GOOD PAYMENT PERFORMANCE:\n");
            reportContent.append("  • Continue current payment practices\n");
            reportContent.append("  • Consider loyalty/discount programs\n");
        }

        if (totalInsurancePaid < totalPatientPaid && totalInsurancePaid > 0) {
            reportContent.append("  • Review insurance coverage optimization\n");
        }

        if (unpaidBills > paymentHistory.size() * 0.2) {
            reportContent.append("  • Implement automated payment reminders\n");
            reportContent.append("  • Provide multiple payment method options\n");
        }

        reportContent.append("\n");
        reportContent.append(repeatString("=", 90)).append("\n");
        reportContent.append("End of Patient Payment History Report\n");
        reportContent.append(repeatString("=", 90)).append("\n");
    }

    private String repeatString(String str, int count) {
        return str.repeat(count);
    }

    private String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    // PaymentRecord class with accurate financial tracking
    private static class PaymentRecord {
        private final int billId;
        private final String serviceDescription;
        private final double billedAmount;
        private final double patientPaid;
        private final double insurancePaid;
        private final double totalCollected;
        private final double outstanding;
        private final String billedDate;
        private final String paymentStatus;

        public PaymentRecord(MedicalBill bill) {
            this.billId = bill.getBillId();
            this.serviceDescription = bill.getServiceDescription();
            this.billedAmount = bill.getAmount();
            this.patientPaid = bill.getAmountPaid();
            this.insurancePaid = bill.getInsurancePaidAmount();
            this.totalCollected = bill.getTotalCollected();
            this.outstanding = bill.getRemainingBalance();
            this.billedDate = bill.getBilledDateTime().toLocalDate().toString();

            // Determine payment status
            if (bill.isFullyPaid()) {
                this.paymentStatus = "Paid";
            } else if (bill.getTotalCollected() > 0) {
                this.paymentStatus = "Partial";
            } else {
                this.paymentStatus = bill.getStatus();
            }
        }

        // Getters
        public int getBillId() { return billId; }
        public String getServiceDescription() { return serviceDescription; }
        public double getBilledAmount() { return billedAmount; }
        public double getPatientPaid() { return patientPaid; }
        public double getInsurancePaid() { return insurancePaid; }
        public double getTotalCollected() { return totalCollected; }
        public double getOutstanding() { return outstanding; }
        public String getBilledDate() { return billedDate; }
        public String getPaymentStatus() { return paymentStatus; }
    }
}