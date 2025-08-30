package com.globemed.patient;

import java.util.ArrayList;
import java.util.List;
import com.globemed.insurance.InsurancePlan;
import com.globemed.reports.ReportVisitor;
import com.globemed.reports.Visitable;

// This class is both the Originator for the Memento pattern
// and the Prototype for the Prototype pattern.
public class PatientRecord implements Cloneable, Visitable {

    private String patientId;
    private String name;
    private List<String> medicalHistory;
    private List<String> treatmentPlans;
    private InsurancePlan insurancePlan;

    public PatientRecord(String patientId, String name) {
        this.patientId = patientId;
        this.name = name;
        this.medicalHistory = new ArrayList<>();
        this.treatmentPlans = new ArrayList<>();
    }

    // Getters and Setters
    public String getPatientId() { return patientId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMedicalHistory() { return String.valueOf(new ArrayList<>(medicalHistory)); } // Return copy
    public String getTreatmentPlans() { return String.valueOf(new ArrayList<>(treatmentPlans)); } // Return copy
    public InsurancePlan getInsurancePlan() { return insurancePlan; }
    public void setInsurancePlan(InsurancePlan insurancePlan) { this.insurancePlan = insurancePlan; }

    // Methods to modify the state
    public void addMedicalHistory(String history) {
        this.medicalHistory.add(history);
    }

    public void addTreatmentPlan(String plan) {
        this.treatmentPlans.add(plan);
    }

    /**
     * Replaces the entire medical history with a new one.
     * @param medicalHistory The new list of medical history items.
     */
    public void setMedicalHistory(List<String> medicalHistory) {
        // We create a new ArrayList to ensure our internal list is independent
        this.medicalHistory = new ArrayList<>(medicalHistory);
    }

    /**
     * Replaces the entire treatment plan list with a new one.
     * @param treatmentPlans The new list of treatment plans.
     */
    public void setTreatmentPlans(List<String> treatmentPlans) {
        this.treatmentPlans = new ArrayList<>(treatmentPlans);
    }

    // --- Memento Pattern Methods ---

    /**
     * Saves the current state inside a memento.
     */
    public PatientRecordMemento save() {
        // Create a memento with a deep copy of the mutable lists
        return new PatientRecordMemento(
                this.name,
                new ArrayList<>(this.medicalHistory),
                new ArrayList<>(this.treatmentPlans)
        );
    }

    /**
     * Restores the state from a memento object.
     */
    public void restore(PatientRecordMemento memento) {
        this.name = memento.getName();
        this.medicalHistory = memento.getMedicalHistory(); // Assumes memento gives a safe copy
        this.treatmentPlans = memento.getTreatmentPlans();
    }

    // --- Prototype Pattern Method ---

    @Override
    public PatientRecord clone() {
        try {
            PatientRecord clonedRecord = (PatientRecord) super.clone();
            // Perform a deep copy of mutable fields
            clonedRecord.medicalHistory = new ArrayList<>(this.medicalHistory);
            clonedRecord.treatmentPlans = new ArrayList<>(this.treatmentPlans);
            return clonedRecord;
        } catch (CloneNotSupportedException e) {
            // This should not happen since we are Cloneable
            throw new AssertionError();
        }
    }

    // --- Visitor Pattern Method ---

    @Override
    public void accept(ReportVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "PatientRecord{" +
                "patientId='" + patientId + '\'' +
                ", name='" + name + '\'' +
                ", medicalHistory=" + medicalHistory +
                ", treatmentPlans=" + treatmentPlans +
                '}';
    }
}