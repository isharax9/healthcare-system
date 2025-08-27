package com.globemed.patient;

import java.util.Stack;

/**
 * The Caretaker class. It never examines the contents of a Memento.
 * It is responsible for holding the stack of mementos.
 */
public class RecordHistory {
    private final Stack<PatientRecordMemento> history = new Stack<>();
    private final PatientRecord patientRecord;

    public RecordHistory(PatientRecord patientRecord) {
        this.patientRecord = patientRecord;
    }

    public void save() {
        System.out.println("Saving state...");
        history.push(patientRecord.save());
    }

    public void undo() {
        if (history.isEmpty()) {
            System.out.println("Cannot undo. No history available.");
            return;
        }
        PatientRecordMemento lastState = history.pop();
        System.out.println("Restoring to previous state...");
        patientRecord.restore(lastState);
    }
}