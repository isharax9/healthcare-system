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
     *
     * @param bill The MedicalBill object to save.
     * @return The billId of the saved bill, or -1 on failure.
     */
    public int saveBill(MedicalBill bill) {
        // We will use a single, slightly more complex query that handles both cases
        // This is called an "UPSERT" (Update or Insert)
        String sql = "INSERT INTO billing (bill_id, patient_id, service_description, amount, insurance_policy_number, status, processing_log) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "status = VALUES(status), processing_log = VALUES(processing_log)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (bill.getBillId() == 0) { // New Bill
                pstmt.setNull(1, java.sql.Types.INTEGER); // Let the DB generate the ID
            } else { // Existing Bill
                pstmt.setInt(1, bill.getBillId());
            }
            pstmt.setString(2, bill.getPatientId());
            pstmt.setString(3, bill.getServiceDescription());
            pstmt.setDouble(4, bill.getAmount());
            pstmt.setString(5, bill.getInsurancePolicyNumber());
            pstmt.setString(6, bill.getStatus());
            pstmt.setString(7, bill.getProcessingLog());

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
}