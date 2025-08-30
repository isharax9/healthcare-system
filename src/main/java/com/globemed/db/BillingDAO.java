package com.globemed.db;

import com.globemed.billing.MedicalBill;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
    public int saveBill(MedicalBill bill) {
        String sql = "INSERT INTO billing (bill_id, patient_id, service_description, amount, status, processing_log, final_amount, insurance_policy_number, billed_datetime, amount_paid, insurance_paid_amount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "status = VALUES(status), processing_log = VALUES(processing_log), final_amount = VALUES(final_amount), billed_datetime = VALUES(billed_datetime), amount_paid = VALUES(amount_paid), insurance_paid_amount = VALUES(insurance_paid_amount)";

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

            if (bill.getBilledDateTime() != null) {
                pstmt.setTimestamp(9, Timestamp.valueOf(bill.getBilledDateTime()));
            } else {
                pstmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            }

            pstmt.setDouble(10, bill.getAmountPaid());
            pstmt.setDouble(11, bill.getInsurancePaidAmount());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                if (bill.getBillId() == 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            return generatedKeys.getInt(1);
                        }
                    }
                } else {
                    return bill.getBillId();
                }
            }

        } catch (SQLException e) {
            System.err.println("Error saving bill: " + e.getMessage());
            e.printStackTrace(); // Add full stack trace for debugging
        }
        return -1;
    }

    /**
     * Searches for all bills associated with a given patient ID.
     * @param patientId The ID of the patient to search for.
     * @return A list of MedicalBill objects.
     */
    public List<MedicalBill> getBillsByPatientId(String patientId) {
        List<MedicalBill> bills = new ArrayList<>();

        // Try with insurance_paid_amount first, fallback if column doesn't exist
        String sql = "SELECT bill_id, patient_id, service_description, amount, status, processing_log, final_amount, billed_datetime, amount_paid, " +
                "COALESCE(insurance_paid_amount, 0.0) as insurance_paid_amount FROM billing WHERE patient_id = ? ORDER BY billed_datetime DESC";

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
            e.printStackTrace();

            // Try fallback query without insurance_paid_amount column
            bills = getBillsByPatientIdFallback(patientId);
        }
        return bills;
    }

    /**
     * Fallback method if insurance_paid_amount column doesn't exist
     */
    private List<MedicalBill> getBillsByPatientIdFallback(String patientId) {
        List<MedicalBill> bills = new ArrayList<>();
        String sql = "SELECT bill_id, patient_id, service_description, amount, status, processing_log, final_amount, billed_datetime, amount_paid FROM billing WHERE patient_id = ? ORDER BY billed_datetime DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MedicalBill bill = createBillFromResultSetFallback(rs);
                bills.add(bill);
            }
        } catch (SQLException e) {
            System.err.println("Error in fallback query: " + e.getMessage());
            e.printStackTrace();
        }
        return bills;
    }

    /**
     * Retrieves a single medical bill by its ID.
     * @param billId The ID of the bill.
     * @return MedicalBill object if found, null otherwise.
     */
    public MedicalBill getBillById(int billId) {
        String sql = "SELECT bill_id, patient_id, service_description, amount, status, processing_log, final_amount, billed_datetime, amount_paid, " +
                "COALESCE(insurance_paid_amount, 0.0) as insurance_paid_amount FROM billing WHERE bill_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, billId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return createBillFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bill by ID: " + e.getMessage());
            e.printStackTrace();
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

        // Get values with debugging
        int billId = rs.getInt("bill_id");
        String patientId = rs.getString("patient_id");
        String serviceDescription = rs.getString("service_description");
        double amount = rs.getDouble("amount");
        String status = rs.getString("status");
        String processingLog = rs.getString("processing_log");
        double finalAmount = rs.getDouble("final_amount");
        double amountPaid = rs.getDouble("amount_paid");
        double insurancePaidAmount = rs.getDouble("insurance_paid_amount");

        // DEBUG: Print database values
        System.out.println("createBillFromResultSet for bill ID " + billId + ":");
        System.out.println("  DB amount: " + amount);
        System.out.println("  DB finalAmount: " + finalAmount);
        System.out.println("  DB amountPaid: " + amountPaid);
        System.out.println("  DB insurancePaidAmount: " + insurancePaidAmount);

        return new MedicalBill(
                billId,
                patientId,
                serviceDescription,
                amount,
                status,
                processingLog,
                finalAmount,
                billedDateTime,
                amountPaid,
                insurancePaidAmount
        );
    }

    /**
     * Fallback method to create MedicalBill without insurance_paid_amount
     */
    private MedicalBill createBillFromResultSetFallback(ResultSet rs) throws SQLException {
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
                rs.getDouble("amount_paid"),
                0.0 // Default insurance paid amount
        );
    }

    /**
     * Updates the amount paid and status of an existing bill.
     * @param billId The ID of the bill to update.
     * @param newAmountPaid The total amount currently paid by the patient.
     * @param newInsurancePaidAmount The total amount currently paid by insurance.
     * @param newStatus The new status of the bill.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateAmountPaidAndStatus(int billId, double newAmountPaid, double newInsurancePaidAmount, String newStatus) {
        String sql = "UPDATE billing SET amount_paid = ?, insurance_paid_amount = ?, status = ? WHERE bill_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newAmountPaid);
            pstmt.setDouble(2, newInsurancePaidAmount);
            pstmt.setString(3, newStatus);
            pstmt.setInt(4, billId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating amount paid and status for bill ID " + billId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all bills from the database.
     * @return A list of all MedicalBill objects.
     */
    public List<MedicalBill> getAllBills() {
        List<MedicalBill> bills = new ArrayList<>();
        String sql = "SELECT bill_id, patient_id, service_description, amount, status, processing_log, final_amount, billed_datetime, amount_paid, " +
                "COALESCE(insurance_paid_amount, 0.0) as insurance_paid_amount FROM billing ORDER BY billed_datetime DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MedicalBill bill = createBillFromResultSet(rs);
                bills.add(bill);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all bills: " + e.getMessage());
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return false;
    }
}