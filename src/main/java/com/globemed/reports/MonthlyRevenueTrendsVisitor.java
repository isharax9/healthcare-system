package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MonthlyRevenueTrendsVisitor implements ReportVisitor {
    private final StringBuilder reportContent = new StringBuilder();
    private final Map<String, Double> monthlyRevenue = new HashMap<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private double totalRevenue = 0;

    @Override
    public void visit(PatientRecord patient) {
        if (reportContent.length() == 0) {
            reportContent.append(repeatString("=", 80)).append("\n");
            reportContent.append("    MONTHLY REVENUE TRENDS REPORT\n");
            reportContent.append(repeatString("=", 80)).append("\n");
            reportContent.append("Generated: ").append(LocalDate.now().format(dateFormatter)).append("\n\n");
        }
    }

    @Override
    public void visit(Appointment appointment) {
        // Could use appointment date for trend analysis
    }

    @Override
    public void visit(MedicalBill bill) {
        totalRevenue += bill.getFinalAmount();

        // For now, using current month as placeholder
        // You would need to enhance this with actual bill dates
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        monthlyRevenue.put(month, monthlyRevenue.getOrDefault(month, 0.0) + bill.getFinalAmount());
    }

    @Override
    public String getReport() {
        generateOverview();
        generateMonthlyBreakdown();
        generateTrendAnalysis();
        return reportContent.toString();
    }

    private void generateOverview() {
        reportContent.append("📈 REVENUE TRENDS OVERVIEW\n");
        reportContent.append(repeatString("-", 50)).append("\n");
        reportContent.append(String.format("Total Revenue Analyzed: $%,.2f\n", totalRevenue));
        reportContent.append(String.format("Number of Months: %d\n", monthlyRevenue.size()));
        reportContent.append(String.format("Average Monthly Revenue: $%,.2f\n",
                monthlyRevenue.size() > 0 ? totalRevenue / monthlyRevenue.size() : 0));
        reportContent.append("\n");
    }

    private void generateMonthlyBreakdown() {
        reportContent.append("📅 MONTHLY REVENUE BREAKDOWN\n");
        reportContent.append(repeatString("-", 50)).append("\n");
        reportContent.append(String.format("%-12s | %-15s | %-12s\n", "Month", "Revenue", "% of Total"));
        reportContent.append(repeatString("-", 50)).append("\n");

        monthlyRevenue.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    double percentage = totalRevenue > 0 ? (entry.getValue() / totalRevenue) * 100 : 0;
                    reportContent.append(String.format("%-12s | $%-14.2f | %9.1f%%\n",
                            entry.getKey(), entry.getValue(), percentage));
                });
        reportContent.append("\n");
    }

    private void generateTrendAnalysis() {
        reportContent.append("📊 TREND ANALYSIS\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        if (monthlyRevenue.size() >= 2) {
            List<Double> revenues = new ArrayList<>(monthlyRevenue.values());
            double firstMonth = revenues.get(0);
            double lastMonth = revenues.get(revenues.size() - 1);

            if (lastMonth > firstMonth) {
                double growth = ((lastMonth - firstMonth) / firstMonth) * 100;
                reportContent.append(String.format("📈 Growth Trend: +%.1f%% from first to last month\n", growth));
            } else {
                double decline = ((firstMonth - lastMonth) / firstMonth) * 100;
                reportContent.append(String.format("📉 Decline Trend: -%.1f%% from first to last month\n", decline));
            }
        } else {
            reportContent.append("Insufficient data for trend analysis\n");
        }

        reportContent.append("\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("End of Monthly Revenue Trends Report\n");
        reportContent.append(repeatString("=", 80)).append("\n");
    }

    private String repeatString(String str, int count) {
        return str.repeat(count);
    }
}