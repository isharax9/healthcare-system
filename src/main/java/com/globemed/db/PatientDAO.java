package com.globemed.db;

import com.globemed.patient.PatientRecord;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Access Object for PatientRecord.
 * Handles all database operations (CRUD) for patients.
 */
public class PatientDAO {

    /**
     * Fetches a patient record from the database by their ID.
     * @param patientId The ID of the patient to retrieve.
     * @return A PatientRecord object, or null if not found.
     */
    public PatientRecord getPatientById(String patientId) {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                PatientRecord record = new PatientRecord(rs.getString("patient_id"), rs.getString("full_name"));
                // Convert stored text back into lists
                String historyStr = rs.getString("medical_history");
                if (historyStr != null && !historyStr.isEmpty()) {
                    Arrays.stream(historyStr.split("\n")).forEach(record::addMedicalHistory);
                }
                String plansStr = rs.getString("treatment_plans");
                if (plansStr != null && !plansStr.isEmpty()) {
                    Arrays.stream(plansStr.split("\n")).forEach(record::addTreatmentPlan);
                }
                return record;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient: " + e.getMessage());
        }
        return null;
    }

    /**
     * Updates an existing patient record in the database.
     * @param record The PatientRecord object to update.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updatePatient(PatientRecord record) {
        String sql = "UPDATE patients SET full_name = ?, medical_history = ?, treatment_plans = ? WHERE patient_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, record.getName());
            // Convert lists to a single string for storage
            String historyStr = String.join("\n", record.getMedicalHistory());
            String plansStr = String.join("\n", record.getTreatmentPlans());
            pstmt.setString(2, historyStr);
            pstmt.setString(3, plansStr);
            pstmt.setString(4, record.getPatientId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating patient: " + e.getMessage());
        }
        return false;
    }

    // You can also add methods for createPatient and deletePatient here later.
}