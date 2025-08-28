package com.globemed.db;

import com.globemed.billing.MedicalBill;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BillingDAO {

    /**
     * Saves a medical bill to the database. This can be used for both
     * creating a new bill and updating an existing one.
     *
     * @param bill The MedicalBill object to save.
     * @return The billId of the saved bill, or -1 on failure.
     */
    // REPLACE saveBill
    public int saveBill(MedicalBill bill) {
        String sql = "INSERT INTO billing (bill_id, patient_id, service_description, amount, status, processing_log, final_amount, insurance_policy_number) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "status = VALUES(status), processing_log = VALUES(processing_log), final_amount = VALUES(final_amount)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (bill.getBillId() == 0) {
                pstmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                pstmt.setInt(1, bill.getBillId());
            }
            pstmt.setString(2, bill.getPatientId());
            pstmt.setString(3, bill.getServiceDescription());
            pstmt.setDouble(4, bill.getAmount());
            pstmt.setString(5, bill.getStatus());
            pstmt.setString(6, bill.getProcessingLog());
            pstmt.setDouble(7, bill.getFinalAmount());
            // Store the plan name for historical record
            pstmt.setString(8, bill.getAppliedInsurancePlan() != null ? bill.getAppliedInsurancePlan().getPlanName() : null);


            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                if (bill.getBillId() == 0) {
                    // Get the auto-generated bill_id for the new bill
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            return generatedKeys.getInt(1);
                        }
                    }
                } else {
                    return bill.getBillId(); // Return existing ID on successful update
                }
            }

        } catch (SQLException e) {
            System.err.println("Error saving bill: " + e.getMessage());
        }
        return -1; // Indicate failure
    }

    /**
     * Searches for all bills associated with a given patient ID.
     * @param patientId The ID of the patient to search for.
     * @return A list of MedicalBill objects.
     */
    // REPLACE getBillsByPatientId
    public List<MedicalBill> getBillsByPatientId(String patientId) {
        List<MedicalBill> bills = new ArrayList<>();
        String sql = "SELECT * FROM billing WHERE patient_id = ? ORDER BY bill_id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MedicalBill bill = new MedicalBill(
                        rs.getString("patient_id"),
                        rs.getString("service_description"),
                        rs.getDouble("amount")
                );
                bill.setBillId(rs.getInt("bill_id"));
                bill.setStatus(rs.getString("status"));
                bill.setFinalAmount(rs.getDouble("final_amount")); // <-- ADD THIS
                bills.add(bill);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bills by patient ID: " + e.getMessage());
        }
        return bills;
    }

    // REPLACE getBillById
    public MedicalBill getBillById(int billId) {
        String sql = "SELECT * FROM billing WHERE bill_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, billId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                MedicalBill bill = new MedicalBill(
                        rs.getString("patient_id"),
                        rs.getString("service_description"),
                        rs.getDouble("amount")
                );
                bill.setBillId(rs.getInt("bill_id"));
                bill.setStatus(rs.getString("status"));
                bill.setFinalAmount(rs.getDouble("final_amount"));
                bill.setProcessingLog(rs.getString("processing_log") != null ? rs.getString("processing_log") : "");
                return bill;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bill by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Deletes a bill from the database by its ID.
     * @param billId The ID of the bill to delete.
     * @return true if deletion was successful, false otherwise.
     */
    public boolean deleteBill(int billId) {
        String sql = "DELETE FROM billing WHERE bill_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, billId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting bill: " + e.getMessage());
        }
        return false;
    }
}