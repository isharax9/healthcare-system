package com.globemed.controller;

import com.globemed.appointment.Appointment;
import com.globemed.appointment.Doctor;
import com.globemed.auth.IUser;
import com.globemed.billing.MedicalBill;
import com.globemed.db.BillingDAO;
import com.globemed.db.PatientDAO;
import com.globemed.db.SchedulingDAO;
import com.globemed.patient.PatientRecord;
import com.globemed.reports.*;
import com.globemed.ui.ReportPanel;
import com.globemed.utils.TextReportPrinter;

import javax.swing.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ReportController {
    private final ReportPanel view;
    private final PatientDAO patientDAO;
    private final SchedulingDAO schedulingDAO;
    private final BillingDAO billingDAO;
    private final JFrame mainFrame;
    private final IUser currentUser;

    // Current state
    private PatientRecord currentPatient;
    private String lastGeneratedReportTitle;
    private String lastGeneratedReportContent;

    // Cache for filters
    private List<Doctor> allDoctors;
    private List<String> allServices;

    public ReportController(ReportPanel view, JFrame mainFrame, IUser currentUser) {
        this.view = view;
        this.mainFrame = mainFrame;
        this.currentUser = currentUser;
        this.patientDAO = new PatientDAO();
        this.schedulingDAO = new SchedulingDAO();
        this.billingDAO = new BillingDAO();

        initController();
        loadInitialData();
        applyPermissions();
    }

    private void initController() {
        // --- Button Listeners ---
        view.findPatientButton.addActionListener(e -> findPatient());
        view.generateReportButton.addActionListener(e -> generateReport());
        view.printReportButton.addActionListener(e -> printReport());
        view.exportPdfButton.addActionListener(e -> exportToPdf());
        view.exportExcelButton.addActionListener(e -> exportToExcel());
        view.refreshFiltersButton.addActionListener(e -> refreshFilters());

        // --- ComboBox Listeners ---
        view.reportCategoryComboBox.addActionListener(e -> {
            String selectedCategory = view.getSelectedReportCategory();
            view.updateReportTypes(selectedCategory);
            updateUIBasedOnReportType();
        });

        view.reportTypeComboBox.addActionListener(e -> updateUIBasedOnReportType());

        view.periodComboBox.addActionListener(e -> {
            String selectedPeriod = view.getSelectedPeriod();
            if (!"Custom Range".equals(selectedPeriod)) {
                updateDateRangeBasedOnPeriod(selectedPeriod);
            }
        });

        // --- Date Spinner Listeners ---
        view.fromDateSpinner.addChangeListener(e -> {
            // Auto-set period to "Custom Range" when dates are manually changed
            if (!"Custom Range".equals(view.getSelectedPeriod())) {
                view.periodComboBox.setSelectedItem("Custom Range");
            }
        });

        view.toDateSpinner.addChangeListener(e -> {
            if (!"Custom Range".equals(view.getSelectedPeriod())) {
                view.periodComboBox.setSelectedItem("Custom Range");
            }
        });
    }

    private void loadInitialData() {
        // Load doctors for filter
        allDoctors = schedulingDAO.getAllDoctors();
        List<String> doctorNames = allDoctors.stream()
                .map(doctor -> doctor.getFullName() + " (" + doctor.getDoctorId() + ")")
                .collect(Collectors.toList());
        view.updateDoctorFilter(doctorNames);

        // Load services for filter (you might need to add this method to your DAO)
        loadServicesFilter();

        view.setReportStatus("Ready to generate reports");
    }

    private void loadServicesFilter() {
        // Get unique services from billing data
        // You might need to add this method to BillingDAO: getAllUniqueServices()
        try {
            List<MedicalBill> allBills = billingDAO.getAllBills(); // Assuming this method exists
            allServices = allBills.stream()
                    .map(MedicalBill::getServiceDescription)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            view.updateServiceFilter(allServices);
        } catch (Exception e) {
            // Fallback if method doesn't exist
            view.updateServiceFilter(List.of("General Consultation", "X-Ray", "Blood Test", "Surgery"));
        }
    }

    private void applyPermissions() {
        // Apply user permissions for report access
        boolean canGenerateReports = currentUser.hasPermission("can_generate_reports");
        view.generateReportButton.setEnabled(canGenerateReports);

        if (!canGenerateReports) {
            view.setReportStatus("You don't have permission to generate reports");
        }
    }

    private void updateUIBasedOnReportType() {
        String reportType = view.getSelectedReportType();
        String reportCategory = view.getSelectedReportCategory();

        // Enable/disable patient selection based on report type
        boolean needsPatientSelection = reportType != null && (
                reportType.contains("Individual Patient") ||
                        reportType.contains("Patient Financial Summary") ||
                        reportType.contains("Patient Payment History")
        );

        view.findPatientButton.setEnabled(needsPatientSelection);
        view.patientIdField.setEnabled(needsPatientSelection);

        // Update status message
        if (needsPatientSelection) {
            view.setReportStatus("Please select a patient to generate this report");
            if (currentPatient == null) {
                view.generateReportButton.setEnabled(false);
            }
        } else {
            view.setReportStatus("Ready to generate system-wide report");
            view.generateReportButton.setEnabled(currentUser.hasPermission("can_generate_reports"));
        }
    }

    private void updateDateRangeBasedOnPeriod(String period) {
        LocalDate today = LocalDate.now();
        LocalDate fromDate, toDate;

        switch (period) {
            case "Today":
                fromDate = today;
                toDate = today;
                break;
            case "Yesterday":
                fromDate = today.minusDays(1);
                toDate = today.minusDays(1);
                break;
            case "This Week":
                fromDate = today.minusDays(today.getDayOfWeek().getValue() - 1);
                toDate = today;
                break;
            case "Last Week":
                fromDate = today.minusDays(today.getDayOfWeek().getValue() + 6);
                toDate = today.minusDays(today.getDayOfWeek().getValue());
                break;
            case "This Month":
                fromDate = today.withDayOfMonth(1);
                toDate = today;
                break;
            case "Last Month":
                fromDate = today.minusMonths(1).withDayOfMonth(1);
                toDate = today.minusMonths(1).withDayOfMonth(today.minusMonths(1).lengthOfMonth());
                break;
            case "This Quarter":
                int currentQuarter = (today.getMonthValue() - 1) / 3;
                fromDate = today.withMonth(currentQuarter * 3 + 1).withDayOfMonth(1);
                toDate = today;
                break;
            case "This Year":
                fromDate = today.withDayOfYear(1);
                toDate = today;
                break;
            case "Last Year":
                fromDate = today.minusYears(1).withDayOfYear(1);
                toDate = today.minusYears(1).withMonth(12).withDayOfMonth(31);
                break;
            default:
                return; // Don't update for "Custom Range" or unknown periods
        }

        view.fromDateSpinner.setValue(Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        view.toDateSpinner.setValue(Date.from(toDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private void refreshFilters() {
        view.setReportStatus("Refreshing filters...");
        loadInitialData();
        view.setReportStatus("Filters refreshed successfully");
    }

    private void findPatient() {
        String patientId = view.getPatientId();
        if (patientId.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter a Patient ID.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        view.setReportStatus("Searching for patient...");
        currentPatient = patientDAO.getPatientById(patientId);

        if (currentPatient != null) {
            view.patientFoundLabel.setText("Status: Loaded " + currentPatient.getName());
            view.generateReportButton.setEnabled(currentUser.hasPermission("can_generate_reports"));
            view.enableExportButtons(false);
            view.setReportContent("");
            lastGeneratedReportContent = null;
            lastGeneratedReportTitle = null;
            view.setReportStatus("Patient loaded successfully");
        } else {
            view.patientFoundLabel.setText("Status: Patient not found.");
            view.generateReportButton.setEnabled(false);
            view.enableExportButtons(false);
            view.setReportContent("");
            currentPatient = null;
            lastGeneratedReportContent = null;
            lastGeneratedReportTitle = null;
            view.setReportStatus("Patient not found");
        }
    }

    private void generateReport() {
        String reportType = view.getSelectedReportType();
        String reportCategory = view.getSelectedReportCategory();

        if (reportType == null) {
            JOptionPane.showMessageDialog(view, "Please select a report type.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if patient is required but not loaded
        boolean needsPatient = reportType.contains("Individual Patient") ||
                reportType.contains("Patient Financial Summary") ||
                reportType.contains("Patient Payment History");

        if (needsPatient && currentPatient == null) {
            JOptionPane.showMessageDialog(view, "Please find and load a patient first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        view.setReportStatus("Generating report...");

        try {
            ReportVisitor visitor = createVisitorForReportType(reportType);

            if (needsPatient) {
                generatePatientSpecificReport(visitor);
            } else {
                generateSystemWideReport(visitor);
            }

            lastGeneratedReportContent = visitor.getReport();
            lastGeneratedReportTitle = reportType;
            view.setReportContent(lastGeneratedReportContent);
            view.enableExportButtons(true);
            view.setReportStatus("Report generated successfully");

        } catch (Exception e) {
            view.setReportStatus("Error generating report: " + e.getMessage());
            JOptionPane.showMessageDialog(view, "Error generating report: " + e.getMessage(),
                    "Report Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private ReportVisitor createVisitorForReportType(String reportType) {
        return switch (reportType) {
            case "Comprehensive Financial Summary" -> new ComprehensiveFinancialSummaryVisitor();
            case "Revenue Analysis Report" -> new RevenueAnalysisVisitor();
            case "Outstanding Payments Report" -> new OutstandingPaymentsVisitor();
            case "Payment Collection Report" -> new PaymentCollectionVisitor();
            case "Service Revenue Breakdown" -> new ServiceRevenueBreakdownVisitor();
            case "Doctor Revenue Performance" -> new DoctorRevenuePerformanceVisitor();
            case "Insurance vs Patient Payments" -> new InsuranceVsPatientPaymentsVisitor();
            case "Monthly Revenue Trends" -> new MonthlyRevenueTrendsVisitor();
            case "Aged Receivables Report" -> new AgedReceivablesVisitor();
            case "Payment Methods Analysis" -> new PaymentMethodsAnalysisVisitor();
            case "Individual Patient Financial Summary", "Financial Report" -> new FinancialReportVisitor();
            case "Patient Payment History" -> new PatientPaymentHistoryVisitor();
            case "Patient's Service Utilization" -> new PatientServiceUtilizationVisitor();
            default -> new PatientSummaryReportVisitor();
        };
    }


    private void generatePatientSpecificReport(ReportVisitor visitor) {
        processPatientData(visitor);
        processPatientAppointments(visitor);
        processPatientBills(visitor);
    }

    private void processPatientData(ReportVisitor visitor) {
        // Process patient data
        currentPatient.accept(visitor);
    }

    private void processPatientAppointments(ReportVisitor visitor) {
        // Get appointments for the patient
        List<Appointment> appointments = schedulingDAO.getAppointmentsByPatientId(currentPatient.getPatientId());
        for (Appointment appointment : appointments) {
            appointment.accept(visitor);
        }
    }

    private void processPatientBills(ReportVisitor visitor) {
        // Get bills for the patient within date range
        List<MedicalBill> bills = getBillsForPatientWithFilters(currentPatient.getPatientId());
        for (MedicalBill bill : bills) {
            bill.accept(visitor);
        }
    }

    private void generateSystemWideReport(ReportVisitor visitor) {
        // Get all patients (limited by filters if needed)
        List<PatientRecord> patients = patientDAO.getAllPatients(); // You might need this method

        // Apply date and other filters
        Date fromDate = view.getFromDate();
        Date toDate = view.getToDate();

        for (PatientRecord patient : patients) {
            patient.accept(visitor);

            // Get appointments for each patient
            List<Appointment> appointments = schedulingDAO.getAppointmentsByPatientId(patient.getPatientId());
            for (Appointment appointment : appointments) {
                appointment.accept(visitor);
            }

            // Get bills for each patient with filters
            List<MedicalBill> bills = getBillsForPatientWithFilters(patient.getPatientId());
            for (MedicalBill bill : bills) {
                bill.accept(visitor);
            }
        }
    }

    private List<MedicalBill> getBillsForPatientWithFilters(String patientId) {
        List<MedicalBill> bills = billingDAO.getBillsByPatientId(patientId);

        // Apply filters
        String selectedService = view.getSelectedService();
        String selectedPaymentStatus = view.getSelectedPaymentStatus();

        return bills.stream()
                .filter(bill -> "All Services".equals(selectedService) ||
                        bill.getServiceDescription().equals(selectedService))
                .filter(bill -> "All Statuses".equals(selectedPaymentStatus) ||
                        matchesPaymentStatus(bill, selectedPaymentStatus))
                .collect(Collectors.toList());
    }

    private boolean matchesPaymentStatus(MedicalBill bill, String status) {
        // Implement logic to match bill payment status
        // This depends on how you track payment status in your MedicalBill class
        switch (status) {
            case "Paid":
                return bill.getFinalAmount() == 0; // Assuming 0 means paid
            case "Pending":
                return bill.getFinalAmount() > 0; // Assuming > 0 means pending
            case "Overdue":
                // You'd need a date field to determine overdue
                return bill.getFinalAmount() > 0; // Placeholder logic
            default:
                return true;
        }
    }

    private void printReport() {
        if (lastGeneratedReportContent == null) {
            JOptionPane.showMessageDialog(view, "No report has been generated yet.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String patientId = currentPatient != null ? currentPatient.getPatientId() : "SYSTEM";
        String filename = TextReportPrinter.printTextReport(
                lastGeneratedReportTitle,
                lastGeneratedReportContent,
                patientId
        );

        if (filename != null) {
            JOptionPane.showMessageDialog(view, "Report PDF generated: " + filename, "Print Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(view, "Failed to generate report PDF.", "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportToPdf() {
        if (lastGeneratedReportContent == null) {
            JOptionPane.showMessageDialog(view, "No report has been generated yet.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Reuse the print functionality for PDF export
        printReport();
    }

    private void exportToExcel() {
        if (lastGeneratedReportContent == null) {
            JOptionPane.showMessageDialog(view, "No report has been generated yet.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // TODO: Implement Excel export functionality
        // You could create an ExcelReportExporter utility class
        JOptionPane.showMessageDialog(view, "Excel export functionality coming soon!", "Feature Not Available", JOptionPane.INFORMATION_MESSAGE);
    }
}