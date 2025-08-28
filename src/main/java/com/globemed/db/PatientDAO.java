package com.globemed.db;

import com.globemed.insurance.InsurancePlan;
import com.globemed.patient.PatientRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PatientDAO {

    /**
     * Retrieves a single patient record from the database by their ID.
     * @param patientId The ID of the patient to retrieve.
     * @return A PatientRecord object, or null if not found.
     */
    public PatientRecord getPatientById(String patientId) {
        // Corrected SQL to match the getAllPatients query structure
        String sql = "SELECT p.*, ip.plan_name, ip.coverage_percent " +
                "FROM patients p " +
                "LEFT JOIN insurance_plans ip ON p.insurance_plan_id = ip.plan_id " +
                "WHERE p.patient_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                PatientRecord patient = new PatientRecord(
                        rs.getString("patient_id"),
                        rs.getString("full_name") // Assuming column is 'full_name'
                );

                // Populate medical history
                String historyStr = rs.getString("medical_history");
                if (historyStr != null && !historyStr.isEmpty()) {
                    patient.setMedicalHistory(Arrays.asList(historyStr.split("\\r?\\n")));
                }

                // Populate treatment plans
                String plansStr = rs.getString("treatment_plans");
                if (plansStr != null && !plansStr.isEmpty()) {
                    patient.setTreatmentPlans(Arrays.asList(plansStr.split("\\r?\\n")));
                }

                // Populate insurance plan
                int planId = rs.getInt("insurance_plan_id");
                if (!rs.wasNull()) {
                    patient.setInsurancePlan(new InsurancePlan(
                            planId,
                            rs.getString("plan_name"),
                            rs.getDouble("coverage_percent")
                    ));
                }
                return patient;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Creates a new patient record in the database.
     * @param patient The PatientRecord object to create.
     * @return true if the creation was successful, false otherwise.
     */
    public boolean createPatient(PatientRecord patient) {
        String sql = "INSERT INTO patients (patient_id, full_name, medical_history, treatment_plans, insurance_plan_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patient.getPatientId());
            pstmt.setString(2, patient.getName());
            pstmt.setString(3, String.join("\n", patient.getMedicalHistory()));
            pstmt.setString(4, String.join("\n", patient.getTreatmentPlans()));

            if (patient.getInsurancePlan() != null) {
                pstmt.setInt(5, patient.getInsurancePlan().getPlanId());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating patient: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates an existing patient record in the database.
     * @param patient The PatientRecord object with updated information.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updatePatient(PatientRecord patient) {
        String sql = "UPDATE patients SET full_name = ?, medical_history = ?, treatment_plans = ?, insurance_plan_id = ? " +
                "WHERE patient_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patient.getName());
            pstmt.setString(2, String.join("\n", patient.getMedicalHistory()));
            pstmt.setString(3, String.join("\n", patient.getTreatmentPlans()));

            if (patient.getInsurancePlan() != null) {
                pstmt.setInt(4, patient.getInsurancePlan().getPlanId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setString(5, patient.getPatientId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating patient: " + e.getMessage());
            return false;
        }
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
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting patient: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves a list of all patient records from the database.
     * @return A list of PatientRecord objects.
     */
    public List<PatientRecord> getAllPatients() {
        List<PatientRecord> patients = new ArrayList<>();
        // This query assumes your patient table has a 'full_name' column
        String sql = "SELECT p.*, ip.plan_name, ip.coverage_percent " +
                "FROM patients p " +
                "LEFT JOIN insurance_plans ip ON p.insurance_plan_id = ip.plan_id " +
                "ORDER BY p.full_name ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                PatientRecord patient = new PatientRecord(
                        rs.getString("patient_id"),
                        rs.getString("full_name")
                );

                // For the summary view, we only need the name and insurance,
                // but this full data retrieval can be kept for consistency.
                String historyStr = rs.getString("medical_history");
                if (historyStr != null && !historyStr.isEmpty()) {
                    patient.setMedicalHistory(Arrays.asList(historyStr.split("\\r?\\n")));
                }
                String plansStr = rs.getString("treatment_plans");
                if (plansStr != null && !plansStr.isEmpty()) {
                    patient.setTreatmentPlans(Arrays.asList(plansStr.split("\\r?\\n")));
                }

                int planId = rs.getInt("insurance_plan_id");
                if (!rs.wasNull()) {
                    patient.setInsurancePlan(new InsurancePlan(
                            planId,
                            rs.getString("plan_name"),
                            rs.getDouble("coverage_percent")
                    ));
                }
                patients.add(patient);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all patients: " + e.getMessage());
        }
        return patients;
    }
}