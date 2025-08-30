package com.globemed.db;

import com.globemed.appointment.Appointment;
import com.globemed.appointment.Doctor;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SchedulingDAO - Enhanced with appointment reason update functionality
 * Updated: 2025-08-30 19:44:54 UTC by isharax9
 */
public class SchedulingDAO {

    public List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctors ORDER BY full_name";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                doctors.add(new Doctor(
                        rs.getString("doctor_id"),
                        rs.getString("full_name"),
                        rs.getString("specialty")
                ));
            }
            System.out.println("DEBUG [2025-08-30 19:44:54] isharax9: Fetched " + doctors.size() + " doctors from database");
        } catch (SQLException e) {
            System.err.println("ERROR [2025-08-30 19:44:54] isharax9: Error fetching doctors: " + e.getMessage());
        }
        return doctors;
    }

    public Doctor getDoctorById(String doctorId) {
        String sql = "SELECT * FROM doctors WHERE doctor_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, doctorId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Doctor doctor = new Doctor(
                        rs.getString("doctor_id"),
                        rs.getString("full_name"),
                        rs.getString("specialty")
                );
                System.out.println("DEBUG [2025-08-30 19:44:54] isharax9: Found doctor: " + doctorId);
                return doctor;
            }
        } catch (SQLException e) {
            System.err.println("ERROR [2025-08-30 19:44:54] isharax9: Error fetching doctor by ID " + doctorId + ": " + e.getMessage());
        }
        System.out.println("DEBUG [2025-08-30 19:44:54] isharax9: Doctor not found: " + doctorId);
        return null;
    }

    public boolean createDoctor(Doctor doctor) {
        if (getDoctorById(doctor.getDoctorId()) != null) {
            System.err.println("ERROR [2025-08-30 19:44:54] isharax9: Doctor with ID " + doctor.getDoctorId() + " already exists.");
            return false;
        }
        String sql = "INSERT INTO doctors (doctor_id, full_name, specialty) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctor.getDoctorId());
            pstmt.setString(2, doctor.getFullName());
            pstmt.setString(3, doctor.getSpecialty());
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("SUCCESS [2025-08-30 19:44:54] isharax9: Created doctor: " + doctor.getDoctorId() + " - " + doctor.getFullName());
            }
            return success;
        } catch (SQLException e) {
            System.err.println("ERROR [2025-08-30 19:44:54] isharax9: Error creating doctor " + doctor.getDoctorId() + ": " + e.getMessage());
            return false;
        }
    }

    public boolean updateDoctor(Doctor doctor) {
        String sql = "UPDATE doctors SET full_name = ?, specialty = ? WHERE doctor_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctor.getFullName());
            pstmt.setString(2, doctor.getSpecialty());
            pstmt.setString(3, doctor.getDoctorId());
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("SUCCESS [2025-08-30 19:44:54] isharax9: Updated doctor: " + doctor.getDoctorId() + " - " + doctor.getFullName());
            }
            return success;
        } catch (SQLException e) {
            System.err.println("ERROR [2025-08-30 19:44:54] isharax9: Error updating doctor " + doctor.getDoctorId() + ": " + e.getMessage());
            return false;
        }
    }

    public boolean deleteDoctor(String doctorId) {
        String sql = "DELETE FROM doctors WHERE doctor_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("SUCCESS [2025-08-30 19:44:54] isharax9: Deleted doctor: " + doctorId);
            }
            return success;
        } catch (SQLException e) {
            System.err.println("ERROR [2025-08-30 19:44:54] isharax9: Error deleting doctor " + doctorId + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Fetches appointments for a specific doctor on a specific date.
     * @param doctorId The ID of the doctor.
     * @param date The date of the appointments.
     * @return A list of Appointment objects.
     */
    public List<Appointment> getAppointmentsForDoctorOnDate(String doctorId, LocalDate date) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE doctor_id = ? AND DATE(appointment_datetime) = ? ORDER BY appointment_datetime";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            pstmt.setDate(2, Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Appointment appt = new Appointment(
                        rs.getString("patient_id"),
                        rs.getString("doctor_id"),
                        rs.getTimestamp("appointment_datetime").toLocalDateTime(),
                        rs.getString("reason")
                );
                appt.setAppointmentId(rs.getInt("appointment_id"));
                appt.setStatus(rs.getString("status"));
                appt.setDoctorNotes(rs.getString("doctor_notes"));
                appointments.add(appt);
            }
            System.out.println("DEBUG [2025-08-30 19:44:54] isharax9: Fetched " + appointments.size() +
                    " appointments for doctor " + doctorId + " on " + date);
        } catch (SQLException e) {
            System.err.println("ERROR [2025-08-30 19:44:54] isharax9: Error fetching appointments for doctor " +
                    doctorId + " on " + date + ": " + e.getMessage());
        }
        return appointments;
    }

    /**
     * Fetches all appointments for a specific patient.
     * @param patientId The ID of the patient.
     * @return A list of all appointments for that patient.
     */
    public List<Appointment> getAppointmentsByPatientId(String patientId) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE patient_id = ? ORDER BY appointment_datetime DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Appointment appt = new Appointment(
                        rs.getString("patient_id"),
                        rs.getString("doctor_id"),
                        rs.getTimestamp("appointment_datetime").toLocalDateTime(),
                        rs.getString("reason")
                );
                appt.setAppointmentId(rs.getInt("appointment_id"));
                appt.setStatus(rs.getString("status"));
                appt.setDoctorNotes(rs.getString("doctor_notes"));
                appointments.add(appt);
            }
            System.out.println("DEBUG [2025-08-30 19:44:54] isharax9: Fetched " + appointments.size() +
                    " appointments for patient " + patientId);
        } catch (SQLException e) {
            System.err.println("ERROR [2025-08-30 19:44:54] isharax9: Error fetching appointments for patient " +
                    patientId + ": " + e.getMessage());
        }
        return appointments;
    }

    /**
     * Fetches all appointments from the database.
     * @return A list of all Appointment objects, ordered by datetime.
     */
    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments ORDER BY appointment_datetime DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Appointment appt = new Appointment(
                        rs.getString("patient_id"),
                        rs.getString("doctor_id"),
                        rs.getTimestamp("appointment_datetime").toLocalDateTime(),
                        rs.getString("reason")
                );
                appt.setAppointmentId(rs.getInt("appointment_id"));
                appt.setStatus(rs.getString("status"));
                appt.setDoctorNotes(rs.getString("doctor_notes"));
                appointments.add(appt);
            }
            System.out.println("DEBUG [2025-08-30 19:44:54] isharax9: Fetched " + appointments.size() + " total appointments");
        } catch (SQLException e) {
            System.err.println("ERROR [2025-08-30 19:44:54] isharax9: Error fetching all appointments: " + e.getMessage());
        }
        return appointments;
    }

    /**
     * Fetches all appointments for a specific doctor.
     * @param doctorId The ID of the doctor to filter by.
     * @return A list of Appointment objects for that doctor, ordered by datetime.
     */
    public List<Appointment> getAppointmentsByDoctorId(String doctorId) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE doctor_id = ? ORDER BY appointment_datetime DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Appointment appt = new Appointment(
                        rs.getString("patient_id"),
                        rs.getString("doctor_id"),
                        rs.getTimestamp("appointment_datetime").toLocalDateTime(),
                        rs.getString("reason")
                );
                appt.setAppointmentId(rs.getInt("appointment_id"));
                appt.setStatus(rs.getString("status"));
                appt.setDoctorNotes(rs.getString("doctor_notes"));
                appointments.add(appt);
            }
            System.out.println("DEBUG [2025-08-30 19:44:54] isharax9: Fetched " + appointments.size() +
                    " appointments for doctor " + doctorId);
        } catch (SQLException e) {
            System.err.println("ERROR [2025-08-30 19:44:54] isharax9: Error fetching appointments for doctor " +
                    doctorId + ": " + e.getMessage());
        }
        return appointments;
    }

    public boolean createAppointment(Appointment appointment) {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_datetime, reason, doctor_notes, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, appointment.getPatientId());
            pstmt.setString(2, appointment.getDoctorId());
            pstmt.setTimestamp(3, Timestamp.valueOf(appointment.getAppointmentDateTime()));
            pstmt.setString(4, appointment.getReason());
            pstmt.setString(5, appointment.getDoctorNotes());
            pstmt.setString(6, appointment.getStatus() != null ? appointment.getStatus() : "Scheduled"); // Default status

            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("SUCCESS [2025-08-30 19:44:54] isharax9: Created appointment for patient " +
                        appointment.getPatientId() + " with doctor " + appointment.getDoctorId() +
                        " on " + appointment.getAppointmentDateTime());
            }
            return success;
        } catch (SQLException e) {
            System.err.println("ERROR [2025-08-30 19:44:54] isharax9: Error creating appointment for patient " +
                    appointment.getPatientId() + ": " + e.getMessage());
            return false;
        }
    }

    public boolean updateAppointment(Appointment appointment) {
        String sql = "UPDATE appointments SET appointment_datetime = ?, reason = ?, status = ?, doctor_notes = ? WHERE appointment_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(appointment.getAppointmentDateTime()));
            pstmt.setString(2, appointment.getReason());
            pstmt.setString(3, appointment.getStatus());
            pstmt.setString(4, appointment.getDoctorNotes());
            pstmt.setInt(5, appointment.getAppointmentId());

            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("SUCCESS [2025-08-30 19:44:54] isharax9: Updated appointment ID " +
                        appointment.getAppointmentId() + " - Patient: " + appointment.getPatientId() +
                        ", Status: " + appointment.getStatus() + ", Reason: " + appointment.getReason());
            } else {
                System.err.println("WARNING [2025-08-30 19:44:54] isharax9: No rows updated for appointment ID " +
                        appointment.getAppointmentId());
            }
            return success;
        } catch (SQLException e) {
            System.err.println("ERROR [2025-08-30 19:44:54] isharax9: Error updating appointment ID " +
                    appointment.getAppointmentId() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * NEW: Specific method for updating only the appointment reason
     * This method is designed for nurses and admins to update appointment reasons
     * @param appointmentId The ID of the appointment to update
     * @param newReason The new reason for the appointment
     * @param updatedBy The username of the person making the update
     * @return true if update was successful, false otherwise
     */
    public boolean updateAppointmentReason(int appointmentId, String newReason, String updatedBy) {
        String sql = "UPDATE appointments SET reason = ? WHERE appointment_id = ? AND status = 'Scheduled'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newReason);
            pstmt.setInt(2, appointmentId);

            int rowsUpdated = pstmt.executeUpdate();
            boolean success = rowsUpdated > 0;

            if (success) {
                System.out.println("SUCCESS [2025-08-30 19:44:54] " + updatedBy + ": Updated reason for appointment ID " +
                        appointmentId + " to: " + newReason);
            } else {
                System.err.println("WARNING [2025-08-30 19:44:54] " + updatedBy + ": No rows updated for appointment ID " +
                        appointmentId + " - appointment may not exist or not in 'Scheduled' status");
            }
            return success;
        } catch (SQLException e) {
            System.err.println("ERROR [2025-08-30 19:44:54] " + updatedBy + ": Error updating reason for appointment ID " +
                    appointmentId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * NEW: Get specific appointment by ID for validation
     * @param appointmentId The ID of the appointment
     * @return Appointment object if found, null otherwise
     */
    public Appointment getAppointmentById(int appointmentId) {
        String sql = "SELECT * FROM appointments WHERE appointment_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Appointment appt = new Appointment(
                        rs.getString("patient_id"),
                        rs.getString("doctor_id"),
                        rs.getTimestamp("appointment_datetime").toLocalDateTime(),
                        rs.getString("reason")
                );
                appt.setAppointmentId(rs.getInt("appointment_id"));
                appt.setStatus(rs.getString("status"));
                appt.setDoctorNotes(rs.getString("doctor_notes"));

                System.out.println("DEBUG [2025-08-30 19:44:54] isharax9: Found appointment ID " + appointmentId +
                        " - Status: " + appt.getStatus());
                return appt;
            }
        } catch (SQLException e) {
            System.err.println("ERROR [2025-08-30 19:44:54] isharax9: Error fetching appointment ID " +
                    appointmentId + ": " + e.getMessage());
        }
        System.out.println("DEBUG [2025-08-30 19:44:54] isharax9: Appointment ID " + appointmentId + " not found");
        return null;
    }

    /**
     * NEW: Get appointments that can be updated (only Scheduled status)
     * @param doctorId Optional doctor ID filter
     * @return List of appointments that can be updated
     */
    public List<Appointment> getUpdatableAppointments(String doctorId) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = doctorId != null ?
                "SELECT * FROM appointments WHERE doctor_id = ? AND status = 'Scheduled' ORDER BY appointment_datetime" :
                "SELECT * FROM appointments WHERE status = 'Scheduled' ORDER BY appointment_datetime";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (doctorId != null) {
                pstmt.setString(1, doctorId);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Appointment appt = new Appointment(
                        rs.getString("patient_id"),
                        rs.getString("doctor_id"),
                        rs.getTimestamp("appointment_datetime").toLocalDateTime(),
                        rs.getString("reason")
                );
                appt.setAppointmentId(rs.getInt("appointment_id"));
                appt.setStatus(rs.getString("status"));
                appt.setDoctorNotes(rs.getString("doctor_notes"));
                appointments.add(appt);
            }
            System.out.println("DEBUG [2025-08-30 19:44:54] isharax9: Found " + appointments.size() +
                    " updatable appointments" + (doctorId != null ? " for doctor " + doctorId : ""));
        } catch (SQLException e) {
            System.err.println("ERROR [2025-08-30 19:44:54] isharax9: Error fetching updatable appointments: " + e.getMessage());
        }
        return appointments;
    }

    public boolean deleteAppointment(int appointmentId) {
        String sql = "DELETE FROM appointments WHERE appointment_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                System.out.println("SUCCESS [2025-08-30 19:44:54] isharax9: Deleted appointment ID " + appointmentId);
            }
            return success;
        } catch (SQLException e) {
            System.err.println("ERROR [2025-08-30 19:44:54] isharax9: Error deleting appointment ID " +
                    appointmentId + ": " + e.getMessage());
            return false;
        }
    }
}