package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Revenue Analysis Visitor for detailed revenue insights and trends
 */
public class RevenueAnalysisVisitor implements ReportVisitor {
    private final StringBuilder reportContent = new StringBuilder();
    private final Map<String, Double> serviceRevenue = new HashMap<>();
    private final Map<String, Integer> serviceVolume = new HashMap<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private double totalRevenue = 0;
    private double highestBill = 0;
    private double lowestBill = Double.MAX_VALUE;
    private String highestRevenueService = "";
    private double highestServiceRevenue = 0;

    @Override
    public void visit(PatientRecord patient) {
        // Initialize report header
        if (reportContent.length() == 0) {
            reportContent.append(repeatString("=", 80)).append("\n");
            reportContent.append("    REVENUE ANALYSIS REPORT\n");
            reportContent.append(repeatString("=", 80)).append("\n");
            reportContent.append("Generated: ").append(LocalDate.now().format(dateFormatter)).append("\n\n");
        }
    }

    @Override
    public void visit(Appointment appointment) {
        // Could correlate appointment data with revenue
    }

    @Override
    public void visit(MedicalBill bill) {
        double amount = bill.getFinalAmount();
        totalRevenue += amount;

        // Track highest and lowest bills
        if (amount > highestBill) {
            highestBill = amount;
        }
        if (amount < lowestBill && amount > 0) {
            lowestBill = amount;
        }

        // Track service revenue and volume
        String service = bill.getServiceDescription();
        double currentServiceRevenue = serviceRevenue.getOrDefault(service, 0.0) + amount;
        serviceRevenue.put(service, currentServiceRevenue);
        serviceVolume.put(service, serviceVolume.getOrDefault(service, 0) + 1);

        // Track highest revenue service
        if (currentServiceRevenue > highestServiceRevenue) {
            highestServiceRevenue = currentServiceRevenue;
            highestRevenueService = service;
        }
    }

    @Override
    public String getReport() {
        generateRevenueOverview();
        generateServicePerformance();
        generateRevenueMetrics();
        generateRecommendations();

        return reportContent.toString();
    }

    private void generateRevenueOverview() {
        reportContent.append("💰 REVENUE OVERVIEW\n");
        reportContent.append(repeatString("-", 50)).append("\n");
        reportContent.append(String.format("Total Revenue: $%,.2f\n", totalRevenue));
        reportContent.append(String.format("Highest Single Bill: $%,.2f\n", highestBill));
        reportContent.append(String.format("Lowest Single Bill: $%,.2f\n",
                lowestBill == Double.MAX_VALUE ? 0 : lowestBill));
        reportContent.append(String.format("Number of Services: %d\n", serviceRevenue.size()));
        reportContent.append(String.format("Top Revenue Service: %s ($%,.2f)\n",
                highestRevenueService, highestServiceRevenue));
        reportContent.append("\n");
    }

    private void generateServicePerformance() {
        reportContent.append("📊 SERVICE PERFORMANCE ANALYSIS\n");
        reportContent.append(repeatString("-", 80)).append("\n");
        reportContent.append(String.format("%-30s | %-10s | %-15s | %-12s | %-10s\n",
                "Service", "Volume", "Revenue", "Avg/Service", "% of Total"));
        reportContent.append(repeatString("-", 80)).append("\n");

        serviceRevenue.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> {
                    String service = entry.getKey();
                    double revenue = entry.getValue();
                    int volume = serviceVolume.get(service);
                    double average = revenue / volume;
                    double percentage = (revenue / totalRevenue) * 100;

                    reportContent.append(String.format("%-30s | %-10d | $%-14.2f | $%-11.2f | %6.1f%%\n",
                            truncateString(service, 30), volume, revenue, average, percentage));
                });
        reportContent.append("\n");
    }

    private void generateRevenueMetrics() {
        reportContent.append("📈 REVENUE METRICS\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        double averageServiceRevenue = serviceRevenue.size() > 0 ?
                totalRevenue / serviceRevenue.size() : 0;

        // Calculate revenue concentration (Pareto analysis)
        List<Double> sortedRevenues = serviceRevenue.values().stream()
                .sorted(Collections.reverseOrder())
                .toList();

        int top20PercentCount = Math.max(1, (int) Math.ceil(sortedRevenues.size() * 0.2));
        double top20PercentRevenue = sortedRevenues.stream()
                .limit(top20PercentCount)
                .mapToDouble(Double::doubleValue)
                .sum();

        double revenueConcentration = (top20PercentRevenue / totalRevenue) * 100;

        reportContent.append(String.format("Average Revenue per Service: $%,.2f\n", averageServiceRevenue));
        reportContent.append(String.format("Revenue Concentration (80/20 Rule): %.1f%%\n", revenueConcentration));
        reportContent.append(String.format("Service Diversity Index: %d services\n", serviceRevenue.size()));
        reportContent.append("\n");
    }

    private void generateRecommendations() {
        reportContent.append("💡 STRATEGIC RECOMMENDATIONS\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        // Identify top performing services
        List<String> topServices = serviceRevenue.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        reportContent.append("🎯 Focus Areas:\n");
        reportContent.append("1. Top Revenue Services: Continue to promote and optimize\n");
        for (int i = 0; i < topServices.size(); i++) {
            reportContent.append(String.format("   - %s\n", topServices.get(i)));
        }

        reportContent.append("\n2. Revenue Optimization:\n");
        if (serviceRevenue.size() > 10) {
            reportContent.append("   - Consider consolidating low-revenue services\n");
        }
        reportContent.append("   - Analyze pricing strategies for high-volume services\n");
        reportContent.append("   - Focus marketing on top-performing services\n");

        reportContent.append("\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("End of Revenue Analysis Report\n");
        reportContent.append(repeatString("=", 80)).append("\n");
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