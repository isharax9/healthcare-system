package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Comprehensive Financial Summary Visitor for system-wide financial analysis
 */
public class ComprehensiveFinancialSummaryVisitor implements ReportVisitor {
    private final StringBuilder reportContent = new StringBuilder();
    private final Map<String, PatientFinancialData> patientData = new HashMap<>();
    private final Map<String, Double> serviceRevenue = new HashMap<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private double systemTotalRevenue = 0;
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
        // Can track appointment revenue correlation
    }

    @Override
    public void visit(MedicalBill bill) {
        totalBills++;
        systemTotalRevenue += bill.getFinalAmount();

        // Track per-patient data
        PatientFinancialData data = patientData.get(bill.getPatientId());
        if (data != null) {
            data.addBill(bill);
        }

        // Track service revenue
        serviceRevenue.put(bill.getServiceDescription(),
                serviceRevenue.getOrDefault(bill.getServiceDescription(), 0.0) + bill.getFinalAmount());
    }

    @Override
    public String getReport() {
        generateSystemOverview();
        generateTopPatientsByRevenue();
        generateTopServicesByRevenue();
        generateFinancialHealth();

        return reportContent.toString();
    }

    private void generateSystemOverview() {
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("    SYSTEM-WIDE FINANCIAL SUMMARY\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("Report Date: ").append(LocalDate.now().format(dateFormatter)).append("\n\n");

        reportContent.append("📊 SYSTEM OVERVIEW\n");
        reportContent.append(repeatString("-", 50)).append("\n");
        reportContent.append(String.format("Total Patients: %d\n", totalPatients));
        reportContent.append(String.format("Total Bills: %d\n", totalBills));
        reportContent.append(String.format("Total System Revenue: $%,.2f\n", systemTotalRevenue));
        reportContent.append(String.format("Average Revenue per Patient: $%,.2f\n",
                totalPatients > 0 ? systemTotalRevenue / totalPatients : 0));
        reportContent.append(String.format("Average Bill Amount: $%,.2f\n",
                totalBills > 0 ? systemTotalRevenue / totalBills : 0));
        reportContent.append("\n");
    }

    private void generateTopPatientsByRevenue() {
        reportContent.append("👥 TOP 10 PATIENTS BY REVENUE\n");
        reportContent.append(repeatString("-", 60)).append("\n");
        reportContent.append(String.format("%-20s | %-15s | %-10s | %-10s\n",
                "Patient Name", "Patient ID", "Bills", "Revenue"));
        reportContent.append(repeatString("-", 60)).append("\n");

        patientData.values().stream()
                .sorted((p1, p2) -> Double.compare(p2.getTotalRevenue(), p1.getTotalRevenue()))
                .limit(10)
                .forEach(patient -> {
                    reportContent.append(String.format("%-20s | %-15s | %-10d | $%-9.2f\n",
                            truncateString(patient.getName(), 20),
                            patient.getPatientId(),
                            patient.getBillCount(),
                            patient.getTotalRevenue()));
                });
        reportContent.append("\n");
    }

    private void generateTopServicesByRevenue() {
        reportContent.append("🏥 TOP 10 SERVICES BY REVENUE\n");
        reportContent.append(repeatString("-", 50)).append("\n");
        reportContent.append(String.format("%-30s | %-15s\n", "Service", "Revenue"));
        reportContent.append(repeatString("-", 50)).append("\n");

        serviceRevenue.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    reportContent.append(String.format("%-30s | $%-14.2f\n",
                            truncateString(entry.getKey(), 30), entry.getValue()));
                });
        reportContent.append("\n");
    }

    private void generateFinancialHealth() {
        reportContent.append("💊 FINANCIAL HEALTH INDICATORS\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        double avgPatientsPerBill = totalPatients > 0 ? (double) totalBills / totalPatients : 0;

        reportContent.append(String.format("Average Bills per Patient: %.1f\n", avgPatientsPerBill));
        reportContent.append(String.format("Revenue Concentration (Top 10%%): %.1f%%\n",
                calculateRevenueConcentration()));

        reportContent.append("\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("End of Comprehensive Financial Summary\n");
        reportContent.append(repeatString("=", 80)).append("\n");
    }

    private double calculateRevenueConcentration() {
        if (patientData.isEmpty()) return 0;

        List<Double> revenues = patientData.values().stream()
                .map(PatientFinancialData::getTotalRevenue)
                .sorted(Collections.reverseOrder())
                .toList();

        int top10Percent = Math.max(1, revenues.size() / 10);
        double top10Revenue = revenues.stream().limit(top10Percent).mapToDouble(Double::doubleValue).sum();

        return systemTotalRevenue > 0 ? (top10Revenue / systemTotalRevenue) * 100 : 0;
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

    // Helper class to track patient financial data
    private static class PatientFinancialData {
        private final String name;
        private final String patientId;
        private double totalRevenue = 0;
        private int billCount = 0;

        public PatientFinancialData(String name, String patientId) {
            this.name = name;
            this.patientId = patientId;
        }

        public void addBill(MedicalBill bill) {
            totalRevenue += bill.getFinalAmount();
            billCount++;
        }

        public String getName() { return name; }
        public String getPatientId() { return patientId; }
        public double getTotalRevenue() { return totalRevenue; }
        public int getBillCount() { return billCount; }
    }
}