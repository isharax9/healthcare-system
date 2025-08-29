package com.globemed.db;

import com.globemed.appointment.Appointment;
import com.globemed.appointment.Doctor;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
        } catch (SQLException e) {
            System.err.println("Error fetching doctors: " + e.getMessage());
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
                return new Doctor(
                        rs.getString("doctor_id"),
                        rs.getString("full_name"),
                        rs.getString("specialty")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctor by ID: " + e.getMessage());
        }
        return null;
    }

    public boolean createDoctor(Doctor doctor) {
        if (getDoctorById(doctor.getDoctorId()) != null) {
            System.err.println("Error: Doctor with ID " + doctor.getDoctorId() + " already exists.");
            return false;
        }
        String sql = "INSERT INTO doctors (doctor_id, full_name, specialty) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctor.getDoctorId());
            pstmt.setString(2, doctor.getFullName());
            pstmt.setString(3, doctor.getSpecialty());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating doctor: " + e.getMessage());
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
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating doctor: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteDoctor(String doctorId) {
        String sql = "DELETE FROM doctors WHERE doctor_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting doctor: " + e.getMessage());
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
        // --- MODIFIED: Include doctor_notes in SELECT ---
        String sql = "SELECT * FROM appointments WHERE doctor_id = ? AND DATE(appointment_datetime) = ?";
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
                appt.setDoctorNotes(rs.getString("doctor_notes")); // --- NEW: Set doctor_notes ---
                appointments.add(appt);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching appointments: " + e.getMessage());
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
        // --- MODIFIED: Include doctor_notes in SELECT ---
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
                appt.setDoctorNotes(rs.getString("doctor_notes")); // --- NEW: Set doctor_notes ---
                appointments.add(appt);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching appointments by patient ID: " + e.getMessage());
        }
        return appointments;
    }

    /**
     * Fetches all appointments from the database.
     * @return A list of all Appointment objects, ordered by datetime.
     */
    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        // --- MODIFIED: Include doctor_notes in SELECT ---
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
                appt.setDoctorNotes(rs.getString("doctor_notes")); // --- NEW: Set doctor_notes ---
                appointments.add(appt);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all appointments: " + e.getMessage());
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
        // --- MODIFIED: Include doctor_notes in SELECT ---
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
                appt.setDoctorNotes(rs.getString("doctor_notes")); // --- NEW: Set doctor_notes ---
                appointments.add(appt);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching appointments by doctor ID: " + e.getMessage());
        }
        return appointments;
    }

    public boolean createAppointment(Appointment appointment) {
        // --- MODIFIED: Include doctor_notes in INSERT ---
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_datetime, reason, doctor_notes) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, appointment.getPatientId());
            pstmt.setString(2, appointment.getDoctorId());
            pstmt.setTimestamp(3, Timestamp.valueOf(appointment.getAppointmentDateTime()));
            pstmt.setString(4, appointment.getReason());
            pstmt.setString(5, appointment.getDoctorNotes()); // --- NEW: Set doctor_notes ---
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating appointment: " + e.getMessage());
            return false;
        }
    }

    public boolean updateAppointment(Appointment appointment) {
        // --- MODIFIED: Include doctor_notes in UPDATE ---
        String sql = "UPDATE appointments SET appointment_datetime = ?, reason = ?, status = ?, doctor_notes = ? WHERE appointment_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(appointment.getAppointmentDateTime()));
            pstmt.setString(2, appointment.getReason());
            pstmt.setString(3, appointment.getStatus());
            pstmt.setString(4, appointment.getDoctorNotes()); // --- NEW: Set doctor_notes ---
            pstmt.setInt(5, appointment.getAppointmentId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating appointment: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteAppointment(int appointmentId) {
        String sql = "DELETE FROM appointments WHERE appointment_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting appointment: " + e.getMessage());
            return false;
        }
    }
}