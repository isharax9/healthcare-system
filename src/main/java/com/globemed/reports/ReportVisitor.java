package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;

/**
 * The Visitor interface. It declares a set of visiting methods for each
 * concrete Visitable element.
 */
public interface ReportVisitor {
    void visit(PatientRecord patient);
    void visit(Appointment appointment);
    void visit(MedicalBill bill);

    // Method to retrieve the final generated report
    String getReport();
}