package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;
import java.time.format.DateTimeFormatter;

/**
 * A Concrete Visitor that generates a full summary report for a patient.
 */
public class PatientSummaryReportVisitor implements ReportVisitor {
    private final StringBuilder reportContent = new StringBuilder();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void visit(PatientRecord patient) {
        reportContent.append("--- Patient Summary Report ---\n");
        reportContent.append("Patient ID: ").append(patient.getPatientId()).append("\n");
        reportContent.append("Patient Name: ").append(patient.getName()).append("\n");
        if (patient.getInsurancePlan() != null) {
            reportContent.append("Insurance Plan: ").append(patient.getInsurancePlan().getPlanName()).append("\n");
        } else {
            reportContent.append("Insurance Plan: None\n");
        }

        // --- Medical History ---
        reportContent.append("\n--- Medical History ---\n");
        if (patient.getMedicalHistory().isEmpty()) {
            reportContent.append("No medical history recorded.\n");
        } else {
            patient.getMedicalHistory().forEach(line -> reportContent.append("- ").append(line).append("\n"));
        }

        // --- NEW: Treatment Plans Section ---
        reportContent.append("\n--- Treatment Plans ---\n");
        if (patient.getTreatmentPlans().isEmpty()) {
            reportContent.append("No treatment plans recorded.\n");
        } else {
            patient.getTreatmentPlans().forEach(line -> reportContent.append("- ").append(line).append("\n"));
        }
        // --- END NEW SECTION ---

        reportContent.append("\n"); // Add a final newline for spacing before appointments/billing
    }

    @Override
    public void visit(Appointment appointment) {
        if (!reportContent.toString().contains("--- Appointments ---")) {
            reportContent.append("--- Appointments ---\n");
        }
        reportContent.append(String.format("- Date: %s | Reason: %s | Status: %s\n",
                appointment.getAppointmentDateTime().format(dateTimeFormatter),
                appointment.getReason(),
                appointment.getStatus()
        ));
    }

    @Override
    public void visit(MedicalBill bill) {
        if (!reportContent.toString().contains("--- Billing History ---")) {
            reportContent.append("\n--- Billing History ---\n");
        }
        reportContent.append(String.format("- Bill #%d: %s | Original: $%.2f | Final Due: $%.2f | Status: %s\n",
                bill.getBillId(),
                bill.getServiceDescription(),
                bill.getAmount(),
                bill.getFinalAmount(),
                bill.getStatus()
        ));
    }

    @Override
    public String getReport() {
        return reportContent.toString();
    }
}