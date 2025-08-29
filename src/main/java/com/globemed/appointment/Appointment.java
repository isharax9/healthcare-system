package com.globemed.appointment;

import java.time.LocalDateTime;
import com.globemed.reports.ReportVisitor;
import com.globemed.reports.Visitable;

public class Appointment implements Visitable {
    private int appointmentId;
    private String patientId;
    private String doctorId;
    private LocalDateTime appointmentDateTime;
    private String reason;
    private String status;
    private String doctorNotes; // <-- NEW FIELD: For notes/prescriptions

    public Appointment(String patientId, String doctorId, LocalDateTime appointmentDateTime, String reason) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDateTime = appointmentDateTime;
        this.reason = reason;
        this.status = "Scheduled"; // Default status
        this.doctorNotes = ""; // Default empty notes
    }

    // Getters and setters...
    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDoctorNotes() { return doctorNotes; } // <-- NEW GETTER
    public void setDoctorNotes(String doctorNotes) { this.doctorNotes = doctorNotes; } // <-- NEW SETTER

    @Override
    public void accept(ReportVisitor visitor) {
        visitor.visit(this);
    }

    public void setReason(String trim) {
    }
}