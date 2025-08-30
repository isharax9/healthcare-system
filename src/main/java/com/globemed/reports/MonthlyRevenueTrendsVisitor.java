package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Monthly Revenue Trends Visitor - Fixed to use actual billing dates and accurate calculations
 * Analyzes revenue trends over time based on bill dates and payment patterns
 */
public class MonthlyRevenueTrendsVisitor implements ReportVisitor {
    private final StringBuilder reportContent = new StringBuilder();
    private final Map<String, MonthlyData> monthlyRevenue = new TreeMap<>(); // TreeMap for sorted dates
    private final Map<String, MonthlyData> monthlyBilled = new TreeMap<>();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    private final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

    private double totalRevenue = 0;
    private double totalBilled = 0;
    private int totalBills = 0;

    @Override
    public void visit(PatientRecord patient) {
        if (reportContent.length() == 0) {
            reportContent.append(repeatString("=", 90)).append("\n");
            reportContent.append("    MONTHLY REVENUE TRENDS REPORT\n");
            reportContent.append(repeatString("=", 90)).append("\n");
            reportContent.append("Report Date: ").append(LocalDate.now().format(dateTimeFormatter)).append("\n");
            reportContent.append("System Version: v1.4\n");
            reportContent.append(repeatString("=", 90)).append("\n\n");
        }
    }

    @Override
    public void visit(Appointment appointment) {
        // Could use appointment dates for trend correlation if needed
    }

    @Override
    public void visit(MedicalBill bill) {
        totalBills++;
        totalBilled += bill.getAmount();
        totalRevenue += bill.getTotalCollected();

        // Extract month from billing date
        LocalDateTime billedDateTime = bill.getBilledDateTime();
        String monthKey = billedDateTime.format(monthFormatter); // "2025-08"
        String displayMonth = billedDateTime.format(displayFormatter); // "Aug 2025"

        // Track monthly billing amounts
        MonthlyData billedData = monthlyBilled.getOrDefault(monthKey, new MonthlyData(monthKey, displayMonth));
        billedData.addBilledAmount(bill.getAmount());
        monthlyBilled.put(monthKey, billedData);

        // Track monthly collected amounts
        MonthlyData collectedData = monthlyRevenue.getOrDefault(monthKey, new MonthlyData(monthKey, displayMonth));
        collectedData.addCollectedAmount(bill.getTotalCollected());
        collectedData.addPatientPayment(bill.getAmountPaid());
        collectedData.addInsurancePayment(bill.getInsurancePaidAmount());
        collectedData.addOutstanding(bill.getRemainingBalance());
        collectedData.incrementBillCount();
        monthlyRevenue.put(monthKey, collectedData);
    }

    @Override
    public String getReport() {
        generateOverview();
        generateMonthlyBreakdown();
        generateTrendAnalysis();
        generateGrowthAnalysis();
        generateSeasonalAnalysis();
        generateProjections();
        return reportContent.toString();
    }

    private void generateOverview() {
        reportContent.append("📈 REVENUE TRENDS OVERVIEW\n");
        reportContent.append(repeatString("-", 60)).append("\n");
        reportContent.append(String.format("Analysis Period: %s to %s\n",
                getEarliestMonth(), getLatestMonth()));
        reportContent.append(String.format("Total Months Analyzed: %d\n", monthlyRevenue.size()));
        reportContent.append(String.format("Total Bills: %d\n", totalBills));
        reportContent.append(String.format("Total Billed: $%,.2f\n", totalBilled));
        reportContent.append(String.format("Total Collected: $%,.2f\n", totalRevenue));

        double collectionRate = totalBilled > 0 ? (totalRevenue / totalBilled) * 100 : 0;
        reportContent.append(String.format("Overall Collection Rate: %.1f%%\n", collectionRate));

        if (monthlyRevenue.size() > 0) {
            double avgMonthlyRevenue = totalRevenue / monthlyRevenue.size();
            double avgMonthlyBills = (double) totalBills / monthlyRevenue.size();
            reportContent.append(String.format("Average Monthly Revenue: $%,.2f\n", avgMonthlyRevenue));
            reportContent.append(String.format("Average Monthly Bills: %.1f\n", avgMonthlyBills));
        }
        reportContent.append("\n");
    }

    private void generateMonthlyBreakdown() {
        reportContent.append("📅 MONTHLY REVENUE BREAKDOWN\n");
        reportContent.append(repeatString("-", 110)).append("\n");
        reportContent.append(String.format("%-10s | %-8s | %-12s | %-12s | %-12s | %-12s | %-12s | %-8s\n",
                "Month", "Bills", "Billed", "Collected", "Patient", "Insurance", "Outstanding", "Coll%"));
        reportContent.append(repeatString("-", 110)).append("\n");

        monthlyRevenue.entrySet().forEach(entry -> {
            String monthKey = entry.getKey();
            MonthlyData data = entry.getValue();
            MonthlyData billedData = monthlyBilled.get(monthKey);

            double billedAmount = billedData != null ? billedData.getTotalBilled() : 0;
            double collectionRate = billedAmount > 0 ? (data.getTotalCollected() / billedAmount) * 100 : 0;

            reportContent.append(String.format("%-10s | %-8d | $%-11.2f | $%-11.2f | $%-11.2f | $%-11.2f | $%-11.2f | %6.1f%%\n",
                    data.getDisplayMonth(),
                    data.getBillCount(),
                    billedAmount,
                    data.getTotalCollected(),
                    data.getPatientPayments(),
                    data.getInsurancePayments(),
                    data.getOutstanding(),
                    collectionRate));
        });

        reportContent.append(repeatString("-", 110)).append("\n");
        reportContent.append(String.format("%-10s | %-8d | $%-11.2f | $%-11.2f | %-12s | %-12s | %-12s | %6.1f%%\n",
                "TOTAL", totalBills, totalBilled, totalRevenue, "-", "-", "-",
                totalBilled > 0 ? (totalRevenue / totalBilled) * 100 : 0));
        reportContent.append("\n");
    }

    private void generateTrendAnalysis() {
        reportContent.append("📊 TREND ANALYSIS\n");
        reportContent.append(repeatString("-", 60)).append("\n");

        if (monthlyRevenue.size() >= 2) {
            List<MonthlyData> monthlyList = new ArrayList<>(monthlyRevenue.values());

            // Overall trend from first to last month
            MonthlyData firstMonth = monthlyList.get(0);
            MonthlyData lastMonth = monthlyList.get(monthlyList.size() - 1);

            double overallGrowth = calculateGrowthRate(firstMonth.getTotalCollected(), lastMonth.getTotalCollected());

            reportContent.append(String.format("Overall Trend (%s to %s):\n",
                    firstMonth.getDisplayMonth(), lastMonth.getDisplayMonth()));

            if (overallGrowth > 0) {
                reportContent.append(String.format("📈 Revenue Growth: +%.1f%% ($%.2f to $%.2f)\n",
                        overallGrowth, firstMonth.getTotalCollected(), lastMonth.getTotalCollected()));
            } else if (overallGrowth < 0) {
                reportContent.append(String.format("📉 Revenue Decline: %.1f%% ($%.2f to $%.2f)\n",
                        overallGrowth, firstMonth.getTotalCollected(), lastMonth.getTotalCollected()));
            } else {
                reportContent.append("➡️ Revenue Stable (no significant change)\n");
            }

            // Month-over-month analysis
            reportContent.append("\nMonth-over-Month Changes:\n");
            for (int i = 1; i < monthlyList.size(); i++) {
                MonthlyData prevMonth = monthlyList.get(i - 1);
                MonthlyData currMonth = monthlyList.get(i);
                double monthlyGrowth = calculateGrowthRate(prevMonth.getTotalCollected(), currMonth.getTotalCollected());

                String trend = monthlyGrowth > 5 ? "🟢" : monthlyGrowth < -5 ? "🔴" : "🟡";
                reportContent.append(String.format("  %s %s: %+.1f%% ($%.2f)\n",
                        trend, currMonth.getDisplayMonth(), monthlyGrowth, currMonth.getTotalCollected()));
            }

            // Calculate average monthly growth
            double avgMonthlyGrowth = calculateAverageGrowthRate(monthlyList);
            reportContent.append(String.format("\nAverage Monthly Growth Rate: %.1f%%\n", avgMonthlyGrowth));

        } else {
            reportContent.append("Insufficient data for trend analysis (need at least 2 months)\n");
        }
        reportContent.append("\n");
    }

    private void generateGrowthAnalysis() {
        reportContent.append("📈 GROWTH PATTERN ANALYSIS\n");
        reportContent.append(repeatString("-", 60)).append("\n");

        if (monthlyRevenue.size() >= 3) {
            List<MonthlyData> monthlyList = new ArrayList<>(monthlyRevenue.values());

            // Find best and worst performing months
            MonthlyData bestMonth = monthlyList.stream()
                    .max(Comparator.comparing(MonthlyData::getTotalCollected))
                    .orElse(null);

            MonthlyData worstMonth = monthlyList.stream()
                    .min(Comparator.comparing(MonthlyData::getTotalCollected))
                    .orElse(null);

            if (bestMonth != null && worstMonth != null) {
                reportContent.append(String.format("🥇 Best Month: %s ($%,.2f collected)\n",
                        bestMonth.getDisplayMonth(), bestMonth.getTotalCollected()));
                reportContent.append(String.format("📉 Lowest Month: %s ($%,.2f collected)\n",
                        worstMonth.getDisplayMonth(), worstMonth.getTotalCollected()));

                double variability = ((bestMonth.getTotalCollected() - worstMonth.getTotalCollected()) /
                        worstMonth.getTotalCollected()) * 100;
                reportContent.append(String.format("Revenue Variability: %.1f%% difference\n", variability));
            }

            // Analyze growth acceleration/deceleration
            analyzeGrowthAcceleration(monthlyList);

        }
        reportContent.append("\n");
    }

    private void analyzeGrowthAcceleration(List<MonthlyData> monthlyList) {
        if (monthlyList.size() >= 4) {
            // Compare first half vs second half growth
            int midPoint = monthlyList.size() / 2;
            List<MonthlyData> firstHalf = monthlyList.subList(0, midPoint);
            List<MonthlyData> secondHalf = monthlyList.subList(midPoint, monthlyList.size());

            double firstHalfAvg = firstHalf.stream().mapToDouble(MonthlyData::getTotalCollected).average().orElse(0);
            double secondHalfAvg = secondHalf.stream().mapToDouble(MonthlyData::getTotalCollected).average().orElse(0);

            double accelerationRate = firstHalfAvg > 0 ? ((secondHalfAvg - firstHalfAvg) / firstHalfAvg) * 100 : 0;

            if (accelerationRate > 10) {
                reportContent.append("🚀 Strong Growth Acceleration detected\n");
            } else if (accelerationRate > 0) {
                reportContent.append("📈 Moderate Growth Acceleration\n");
            } else if (accelerationRate < -10) {
                reportContent.append("⚠️ Growth Deceleration - needs attention\n");
            } else {
                reportContent.append("➡️ Steady growth pattern\n");
            }
        }
    }

    private void generateSeasonalAnalysis() {
        reportContent.append("🌟 SEASONAL PATTERN ANALYSIS\n");
        reportContent.append(repeatString("-", 60)).append("\n");

        // Group by month of year to detect seasonal patterns
        Map<Integer, List<Double>> monthlyPatterns = new HashMap<>();

        monthlyRevenue.values().forEach(data -> {
            // Extract month number from monthKey (e.g., "2025-08" -> 8)
            int monthNum = Integer.parseInt(data.getMonthKey().split("-")[1]);
            monthlyPatterns.computeIfAbsent(monthNum, k -> new ArrayList<>())
                    .add(data.getTotalCollected());
        });

        // Calculate average for each month across years
        Map<Integer, Double> seasonalAverages = monthlyPatterns.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0)
                ));

        if (seasonalAverages.size() >= 3) {
            reportContent.append("Monthly Performance Patterns:\n");
            String[] monthNames = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

            seasonalAverages.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                    .forEach(entry -> {
                        double avgRevenue = entry.getValue();
                        double percentageOfTotal = totalRevenue > 0 ? (avgRevenue / (totalRevenue / monthlyRevenue.size())) * 100 : 0;
                        reportContent.append(String.format("  %s: $%,.2f avg (%.0f%% of average)\n",
                                monthNames[entry.getKey()], avgRevenue, percentageOfTotal));
                    });
        }
        reportContent.append("\n");
    }

    private void generateProjections() {
        reportContent.append("🔮 REVENUE PROJECTIONS\n");
        reportContent.append(repeatString("-", 60)).append("\n");

        if (monthlyRevenue.size() >= 3) {
            List<MonthlyData> recentMonths = new ArrayList<>(monthlyRevenue.values());

            // Use last 3 months for projection
            int startIndex = Math.max(0, recentMonths.size() - 3);
            List<MonthlyData> lastThreeMonths = recentMonths.subList(startIndex, recentMonths.size());

            double avgRecentRevenue = lastThreeMonths.stream()
                    .mapToDouble(MonthlyData::getTotalCollected)
                    .average().orElse(0);

            double recentGrowthRate = calculateAverageGrowthRate(lastThreeMonths);

            // Project next 3 months
            reportContent.append("Projected Next 3 Months (based on recent trends):\n");
            double projectedRevenue = avgRecentRevenue;

            for (int i = 1; i <= 3; i++) {
                projectedRevenue *= (1 + recentGrowthRate / 100);
                LocalDate nextMonth = LocalDate.now().plusMonths(i);
                reportContent.append(String.format("  %s: $%,.2f (%.1f%% growth assumed)\n",
                        nextMonth.format(displayFormatter), projectedRevenue, recentGrowthRate));
            }

            double quarterlyProjection = projectedRevenue * 3;
            reportContent.append(String.format("\nProjected Quarterly Revenue: $%,.2f\n", quarterlyProjection));

            // Conservative and optimistic scenarios
            double conservativeGrowth = Math.max(0, recentGrowthRate * 0.5);
            double optimisticGrowth = recentGrowthRate * 1.5;

            reportContent.append(String.format("Conservative Scenario (%.1f%% growth): $%,.2f quarterly\n",
                    conservativeGrowth, avgRecentRevenue * 3 * (1 + conservativeGrowth / 100)));
            reportContent.append(String.format("Optimistic Scenario (%.1f%% growth): $%,.2f quarterly\n",
                    optimisticGrowth, avgRecentRevenue * 3 * (1 + optimisticGrowth / 100)));
        }

        reportContent.append("\n");
        reportContent.append(repeatString("=", 90)).append("\n");
        reportContent.append("End of Monthly Revenue Trends Report\n");
        reportContent.append(repeatString("=", 90)).append("\n");
    }

    // Helper methods
    private String getEarliestMonth() {
        return monthlyRevenue.isEmpty() ? "N/A" :
                monthlyRevenue.values().iterator().next().getDisplayMonth();
    }

    private String getLatestMonth() {
        return monthlyRevenue.isEmpty() ? "N/A" :
                monthlyRevenue.values().stream().reduce((first, second) -> second)
                        .map(MonthlyData::getDisplayMonth).orElse("N/A");
    }

    private double calculateGrowthRate(double oldValue, double newValue) {
        if (oldValue == 0) return newValue > 0 ? 100 : 0;
        return ((newValue - oldValue) / oldValue) * 100;
    }

    private double calculateAverageGrowthRate(List<MonthlyData> monthlyList) {
        if (monthlyList.size() < 2) return 0;

        double totalGrowth = 0;
        int growthCount = 0;

        for (int i = 1; i < monthlyList.size(); i++) {
            double prevRevenue = monthlyList.get(i - 1).getTotalCollected();
            double currRevenue = monthlyList.get(i).getTotalCollected();

            if (prevRevenue > 0) {
                totalGrowth += calculateGrowthRate(prevRevenue, currRevenue);
                growthCount++;
            }
        }

        return growthCount > 0 ? totalGrowth / growthCount : 0;
    }

    private String repeatString(String str, int count) {
        return str.repeat(count);
    }

    // MonthlyData class to track monthly financial metrics
    private static class MonthlyData {
        private final String monthKey;
        private final String displayMonth;
        private double totalCollected = 0;
        private double totalBilled = 0;
        private double patientPayments = 0;
        private double insurancePayments = 0;
        private double outstanding = 0;
        private int billCount = 0;

        public MonthlyData(String monthKey, String displayMonth) {
            this.monthKey = monthKey;
            this.displayMonth = displayMonth;
        }

        public void addCollectedAmount(double amount) { totalCollected += amount; }
        public void addBilledAmount(double amount) { totalBilled += amount; }
        public void addPatientPayment(double amount) { patientPayments += amount; }
        public void addInsurancePayment(double amount) { insurancePayments += amount; }
        public void addOutstanding(double amount) { outstanding += amount; }
        public void incrementBillCount() { billCount++; }

        // Getters
        public String getMonthKey() { return monthKey; }
        public String getDisplayMonth() { return displayMonth; }
        public double getTotalCollected() { return totalCollected; }
        public double getTotalBilled() { return totalBilled; }
        public double getPatientPayments() { return patientPayments; }
        public double getInsurancePayments() { return insurancePayments; }
        public double getOutstanding() { return outstanding; }
        public int getBillCount() { return billCount; }
    }
}