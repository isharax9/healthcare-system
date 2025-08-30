package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DoctorRevenuePerformanceVisitor implements ReportVisitor {
    private final StringBuilder reportContent = new StringBuilder();
    private final Map<String, DoctorPerformance> doctorPerformance = new HashMap<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private double totalSystemRevenue = 0;
    private int totalAppointments = 0;

    @Override
    public void visit(PatientRecord patient) {
        if (reportContent.length() == 0) {
            reportContent.append(repeatString("=", 80)).append("\n");
            reportContent.append("    DOCTOR REVENUE PERFORMANCE REPORT\n");
            reportContent.append(repeatString("=", 80)).append("\n");
            reportContent.append("Generated: ").append(LocalDate.now().format(dateFormatter)).append("\n\n");
        }
    }

    @Override
    public void visit(Appointment appointment) {
        totalAppointments++;
        String doctorId = appointment.getDoctorId();
        DoctorPerformance performance = doctorPerformance.getOrDefault(doctorId, new DoctorPerformance(doctorId));
        performance.addAppointment();
        doctorPerformance.put(doctorId, performance);
    }

    @Override
    public void visit(MedicalBill bill) {
        totalSystemRevenue += bill.getFinalAmount();

        // Try to associate bill with doctor through patient appointments
        // This is a simplified approach - you might need to enhance based on your data model
        String doctorId = "UNKNOWN"; // Default

        DoctorPerformance performance = doctorPerformance.getOrDefault(doctorId, new DoctorPerformance(doctorId));
        performance.addRevenue(bill.getFinalAmount());
        doctorPerformance.put(doctorId, performance);
    }

    @Override
    public String getReport() {
        generateOverview();
        generateDoctorPerformanceTable();
        generateTopPerformers();
        generatePerformanceMetrics();
        return reportContent.toString();
    }

    private void generateOverview() {
        reportContent.append("👨‍⚕️ DOCTOR PERFORMANCE OVERVIEW\n");
        reportContent.append(repeatString("-", 50)).append("\n");
        reportContent.append(String.format("Total Doctors: %d\n", doctorPerformance.size()));
        reportContent.append(String.format("Total Appointments: %d\n", totalAppointments));
        reportContent.append(String.format("Total System Revenue: $%,.2f\n", totalSystemRevenue));
        reportContent.append(String.format("Average Revenue per Doctor: $%,.2f\n",
                doctorPerformance.size() > 0 ? totalSystemRevenue / doctorPerformance.size() : 0));
        reportContent.append("\n");
    }

    private void generateDoctorPerformanceTable() {
        reportContent.append("📊 INDIVIDUAL DOCTOR PERFORMANCE\n");
        reportContent.append(repeatString("-", 80)).append("\n");
        reportContent.append(String.format("%-15s | %-12s | %-15s | %-15s | %-12s\n",
                "Doctor ID", "Appointments", "Total Revenue", "Avg/Appointment", "% of Total"));
        reportContent.append(repeatString("-", 80)).append("\n");

        doctorPerformance.values().stream()
                .sorted((d1, d2) -> Double.compare(d2.getTotalRevenue(), d1.getTotalRevenue()))
                .forEach(doctor -> {
                    double percentage = totalSystemRevenue > 0 ? (doctor.getTotalRevenue() / totalSystemRevenue) * 100 : 0;
                    double avgPerAppointment = doctor.getAppointmentCount() > 0 ?
                            doctor.getTotalRevenue() / doctor.getAppointmentCount() : 0;

                    reportContent.append(String.format("%-15s | %-12d | $%-14.2f | $%-14.2f | %9.1f%%\n",
                            doctor.getDoctorId(),
                            doctor.getAppointmentCount(),
                            doctor.getTotalRevenue(),
                            avgPerAppointment,
                            percentage));
                });
        reportContent.append("\n");
    }

    private void generateTopPerformers() {
        reportContent.append("🏆 TOP PERFORMERS\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        List<DoctorPerformance> sortedByRevenue = doctorPerformance.values().stream()
                .sorted((d1, d2) -> Double.compare(d2.getTotalRevenue(), d1.getTotalRevenue()))
                .limit(5)
                .toList();

        List<DoctorPerformance> sortedByAppointments = doctorPerformance.values().stream()
                .sorted((d1, d2) -> Integer.compare(d2.getAppointmentCount(), d1.getAppointmentCount()))
                .limit(5)
                .toList();

        reportContent.append("💰 Top 5 by Revenue:\n");
        for (int i = 0; i < sortedByRevenue.size(); i++) {
            DoctorPerformance doctor = sortedByRevenue.get(i);
            reportContent.append(String.format("%d. Dr. %s - $%,.2f\n",
                    i + 1, doctor.getDoctorId(), doctor.getTotalRevenue()));
        }

        reportContent.append("\n📅 Top 5 by Appointments:\n");
        for (int i = 0; i < sortedByAppointments.size(); i++) {
            DoctorPerformance doctor = sortedByAppointments.get(i);
            reportContent.append(String.format("%d. Dr. %s - %d appointments\n",
                    i + 1, doctor.getDoctorId(), doctor.getAppointmentCount()));
        }
        reportContent.append("\n");
    }

    private void generatePerformanceMetrics() {
        reportContent.append("📈 PERFORMANCE INSIGHTS\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        double avgAppointmentsPerDoctor = doctorPerformance.size() > 0 ?
                (double) totalAppointments / doctorPerformance.size() : 0;

        OptionalDouble avgRevenuePerAppointment = doctorPerformance.values().stream()
                .filter(d -> d.getAppointmentCount() > 0)
                .mapToDouble(d -> d.getTotalRevenue() / d.getAppointmentCount())
                .average();

        reportContent.append(String.format("Average Appointments per Doctor: %.1f\n", avgAppointmentsPerDoctor));
        reportContent.append(String.format("Average Revenue per Appointment: $%.2f\n",
                avgRevenuePerAppointment.orElse(0)));

        reportContent.append("\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("End of Doctor Revenue Performance Report\n");
        reportContent.append(repeatString("=", 80)).append("\n");
    }

    private String repeatString(String str, int count) {
        return str.repeat(count);
    }

    private static class DoctorPerformance {
        private final String doctorId;
        private double totalRevenue = 0;
        private int appointmentCount = 0;

        public DoctorPerformance(String doctorId) {
            this.doctorId = doctorId;
        }

        public void addRevenue(double amount) {
            totalRevenue += amount;
        }

        public void addAppointment() {
            appointmentCount++;
        }

        public String getDoctorId() { return doctorId; }
        public double getTotalRevenue() { return totalRevenue; }
        public int getAppointmentCount() { return appointmentCount; }
    }
}