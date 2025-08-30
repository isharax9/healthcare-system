package com.globemed.reports;

import com.globemed.appointment.Appointment;
import com.globemed.billing.MedicalBill;
import com.globemed.patient.PatientRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PatientServiceUtilizationVisitor implements ReportVisitor {
    private final StringBuilder reportContent = new StringBuilder();
    private final Map<String, ServiceUtilization> serviceUsage = new HashMap<>();
    private final List<Appointment> appointments = new ArrayList<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String patientName = "";
    private String patientId = "";
    private double totalSpent = 0;
    private int totalServices = 0;

    @Override
    public void visit(PatientRecord patient) {
        this.patientName = patient.getName();
        this.patientId = patient.getPatientId();

        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("    PATIENT SERVICE UTILIZATION REPORT\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("Patient: ").append(patient.getName())
                .append(" (ID: ").append(patient.getPatientId()).append(")\n");
        reportContent.append("Generated: ").append(LocalDate.now().format(dateFormatter)).append("\n");
        reportContent.append(repeatString("=", 80)).append("\n\n");
    }

    @Override
    public void visit(Appointment appointment) {
        appointments.add(appointment);
    }

    @Override
    public void visit(MedicalBill bill) {
        totalServices++;
        totalSpent += bill.getFinalAmount();

        String serviceName = bill.getServiceDescription();
        ServiceUtilization utilization = serviceUsage.getOrDefault(serviceName,
                new ServiceUtilization(serviceName));
        utilization.addService(bill.getFinalAmount());
        serviceUsage.put(serviceName, utilization);
    }

    @Override
    public String getReport() {
        generateUtilizationSummary();
        generateServiceBreakdown();
        generateAppointmentAnalysis();
        generateRecommendations();
        return reportContent.toString();
    }

    private void generateUtilizationSummary() {
        reportContent.append("🏥 SERVICE UTILIZATION SUMMARY\n");
        reportContent.append(repeatString("-", 50)).append("\n");
        reportContent.append(String.format("Total Appointments: %d\n", appointments.size()));
        reportContent.append(String.format("Total Services Used: %d\n", totalServices));
        reportContent.append(String.format("Unique Service Types: %d\n", serviceUsage.size()));
        reportContent.append(String.format("Total Amount Spent: $%,.2f\n", totalSpent));
        reportContent.append(String.format("Average per Service: $%,.2f\n",
                totalServices > 0 ? totalSpent / totalServices : 0));
        reportContent.append("\n");
    }

    private void generateServiceBreakdown() {
        reportContent.append("📊 SERVICE USAGE BREAKDOWN\n");
        reportContent.append(repeatString("-", 80)).append("\n");
        reportContent.append(String.format("%-30s | %-8s | %-12s | %-12s | %-10s\n",
                "Service Type", "Count", "Total Cost", "Avg Cost", "% of Total"));
        reportContent.append(repeatString("-", 80)).append("\n");

        serviceUsage.values().stream()
                .sorted((s1, s2) -> Integer.compare(s2.getUsageCount(), s1.getUsageCount()))
                .forEach(service -> {
                    double percentage = totalSpent > 0 ? (service.getTotalCost() / totalSpent) * 100 : 0;
                    reportContent.append(String.format("%-30s | %-8d | $%-11.2f | $%-11.2f | %7.1f%%\n",
                            truncateString(service.getServiceName(), 30),
                            service.getUsageCount(),
                            service.getTotalCost(),
                            service.getAverageCost(),
                            percentage));
                });
        reportContent.append("\n");
    }

    private void generateAppointmentAnalysis() {
        reportContent.append("📅 APPOINTMENT ANALYSIS\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        // Count appointments by doctor
        Map<String, Integer> doctorAppointments = new HashMap<>();
        appointments.forEach(apt -> {
            String doctorId = apt.getDoctorId();
            doctorAppointments.put(doctorId, doctorAppointments.getOrDefault(doctorId, 0) + 1);
        });

        reportContent.append("Appointments by Doctor:\n");
        doctorAppointments.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    reportContent.append(String.format("  Dr. %s: %d appointments\n",
                            entry.getKey(), entry.getValue()));
                });

        // Most used service
        serviceUsage.values().stream()
                .max(Comparator.comparing(ServiceUtilization::getUsageCount))
                .ifPresent(mostUsed -> {
                    reportContent.append(String.format("\nMost Utilized Service: %s (%d times)\n",
                            mostUsed.getServiceName(), mostUsed.getUsageCount()));
                });

        // Most expensive service
        serviceUsage.values().stream()
                .max(Comparator.comparing(ServiceUtilization::getTotalCost))
                .ifPresent(mostExpensive -> {
                    reportContent.append(String.format("Most Expensive Service: %s ($%.2f total)\n",
                            mostExpensive.getServiceName(), mostExpensive.getTotalCost()));
                });

        reportContent.append("\n");
    }

    private void generateRecommendations() {
        reportContent.append("💡 UTILIZATION INSIGHTS & RECOMMENDATIONS\n");
        reportContent.append(repeatString("-", 50)).append("\n");

        double avgServiceCost = totalServices > 0 ? totalSpent / totalServices : 0;

        // Find high-cost services
        List<ServiceUtilization> expensiveServices = serviceUsage.values().stream()
                .filter(s -> s.getAverageCost() > avgServiceCost * 1.5)
                .sorted((s1, s2) -> Double.compare(s2.getAverageCost(), s1.getAverageCost()))
                .toList();

        if (!expensiveServices.isEmpty()) {
            reportContent.append("High-Cost Services (above average):\n");
            expensiveServices.forEach(service -> {
                reportContent.append(String.format("  • %s: $%.2f avg (%.1fx above average)\n",
                        service.getServiceName(),
                        service.getAverageCost(),
                        service.getAverageCost() / avgServiceCost));
            });
        }

        // Frequency analysis
        reportContent.append("\nUtilization Patterns:\n");
        if (appointments.size() > 10) {
            reportContent.append("  • High utilization patient - consider preventive care programs\n");
        } else if (appointments.size() < 3) {
            reportContent.append("  • Low utilization - encourage regular check-ups\n");
        } else {
            reportContent.append("  • Normal utilization pattern\n");
        }

        if (serviceUsage.size() > 5) {
            reportContent.append("  • Uses diverse range of services\n");
        } else {
            reportContent.append("  • Limited service usage - assess unmet needs\n");
        }

        reportContent.append("\n");
        reportContent.append(repeatString("=", 80)).append("\n");
        reportContent.append("End of Patient Service Utilization Report\n");
        reportContent.append(repeatString("=", 80)).append("\n");
    }

    private String repeatString(String str, int count) {
        return str.repeat(count);
    }

    private String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    private static class ServiceUtilization {
        private final String serviceName;
        private int usageCount = 0;
        private double totalCost = 0;

        public ServiceUtilization(String serviceName) {
            this.serviceName = serviceName;
        }

        public void addService(double cost) {
            usageCount++;
            totalCost += cost;
        }

        public String getServiceName() { return serviceName; }
        public int getUsageCount() { return usageCount; }
        public double getTotalCost() { return totalCost; }
        public double getAverageCost() {
            return usageCount > 0 ? totalCost / usageCount : 0;
        }
    }
}