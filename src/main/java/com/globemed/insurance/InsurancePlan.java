package com.globemed.insurance;

public class InsurancePlan {
    private final int planId;
    private final String planName;
    private final double coveragePercent;

    public InsurancePlan(int planId, String planName, double coveragePercent) {
        this.planId = planId;
        this.planName = planName;
        this.coveragePercent = coveragePercent;
    }

    public int getPlanId() { return planId; }
    public String getPlanName() { return planName; }
    public double getCoveragePercent() { return coveragePercent; }

    @Override
    public String toString() {
        // This is important! This text will be displayed in the JComboBox in the UI.
        return String.format("%s (%.0f%% Coverage)", planName, coveragePercent);
    }
}