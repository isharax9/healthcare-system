package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Revenue Analysis Visitor - Fixed to match actual database schema
 * Provides detailed revenue insights, trends, and performance analysis
 */
public class RevenueAnalysisVisitor implements ReportVisitor {
    private final StringBuilder reportContent = new StringBuilder();
    private final Map<String, Double> serviceRevenue = new HashMap<>();
    private final Map<String, Integer> serviceVolume = new HashMap<>();
    private final Map<String, RevenueMetrics> serviceMetrics = new HashMap<>();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private double totalBilled = 0;
    private double totalCollected = 0;
    private double totalPatientRevenue = 0;
    private double totalInsuranceRevenue = 0;
    private double totalOutstanding = 0;
    private double highestBill = 0;
    private double lowestBill = Double.MAX_VALUE;
    private int totalBills = 0;

    @Override
    public void visit(PatientRecord patient) {
        if (reportContent.length() == 0) {
            reportContent.append(repeatString("=", 90)).append("\n");
            reportContent.append("    COMPREHENSIVE REVENUE ANALYSIS REPORT\n");
            reportContent.append(repeatString("=", 90)).append("\n");
            reportContent.append("Report Date: ").append(LocalDate.now().format(dateTimeFormatter)).append("\n");
            reportContent.append("System Version: v1.4\n");
            reportContent.append(repeatString("=", 90)).append("\n\n");
        }
    }

    @Override
    public void visit(Appointment appointment) {
        // Could correlate appointment data with revenue if needed
    }

    @Override
    public void visit(MedicalBill bill) {
        totalBills++;
        double billedAmount = bill.getAmount();
        double collectedAmount = bill.getTotalCollected();

        totalBilled += billedAmount;
        totalCollected += collectedAmount;
        totalPatientRevenue += bill.getAmountPaid();
        totalInsuranceRevenue += bill.getInsurancePaidAmount();
        totalOutstanding += bill.getRemainingBalance();

        // Track highest and lowest bills
        if (billedAmount > highestBill) {
            highestBill = billedAmount;
        }
        if (billedAmount < lowestBill && billedAmount > 0) {
            lowestBill = billedAmount;
        }

        // Track service revenue and metrics
        String service = bill.getServiceDescription();
        serviceRevenue.put(service, serviceRevenue.getOrDefault(service, 0.0) + collectedAmount);
        serviceVolume.put(service, serviceVolume.getOrDefault(service, 0) + 1);

        // Update service metrics
        RevenueMetrics metrics = serviceMetrics.getOrDefault(service, new RevenueMetrics(service));
        metrics.addBill(bill);
        serviceMetrics.put(service, metrics);
    }

    @Override
    public String getReport() {
        generateRevenueOverview();
        generateRevenuePerformance();
        generateServicePerformance();
        generateRevenueMetrics();
        generateRevenueDistribution();
        generateGrowthAnalysis();
        generateRecommendations();

        return reportContent.toString();
    }

    private void generateRevenueOverview() {
        reportContent.append("💰 REVENUE OVERVIEW\n");
        reportContent.append(repeatString("-", 60)).append("\n");
        reportContent.append(String.format("Total Bills Analyzed: %d\n", totalBills));
        reportContent.append(String.format("Total Billed Amount: $%,.2f\n", totalBilled));
        reportContent.append(String.format("Total Collected Revenue: $%,.2f\n", totalCollected));
        reportContent.append(String.format("Total Outstanding: $%,.2f\n", totalOutstanding));

        double revenueRealizationRate = totalBilled > 0 ? (totalCollected / totalBilled) * 100 : 0;
        reportContent.append(String.format("Revenue Realization Rate: %.1f%%\n", revenueRealizationRate));

        reportContent.append(String.format("Highest Single Bill: $%,.2f\n", highestBill));
        reportContent.append(String.format("Lowest Single Bill: $%,.2f\n",
                lowestBill == Double.MAX_VALUE ? 0 : lowestBill));

        double avgBillAmount = totalBills > 0 ? totalBilled / totalBills : 0;
        double avgRevenuePerBill = totalBills > 0 ? totalCollected / totalBills : 0;

        reportContent.append(String.format("Average Bill Amount: $%,.2f\n", avgBillAmount));
        reportContent.append(String.format("Average Revenue per Bill: $%,.2f\n", avgRevenuePerBill));
        reportContent.append(String.format("Number of Services: %d\n", serviceRevenue.size()));
        reportContent.append("\n");
    }

    private void generateRevenuePerformance() {
        reportContent.append("📊 REVENUE PERFORMANCE ANALYSIS\n");
        reportContent.append(repeatString("-", 70)).append("\n");

        // Revenue source breakdown
        if (totalCollected > 0) {
            double patientRevenuePercentage = (totalPatientRevenue / totalCollected) * 100;
            double insuranceRevenuePercentage = (totalInsuranceRevenue / totalCollected) * 100;

            reportContent.append(String.format("Patient Direct Revenue: $%,.2f (%.1f%%)\n",
                    totalPatientRevenue, patientRevenuePercentage));
            reportContent.append(String.format("Insurance Revenue: $%,.2f (%.1f%%)\n",
                    totalInsuranceRevenue, insuranceRevenuePercentage));
        }

        // Collection efficiency
        double collectionEfficiency = totalBilled > 0 ? (totalCollected / totalBilled) * 100 : 0;
        double outstandingRate = totalBilled > 0 ? (totalOutstanding / totalBilled) * 100 : 0;

        reportContent.append(String.format("Collection Efficiency: %.1f%%\n", collectionEfficiency));
        reportContent.append(String.format("Outstanding Rate: %.1f%%\n", outstandingRate));

        // Performance indicators
        if (collectionEfficiency >= 95) {
            reportContent.append("Revenue Performance: 🟢 Excellent\n");
        } else if (collectionEfficiency >= 85) {
            reportContent.append("Revenue Performance: 🟡 Good\n");
        } else if (collectionEfficiency >= 70) {
            reportContent.append("Revenue Performance: 🟠 Fair\n");
        } else {
            reportContent.append("Revenue Performance: 🔴 Needs Improvement\n");
        }

        // Revenue mix analysis
        if (totalInsuranceRevenue > totalPatientRevenue * 2) {
            reportContent.append("Revenue Mix: Insurance-dependent model\n");
        } else if (totalPatientRevenue > totalInsuranceRevenue * 2) {
            reportContent.append("Revenue Mix: Patient-pay model\n");
        } else {
            reportContent.append("Revenue Mix: Balanced insurance/patient model\n");
        }
        reportContent.append("\n");
    }

    private void generateServicePerformance() {
        reportContent.append("🏥 SERVICE REVENUE PERFORMANCE\n");
        reportContent.append(repeatString("-", 110)).append("\n");
        reportContent.append(String.format("%-30s | %-6s | %-12s | %-12s | %-12s | %-12s | %-8s | %-8s\n",
                "Service", "Vol", "Revenue", "Avg Revenue", "Collection%", "Outstanding", "% Total", "Rank"));
        reportContent.append(repeatString("-", 110)).append("\n");

        List<RevenueMetrics> sortedServices = new ArrayList<>(serviceMetrics.values());
        sortedServices.sort((s1, s2) -> Double.compare(s2.getTotalRevenue(), s1.getTotalRevenue()));

        for (int i = 0; i < sortedServices.size(); i++) {
            RevenueMetrics metrics = sortedServices.get(i);
            double percentage = totalCollected > 0 ? (metrics.getTotalRevenue() / totalCollected) * 100 : 0;

            reportContent.append(String.format("%-30s | %-6d | $%-11.2f | $%-11.2f | %10.1f%% | $%-11.2f | %6.1f%% | %-6d\n",
                    truncateString(metrics.getServiceName(), 30),
                    metrics.getVolume(),
                    metrics.getTotalRevenue(),
                    metrics.getAverageRevenue(),
                    metrics.getCollectionRate(),
                    metrics.getTotalOutstanding(),
                    percentage,
                    i + 1));
        }
        reportContent.append("\n");
    }

    private void generateRevenueMetrics() {
        reportContent.append("📈 KEY REVENUE METRICS\n");
        reportContent.append(repeatString("-", 60)).append("\n");

        double averageServiceRevenue = serviceRevenue.size() > 0 ? totalCollected / serviceRevenue.size() : 0;

        // Revenue concentration analysis (Pareto analysis)
        List<Double> sortedRevenues = serviceRevenue.values().stream()
                .sorted(Collections.reverseOrder())
                .toList();

        int top20PercentCount = Math.max(1, (int) Math.ceil(sortedRevenues.size() * 0.2));
        double top20PercentRevenue = sortedRevenues.stream()
                .limit(top20PercentCount)
                .mapToDouble(Double::doubleValue)
                .sum();

        double revenueConcentration = totalCollected > 0 ? (top20PercentRevenue / totalCollected) * 100 : 0;

        reportContent.append(String.format("Average Revenue per Service: $%,.2f\n", averageServiceRevenue));
        reportContent.append(String.format("Revenue Concentration (80/20 Rule): %.1f%%\n", revenueConcentration));
        reportContent.append(String.format("Service Diversity Index: %d services\n", serviceRevenue.size()));

        // Find top performing services
        List<String> topServices = serviceRevenue.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        reportContent.append("\n🎯 TOP REVENUE GENERATORS:\n");
        for (int i = 0; i < topServices.size(); i++) {
            String service = topServices.get(i);
            double revenue = serviceRevenue.get(service);
            double percentage = totalCollected > 0 ? (revenue / totalCollected) * 100 : 0;
            reportContent.append(String.format("%d. %s: $%,.2f (%.1f%%)\n",
                    i + 1, service, revenue, percentage));
        }

        // Revenue efficiency metrics
        double revenuePerService = serviceRevenue.size() > 0 ? totalCollected / serviceRevenue.size() : 0;
        double revenuePerBill = totalBills > 0 ? totalCollected / totalBills : 0;

        reportContent.append(String.format("\nRevenue Efficiency per Service Type: $%.2f\n", revenuePerService));
        reportContent.append(String.format("Revenue Efficiency per Bill: $%.2f\n", revenuePerBill));
        reportContent.append("\n");
    }

    private void generateRevenueDistribution() {
        reportContent.append("📊 REVENUE DISTRIBUTION ANALYSIS\n");
        reportContent.append(repeatString("-", 60)).append("\n");

        // Bill amount distribution
        int smallBills = 0, mediumBills = 0, largeBills = 0;
        double smallTotal = 0, mediumTotal = 0, largeTotal = 0;

        for (RevenueMetrics metrics : serviceMetrics.values()) {
            for (double amount : metrics.getBillAmounts()) {
                if (amount <= 100) {
                    smallBills++;
                    smallTotal += amount;
                } else if (amount <= 500) {
                    mediumBills++;
                    mediumTotal += amount;
                } else {
                    largeBills++;
                    largeTotal += amount;
                }
            }
        }

        reportContent.append("Bill Size Distribution:\n");
        reportContent.append(String.format("Small Bills (≤$100): %d bills, $%.2f total\n", smallBills, smallTotal));
        reportContent.append(String.format("Medium Bills ($101-$500): %d bills, $%.2f total\n", mediumBills, mediumTotal));
        reportContent.append(String.format("Large Bills (>$500): %d bills, $%.2f total\n", largeBills, largeTotal));

        // Revenue quartile analysis
        double q1Revenue = totalCollected * 0.25;
        double q2Revenue = totalCollected * 0.5;
        double q3Revenue = totalCollected * 0.75;

        reportContent.append(String.format("\nRevenue Quartiles:\n"));
        reportContent.append(String.format("Q1 (25%%): $%.2f\n", q1Revenue));
        reportContent.append(String.format("Q2 (50%%): $%.2f\n", q2Revenue));
        reportContent.append(String.format("Q3 (75%%): $%.2f\n", q3Revenue));
        reportContent.append("\n");
    }

    private void generateGrowthAnalysis() {
        reportContent.append("🚀 REVENUE GROWTH POTENTIAL\n");
        reportContent.append(repeatString("-", 60)).append("\n");

        // Identify growth opportunities
        List<RevenueMetrics> underperformingServices = serviceMetrics.values().stream()
                .filter(s -> s.getCollectionRate() < 80)
                .sorted((s1, s2) -> Double.compare(s2.getTotalBilled(), s1.getTotalBilled()))
                .toList();

        if (!underperformingServices.isEmpty()) {
            reportContent.append("🔍 REVENUE OPTIMIZATION OPPORTUNITIES:\n");
            underperformingServices.stream().limit(5).forEach(service -> {
                double potentialRevenue = service.getTotalBilled() - service.getTotalRevenue();
                reportContent.append(String.format("  • %s: $%.2f potential (%.1f%% collection rate)\n",
                        service.getServiceName(), potentialRevenue, service.getCollectionRate()));
            });

            double totalPotential = underperformingServices.stream()
                    .mapToDouble(s -> s.getTotalBilled() - s.getTotalRevenue())
                    .sum();
            reportContent.append(String.format("Total Optimization Potential: $%,.2f\n", totalPotential));
        }

        // High-value service expansion opportunities
        reportContent.append("\n💎 HIGH-VALUE SERVICE EXPANSION:\n");
        serviceMetrics.values().stream()
                .filter(s -> s.getAverageRevenue() > 300)
                .sorted((s1, s2) -> Double.compare(s2.getAverageRevenue(), s1.getAverageRevenue()))
                .limit(3)
                .forEach(service -> {
                    reportContent.append(String.format("  • %s: $%.2f avg revenue (%d volume)\n",
                            service.getServiceName(), service.getAverageRevenue(), service.getVolume()));
                });
        reportContent.append("\n");
    }

    private void generateRecommendations() {
        reportContent.append("💡 STRATEGIC REVENUE RECOMMENDATIONS\n");
        reportContent.append(repeatString("-", 60)).append("\n");

        double collectionRate = totalBilled > 0 ? (totalCollected / totalBilled) * 100 : 0;

        // Collection improvement recommendations
        if (collectionRate < 85) {
            reportContent.append("🔴 URGENT COLLECTION IMPROVEMENTS:\n");
            reportContent.append("  • Implement aggressive collection procedures\n");
            reportContent.append("  • Review pricing strategies\n");
            reportContent.append("  • Enhance patient payment options\n");
        } else if (collectionRate < 95) {
            reportContent.append("🟡 COLLECTION ENHANCEMENTS:\n");
            reportContent.append("  • Optimize payment processes\n");
            reportContent.append("  • Improve insurance verification\n");
        }

        // Service portfolio recommendations
        List<String> topServices = serviceRevenue.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        reportContent.append("\n🎯 SERVICE PORTFOLIO OPTIMIZATION:\n");
        reportContent.append("  • Focus marketing on top revenue services:\n");
        topServices.forEach(service ->
                reportContent.append(String.format("    - %s\n", service)));

        // Revenue diversification
        if (serviceRevenue.size() < 5) {
            reportContent.append("  • Consider expanding service offerings\n");
        }

        double revenueConcentration = calculateRevenueConcentration();
        if (revenueConcentration > 60) {
            reportContent.append("  • Diversify revenue sources to reduce concentration risk\n");
        }

        // Pricing optimization
        reportContent.append("\n💰 PRICING OPTIMIZATION:\n");
        reportContent.append("  • Review pricing for high-volume, low-margin services\n");
        reportContent.append("  • Consider value-based pricing models\n");
        reportContent.append("  • Analyze competitor pricing strategies\n");

        // Operational efficiency
        reportContent.append("\n⚡ OPERATIONAL EFFICIENCY:\n");
        reportContent.append("  • Streamline billing processes\n");
        reportContent.append("  • Reduce days in accounts receivable\n");
        reportContent.append("  • Implement automated payment follow-up\n");
        reportContent.append("  • Regular revenue cycle analysis\n");

        reportContent.append("\n");
        reportContent.append(repeatString("=", 90)).append("\n");
        reportContent.append("End of Comprehensive Revenue Analysis Report\n");
        reportContent.append(repeatString("=", 90)).append("\n");
    }

    private double calculateRevenueConcentration() {
        List<Double> sortedRevenues = serviceRevenue.values().stream()
                .sorted(Collections.reverseOrder())
                .toList();

        int top20PercentCount = Math.max(1, (int) Math.ceil(sortedRevenues.size() * 0.2));
        double top20PercentRevenue = sortedRevenues.stream()
                .limit(top20PercentCount)
                .mapToDouble(Double::doubleValue)
                .sum();

        return totalCollected > 0 ? (top20PercentRevenue / totalCollected) * 100 : 0;
    }

    private String repeatString(String str, int count) {
        return str.repeat(count);
    }

    private String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    // RevenueMetrics helper class
    private static class RevenueMetrics {
        private final String serviceName;
        private double totalBilled = 0;
        private double totalRevenue = 0;
        private double totalOutstanding = 0;
        private int volume = 0;
        private final List<Double> billAmounts = new ArrayList<>();

        public RevenueMetrics(String serviceName) {
            this.serviceName = serviceName;
        }

        public void addBill(MedicalBill bill) {
            volume++;
            totalBilled += bill.getAmount();
            totalRevenue += bill.getTotalCollected();
            totalOutstanding += bill.getRemainingBalance();
            billAmounts.add(bill.getAmount());
        }

        public String getServiceName() { return serviceName; }
        public double getTotalBilled() { return totalBilled; }
        public double getTotalRevenue() { return totalRevenue; }
        public double getTotalOutstanding() { return totalOutstanding; }
        public int getVolume() { return volume; }
        public List<Double> getBillAmounts() { return billAmounts; }

        public double getAverageRevenue() {
            return volume > 0 ? totalRevenue / volume : 0;
        }

        public double getCollectionRate() {
            return totalBilled > 0 ? (totalRevenue / totalBilled) * 100 : 0;
        }
    }
}