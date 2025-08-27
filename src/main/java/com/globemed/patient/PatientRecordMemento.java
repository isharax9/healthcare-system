package com.globemed.patient;

import java.util.ArrayList;
import java.util.List;

/**
 * The Memento class. Stores the internal state of the PatientRecord object.
 * To protect the state's integrity, only the PatientRecord (Originator)
 * should be allowed to access it.
 */
public final class PatientRecordMemento {
    private final String name;
    private final List<String> medicalHistory;
    private final List<String> treatmentPlans;

    public PatientRecordMemento(String name, List<String> medicalHistory, List<String> treatmentPlans) {
        this.name = name;
        this.medicalHistory = new ArrayList<>(medicalHistory); // Store a copy
        this.treatmentPlans = new ArrayList<>(treatmentPlans); // Store a copy
    }

    // Package-private getters to allow only the Originator (in the same package) to access the state.
    // This enforces encapsulation.
    String getName() {
        return name;
    }

    List<String> getMedicalHistory() {
        return new ArrayList<>(medicalHistory); // Return a copy
    }



    List<String> getTreatmentPlans() {
        return new ArrayList<>(treatmentPlans); // Return a copy
    }
}