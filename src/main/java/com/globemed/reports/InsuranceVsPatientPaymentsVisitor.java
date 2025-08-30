package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InsuranceVsPatientPaymentsVisitor implements ReportVisitor {
    private final StringBuilder reportContent = new StringBuilder();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private double totalInsurancePayments = 0;
    private double totalPatientPayments = 0;
    private int insuranceBills = 0;
    private int patientBills = 0;

    @Override
    public void visit(PatientRecord patient) {
        if (reportContent.length() == 0) {
            reportContent.append(repeatString("=", 80)).append("\n");
            reportContent.append("    INSURANCE VS PATIENT PAYMENTS REPORT\n");
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
        // Simplified logic - you may need to enhance based on your bill structure
        // Assuming bills have some way to identify insurance vs patient payment
        if (isInsuranceBill(bill)) {
            totalInsurancePayments += bill.getFinalAmount();
            insuranceBills++;
        } else {
            totalPatientPayments += bill.getFinalAmount();
            patientBills++;
        }
    }

    private boolean isInsuranceBill(MedicalBill bill) {
        // Placeholder logic - enhance based on your MedicalBill structure
        // This could check for insurance ID, payment method, etc.
        return bill.getPatientId().startsWith("INS") ||
                bill.getServiceDescription().toLowerCase().contains("insurance");
    }

    @Override
    public String getReport() {
        generateOverview();
        generatePaymentBreakdown();
        generateComparison();
        generateRecommendations();
        return reportContent.toString();
    }

    private void generateOverview() {
        double totalPayments = totalInsurancePayments + totalPatientPayments;
        int totalBills = insuranceBills + patientBills;

        reportContent.append("💰 PAYMENT OVERVIEW\n");
        reportContent.append(repeatString("-", 50)).append("\n");
        reportContent.append(String.format("Total Bills: %d\n", totalBills));
        reportContent.append(String.format("Total Payments: $%,.2f\n", totalPayments));
        reportContent.append(String.format("Insurance Bills: %d\n", insuranceBills));
        reportContent.append(String.format("Patient Bills: %d\n", patientBills));
        reportContent.append("\n");
    }

    private void generatePaymentBreakdown() {
        double totalPayments = totalInsurancePayments + totalPatientPayments;

        reportContent.append("📊 PAYMENT BREAKDOWN\n");
        reportContent.append(repeatString("-", 70)).append("\n");
        reportContent.append(String.format("%-20s | %-12s | %-15s | %-12s | %-10s\n",
                "Payment Source", "Bill Count", "Total Amount", "Average", "% of Total"));
        reportContent.append(repeatString("-", 70)).append("\n");

        double insurancePercentage = totalPayments > 0 ? (totalInsurancePayments / totalPayments) * 100 : 0;
        double patientPercentage = totalPayments > 0 ? (totalPatientPayments / totalPayments) * 100 : 0;

        double avgInsurance = insuranceBills > 0 ? totalInsurancePayments / insuranceBills : 0;
        double avgPatient = patientBills > 0 ? totalPatientPayments / patientBills : 0;

        reportContent.append(String.format("%-20s | %-12d | $%-14.2f | $%-11.2f | %7.1f%%\n",
                "Insurance", insuranceBills, totalInsurancePayments, avgInsurance, insurancePercentage));
        reportContent.append(String.format("%-20s | %-12d | $%-14.2f | $%-11.2f | %7.1f%%\n",
                "Patient Direct", patientBills, totalPatientPayments, avgPatient, patientPercentage));
        reportContent.append(repeatString("-", 70)).append("\n");
        reportContent.append(String.format("%-20s | %-12d | $%-14.2f | $%-11.2f | %7.1f%%\n",
                "TOTAL", insuranceBills + patientBills, totalPayments,
                (insuranceBills + patientBills) > 0 ? totalPayments / (insuranceBills + patientBills) : 0, 100.0));
        reportContent.append("\n");
    }

    private void generateComparison() {
        reportContent.append("⚖️ INSURANCE VS PATIENT COMPARISON\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        double avgInsurance = insuranceBills > 0 ? totalInsurancePayments / insuranceBills : 0;
        double avgPatient = patientBills > 0 ? totalPatientPayments / patientBills : 0;

        reportContent.append(String.format("Average Insurance Bill: $%.2f\n", avgInsurance));
        reportContent.append(String.format("Average Patient Bill: $%.2f\n", avgPatient));

        if (avgInsurance > avgPatient) {
            double difference = ((avgInsurance - avgPatient) / avgPatient) * 100;
            reportContent.append(String.format("Insurance bills are %.1f%% higher on average\n", difference));
        } else if (avgPatient > avgInsurance) {
            double difference = ((avgPatient - avgInsurance) / avgInsurance) * 100;
            reportContent.append(String.format("Patient bills are %.1f%% higher on average\n", difference));
        }
        reportContent.append("\n");
    }

    private void generateRecommendations() {
        double totalPayments = totalInsurancePayments + totalPatientPayments;
        double insurancePercentage = totalPayments > 0 ? (totalInsurancePayments / totalPayments) * 100 : 0;

        reportContent.append("💡 RECOMMENDATIONS\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        if (insurancePercentage > 70) {
            reportContent.append("• High insurance dependency - diversify payment sources\n");
        } else if (insurancePercentage < 30) {
            reportContent.append("• Low insurance coverage - promote insurance partnerships\n");
        } else {
            reportContent.append("• Good balance between insurance and patient payments\n");
        }

        reportContent.append("• Monitor insurance reimbursement rates\n");
        reportContent.append("• Consider patient payment plan options\n");

        reportContent.append("\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("End of Insurance vs Patient Payments Report\n");
        reportContent.append(repeatString("=", 80)).append("\n");
    }

    private String repeatString(String str, int count) {
        return str.repeat(count);
    }
}