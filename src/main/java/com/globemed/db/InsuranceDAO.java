package com.globemed.db;

import com.globemed.insurance.InsurancePlan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InsuranceDAO {

    public List<InsurancePlan> getAllPlans() {
        List<InsurancePlan> plans = new ArrayList<>();
        String sql = "SELECT * FROM insurance_plans ORDER BY coverage_percent";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                plans.add(new InsurancePlan(
                        rs.getInt("plan_id"),
                        rs.getString("plan_name"),
                        rs.getDouble("coverage_percent")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching insurance plans: " + e.getMessage());
        }
        return plans;
    }
}