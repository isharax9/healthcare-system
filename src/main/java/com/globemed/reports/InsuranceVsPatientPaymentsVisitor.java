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
    private double totalBilled = 0;
    private int insuranceBills = 0;  // Bills with insurance payments
    private int patientBills = 0;    // Bills with patient payments
    private int totalBills = 0;

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
        totalBills++;
        totalBilled += bill.getAmount();

        // Use actual payment amounts from MedicalBill
        double insurancePayment = bill.getInsurancePaidAmount();
        double patientPayment = bill.getAmountPaid();

        totalInsurancePayments += insurancePayment;
        totalPatientPayments += patientPayment;

        // Count bills that have each type of payment
        if (insurancePayment > 0) {
            insuranceBills++;
        }
        if (patientPayment > 0) {
            patientBills++;
        }
    }

    @Override
    public String getReport() {
        generateOverview();
        generatePaymentBreakdown();
        generateComparison();
        generateEfficiencyMetrics();
        generateRecommendations();
        return reportContent.toString();
    }

    private void generateOverview() {
        double totalPayments = totalInsurancePayments + totalPatientPayments;

        reportContent.append("💰 PAYMENT OVERVIEW\n");
        reportContent.append(repeatString("-", 50)).append("\n");
        reportContent.append(String.format("Total Bills Analyzed: %d\n", totalBills));
        reportContent.append(String.format("Total Amount Billed: $%,.2f\n", totalBilled));
        reportContent.append(String.format("Total Payments Received: $%,.2f\n", totalPayments));
        reportContent.append(String.format("Bills with Insurance: %d\n", insuranceBills));
        reportContent.append(String.format("Bills with Patient Payments: %d\n", patientBills));

        double collectionRate = totalBilled > 0 ? (totalPayments / totalBilled) * 100 : 0;
        reportContent.append(String.format("Overall Collection Rate: %.1f%%\n", collectionRate));
        reportContent.append("\n");
    }

    private void generatePaymentBreakdown() {
        double totalPayments = totalInsurancePayments + totalPatientPayments;

        reportContent.append("📊 PAYMENT SOURCE BREAKDOWN\n");
        reportContent.append(repeatString("-", 70)).append("\n");
        reportContent.append(String.format("%-20s | %-8s | %-15s | %-12s | %-10s\n",
                "Payment Source", "Bills", "Total Amount", "Average", "% of Total"));
        reportContent.append(repeatString("-", 70)).append("\n");

        double insurancePercentage = totalPayments > 0 ? (totalInsurancePayments / totalPayments) * 100 : 0;
        double patientPercentage = totalPayments > 0 ? (totalPatientPayments / totalPayments) * 100 : 0;

        double avgInsurance = insuranceBills > 0 ? totalInsurancePayments / insuranceBills : 0;
        double avgPatient = patientBills > 0 ? totalPatientPayments / patientBills : 0;

        reportContent.append(String.format("%-20s | %-8d | $%-14.2f | $%-11.2f | %7.1f%%\n",
                "Insurance", insuranceBills, totalInsurancePayments, avgInsurance, insurancePercentage));
        reportContent.append(String.format("%-20s | %-8d | $%-14.2f | $%-11.2f | %7.1f%%\n",
                "Patient Direct", patientBills, totalPatientPayments, avgPatient, patientPercentage));
        reportContent.append(repeatString("-", 70)).append("\n");
        reportContent.append(String.format("%-20s | %-8d | $%-14.2f | $%-11.2f | %7.1f%%\n",
                "TOTAL", Math.max(insuranceBills, patientBills), totalPayments,
                totalBills > 0 ? totalPayments / totalBills : 0, 100.0));
        reportContent.append("\n");
    }

    private void generateComparison() {
        reportContent.append("⚖️ INSURANCE VS PATIENT COMPARISON\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        double avgInsurance = insuranceBills > 0 ? totalInsurancePayments / insuranceBills : 0;
        double avgPatient = patientBills > 0 ? totalPatientPayments / patientBills : 0;

        reportContent.append(String.format("Average Insurance Payment: $%.2f\n", avgInsurance));
        reportContent.append(String.format("Average Patient Payment: $%.2f\n", avgPatient));

        if (avgInsurance > 0 && avgPatient > 0) {
            if (avgInsurance > avgPatient) {
                double ratio = avgInsurance / avgPatient;
                reportContent.append(String.format("Insurance payments are %.1fx higher on average\n", ratio));
            } else {
                double ratio = avgPatient / avgInsurance;
                reportContent.append(String.format("Patient payments are %.1fx higher on average\n", ratio));
            }
        }

        // Payment mix analysis
        double totalPayments = totalInsurancePayments + totalPatientPayments;
        if (totalPayments > 0) {
            double insuranceMix = (totalInsurancePayments / totalPayments) * 100;
            reportContent.append(String.format("Payment Mix: %.1f%% Insurance, %.1f%% Patient\n",
                    insuranceMix, 100 - insuranceMix));
        }
        reportContent.append("\n");
    }

    private void generateEfficiencyMetrics() {
        reportContent.append("⚡ COLLECTION EFFICIENCY\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        double insuranceEfficiency = totalBilled > 0 ? (totalInsurancePayments / totalBilled) * 100 : 0;
        double patientEfficiency = totalBilled > 0 ? (totalPatientPayments / totalBilled) * 100 : 0;

        reportContent.append(String.format("Insurance Collection Rate: %.1f%%\n", insuranceEfficiency));
        reportContent.append(String.format("Patient Collection Rate: %.1f%%\n", patientEfficiency));

        // Bills per payment type ratio
        double insuranceBillRatio = totalBills > 0 ? ((double) insuranceBills / totalBills) * 100 : 0;
        double patientBillRatio = totalBills > 0 ? ((double) patientBills / totalBills) * 100 : 0;

        reportContent.append(String.format("Bills with Insurance Coverage: %.1f%%\n", insuranceBillRatio));
        reportContent.append(String.format("Bills with Patient Payments: %.1f%%\n", patientBillRatio));
        reportContent.append("\n");
    }

    private void generateRecommendations() {
        double totalPayments = totalInsurancePayments + totalPatientPayments;
        double insurancePercentage = totalPayments > 0 ? (totalInsurancePayments / totalPayments) * 100 : 0;

        reportContent.append("💡 STRATEGIC RECOMMENDATIONS\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        if (insurancePercentage > 70) {
            reportContent.append("📋 High Insurance Dependency:\n");
            reportContent.append("  • Monitor insurance reimbursement rates closely\n");
            reportContent.append("  • Diversify payment sources\n");
            reportContent.append("  • Negotiate better insurance contracts\n");
        } else if (insurancePercentage < 30) {
            reportContent.append("💳 Low Insurance Coverage:\n");
            reportContent.append("  • Promote insurance plan partnerships\n");
            reportContent.append("  • Verify patient insurance coverage\n");
            reportContent.append("  • Offer patient payment plans\n");
        } else {
            reportContent.append("✅ Balanced Payment Mix:\n");
            reportContent.append("  • Good balance between insurance and patient payments\n");
            reportContent.append("  • Continue current payment strategies\n");
        }

        reportContent.append("\n📈 Optimization Opportunities:\n");
        reportContent.append("  • Implement automated insurance verification\n");
        reportContent.append("  • Streamline patient payment processes\n");
        reportContent.append("  • Consider payment plan options for large bills\n");
        reportContent.append("  • Review insurance claim processing efficiency\n");

        reportContent.append("\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("End of Insurance vs Patient Payments Report\n");
        reportContent.append(repeatString("=", 80)).append("\n");
    }

    private String repeatString(String str, int count) {
        return str.repeat(count);
    }
}