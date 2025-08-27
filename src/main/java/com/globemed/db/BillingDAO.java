package com.globemed.db;

import com.globemed.billing.MedicalBill;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BillingDAO {

    /**
     * Saves a medical bill to the database. This can be used for both
     * creating a new bill and updating an existing one.
     * @param bill The MedicalBill object to save.
     * @return The billId of the saved bill, or -1 on failure.
     */
    public int saveBill(MedicalBill bill) {
        // If billId is 0, it's a new bill (INSERT), otherwise UPDATE.
        String sql = bill.getBillId() == 0
                ? "INSERT INTO billing (patient_id, service_description, amount, insurance_policy_number, status, processing_log) VALUES (?, ?, ?, ?, ?, ?)"
                : "UPDATE billing SET status = ?, processing_log = ? WHERE bill_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (bill.getBillId() == 0) { // New Bill
                pstmt.setString(1, bill.getPatientId());
                pstmt.setString(2, bill.getServiceDescription());
                pstmt.setDouble(3, bill.getAmount());
                pstmt.setString(4, bill.getInsurancePolicyNumber());
                pstmt.setString(5, bill.getStatus());
                pstmt.setString(6, bill.getProcessingLog());
            } else { // Existing Bill
                pstmt.setString(1, bill.getStatus());
                pstmt.setString(2, bill.getProcessingLog());
                pstmt.setInt(3, bill.getBillId());
            }

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0 && bill.getBillId() == 0) {
                // Get the auto-generated bill_id for the new bill
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            } else if (affectedRows > 0) {
                return bill.getBillId(); // Return existing ID on successful update
            }

        } catch (SQLException e) {
            System.err.println("Error saving bill: " + e.getMessage());
        }
        return -1; // Indicate failure
    }
}