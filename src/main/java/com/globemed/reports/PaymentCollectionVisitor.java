package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Payment Collection Visitor - Fixed to match actual database schema
 * Analyzes payment collection performance and efficiency
 */
public class PaymentCollectionVisitor implements ReportVisitor {
    private final StringBuilder reportContent = new StringBuilder();
    private final Map<String, CollectionData> monthlyCollection = new TreeMap<>();
    private final Map<String, Double> statusCollections = new HashMap<>();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    private final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

    private double totalBilled = 0;
    private double totalPatientCollected = 0;
    private double totalInsuranceCollected = 0;
    private double totalCollected = 0;
    private double totalOutstanding = 0;
    private int totalBills = 0;
    private int billsWithPayments = 0;
    private int fullyPaidBills = 0;

    @Override
    public void visit(PatientRecord patient) {
        if (reportContent.length() == 0) {
            reportContent.append(repeatString("=", 90)).append("\n");
            reportContent.append("    PAYMENT COLLECTION PERFORMANCE REPORT\n");
            reportContent.append(repeatString("=", 90)).append("\n");
            reportContent.append("Report Date: ").append(LocalDate.now().format(dateTimeFormatter)).append("\n");
            reportContent.append("System Version: v1.4\n");
            reportContent.append(repeatString("=", 90)).append("\n\n");
        }
    }

    @Override
    public void visit(Appointment appointment) {
        // Not used for this report
    }

    @Override
    public void visit(MedicalBill bill) {
        totalBills++;
        totalBilled += bill.getAmount();
        totalPatientCollected += bill.getAmountPaid();
        totalInsuranceCollected += bill.getInsurancePaidAmount();
        totalCollected = totalPatientCollected + totalInsuranceCollected;
        totalOutstanding += bill.getRemainingBalance();

        if (bill.getTotalCollected() > 0) {
            billsWithPayments++;
        }

        if (bill.isFullyPaid()) {
            fullyPaidBills++;
        }

        // Track collections by month
        String monthKey = bill.getBilledDateTime().format(monthFormatter);
        String displayMonth = bill.getBilledDateTime().format(displayFormatter);

        CollectionData monthData = monthlyCollection.getOrDefault(monthKey, new CollectionData(monthKey, displayMonth));
        monthData.addBill(bill);
        monthlyCollection.put(monthKey, monthData);

        // Track collections by status
        String status = bill.getStatus();
        double collected = bill.getTotalCollected();
        statusCollections.put(status, statusCollections.getOrDefault(status, 0.0) + collected);
    }

    @Override
    public String getReport() {
        generateCollectionOverview();
        generateCollectionEfficiency();
        generateMonthlyCollectionTrends();
        generatePaymentSourceAnalysis();
        generateCollectionByStatus();
        generatePerformanceMetrics();
        generateRecommendations();
        return reportContent.toString();
    }

    private void generateCollectionOverview() {
        reportContent.append("💰 COLLECTION OVERVIEW\n");
        reportContent.append(repeatString("-", 60)).append("\n");
        reportContent.append(String.format("Total Bills Generated: %d\n", totalBills));
        reportContent.append(String.format("Bills with Payments: %d\n", billsWithPayments));
        reportContent.append(String.format("Fully Paid Bills: %d\n", fullyPaidBills));
        reportContent.append(String.format("Total Amount Billed: $%,.2f\n", totalBilled));
        reportContent.append(String.format("Total Amount Collected: $%,.2f\n", totalCollected));
        reportContent.append(String.format("Total Outstanding: $%,.2f\n", totalOutstanding));

        double collectionRate = totalBilled > 0 ? (totalCollected / totalBilled) * 100 : 0;
        double paymentRate = totalBills > 0 ? ((double) billsWithPayments / totalBills) * 100 : 0;
        double fullPaymentRate = totalBills > 0 ? ((double) fullyPaidBills / totalBills) * 100 : 0;

        reportContent.append(String.format("Overall Collection Rate: %.1f%%\n", collectionRate));
        reportContent.append(String.format("Payment Rate: %.1f%%\n", paymentRate));
        reportContent.append(String.format("Full Payment Rate: %.1f%%\n", fullPaymentRate));

        // Collection performance indicator
        if (collectionRate >= 95) {
            reportContent.append("Collection Performance: 🟢 Excellent\n");
        } else if (collectionRate >= 85) {
            reportContent.append("Collection Performance: 🟡 Good\n");
        } else if (collectionRate >= 70) {
            reportContent.append("Collection Performance: 🟠 Fair\n");
        } else {
            reportContent.append("Collection Performance: 🔴 Needs Improvement\n");
        }
        reportContent.append("\n");
    }

    private void generateCollectionEfficiency() {
        reportContent.append("⚡ COLLECTION EFFICIENCY\n");
        reportContent.append(repeatString("-", 60)).append("\n");

        double avgBillAmount = totalBills > 0 ? totalBilled / totalBills : 0;
        double avgCollectionAmount = billsWithPayments > 0 ? totalCollected / billsWithPayments : 0;
        double avgOutstandingAmount = totalBills > 0 ? totalOutstanding / totalBills : 0;

        reportContent.append(String.format("Average Bill Amount: $%.2f\n", avgBillAmount));
        reportContent.append(String.format("Average Collection Amount: $%.2f\n", avgCollectionAmount));
        reportContent.append(String.format("Average Outstanding Amount: $%.2f\n", avgOutstandingAmount));

        // Collection efficiency metrics
        if (totalCollected > 0) {
            double patientCollectionRatio = (totalPatientCollected / totalCollected) * 100;
            double insuranceCollectionRatio = (totalInsuranceCollected / totalCollected) * 100;

            reportContent.append(String.format("Patient Collection Ratio: %.1f%%\n", patientCollectionRatio));
            reportContent.append(String.format("Insurance Collection Ratio: %.1f%%\n", insuranceCollectionRatio));
        }

        // Days Sales Outstanding (simplified calculation)
        if (monthlyCollection.size() > 0) {
            double avgMonthlyBilling = totalBilled / monthlyCollection.size();
            double dso = avgMonthlyBilling > 0 ? (totalOutstanding / avgMonthlyBilling) * 30 : 0;
            reportContent.append(String.format("Estimated Days Sales Outstanding: %.0f days\n", dso));
        }
        reportContent.append("\n");
    }

    private void generateMonthlyCollectionTrends() {
        reportContent.append("📈 MONTHLY COLLECTION TRENDS\n");
        reportContent.append(repeatString("-", 100)).append("\n");
        reportContent.append(String.format("%-10s | %-8s | %-12s | %-12s | %-12s | %-12s | %-8s | %-8s\n",
                "Month", "Bills", "Billed", "Collected", "Patient", "Insurance", "Coll%", "Full%"));
        reportContent.append(repeatString("-", 100)).append("\n");

        monthlyCollection.values().forEach(data -> {
            double collectionRate = data.getTotalBilled() > 0 ?
                    (data.getTotalCollected() / data.getTotalBilled()) * 100 : 0;
            double fullPaymentRate = data.getBillCount() > 0 ?
                    ((double) data.getFullyPaidCount() / data.getBillCount()) * 100 : 0;

            reportContent.append(String.format("%-10s | %-8d | $%-11.2f | $%-11.2f | $%-11.2f | $%-11.2f | %6.1f%% | %6.1f%%\n",
                    data.getDisplayMonth(),
                    data.getBillCount(),
                    data.getTotalBilled(),
                    data.getTotalCollected(),
                    data.getPatientCollected(),
                    data.getInsuranceCollected(),
                    collectionRate,
                    fullPaymentRate));
        });

        // Trend analysis
        if (monthlyCollection.size() >= 2) {
            List<CollectionData> monthlyList = new ArrayList<>(monthlyCollection.values());
            CollectionData firstMonth = monthlyList.get(0);
            CollectionData lastMonth = monthlyList.get(monthlyList.size() - 1);

            double firstMonthRate = firstMonth.getTotalBilled() > 0 ?
                    (firstMonth.getTotalCollected() / firstMonth.getTotalBilled()) * 100 : 0;
            double lastMonthRate = lastMonth.getTotalBilled() > 0 ?
                    (lastMonth.getTotalCollected() / lastMonth.getTotalBilled()) * 100 : 0;

            double trendChange = lastMonthRate - firstMonthRate;

            reportContent.append(String.format("\nCollection Rate Trend: %+.1f%% (%s to %s)\n",
                    trendChange, firstMonth.getDisplayMonth(), lastMonth.getDisplayMonth()));
        }
        reportContent.append("\n");
    }

    private void generatePaymentSourceAnalysis() {
        reportContent.append("💳 PAYMENT SOURCE ANALYSIS\n");
        reportContent.append(repeatString("-", 70)).append("\n");
        reportContent.append(String.format("%-20s | %-15s | %-15s | %-12s\n",
                "Payment Source", "Amount", "% of Total", "Avg/Payment"));
        reportContent.append(repeatString("-", 70)).append("\n");

        int patientPaymentCount = (int) monthlyCollection.values().stream()
                .mapToLong(CollectionData::getBillsWithPatientPayments).sum();
        int insurancePaymentCount = (int) monthlyCollection.values().stream()
                .mapToLong(CollectionData::getBillsWithInsurancePayments).sum();

        if (totalCollected > 0) {
            double patientPercentage = (totalPatientCollected / totalCollected) * 100;
            double insurancePercentage = (totalInsuranceCollected / totalCollected) * 100;

            double avgPatientPayment = patientPaymentCount > 0 ? totalPatientCollected / patientPaymentCount : 0;
            double avgInsurancePayment = insurancePaymentCount > 0 ? totalInsuranceCollected / insurancePaymentCount : 0;

            reportContent.append(String.format("%-20s | $%-14.2f | %13.1f%% | $%-11.2f\n",
                    "Patient Direct", totalPatientCollected, patientPercentage, avgPatientPayment));
            reportContent.append(String.format("%-20s | $%-14.2f | %13.1f%% | $%-11.2f\n",
                    "Insurance", totalInsuranceCollected, insurancePercentage, avgInsurancePayment));
            reportContent.append(repeatString("-", 70)).append("\n");
            reportContent.append(String.format("%-20s | $%-14.2f | %13.1f%% | $%-11.2f\n",
                    "TOTAL", totalCollected, 100.0, totalCollected / Math.max(1, billsWithPayments)));
        }
        reportContent.append("\n");
    }

    private void generateCollectionByStatus() {
        reportContent.append("📊 COLLECTION BY BILL STATUS\n");
        reportContent.append(repeatString("-", 60)).append("\n");
        reportContent.append(String.format("%-15s | %-15s | %-15s\n", "Status", "Amount Collected", "% of Total"));
        reportContent.append(repeatString("-", 60)).append("\n");

        statusCollections.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> {
                    double percentage = totalCollected > 0 ? (entry.getValue() / totalCollected) * 100 : 0;
                    reportContent.append(String.format("%-15s | $%-14.2f | %13.1f%%\n",
                            entry.getKey(), entry.getValue(), percentage));
                });
        reportContent.append("\n");
    }

    private void generatePerformanceMetrics() {
        reportContent.append("📊 KEY PERFORMANCE INDICATORS\n");
        reportContent.append(repeatString("-", 60)).append("\n");

        // Collection KPIs
        double netCollectionRate = totalBilled > 0 ? (totalCollected / totalBilled) * 100 : 0;
        double firstPassResolutionRate = totalBills > 0 ? ((double) fullyPaidBills / totalBills) * 100 : 0;
        double patientResponsibilityRate = totalCollected > 0 ? (totalPatientCollected / totalCollected) * 100 : 0;

        reportContent.append(String.format("Net Collection Rate: %.1f%%\n", netCollectionRate));
        reportContent.append(String.format("First Pass Resolution: %.1f%%\n", firstPassResolutionRate));
        reportContent.append(String.format("Patient Responsibility: %.1f%%\n", patientResponsibilityRate));

        // Benchmark comparison
        reportContent.append("\n🎯 INDUSTRY BENCHMARKS:\n");
        reportContent.append("  Net Collection Rate Target: 95%+\n");
        reportContent.append("  First Pass Resolution Target: 90%+\n");
        reportContent.append("  Days Sales Outstanding Target: <30 days\n");

        // Performance against benchmarks
        if (netCollectionRate >= 95) {
            reportContent.append("✅ Meeting collection rate benchmark\n");
        } else {
            reportContent.append("❌ Below collection rate benchmark\n");
        }

        if (firstPassResolutionRate >= 90) {
            reportContent.append("✅ Meeting resolution rate benchmark\n");
        } else {
            reportContent.append("❌ Below resolution rate benchmark\n");
        }
        reportContent.append("\n");
    }

    private void generateRecommendations() {
        reportContent.append("💡 COLLECTION IMPROVEMENT RECOMMENDATIONS\n");
        reportContent.append(repeatString("-", 60)).append("\n");

        double collectionRate = totalBilled > 0 ? (totalCollected / totalBilled) * 100 : 0;
        double outstandingRate = totalBilled > 0 ? (totalOutstanding / totalBilled) * 100 : 0;

        if (collectionRate < 85) {
            reportContent.append("🔴 CRITICAL COLLECTION ISSUES:\n");
            reportContent.append("  • Implement aggressive collection procedures\n");
            reportContent.append("  • Review and update collection policies\n");
            reportContent.append("  • Consider third-party collection services\n");
            reportContent.append("  • Analyze root causes of collection failures\n");
        } else if (collectionRate < 95) {
            reportContent.append("🟡 COLLECTION IMPROVEMENTS NEEDED:\n");
            reportContent.append("  • Enhance patient payment processes\n");
            reportContent.append("  • Improve insurance verification procedures\n");
            reportContent.append("  • Implement automated payment reminders\n");
            reportContent.append("  • Offer multiple payment methods\n");
        } else {
            reportContent.append("🟢 EXCELLENT COLLECTION PERFORMANCE:\n");
            reportContent.append("  • Maintain current collection practices\n");
            reportContent.append("  • Consider best practice documentation\n");
            reportContent.append("  • Monitor for any performance degradation\n");
        }

        // Specific recommendations
        if (outstandingRate > 20) {
            reportContent.append("  • Focus on aging accounts receivable\n");
            reportContent.append("  • Implement payment plans for large balances\n");
        }

        if (totalInsuranceCollected < totalPatientCollected) {
            reportContent.append("  • Review insurance billing and follow-up processes\n");
            reportContent.append("  • Improve insurance authorization procedures\n");
        }

        reportContent.append("\n📈 PROCESS IMPROVEMENTS:\n");
        reportContent.append("  • Regular collection staff training\n");
        reportContent.append("  • Monthly collection performance reviews\n");
        reportContent.append("  • Patient financial counseling programs\n");
        reportContent.append("  • Technology upgrades for payment processing\n");

        reportContent.append("\n");
        reportContent.append(repeatString("=", 90)).append("\n");
        reportContent.append("End of Payment Collection Performance Report\n");
        reportContent.append(repeatString("=", 90)).append("\n");
    }

    private String repeatString(String str, int count) {
        return str.repeat(count);
    }

    // CollectionData helper class
    private static class CollectionData {
        private final String monthKey;
        private final String displayMonth;
        private double totalBilled = 0;
        private double totalCollected = 0;
        private double patientCollected = 0;
        private double insuranceCollected = 0;
        private int billCount = 0;
        private int fullyPaidCount = 0;
        private int billsWithPatientPayments = 0;
        private int billsWithInsurancePayments = 0;

        public CollectionData(String monthKey, String displayMonth) {
            this.monthKey = monthKey;
            this.displayMonth = displayMonth;
        }

        public void addBill(MedicalBill bill) {
            billCount++;
            totalBilled += bill.getAmount();
            patientCollected += bill.getAmountPaid();
            insuranceCollected += bill.getInsurancePaidAmount();
            totalCollected = patientCollected + insuranceCollected;

            if (bill.isFullyPaid()) {
                fullyPaidCount++;
            }

            if (bill.hasPatientPayment()) {
                billsWithPatientPayments++;
            }

            if (bill.hasInsuranceCoverage()) {
                billsWithInsurancePayments++;
            }
        }

        // Getters
        public String getMonthKey() { return monthKey; }
        public String getDisplayMonth() { return displayMonth; }
        public double getTotalBilled() { return totalBilled; }
        public double getTotalCollected() { return totalCollected; }
        public double getPatientCollected() { return patientCollected; }
        public double getInsuranceCollected() { return insuranceCollected; }
        public int getBillCount() { return billCount; }
        public int getFullyPaidCount() { return fullyPaidCount; }
        public int getBillsWithPatientPayments() { return billsWithPatientPayments; }
        public int getBillsWithInsurancePayments() { return billsWithInsurancePayments; }
    }
}