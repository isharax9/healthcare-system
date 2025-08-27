package com.globemed.patient;

import java.util.ArrayList;
import java.util.List;

// This class is both the Originator for the Memento pattern
// and the Prototype for the Prototype pattern.
public class PatientRecord implements Cloneable {

    private String patientId;
    private String name;
    private List<String> medicalHistory;
    private List<String> treatmentPlans;

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
    public List<String> getMedicalHistory() { return new ArrayList<>(medicalHistory); } // Return copy
    public List<String> getTreatmentPlans() { return new ArrayList<>(treatmentPlans); } // Return copy

    // Methods to modify the state
    public void addMedicalHistory(String history) {
        this.medicalHistory.add(history);
    }

    public void addTreatmentPlan(String plan) {
        this.treatmentPlans.add(plan);
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