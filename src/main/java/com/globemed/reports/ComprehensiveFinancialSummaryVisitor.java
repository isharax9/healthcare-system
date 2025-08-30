package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Comprehensive Financial Summary Visitor for system-wide financial analysis
 * Fixed to match actual database schema and MedicalBill class structure
 */
public class ComprehensiveFinancialSummaryVisitor implements ReportVisitor {
    private final StringBuilder reportContent = new StringBuilder();
    private final Map<String, PatientFinancialData> patientData = new HashMap<>();
    private final Map<String, Double> serviceRevenue = new HashMap<>();
    private final Map<String, Integer> serviceCount = new HashMap<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // System-wide totals
    private double systemTotalBilled = 0;
    private double systemTotalCollected = 0;
    private double systemTotalPatientPaid = 0;
    private double systemTotalInsurancePaid = 0;
    private double systemTotalOutstanding = 0;
    private int totalPatients = 0;
    private int totalBills = 0;

    @Override
    public void visit(PatientRecord patient) {
        totalPatients++;
        patientData.putIfAbsent(patient.getPatientId(),
                new PatientFinancialData(patient.getName(), patient.getPatientId()));
    }

    @Override
    public void visit(Appointment appointment) {
        // Can track appointment revenue correlation if needed
    }

    @Override
    public void visit(MedicalBill bill) {
        totalBills++;

        // Use correct MedicalBill methods for accurate calculations
        systemTotalBilled += bill.getAmount();
        systemTotalPatientPaid += bill.getAmountPaid();
        systemTotalInsurancePaid += bill.getInsurancePaidAmount();
        systemTotalCollected = systemTotalPatientPaid + systemTotalInsurancePaid;
        systemTotalOutstanding += bill.getRemainingBalance();

        // Track per-patient data
        PatientFinancialData data = patientData.get(bill.getPatientId());
        if (data != null) {
            data.addBill(bill);
        }

        // Track service revenue (use actual collected amount, not billed)
        String serviceName = bill.getServiceDescription();
        double serviceCollected = bill.getTotalCollected();
        serviceRevenue.put(serviceName, serviceRevenue.getOrDefault(serviceName, 0.0) + serviceCollected);
        serviceCount.put(serviceName, serviceCount.getOrDefault(serviceName, 0) + 1);
    }

    @Override
    public String getReport() {
        generateSystemOverview();
        generateFinancialPerformance();
        generateTopPatientsByRevenue();
        generateTopServicesByRevenue();
        generatePaymentAnalysis();
        generateFinancialHealth();

        return reportContent.toString();
    }

    private void generateSystemOverview() {
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("    SYSTEM-WIDE FINANCIAL SUMMARY\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("Report Date: ").append(LocalDate.now().format(dateFormatter)).append("\n");
        reportContent.append("System Version: v1.4\n");
        reportContent.append(repeatString("=", 80)).append("\n\n");

        reportContent.append("📊 SYSTEM OVERVIEW\n");
        reportContent.append(repeatString("-", 60)).append("\n");
        reportContent.append(String.format("Total Patients: %d\n", totalPatients));
        reportContent.append(String.format("Total Bills: %d\n", totalBills));
        reportContent.append(String.format("Total Amount Billed: $%,.2f\n", systemTotalBilled));
        reportContent.append(String.format("Total Amount Collected: $%,.2f\n", systemTotalCollected));
        reportContent.append(String.format("Total Outstanding: $%,.2f\n", systemTotalOutstanding));

        double avgBilledPerPatient = totalPatients > 0 ? systemTotalBilled / totalPatients : 0;
        double avgCollectedPerPatient = totalPatients > 0 ? systemTotalCollected / totalPatients : 0;
        double avgBillAmount = totalBills > 0 ? systemTotalBilled / totalBills : 0;

        reportContent.append(String.format("Average Billed per Patient: $%,.2f\n", avgBilledPerPatient));
        reportContent.append(String.format("Average Collected per Patient: $%,.2f\n", avgCollectedPerPatient));
        reportContent.append(String.format("Average Bill Amount: $%,.2f\n", avgBillAmount));
        reportContent.append("\n");
    }

    private void generateFinancialPerformance() {
        reportContent.append("💰 FINANCIAL PERFORMANCE\n");
        reportContent.append(repeatString("-", 60)).append("\n");

        double collectionRate = systemTotalBilled > 0 ? (systemTotalCollected / systemTotalBilled) * 100 : 0;
        double outstandingRate = systemTotalBilled > 0 ? (systemTotalOutstanding / systemTotalBilled) * 100 : 0;

        reportContent.append(String.format("Collection Rate: %.1f%%\n", collectionRate));
        reportContent.append(String.format("Outstanding Rate: %.1f%%\n", outstandingRate));

        // Payment source breakdown
        if (systemTotalCollected > 0) {
            double insurancePercentage = (systemTotalInsurancePaid / systemTotalCollected) * 100;
            double patientPercentage = (systemTotalPatientPaid / systemTotalCollected) * 100;

            reportContent.append(String.format("Insurance Payments: $%,.2f (%.1f%%)\n",
                    systemTotalInsurancePaid, insurancePercentage));
            reportContent.append(String.format("Patient Direct Payments: $%,.2f (%.1f%%)\n",
                    systemTotalPatientPaid, patientPercentage));
        }

        // Performance indicators
        if (collectionRate >= 95) {
            reportContent.append("🟢 Excellent collection performance\n");
        } else if (collectionRate >= 85) {
            reportContent.append("🟡 Good collection performance\n");
        } else {
            reportContent.append("🔴 Collection performance needs improvement\n");
        }

        reportContent.append("\n");
    }

    private void generateTopPatientsByRevenue() {
        reportContent.append("👥 TOP 10 PATIENTS BY REVENUE\n");
        reportContent.append(repeatString("-", 80)).append("\n");
        reportContent.append(String.format("%-20s | %-12s | %-6s | %-12s | %-12s | %-10s\n",
                "Patient Name", "Patient ID", "Bills", "Billed", "Collected", "Outstanding"));
        reportContent.append(repeatString("-", 80)).append("\n");

        patientData.values().stream()
                .sorted((p1, p2) -> Double.compare(p2.getTotalCollected(), p1.getTotalCollected()))
                .limit(10)
                .forEach(patient -> {
                    reportContent.append(String.format("%-20s | %-12s | %-6d | $%-11.2f | $%-11.2f | $%-9.2f\n",
                            truncateString(patient.getName(), 20),
                            patient.getPatientId(),
                            patient.getBillCount(),
                            patient.getTotalBilled(),
                            patient.getTotalCollected(),
                            patient.getTotalOutstanding()));
                });
        reportContent.append("\n");
    }

    private void generateTopServicesByRevenue() {
        reportContent.append("🏥 TOP 10 SERVICES BY REVENUE\n");
        reportContent.append(repeatString("-", 70)).append("\n");
        reportContent.append(String.format("%-35s | %-8s | %-15s | %-10s\n",
                "Service", "Count", "Revenue", "Avg/Service"));
        reportContent.append(repeatString("-", 70)).append("\n");

        serviceRevenue.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    String service = entry.getKey();
                    double revenue = entry.getValue();
                    int count = serviceCount.get(service);
                    double average = count > 0 ? revenue / count : 0;

                    reportContent.append(String.format("%-35s | %-8d | $%-14.2f | $%-9.2f\n",
                            truncateString(service, 35), count, revenue, average));
                });
        reportContent.append("\n");
    }

    private void generatePaymentAnalysis() {
        reportContent.append("💳 PAYMENT SOURCE ANALYSIS\n");
        reportContent.append(repeatString("-", 60)).append("\n");

        if (systemTotalCollected > 0) {
            double insuranceRatio = systemTotalInsurancePaid / systemTotalCollected;
            double patientRatio = systemTotalPatientPaid / systemTotalCollected;

            reportContent.append(String.format("Insurance Dependency Ratio: %.2f\n", insuranceRatio));
            reportContent.append(String.format("Patient Payment Ratio: %.2f\n", patientRatio));

            if (insuranceRatio > 0.7) {
                reportContent.append("⚠️  High dependency on insurance payments\n");
            } else if (insuranceRatio < 0.3) {
                reportContent.append("⚠️  Low insurance coverage - consider partnerships\n");
            } else {
                reportContent.append("✅ Balanced payment mix\n");
            }
        }

        // Calculate average payments per bill type
        long billsWithInsurance = patientData.values().stream()
                .mapToInt(PatientFinancialData::getBillsWithInsurance)
                .sum();
        long billsWithPatientPayments = patientData.values().stream()
                .mapToInt(PatientFinancialData::getBillsWithPatientPayments)
                .sum();

        if (billsWithInsurance > 0) {
            double avgInsurancePayment = systemTotalInsurancePaid / billsWithInsurance;
            reportContent.append(String.format("Average Insurance Payment: $%.2f (%d bills)\n",
                    avgInsurancePayment, billsWithInsurance));
        }

        if (billsWithPatientPayments > 0) {
            double avgPatientPayment = systemTotalPatientPaid / billsWithPatientPayments;
            reportContent.append(String.format("Average Patient Payment: $%.2f (%d bills)\n",
                    avgPatientPayment, billsWithPatientPayments));
        }

        reportContent.append("\n");
    }

    private void generateFinancialHealth() {
        reportContent.append("💊 FINANCIAL HEALTH INDICATORS\n");
        reportContent.append(repeatString("-", 60)).append("\n");

        double avgBillsPerPatient = totalPatients > 0 ? (double) totalBills / totalPatients : 0;
        double revenueConcentration = calculateRevenueConcentration();

        reportContent.append(String.format("Average Bills per Patient: %.1f\n", avgBillsPerPatient));
        reportContent.append(String.format("Revenue Concentration (Top 20%%): %.1f%%\n", revenueConcentration));

        // Service diversity
        reportContent.append(String.format("Service Diversity: %d unique services\n", serviceRevenue.size()));

        // Financial health score
        double healthScore = calculateFinancialHealthScore();
        reportContent.append(String.format("Financial Health Score: %.1f/100\n", healthScore));

        if (healthScore >= 80) {
            reportContent.append("🟢 Excellent financial health\n");
        } else if (healthScore >= 60) {
            reportContent.append("🟡 Good financial health\n");
        } else {
            reportContent.append("🔴 Financial health needs attention\n");
        }

        // Key recommendations
        reportContent.append("\n💡 KEY RECOMMENDATIONS:\n");
        if (revenueConcentration > 60) {
            reportContent.append("  • Diversify patient base to reduce concentration risk\n");
        }
        if (systemTotalOutstanding / systemTotalBilled > 0.2) {
            reportContent.append("  • Improve collection processes for outstanding amounts\n");
        }
        if (serviceRevenue.size() < 5) {
            reportContent.append("  • Consider expanding service offerings\n");
        }

        reportContent.append("\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("End of Comprehensive Financial Summary\n");
        reportContent.append(repeatString("=", 80)).append("\n");
    }

    private double calculateRevenueConcentration() {
        if (patientData.isEmpty()) return 0;

        List<Double> revenues = patientData.values().stream()
                .map(PatientFinancialData::getTotalCollected)
                .sorted(Collections.reverseOrder())
                .toList();

        int top20Percent = Math.max(1, revenues.size() / 5); // Top 20%
        double top20Revenue = revenues.stream().limit(top20Percent).mapToDouble(Double::doubleValue).sum();

        return systemTotalCollected > 0 ? (top20Revenue / systemTotalCollected) * 100 : 0;
    }

    private double calculateFinancialHealthScore() {
        double collectionRate = systemTotalBilled > 0 ? (systemTotalCollected / systemTotalBilled) * 100 : 0;
        double diversityScore = Math.min(100, serviceRevenue.size() * 10); // Max 10 services for full score
        double concentrationPenalty = Math.max(0, calculateRevenueConcentration() - 50); // Penalty for >50% concentration

        return Math.max(0, Math.min(100, collectionRate * 0.6 + diversityScore * 0.3 - concentrationPenalty * 0.1));
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

    // Enhanced PatientFinancialData class with accurate tracking
    private static class PatientFinancialData {
        private final String name;
        private final String patientId;
        private double totalBilled = 0;
        private double totalCollected = 0;
        private double totalPatientPaid = 0;
        private double totalInsurancePaid = 0;
        private double totalOutstanding = 0;
        private int billCount = 0;
        private int billsWithInsurance = 0;
        private int billsWithPatientPayments = 0;

        public PatientFinancialData(String name, String patientId) {
            this.name = name;
            this.patientId = patientId;
        }

        public void addBill(MedicalBill bill) {
            billCount++;
            totalBilled += bill.getAmount();
            totalPatientPaid += bill.getAmountPaid();
            totalInsurancePaid += bill.getInsurancePaidAmount();
            totalCollected = totalPatientPaid + totalInsurancePaid;
            totalOutstanding += bill.getRemainingBalance();

            if (bill.hasInsuranceCoverage()) {
                billsWithInsurance++;
            }
            if (bill.hasPatientPayment()) {
                billsWithPatientPayments++;
            }
        }

        // Getters
        public String getName() { return name; }
        public String getPatientId() { return patientId; }
        public double getTotalBilled() { return totalBilled; }
        public double getTotalCollected() { return totalCollected; }
        public double getTotalOutstanding() { return totalOutstanding; }
        public int getBillCount() { return billCount; }
        public int getBillsWithInsurance() { return billsWithInsurance; }
        public int getBillsWithPatientPayments() { return billsWithPatientPayments; }
    }
}