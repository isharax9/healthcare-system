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

    public List<Appointment> getAppointmentsForDoctorOnDate(String doctorId, LocalDate date) {
        List<Appointment> appointments = new ArrayList<>();
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
                appointments.add(appt);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching appointments by patient ID: " + e.getMessage());
        }
        return appointments;
    }

    public boolean createAppointment(Appointment appointment) {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_datetime, reason) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, appointment.getPatientId());
            pstmt.setString(2, appointment.getDoctorId());
            pstmt.setTimestamp(3, Timestamp.valueOf(appointment.getAppointmentDateTime()));
            pstmt.setString(4, appointment.getReason());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating appointment: " + e.getMessage());
            return false;
        }
    }

    public boolean updateAppointment(Appointment appointment) {
        String sql = "UPDATE appointments SET appointment_datetime = ?, reason = ?, status = ? WHERE appointment_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(appointment.getAppointmentDateTime()));
            pstmt.setString(2, appointment.getReason());
            pstmt.setString(3, appointment.getStatus());
            pstmt.setInt(4, appointment.getAppointmentId());
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