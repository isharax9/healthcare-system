package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PatientPaymentHistoryVisitor implements ReportVisitor {
    private final StringBuilder reportContent = new StringBuilder();
    private final List<PaymentRecord> paymentHistory = new ArrayList<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String patientName = "";
    private String patientId = "";
    private double totalBilled = 0;
    private double totalPaid = 0;
    private double totalOutstanding = 0;

    @Override
    public void visit(PatientRecord patient) {
        this.patientName = patient.getName();
        this.patientId = patient.getPatientId();

        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("    PATIENT PAYMENT HISTORY REPORT\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("Patient: ").append(patient.getName())
                .append(" (ID: ").append(patient.getPatientId()).append(")\n");
        reportContent.append("Generated: ").append(LocalDate.now().format(dateFormatter)).append("\n");
        reportContent.append(repeatString("=", 80)).append("\n\n");
    }

    @Override
    public void visit(Appointment appointment) {
        // Could track appointment dates for payment correlation
    }

    @Override
    public void visit(MedicalBill bill) {
        totalBilled += bill.getAmount();
        double paidAmount = bill.getAmount() - bill.getFinalAmount();
        totalPaid += paidAmount;
        totalOutstanding += bill.getFinalAmount();

        paymentHistory.add(new PaymentRecord(bill, paidAmount));
    }

    @Override
    public String getReport() {
        generatePaymentSummary();
        generatePaymentHistory();
        generatePaymentAnalysis();
        return reportContent.toString();
    }

    private void generatePaymentSummary() {
        reportContent.append("💰 PAYMENT SUMMARY\n");
        reportContent.append(repeatString("-", 50)).append("\n");
        reportContent.append(String.format("Total Bills: %d\n", paymentHistory.size()));
        reportContent.append(String.format("Total Amount Billed: $%,.2f\n", totalBilled));
        reportContent.append(String.format("Total Amount Paid: $%,.2f\n", totalPaid));
        reportContent.append(String.format("Total Outstanding: $%,.2f\n", totalOutstanding));

        double paymentRate = totalBilled > 0 ? (totalPaid / totalBilled) * 100 : 0;
        reportContent.append(String.format("Payment Rate: %.1f%%\n", paymentRate));
        reportContent.append("\n");
    }

    private void generatePaymentHistory() {
        reportContent.append("📋 DETAILED PAYMENT HISTORY\n");
        reportContent.append(repeatString("-", 90)).append("\n");
        reportContent.append(String.format("%-10s | %-30s | %-12s | %-12s | %-12s | %-10s\n",
                "Bill ID", "Service", "Billed", "Paid", "Outstanding", "Status"));
        reportContent.append(repeatString("-", 90)).append("\n");

        paymentHistory.forEach(record -> {
            String status = record.getOutstanding() == 0 ? "PAID" : "PENDING";
            reportContent.append(String.format("%-10d | %-30s | $%-11.2f | $%-11.2f | $%-11.2f | %-10s\n",
                    record.getBillId(),
                    truncateString(record.getServiceDescription(), 30),
                    record.getBilledAmount(),
                    record.getPaidAmount(),
                    record.getOutstanding(),
                    status));
        });
        reportContent.append("\n");
    }

    private void generatePaymentAnalysis() {
        reportContent.append("📊 PAYMENT ANALYSIS\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        // Count paid vs pending bills
        long paidBills = paymentHistory.stream().filter(r -> r.getOutstanding() == 0).count();
        long pendingBills = paymentHistory.size() - paidBills;

        reportContent.append(String.format("Fully Paid Bills: %d\n", paidBills));
        reportContent.append(String.format("Pending Bills: %d\n", pendingBills));

        // Average amounts
        double avgBillAmount = paymentHistory.size() > 0 ? totalBilled / paymentHistory.size() : 0;
        double avgPayment = paymentHistory.size() > 0 ? totalPaid / paymentHistory.size() : 0;

        reportContent.append(String.format("Average Bill Amount: $%.2f\n", avgBillAmount));
        reportContent.append(String.format("Average Payment: $%.2f\n", avgPayment));

        // Most expensive service
        Optional<PaymentRecord> mostExpensive = paymentHistory.stream()
                .max(Comparator.comparing(PaymentRecord::getBilledAmount));

        if (mostExpensive.isPresent()) {
            reportContent.append(String.format("Most Expensive Service: %s ($%.2f)\n",
                    mostExpensive.get().getServiceDescription(),
                    mostExpensive.get().getBilledAmount()));
        }

        reportContent.append("\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("End of Patient Payment History Report\n");
        reportContent.append(repeatString("=", 80)).append("\n");
    }

    private String repeatString(String str, int count) {
        return str.repeat(count);
    }

    private String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    private static class PaymentRecord {
        private final int billId;
        private final String serviceDescription;
        private final double billedAmount;
        private final double paidAmount;
        private final double outstanding;

        public PaymentRecord(MedicalBill bill, double paidAmount) {
            this.billId = bill.getBillId();
            this.serviceDescription = bill.getServiceDescription();
            this.billedAmount = bill.getAmount();
            this.paidAmount = paidAmount;
            this.outstanding = bill.getFinalAmount();
        }

        public int getBillId() { return billId; }
        public String getServiceDescription() { return serviceDescription; }
        public double getBilledAmount() { return billedAmount; }
        public double getPaidAmount() { return paidAmount; }
        public double getOutstanding() { return outstanding; }
    }
}