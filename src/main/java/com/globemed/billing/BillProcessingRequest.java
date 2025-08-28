package com.globemed.billing;

import com.globemed.patient.PatientRecord;

public class BillProcessingRequest {
    private final MedicalBill bill;
    private final PatientRecord patient;

    public BillProcessingRequest(MedicalBill bill, PatientRecord patient) {
        this.bill = bill;
        this.patient = patient;
    }

    public MedicalBill getBill() { return bill; }
    public PatientRecord getPatient() { return patient; }
}