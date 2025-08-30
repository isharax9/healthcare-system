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
    private String doctorNotes; // For notes/prescriptions

    // NEW: Track when appointment was last updated and by whom
    private LocalDateTime lastUpdated;
    private String lastUpdatedBy;

    public Appointment(String patientId, String doctorId, LocalDateTime appointmentDateTime, String reason) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDateTime = appointmentDateTime;
        this.reason = reason;
        this.status = "Scheduled"; // Default status
        this.doctorNotes = ""; // Default empty notes
        this.lastUpdated = LocalDateTime.now();
        this.lastUpdatedBy = "system"; // Default value

        System.out.println("DEBUG [2025-08-30 19:46:08] isharax9: Created new appointment for patient " +
                patientId + " with doctor " + doctorId);
    }

    // FIXED: Corrected getter and setter for appointmentId
    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
        System.out.println("DEBUG [2025-08-30 19:46:08] isharax9: Set appointment ID to " + appointmentId);
    }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) {
        this.patientId = patientId;
        updateLastModified("isharax9");
    }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
        updateLastModified("isharax9");
    }

    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }
    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
        updateLastModified("isharax9");
    }

    public String getReason() { return reason; }

    // ENHANCED: Proper setReason method with logging and validation
    public void setReason(String reason) {
        if (reason == null) {
            reason = "";
        }

        String oldReason = this.reason;
        this.reason = reason.trim();
        updateLastModified("isharax9");

        System.out.println("INFO [2025-08-30 19:46:08] isharax9: Updated appointment " + appointmentId +
                " reason from '" + oldReason + "' to '" + this.reason + "'");
    }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        String oldStatus = this.status;
        this.status = status;
        updateLastModified("isharax9");

        System.out.println("INFO [2025-08-30 19:46:08] isharax9: Updated appointment " + appointmentId +
                " status from '" + oldStatus + "' to '" + status + "'");
    }

    public String getDoctorNotes() { return doctorNotes; }
    public void setDoctorNotes(String doctorNotes) {
        if (doctorNotes == null) {
            doctorNotes = "";
        }
        this.doctorNotes = doctorNotes.trim();
        updateLastModified("isharax9");

        System.out.println("INFO [2025-08-30 19:46:08] isharax9: Updated doctor notes for appointment " + appointmentId);
    }

    // NEW: Tracking methods for audit purposes
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public String getLastUpdatedBy() { return lastUpdatedBy; }

    /**
     * Updates the last modified timestamp and user
     * @param updatedBy Username of the person making the change
     */
    public void updateLastModified(String updatedBy) {
        this.lastUpdated = LocalDateTime.now();
        this.lastUpdatedBy = updatedBy != null ? updatedBy : "unknown";
    }

    /**
     * NEW: Convenience method specifically for reason updates by nurses/admins
     * @param newReason The new reason for the appointment
     * @param updatedBy The username of the person updating the reason
     * @return true if the reason was successfully updated, false if appointment cannot be updated
     */
    public boolean updateReasonIfAllowed(String newReason, String updatedBy) {
        // Only allow reason updates for scheduled appointments
        if (!"Scheduled".equalsIgnoreCase(this.status)) {
            System.out.println("WARNING [2025-08-30 19:46:08] " + updatedBy +
                    ": Cannot update reason for appointment " + appointmentId +
                    " - current status is '" + this.status + "'");
            return false;
        }

        if (newReason == null || newReason.trim().isEmpty()) {
            System.out.println("WARNING [2025-08-30 19:46:08] " + updatedBy +
                    ": Cannot set empty reason for appointment " + appointmentId);
            return false;
        }

        String oldReason = this.reason;
        this.reason = newReason.trim();
        updateLastModified(updatedBy);

        System.out.println("SUCCESS [2025-08-30 19:46:08] " + updatedBy +
                ": Updated appointment " + appointmentId + " reason: '" + oldReason +
                "' → '" + this.reason + "'");
        return true;
    }

    /**
     * NEW: Check if appointment can be updated (only scheduled appointments)
     * @return true if appointment can be modified, false otherwise
     */
    public boolean canBeUpdated() {
        return "Scheduled".equalsIgnoreCase(this.status);
    }

    /**
     * NEW: Check if appointment is in a final state (Done or Canceled)
     * @return true if appointment is in final state, false otherwise
     */
    public boolean isInFinalState() {
        return "Done".equalsIgnoreCase(this.status) || "Canceled".equalsIgnoreCase(this.status);
    }

    /**
     * NEW: Get a summary of the appointment for logging/display purposes
     * @return String summary of appointment details
     */
    public String getSummary() {
        return String.format("Appointment[ID=%d, Patient=%s, Doctor=%s, Date=%s, Status=%s, Reason=%s]",
                appointmentId, patientId, doctorId,
                appointmentDateTime != null ? appointmentDateTime.toLocalDate() : "null",
                status, reason);
    }

    /**
     * NEW: Validate appointment data integrity
     * @return true if appointment data is valid, false otherwise
     */
    public boolean isValid() {
        return patientId != null && !patientId.trim().isEmpty() &&
                doctorId != null && !doctorId.trim().isEmpty() &&
                appointmentDateTime != null &&
                status != null && !status.trim().isEmpty();
    }

    @Override
    public void accept(ReportVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getSummary();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Appointment that = (Appointment) obj;
        return appointmentId == that.appointmentId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(appointmentId);
    }
}