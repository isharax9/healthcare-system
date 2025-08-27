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
     * @return A status message indicating success or failure.
     */
    public String bookAppointment(String patientId, Doctor doctor, LocalDateTime requestedDateTime, String reason) {
        // Business Rule: Check for conflicts. For simplicity, assume appointments are 1 hour long.
        List<Appointment> existingAppointments = schedulingDAO.getAppointmentsForDoctorOnDate(doctor.getDoctorId(), requestedDateTime.toLocalDate());

        for (Appointment existing : existingAppointments) {
            // Check if the requested time is within 1 hour of an existing appointment
            if (requestedDateTime.isAfter(existing.getAppointmentDateTime().minusHours(1)) &&
                    requestedDateTime.isBefore(existing.getAppointmentDateTime().plusHours(1))) {
                return "Booking failed: Time slot conflicts with an existing appointment.";
            }
        }

        // No conflicts, proceed to book
        Appointment newAppointment = new Appointment(patientId, doctor.getDoctorId(), requestedDateTime, reason);
        boolean success = schedulingDAO.createAppointment(newAppointment);

        return success ? "Appointment booked successfully!" : "Booking failed: Could not save to database.";
    }
}