package com.globemed.appointment;

import com.globemed.db.SchedulingDAO;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The Mediator. It handles the complex logic of scheduling,
 * checking for conflicts, and coordinating between the database and the request.
 */
public class AppointmentScheduler {
    private final SchedulingDAO schedulingDAO;

    public AppointmentScheduler() {
        this.schedulingDAO = new SchedulingDAO();
    }

    /**
     * Attempts to book an appointment after checking for conflicts.
     * @param patientId The ID of the patient.
     * @param doctor The doctor for the appointment.
     * @param requestedDateTime The proposed date and time for the appointment.
     * @param reason The reason for the appointment.
     * @param doctorNotes Initial notes/prescription from the doctor (can be empty). <-- NEW PARAM
     * @return A status message indicating success or failure.
     */
    // --- MODIFIED: New parameter for doctorNotes ---
    public String bookAppointment(String patientId, Doctor doctor, LocalDateTime requestedDateTime, String reason, String doctorNotes) {
        // Business Rule: Check for conflicts. Assume appointments are 30 minutes long.
        List<Appointment> existingAppointments = schedulingDAO.getAppointmentsForDoctorOnDate(doctor.getDoctorId(), requestedDateTime.toLocalDate());

        for (Appointment existing : existingAppointments) {
            LocalDateTime existingStart = existing.getAppointmentDateTime();

            // Conflict if requested time is within 30 min of existing start time
            // (e.g., existing at 10:00, requested at 10:15 conflicts)
            // (e.g., existing at 10:00, requested at 09:45 conflicts)
            // --- MODIFIED: Using 30-minute conflict rule ---
            if (requestedDateTime.isAfter(existingStart.minusMinutes(30)) &&
                    requestedDateTime.isBefore(existingStart.plusMinutes(30))) {
                return "Booking failed: Time slot conflicts with an existing appointment (30-min rule).";
            }
        }

        // No conflicts, proceed to book
        // --- MODIFIED: Pass doctorNotes to Appointment constructor ---
        Appointment newAppointment = new Appointment(patientId, doctor.getDoctorId(), requestedDateTime, reason);
        newAppointment.setDoctorNotes(doctorNotes); // Set the initial notes

        boolean success = schedulingDAO.createAppointment(newAppointment);

        return success ? "Appointment booked successfully!" : "Booking failed: Could not save to database.";
    }
}