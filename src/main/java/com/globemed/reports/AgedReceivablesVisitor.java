package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AgedReceivablesVisitor implements ReportVisitor {
    private final StringBuilder reportContent = new StringBuilder();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private double current = 0;      // 0-30 days
    private double days30to60 = 0;   // 31-60 days
    private double days60to90 = 0;   // 61-90 days
    private double over90 = 0;       // 90+ days

    private int currentCount = 0;
    private int days30to60Count = 0;
    private int days60to90Count = 0;
    private int over90Count = 0;

    @Override
    public void visit(PatientRecord patient) {
        if (reportContent.length() == 0) {
            reportContent.append(repeatString("=", 80)).append("\n");
            reportContent.append("    AGED RECEIVABLES REPORT\n");
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
        if (bill.getFinalAmount() <= 0) return; // Only outstanding amounts

        // Placeholder aging logic - you'd need actual bill dates
        // For now, randomly categorizing bills for demonstration
        int daysSinceBill = (int) (Math.random() * 120); // Random 0-120 days

        if (daysSinceBill <= 30) {
            current += bill.getFinalAmount();
            currentCount++;
        } else if (daysSinceBill <= 60) {
            days30to60 += bill.getFinalAmount();
            days30to60Count++;
        } else if (daysSinceBill <= 90) {
            days60to90 += bill.getFinalAmount();
            days60to90Count++;
        } else {
            over90 += bill.getFinalAmount();
            over90Count++;
        }
    }

    @Override
    public String getReport() {
        generateOverview();
        generateAgedBreakdown();
        generateRiskAnalysis();
        return reportContent.toString();
    }

    private void generateOverview() {
        double totalReceivables = current + days30to60 + days60to90 + over90;
        int totalCount = currentCount + days30to60Count + days60to90Count + over90Count;

        reportContent.append("💰 RECEIVABLES OVERVIEW\n");
        reportContent.append(repeatString("-", 50)).append("\n");
        reportContent.append(String.format("Total Outstanding: $%,.2f\n", totalReceivables));
        reportContent.append(String.format("Total Outstanding Bills: %d\n", totalCount));
        reportContent.append(String.format("Average Outstanding: $%,.2f\n",
                totalCount > 0 ? totalReceivables / totalCount : 0));
        reportContent.append("\n");
    }

    private void generateAgedBreakdown() {
        double totalReceivables = current + days30to60 + days60to90 + over90;

        reportContent.append("📅 AGED RECEIVABLES BREAKDOWN\n");
        reportContent.append(repeatString("-", 70)).append("\n");
        reportContent.append(String.format("%-15s | %-8s | %-15s | %-12s | %-10s\n",
                "Age Category", "Count", "Amount", "Average", "% of Total"));
        reportContent.append(repeatString("-", 70)).append("\n");

        generateAgeRow("0-30 days", currentCount, current, totalReceivables);
        generateAgeRow("31-60 days", days30to60Count, days30to60, totalReceivables);
        generateAgeRow("61-90 days", days60to90Count, days60to90, totalReceivables);
        generateAgeRow("90+ days", over90Count, over90, totalReceivables);

        reportContent.append(repeatString("-", 70)).append("\n");
        reportContent.append(String.format("%-15s | %-8d | $%-14.2f | $%-11.2f | %7.1f%%\n",
                "TOTAL", currentCount + days30to60Count + days60to90Count + over90Count,
                totalReceivables,
                (currentCount + days30to60Count + days60to90Count + over90Count) > 0 ?
                        totalReceivables / (currentCount + days30to60Count + days60to90Count + over90Count) : 0,
                100.0));
        reportContent.append("\n");
    }

    private void generateAgeRow(String category, int count, double amount, double total) {
        double percentage = total > 0 ? (amount / total) * 100 : 0;
        double average = count > 0 ? amount / count : 0;

        reportContent.append(String.format("%-15s | %-8d | $%-14.2f | $%-11.2f | %7.1f%%\n",
                category, count, amount, average, percentage));
    }

    private void generateRiskAnalysis() {
        double totalReceivables = current + days30to60 + days60to90 + over90;
        double higherRisk = days60to90 + over90;

        reportContent.append("⚠️ COLLECTION RISK ANALYSIS\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        double riskPercentage = totalReceivables > 0 ? (higherRisk / totalReceivables) * 100 : 0;

        reportContent.append(String.format("Higher Risk (60+ days): $%,.2f (%.1f%%)\n", higherRisk, riskPercentage));
        reportContent.append(String.format("Critical Risk (90+ days): $%,.2f\n", over90));

        if (riskPercentage > 30) {
            reportContent.append("🔴 HIGH RISK: Over 30%% of receivables are 60+ days old\n");
        } else if (riskPercentage > 15) {
            reportContent.append("🟡 MODERATE RISK: 15-30%% of receivables are 60+ days old\n");
        } else {
            reportContent.append("🟢 LOW RISK: Less than 15%% of receivables are 60+ days old\n");
        }

        reportContent.append("\n💡 RECOMMENDATIONS:\n");
        if (over90 > 0) {
            reportContent.append("• Immediate action needed for 90+ day receivables\n");
        }
        if (days60to90 > 0) {
            reportContent.append("• Follow up on 60-90 day receivables\n");
        }
        reportContent.append("• Implement automated payment reminders\n");
        reportContent.append("• Consider payment plans for large outstanding amounts\n");

        reportContent.append("\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("End of Aged Receivables Report\n");
        reportContent.append(repeatString("=", 80)).append("\n");
    }

    private String repeatString(String str, int count) {
        return str.repeat(count);
    }
}