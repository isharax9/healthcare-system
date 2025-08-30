package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ServiceRevenueBreakdownVisitor implements ReportVisitor {
    private final StringBuilder reportContent = new StringBuilder();
    private final Map<String, ServiceData> serviceDetails = new HashMap<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private double totalSystemRevenue = 0;
    private int totalBills = 0;

    @Override
    public void visit(PatientRecord patient) {
        // Initialize report header only once
        if (reportContent.length() == 0) {
            reportContent.append(repeatString("=", 80)).append("\n");
            reportContent.append("    SERVICE REVENUE BREAKDOWN REPORT\n");
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
        totalBills++;
        totalSystemRevenue += bill.getFinalAmount();

        String serviceName = bill.getServiceDescription();
        ServiceData data = serviceDetails.getOrDefault(serviceName, new ServiceData(serviceName));
        data.addBill(bill);
        serviceDetails.put(serviceName, data);
    }

    @Override
    public String getReport() {
        generateOverview();
        generateDetailedBreakdown();
        generatePerformanceMetrics();
        generateServiceRecommendations();
        return reportContent.toString();
    }

    private void generateOverview() {
        reportContent.append("📊 SERVICE OVERVIEW\n");
        reportContent.append(repeatString("-", 50)).append("\n");
        reportContent.append(String.format("Total Services Offered: %d\n", serviceDetails.size()));
        reportContent.append(String.format("Total Bills Generated: %d\n", totalBills));
        reportContent.append(String.format("Total System Revenue: $%,.2f\n", totalSystemRevenue));
        reportContent.append(String.format("Average Revenue per Service: $%,.2f\n",
                serviceDetails.size() > 0 ? totalSystemRevenue / serviceDetails.size() : 0));
        reportContent.append("\n");
    }

    private void generateDetailedBreakdown() {
        reportContent.append("🏥 DETAILED SERVICE BREAKDOWN\n");
        reportContent.append(repeatString("-", 90)).append("\n");
        reportContent.append(String.format("%-30s | %-8s | %-12s | %-12s | %-12s | %-8s\n",
                "Service Name", "Count", "Total Rev", "Avg/Bill", "Min Bill", "Max Bill"));
        reportContent.append(repeatString("-", 90)).append("\n");

        serviceDetails.values().stream()
                .sorted((s1, s2) -> Double.compare(s2.getTotalRevenue(), s1.getTotalRevenue()))
                .forEach(service -> {
                    double percentage = (service.getTotalRevenue() / totalSystemRevenue) * 100;
                    reportContent.append(String.format("%-30s | %-8d | $%-11.2f | $%-11.2f | $%-11.2f | $%-7.2f\n",
                            truncateString(service.getServiceName(), 30),
                            service.getBillCount(),
                            service.getTotalRevenue(),
                            service.getAverageAmount(),
                            service.getMinAmount(),
                            service.getMaxAmount()));
                    reportContent.append(String.format("%-30s   Revenue Share: %.1f%%\n\n", "", percentage));
                });
    }

    private void generatePerformanceMetrics() {
        reportContent.append("📈 SERVICE PERFORMANCE METRICS\n");
        reportContent.append(repeatString("-", 60)).append("\n");

        ServiceData topService = serviceDetails.values().stream()
                .max(Comparator.comparing(ServiceData::getTotalRevenue))
                .orElse(null);

        ServiceData mostPopular = serviceDetails.values().stream()
                .max(Comparator.comparing(ServiceData::getBillCount))
                .orElse(null);

        ServiceData highestAvg = serviceDetails.values().stream()
                .max(Comparator.comparing(ServiceData::getAverageAmount))
                .orElse(null);

        if (topService != null) {
            reportContent.append(String.format("🥇 Top Revenue Service: %s ($%,.2f)\n", topService.getServiceName(), topService.getTotalRevenue()));
        }
        if (mostPopular != null) {
            reportContent.append(String.format("🏆 Most Popular Service: %s (%d bills)\n", mostPopular.getServiceName(), mostPopular.getBillCount()));
        }
        if (highestAvg != null) {
            reportContent.append(String.format("💰 Highest Avg Revenue: %s ($%.2f/bill)\n", highestAvg.getServiceName(), highestAvg.getAverageAmount()));
        }
        reportContent.append("\n");
    }

    private void generateServiceRecommendations() {
        reportContent.append("💡 SERVICE RECOMMENDATIONS\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        List<ServiceData> topServices = serviceDetails.values().stream()
                .sorted((s1, s2) -> Double.compare(s2.getTotalRevenue(), s1.getTotalRevenue()))
                .limit(3)
                .toList();

        List<ServiceData> lowPerformers = serviceDetails.values().stream()
                .sorted(Comparator.comparing(ServiceData::getTotalRevenue))
                .limit(3)
                .toList();

        reportContent.append("🎯 Focus on Top Performers:\n");
        topServices.forEach(service -> {
            reportContent.append(String.format("   - %s: Consider capacity expansion\n", service.getServiceName()));
        });

        reportContent.append("\n⚠️  Review Low Performers:\n");
        lowPerformers.forEach(service -> {
            reportContent.append(String.format("   - %s: Analyze pricing or marketing strategy\n", service.getServiceName()));
        });

        reportContent.append("\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("End of Service Revenue Breakdown Report\n");
        reportContent.append(repeatString("=", 80)).append("\n");
    }

    private String repeatString(String str, int count) {
        return str.repeat(count);
    }

    private String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    private static class ServiceData {
        private final String serviceName;
        private double totalRevenue = 0;
        private int billCount = 0;
        private double minAmount = Double.MAX_VALUE;
        private double maxAmount = 0;

        public ServiceData(String serviceName) {
            this.serviceName = serviceName;
        }

        public void addBill(MedicalBill bill) {
            double amount = bill.getFinalAmount();
            totalRevenue += amount;
            billCount++;

            if (amount < minAmount) minAmount = amount;
            if (amount > maxAmount) maxAmount = amount;
        }

        public String getServiceName() { return serviceName; }
        public double getTotalRevenue() { return totalRevenue; }
        public int getBillCount() { return billCount; }
        public double getAverageAmount() { return billCount > 0 ? totalRevenue / billCount : 0; }
        public double getMinAmount() { return minAmount == Double.MAX_VALUE ? 0 : minAmount; }
        public double getMaxAmount() { return maxAmount; }
    }
}