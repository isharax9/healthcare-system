package com.globemed.db;

import com.globemed.billing.MedicalBill;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class BillingDAO {

    /**
     * Saves a medical bill to the database. This can be used for both
     * creating a new bill and updating an existing one.
     *
     * @param bill The MedicalBill object to save.
     * @return The billId of the saved bill, or -1 on failure.
     */
    public int saveBill(MedicalBill bill) {
        String sql = "INSERT INTO billing (bill_id, patient_id, service_description, amount, status, processing_log, final_amount, insurance_policy_number, billed_datetime, amount_paid) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "status = VALUES(status), processing_log = VALUES(processing_log), final_amount = VALUES(final_amount), billed_datetime = VALUES(billed_datetime), amount_paid = VALUES(amount_paid)";

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

            // Handle null billedDateTime
            if (bill.getBilledDateTime() != null) {
                pstmt.setTimestamp(9, Timestamp.valueOf(bill.getBilledDateTime()));
            } else {
                pstmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            }

            pstmt.setDouble(10, bill.getAmountPaid());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                if (bill.getBillId() == 0) {
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
    public List<MedicalBill> getBillsByPatientId(String patientId) {
        List<MedicalBill> bills = new ArrayList<>();
        String sql = "SELECT bill_id, patient_id, service_description, amount, status, processing_log, final_amount, billed_datetime, amount_paid FROM billing WHERE patient_id = ? ORDER BY billed_datetime DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MedicalBill bill = createBillFromResultSet(rs);
                bills.add(bill);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bills by patient ID: " + e.getMessage());
        }
        return bills;
    }

    /**
     * Retrieves a single medical bill by its ID.
     * @param billId The ID of the bill.
     * @return MedicalBill object if found, null otherwise.
     */
    public MedicalBill getBillById(int billId) {
        String sql = "SELECT bill_id, patient_id, service_description, amount, status, processing_log, final_amount, billed_datetime, amount_paid FROM billing WHERE bill_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, billId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return createBillFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bill by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Helper method to create MedicalBill from ResultSet
     */
    private MedicalBill createBillFromResultSet(ResultSet rs) throws SQLException {
        LocalDateTime billedDateTime = null;
        Timestamp timestamp = rs.getTimestamp("billed_datetime");
        if (timestamp != null) {
            billedDateTime = timestamp.toLocalDateTime();
        }

        return new MedicalBill(
                rs.getInt("bill_id"),
                rs.getString("patient_id"),
                rs.getString("service_description"),
                rs.getDouble("amount"),
                rs.getString("status"),
                rs.getString("processing_log"),
                rs.getDouble("final_amount"),
                billedDateTime,
                rs.getDouble("amount_paid")
        );
    }

    /**
     * Updates the amount paid and status of an existing bill.
     * @param billId The ID of the bill to update.
     * @param newAmountPaid The total amount currently paid for the bill.
     * @param newStatus The new status of the bill.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateAmountPaidAndStatus(int billId, double newAmountPaid, String newStatus) {
        String sql = "UPDATE billing SET amount_paid = ?, status = ? WHERE bill_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newAmountPaid);
            pstmt.setString(2, newStatus);
            pstmt.setInt(3, billId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating amount paid and status for bill ID " + billId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all bills from the database.
     * @return A list of all MedicalBill objects.
     */
    public List<MedicalBill> getAllBills() {
        List<MedicalBill> bills = new ArrayList<>();
        String sql = "SELECT bill_id, patient_id, service_description, amount, status, processing_log, final_amount, billed_datetime, amount_paid FROM billing ORDER BY billed_datetime DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MedicalBill bill = createBillFromResultSet(rs);
                bills.add(bill);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all bills: " + e.getMessage());
        }
        return bills;
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