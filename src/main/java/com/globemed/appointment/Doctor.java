package com.globemed.appointment;

public class Doctor {
    private final String doctorId;
    private final String fullName;
    private final String specialty;

    public Doctor(String doctorId, String fullName, String specialty) {
        this.doctorId = doctorId;
        this.fullName = fullName;
        this.specialty = specialty;
    }

    public String getDoctorId() { return doctorId; }
    public String getFullName() { return fullName; }
    public String getSpecialty() { return specialty; }

    @Override
    public String toString() {
        // This will be displayed in the JList in our UI
        return fullName + " (" + specialty + ")";
    }
}