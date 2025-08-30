package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Enhanced Financial Report Visitor - Fixed to match actual DB schema and MedicalBill class
 */
public class FinancialReportVisitor implements ReportVisitor {
    private final StringBuilder reportContent = new StringBuilder();
    private final Map<String, Double> serviceRevenue = new HashMap<>();
    private final Map<String, Integer> serviceCount = new HashMap<>();
    private final Map<String, Double> paymentStatusAmounts = new HashMap<>();
    private final List<MedicalBill> outstandingBills = new ArrayList<>();

    private double totalBilled = 0;           // Total 'amount' from bills
    private double totalPatientPaid = 0;      // Total 'amount_paid'
    private double totalInsurancePaid = 0;    // Total 'insurance_paid_amount'
    private double totalCollected = 0;        // Patient + Insurance payments
    private double totalOutstanding = 0;      // Total remaining balance (what patient still owes)
    private int totalBillCount = 0;

    private String patientName = "";
    private String patientId = "";
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void visit(PatientRecord patient) {
        this.patientName = patient.getName();
        this.patientId = patient.getPatientId();

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
        // Could track appointment revenue correlation if needed
    }

    @Override
    public void visit(MedicalBill bill) {
        totalBillCount++;

        // Use correct MedicalBill methods and calculations
        totalBilled += bill.getAmount();                    // Original billed amount
        totalPatientPaid += bill.getAmountPaid();          // Amount paid by patient
        totalInsurancePaid += bill.getInsurancePaidAmount(); // Amount paid by insurance
        totalCollected = totalPatientPaid + totalInsurancePaid;
        totalOutstanding += bill.getRemainingBalance();     // What patient still owes

        // Track service revenue (use actual collected amount)
        String service = bill.getServiceDescription();
        double serviceCollected = bill.getAmountPaid() + bill.getInsurancePaidAmount();
        serviceRevenue.put(service, serviceRevenue.getOrDefault(service, 0.0) + serviceCollected);
        serviceCount.put(service, serviceCount.getOrDefault(service, 0) + 1);

        // Track payment status based on actual bill status and payment state
        String paymentStatus = determinePaymentStatus(bill);
        double statusAmount = getAmountForStatus(bill, paymentStatus);
        paymentStatusAmounts.put(paymentStatus,
                paymentStatusAmounts.getOrDefault(paymentStatus, 0.0) + statusAmount);

        // Track outstanding bills (bills with remaining balance)
        if (bill.getRemainingBalance() > 0) {
            outstandingBills.add(bill);
        }
    }

    private String determinePaymentStatus(MedicalBill bill) {
        String dbStatus = bill.getStatus();
        double remainingBalance = bill.getRemainingBalance();
        double totalPaid = bill.getAmountPaid() + bill.getInsurancePaidAmount();

        if (remainingBalance == 0 && totalPaid > 0) {
            return "Paid";
        } else if (totalPaid > 0 && remainingBalance > 0) {
            return "Partial";
        } else if ("New".equalsIgnoreCase(dbStatus) || "Pending".equalsIgnoreCase(dbStatus)) {
            return "Pending";
        } else if ("Overdue".equalsIgnoreCase(dbStatus)) {
            return "Overdue";
        }
        return dbStatus != null ? dbStatus : "Pending";
    }

    private double getAmountForStatus(MedicalBill bill, String status) {
        switch (status) {
            case "Paid":
                return bill.getAmountPaid() + bill.getInsurancePaidAmount();
            case "Partial":
            case "Pending":
            case "Overdue":
                return bill.getRemainingBalance();
            default:
                return bill.getFinalAmount();
        }
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
        reportContent.append(repeatString("-", 60)).append("\n");
        reportContent.append(String.format("Total Bills Generated: %d\n", totalBillCount));
        reportContent.append(String.format("Total Original Amount: $%,.2f\n", totalBilled));
        reportContent.append(String.format("Total Patient Payments: $%,.2f\n", totalPatientPaid));
        reportContent.append(String.format("Total Insurance Payments: $%,.2f\n", totalInsurancePaid));
        reportContent.append(String.format("Total Collected: $%,.2f\n", totalCollected));
        reportContent.append(String.format("Total Outstanding: $%,.2f\n", totalOutstanding));

        double collectionRate = totalBilled > 0 ? (totalCollected / totalBilled) * 100 : 0;
        reportContent.append(String.format("Collection Rate: %.1f%%\n", collectionRate));

        if (totalCollected > 0) {
            double insurancePercentage = (totalInsurancePaid / totalCollected) * 100;
            double patientPercentage = (totalPatientPaid / totalCollected) * 100;
            reportContent.append(String.format("Insurance Coverage: %.1f%% | Patient Direct: %.1f%%\n",
                    insurancePercentage, patientPercentage));
        }
        reportContent.append("\n");
    }

    private void generateServiceBreakdown() {
        reportContent.append("🏥 SERVICE REVENUE BREAKDOWN\n");
        reportContent.append(repeatString("-", 70)).append("\n");
        reportContent.append(String.format("%-30s | %-8s | %-12s | %-12s\n",
                "Service", "Count", "Revenue", "Avg/Service"));
        reportContent.append(repeatString("-", 70)).append("\n");

        serviceRevenue.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> {
                    String service = entry.getKey();
                    double revenue = entry.getValue();
                    int count = serviceCount.get(service);
                    double average = count > 0 ? revenue / count : 0;

                    reportContent.append(String.format("%-30s | %-8d | $%-11.2f | $%-11.2f\n",
                            truncateString(service, 30), count, revenue, average));
                });
        reportContent.append("\n");
    }

    private void generatePaymentAnalysis() {
        reportContent.append("💰 PAYMENT STATUS ANALYSIS\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        paymentStatusAmounts.forEach((status, amount) -> {
            double percentage = totalBilled > 0 ? (amount / totalBilled) * 100 : 0;
            reportContent.append(String.format("%-15s: $%,.2f (%.1f%%)\n",
                    status, amount, percentage));
        });
        reportContent.append("\n");
    }

    private void generateOutstandingBillsSection() {
        if (!outstandingBills.isEmpty()) {
            reportContent.append("⚠️  OUTSTANDING BILLS\n");
            reportContent.append(repeatString("-", 80)).append("\n");
            reportContent.append(String.format("%-8s | %-25s | %-12s | %-12s | %-15s\n",
                    "Bill ID", "Service", "Amount Due", "Status", "Billed Date"));
            reportContent.append(repeatString("-", 80)).append("\n");

            outstandingBills.stream()
                    .sorted((b1, b2) -> Double.compare(b2.getRemainingBalance(), b1.getRemainingBalance()))
                    .forEach(bill -> {
                        reportContent.append(String.format("%-8d | %-25s | $%-11.2f | %-12s | %-15s\n",
                                bill.getBillId(),
                                truncateString(bill.getServiceDescription(), 25),
                                bill.getRemainingBalance(),
                                bill.getStatus(),
                                bill.getBilledDateTime().toLocalDate().toString()));
                    });
            reportContent.append("\n");
        }
    }

    private void generateFinancialMetrics() {
        reportContent.append("📈 KEY FINANCIAL METRICS\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        double averageBillAmount = totalBillCount > 0 ? totalBilled / totalBillCount : 0;
        double averageCollection = totalBillCount > 0 ? totalCollected / totalBillCount : 0;
        double outstandingRate = totalBilled > 0 ? (totalOutstanding / totalBilled) * 100 : 0;

        reportContent.append(String.format("Average Bill Amount: $%.2f\n", averageBillAmount));
        reportContent.append(String.format("Average Collection per Bill: $%.2f\n", averageCollection));
        reportContent.append(String.format("Outstanding Rate: %.1f%%\n", outstandingRate));
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