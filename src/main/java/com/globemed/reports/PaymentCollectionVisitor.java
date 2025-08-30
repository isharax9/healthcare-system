package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PaymentCollectionVisitor implements ReportVisitor {
    private final StringBuilder reportContent = new StringBuilder();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private double totalBilled = 0;
    private double totalCollected = 0;
    private int totalBills = 0;
    private int paidBills = 0;

    @Override
    public void visit(PatientRecord patient) {
        // Initialize if first patient
    }

    @Override
    public void visit(Appointment appointment) {
        // Not used for this report
    }

    @Override
    public void visit(MedicalBill bill) {
        totalBills++;
        totalBilled += bill.getAmount();

        double collected = bill.getAmount() - bill.getFinalAmount();
        if (collected > 0) {
            totalCollected += collected;
            paidBills++;
        }
    }

    @Override
    public String getReport() {
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("    PAYMENT COLLECTION REPORT\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("Generated: ").append(LocalDate.now().format(dateFormatter)).append("\n\n");

        double collectionRate = totalBilled > 0 ? (totalCollected / totalBilled) * 100 : 0;
        double paymentRate = totalBills > 0 ? ((double) paidBills / totalBills) * 100 : 0;

        reportContent.append("💰 COLLECTION PERFORMANCE\n");
        reportContent.append(repeatString("-", 50)).append("\n");
        reportContent.append(String.format("Total Bills Generated: %d\n", totalBills));
        reportContent.append(String.format("Bills with Payments: %d\n", paidBills));
        reportContent.append(String.format("Total Amount Billed: $%,.2f\n", totalBilled));
        reportContent.append(String.format("Total Amount Collected: $%,.2f\n", totalCollected));
        reportContent.append(String.format("Collection Rate: %.1f%%\n", collectionRate));
        reportContent.append(String.format("Payment Rate: %.1f%%\n", paymentRate));

        reportContent.append("\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("End of Payment Collection Report\n");
        reportContent.append(repeatString("=", 80)).append("\n");

        return reportContent.toString();
    }

    private String repeatString(String str, int count) {
        return str.repeat(count);
    }
}