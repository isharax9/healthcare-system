package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Enhanced Financial Report Visitor that generates comprehensive financial analysis
 */
public class FinancialReportVisitor implements ReportVisitor {
    private final StringBuilder reportContent = new StringBuilder();
    private final Map<String, Double> serviceRevenue = new HashMap<>();
    private final Map<String, Integer> serviceCount = new HashMap<>();
    private final Map<String, Double> paymentStatusAmounts = new HashMap<>();
    private final List<MedicalBill> outstandingBills = new ArrayList<>();

    private double totalBilled = 0;
    private double totalFinalAmount = 0;
    private double totalPaid = 0;
    private double totalPending = 0;
    private double totalDiscount = 0;
    private int totalBillCount = 0;

    private String patientName = "";
    private String patientId = "";
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void visit(PatientRecord patient) {
        this.patientName = patient.getName();
        this.patientId = patient.getPatientId();

        // Initialize the comprehensive financial report
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("    COMPREHENSIVE FINANCIAL REPORT\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("Patient: ").append(patient.getName())
                .append(" (ID: ").append(patient.getPatientId()).append(")\n");
        reportContent.append("Generated: ").append(LocalDate.now().format(dateFormatter)).append("\n");
        reportContent.append(repeatString("=", 80)).append("\n\n");
    }

    @Override
    public void visit(Appointment appointment) {
        // Enhanced: Track appointment-related financial data if needed
        // This could be used for appointment revenue correlation
    }

    @Override
    public void visit(MedicalBill bill) {
        totalBillCount++;
        totalBilled += bill.getAmount();
        totalFinalAmount += bill.getFinalAmount();

        // Calculate discount
        double discount = bill.getAmount() - bill.getFinalAmount();
        if (discount > 0) {
            totalDiscount += discount;
        }

        // Track service revenue
        String service = bill.getServiceDescription();
        serviceRevenue.put(service, serviceRevenue.getOrDefault(service, 0.0) + bill.getFinalAmount());
        serviceCount.put(service, serviceCount.getOrDefault(service, 0) + 1);

        // Track payment status (assuming you have a payment status field)
        String paymentStatus = determinePaymentStatus(bill);
        paymentStatusAmounts.put(paymentStatus,
                paymentStatusAmounts.getOrDefault(paymentStatus, 0.0) + bill.getFinalAmount());

        if ("Pending".equals(paymentStatus) || "Overdue".equals(paymentStatus)) {
            outstandingBills.add(bill);
        } else if ("Paid".equals(paymentStatus)) {
            totalPaid += bill.getFinalAmount();
        } else if ("Pending".equals(paymentStatus)) {
            totalPending += bill.getFinalAmount();
        }
    }

    private String determinePaymentStatus(MedicalBill bill) {
        // This method should determine payment status based on your bill properties
        // For now, using a placeholder logic - you can adjust based on your MedicalBill class
        if (bill.getFinalAmount() == 0) {
            return "Paid";
        } else if (bill.getFinalAmount() > 0) {
            // You might have a payment date or status field to check
            return "Pending"; // Default for unpaid bills
        }
        return "Unknown";
    }

    @Override
    public String getReport() {
        generateFinancialSummary();
        generateServiceBreakdown();
        generatePaymentAnalysis();
        generateOutstandingBillsSection();
        generateFinancialMetrics();

        return reportContent.toString();
    }

    private void generateFinancialSummary() {
        reportContent.append("📊 FINANCIAL SUMMARY\n");
        reportContent.append(repeatString("-", 50)).append("\n");
        reportContent.append(String.format("Total Bills Generated: %d\n", totalBillCount));
        reportContent.append(String.format("Total Original Amount: $%,.2f\n", totalBilled));
        reportContent.append(String.format("Total Discounts Applied: $%,.2f\n", totalDiscount));
        reportContent.append(String.format("Total Final Amount: $%,.2f\n", totalFinalAmount));
        reportContent.append(String.format("Total Amount Paid: $%,.2f\n", totalPaid));
        reportContent.append(String.format("Total Amount Pending: $%,.2f\n", totalPending));

        double collectionRate = totalFinalAmount > 0 ? (totalPaid / totalFinalAmount) * 100 : 0;
        reportContent.append(String.format("Collection Rate: %.1f%%\n", collectionRate));
        reportContent.append("\n");
    }

    private void generateServiceBreakdown() {
        reportContent.append("🏥 SERVICE REVENUE BREAKDOWN\n");
        reportContent.append(repeatString("-", 70)).append("\n");
        reportContent.append(String.format("%-30s | %-10s | %-15s | %-10s\n",
                "Service", "Count", "Revenue", "Avg/Service"));
        reportContent.append(repeatString("-", 70)).append("\n");

        // Sort services by revenue (highest first)
        serviceRevenue.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> {
                    String service = entry.getKey();
                    double revenue = entry.getValue();
                    int count = serviceCount.get(service);
                    double average = revenue / count;

                    reportContent.append(String.format("%-30s | %-10d | $%-14.2f | $%-9.2f\n",
                            truncateString(service, 30), count, revenue, average));
                });
        reportContent.append("\n");
    }

    private void generatePaymentAnalysis() {
        reportContent.append("💰 PAYMENT STATUS ANALYSIS\n");
        reportContent.append(repeatString("-", 40)).append("\n");

        paymentStatusAmounts.forEach((status, amount) -> {
            double percentage = totalFinalAmount > 0 ? (amount / totalFinalAmount) * 100 : 0;
            reportContent.append(String.format("%-15s: $%,.2f (%.1f%%)\n",
                    status, amount, percentage));
        });
        reportContent.append("\n");
    }

    private void generateOutstandingBillsSection() {
        if (!outstandingBills.isEmpty()) {
            reportContent.append("⚠️  OUTSTANDING BILLS\n");
            reportContent.append(repeatString("-", 75)).append("\n");
            reportContent.append(String.format("%-10s | %-30s | %-15s | %-15s\n",
                    "Bill ID", "Service", "Amount Due", "Status"));
            reportContent.append(repeatString("-", 75)).append("\n");

            // Sort by amount (highest first)
            outstandingBills.stream()
                    .sorted((b1, b2) -> Double.compare(b2.getFinalAmount(), b1.getFinalAmount()))
                    .forEach(bill -> {
                        reportContent.append(String.format("%-10d | %-30s | $%-14.2f | %-15s\n",
                                bill.getBillId(),
                                truncateString(bill.getServiceDescription(), 30),
                                bill.getFinalAmount(),
                                determinePaymentStatus(bill)));
                    });
            reportContent.append("\n");
        }
    }

    private void generateFinancialMetrics() {
        reportContent.append("📈 KEY FINANCIAL METRICS\n");
        reportContent.append(repeatString("-", 40)).append("\n");

        double averageBillAmount = totalBillCount > 0 ? totalFinalAmount / totalBillCount : 0;
        double discountRate = totalBilled > 0 ? (totalDiscount / totalBilled) * 100 : 0;

        reportContent.append(String.format("Average Bill Amount: $%.2f\n", averageBillAmount));
        reportContent.append(String.format("Average Discount Rate: %.1f%%\n", discountRate));
        reportContent.append(String.format("Most Popular Service: %s\n", getMostPopularService()));
        reportContent.append(String.format("Highest Revenue Service: %s\n", getHighestRevenueService()));

        reportContent.append("\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("End of Financial Report\n");
        reportContent.append(repeatString("=", 80)).append("\n");
    }

    private String getMostPopularService() {
        return serviceCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    private String getHighestRevenueService() {
        return serviceRevenue.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    // ✅ FIXED: Helper method to repeat strings (replacing "*" operator)
    private String repeatString(String str, int count) {
        return str.repeat(count);
    }

    private String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}