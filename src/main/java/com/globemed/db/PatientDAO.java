package com.globemed.db;

import com.globemed.patient.PatientRecord;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.globemed.insurance.InsurancePlan;

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
        // Use a LEFT JOIN to get insurance info if it exists
        String sql = "SELECT p.*, ip.plan_name, ip.coverage_percent " +
                "FROM patients p " +
                "LEFT JOIN insurance_plans ip ON p.insurance_plan_id = ip.plan_id " +
                "WHERE p.patient_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                PatientRecord record = new PatientRecord(rs.getString("patient_id"), rs.getString("full_name"));
                // (Code for history and plans is the same)
                String historyStr = rs.getString("medical_history");
                if (historyStr != null && !historyStr.isEmpty()) {
                    Arrays.stream(historyStr.split("\\r?\\n")).forEach(record::addMedicalHistory);
                }
                String plansStr = rs.getString("treatment_plans");
                if (plansStr != null && !plansStr.isEmpty()) {
                    Arrays.stream(plansStr.split("\\r?\\n")).forEach(record::addTreatmentPlan);
                }

                // NEW: Check for and create the insurance plan object
                int planId = rs.getInt("insurance_plan_id");
                if (!rs.wasNull()) { // Check if the plan_id was not NULL in the DB
                    record.setInsurancePlan(new InsurancePlan(
                            planId,
                            rs.getString("plan_name"),
                            rs.getDouble("coverage_percent")
                    ));
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
        String sql = "UPDATE patients SET full_name = ?, medical_history = ?, treatment_plans = ?, insurance_plan_id = ? WHERE patient_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, record.getName());
            String historyStr = String.join("\n", record.getMedicalHistory());
            String plansStr = String.join("\n", record.getTreatmentPlans());
            pstmt.setString(2, historyStr);
            pstmt.setString(3, plansStr);

            // NEW: Handle setting the insurance_plan_id
            if (record.getInsurancePlan() != null) {
                pstmt.setInt(4, record.getInsurancePlan().getPlanId());
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }

            pstmt.setString(5, record.getPatientId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating patient: " + e.getMessage());
        }
        return false;
    }


    /**
     * Creates a new patient record in the database.
     * @param record The PatientRecord object to create.
     * @return true if the creation was successful, false otherwise.
     */
    public boolean createPatient(PatientRecord record) {
        if (getPatientById(record.getPatientId()) != null) {
            System.err.println("Error: Patient with ID " + record.getPatientId() + " already exists.");
            return false;
        }

        String sql = "INSERT INTO patients (patient_id, full_name, medical_history, treatment_plans, insurance_plan_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, record.getPatientId());
            pstmt.setString(2, record.getName());
            String historyStr = String.join("\n", record.getMedicalHistory());
            String plansStr = String.join("\n", record.getTreatmentPlans());
            pstmt.setString(3, historyStr);
            pstmt.setString(4, plansStr);

            // NEW: Handle setting the insurance_plan_id
            if (record.getInsurancePlan() != null) {
                pstmt.setInt(5, record.getInsurancePlan().getPlanId());
            } else {
                pstmt.setNull(5, java.sql.Types.INTEGER);
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating patient: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a patient from the database by their ID.
     * @param patientId The ID of the patient to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    public boolean deletePatient(String patientId) {
        String sql = "DELETE FROM patients WHERE patient_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patientId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting patient: " + e.getMessage());
        }
        return false;
    }
}