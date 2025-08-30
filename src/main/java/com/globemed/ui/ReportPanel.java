package com.globemed.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class ReportPanel extends JPanel {

    // --- Patient Selection Components ---
    public final JTextField patientIdField = new JTextField(15);
    public final JButton findPatientButton = new JButton("Find Patient");
    public final JLabel patientFoundLabel = new JLabel("Status: No patient loaded.");

    // --- Enhanced Report Type Components ---
    public final JComboBox<String> reportCategoryComboBox = new JComboBox<>();
    public final JComboBox<String> reportTypeComboBox = new JComboBox<>();

    // --- Date Range Components ---
    public final JSpinner fromDateSpinner;
    public final JSpinner toDateSpinner;
    public final JComboBox<String> periodComboBox = new JComboBox<>();

    // --- Filter Components ---
    public final JComboBox<String> doctorFilterComboBox = new JComboBox<>();
    public final JComboBox<String> serviceFilterComboBox = new JComboBox<>();
    public final JComboBox<String> paymentStatusFilterComboBox = new JComboBox<>();

    // --- Action Buttons ---
    public final JButton generateReportButton = new JButton("Generate Report");
    public final JButton printReportButton = new JButton("Print Report");
    public final JButton exportPdfButton = new JButton("Export PDF");
    public final JButton exportExcelButton = new JButton("Export Excel");
    public final JButton refreshFiltersButton = new JButton("Refresh Filters");

    // --- Report Display ---
    public final JTextArea reportArea = new JTextArea(30, 100);
    public final JLabel reportStatusLabel = new JLabel("Ready to generate reports");

    public ReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Initialize date spinners
        fromDateSpinner = createDateSpinner();
        toDateSpinner = createDateSpinner();

        // Set default date range (last 30 days)
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        fromDateSpinner.setValue(Date.from(thirtyDaysAgo.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        toDateSpinner.setValue(Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        initializeComponents();
        layoutComponents();
        setInitialStates();
    }

    private JSpinner createDateSpinner() {
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_YEAR);
        JSpinner spinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(dateEditor);
        spinner.setPreferredSize(new Dimension(120, 25));

        // Fade out the date spinners
        spinner.setEnabled(false);
        spinner.setBackground(new Color(245, 245, 245));
        dateEditor.getTextField().setForeground(new Color(150, 150, 150));

        return spinner;
    }

    private void initializeComponents() {
        // Report Categories
        reportCategoryComboBox.addItem("Financial Reports");
        reportCategoryComboBox.addItem("Patient Reports");

        // Financial Report Types (default selection)
        updateReportTypes("Financial Reports");

        // Period Quick Selectors - Fade this dropdown
        periodComboBox.addItem("Custom Range");
        periodComboBox.addItem("Today");
        periodComboBox.addItem("Yesterday");
        periodComboBox.addItem("This Week");
        periodComboBox.addItem("Last Week");
        periodComboBox.addItem("This Month");
        periodComboBox.addItem("Last Month");
        periodComboBox.addItem("This Quarter");
        periodComboBox.addItem("This Year");
        periodComboBox.addItem("Last Year");

        // Apply faded styling to period combo box
        fadeDropdown(periodComboBox);

        // Filter Dropdowns
        doctorFilterComboBox.addItem("All Doctors");
        serviceFilterComboBox.addItem("All Services");
        paymentStatusFilterComboBox.addItem("All Statuses");
        paymentStatusFilterComboBox.addItem("Paid");
        paymentStatusFilterComboBox.addItem("Partially Paid");
        paymentStatusFilterComboBox.addItem("Opened - Pending Payment");

        // Apply faded styling to filter dropdowns
        fadeDropdown(doctorFilterComboBox);
        fadeDropdown(paymentStatusFilterComboBox);

        // Report Area
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        reportArea.setEditable(false);
        reportArea.setBackground(new Color(248, 248, 248));
    }

    /**
     * Helper method to apply faded styling to dropdown components
     */
    private void fadeDropdown(JComboBox<String> comboBox) {
        // Make the dropdown appear faded/disabled
        comboBox.setEnabled(false);
        comboBox.setBackground(new Color(245, 245, 245)); // Light gray background
        comboBox.setForeground(new Color(120, 120, 120)); // Gray text

        // Apply custom renderer for consistent faded appearance
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                // Set faded colors
                setForeground(new Color(120, 120, 120));
                setBackground(new Color(245, 245, 245));

                if (isSelected) {
                    setBackground(new Color(230, 230, 230));
                    setForeground(new Color(100, 100, 100));
                }

                return this;
            }
        });
    }

    private void layoutComponents() {
        // --- Main Container ---
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        // --- Top Controls Panel ---
        JPanel topControlsPanel = new JPanel(new BorderLayout(5, 5));

        // Patient Selection (Optional for some reports)
        JPanel patientPanel = createPatientSelectionPanel();
        topControlsPanel.add(patientPanel, BorderLayout.NORTH);

        // Report Configuration
        JPanel configPanel = createReportConfigurationPanel();
        topControlsPanel.add(configPanel, BorderLayout.CENTER);

        mainPanel.add(topControlsPanel, BorderLayout.NORTH);

        // --- Center: Report Display ---
        JPanel displayPanel = createReportDisplayPanel();
        mainPanel.add(displayPanel, BorderLayout.CENTER);

        // --- Bottom: Status and Actions ---
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createPatientSelectionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(new TitledBorder("Patient Selection (Optional for Individual Reports)"));

        panel.add(new JLabel("Patient ID:"));
        panel.add(patientIdField);
        panel.add(findPatientButton);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(patientFoundLabel);

        return panel;
    }

    private JPanel createReportConfigurationPanel() {
        JPanel mainConfigPanel = new JPanel(new BorderLayout(10, 10));
        mainConfigPanel.setBorder(new TitledBorder("Report Configuration"));

        // --- Row 1: Report Type Selection ---
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        typePanel.add(new JLabel("Category:"));
        typePanel.add(reportCategoryComboBox);
        typePanel.add(Box.createHorizontalStrut(20));
        typePanel.add(new JLabel("Report Type:"));
        reportTypeComboBox.setPreferredSize(new Dimension(250, 25));
        typePanel.add(reportTypeComboBox);

        mainConfigPanel.add(typePanel, BorderLayout.NORTH);

        // --- Row 2: Date Range Selection (Faded) ---
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // Create faded labels for date section
        JLabel periodLabel = new JLabel("Period:");
        periodLabel.setForeground(new Color(150, 150, 150)); // Faded label
        datePanel.add(periodLabel);

        periodComboBox.setPreferredSize(new Dimension(120, 25));
        datePanel.add(periodComboBox);
        datePanel.add(Box.createHorizontalStrut(10));

        JLabel fromLabel = new JLabel("From:");
        fromLabel.setForeground(new Color(150, 150, 150)); // Faded label
        datePanel.add(fromLabel);
        datePanel.add(fromDateSpinner);

        JLabel toLabel = new JLabel("To:");
        toLabel.setForeground(new Color(150, 150, 150)); // Faded label
        datePanel.add(toLabel);
        datePanel.add(toDateSpinner);

        mainConfigPanel.add(datePanel, BorderLayout.CENTER);

        // --- Row 3: Filters (Some Faded) ---
        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // Faded Doctor filter
        JLabel doctorLabel = new JLabel("Doctor:");
        doctorLabel.setForeground(new Color(150, 150, 150)); // Faded label
        filtersPanel.add(doctorLabel);
        doctorFilterComboBox.setPreferredSize(new Dimension(150, 25));
        filtersPanel.add(doctorFilterComboBox);

        // Keep Service filter normal (not faded)
        filtersPanel.add(new JLabel("Service:"));
        serviceFilterComboBox.setPreferredSize(new Dimension(150, 25));
        filtersPanel.add(serviceFilterComboBox);

        // Faded Payment Status filter
        JLabel paymentLabel = new JLabel("Payment Status:");
        paymentLabel.setForeground(new Color(150, 150, 150)); // Faded label
        filtersPanel.add(paymentLabel);
        paymentStatusFilterComboBox.setPreferredSize(new Dimension(150, 25)); // Increased width
        filtersPanel.add(paymentStatusFilterComboBox);

        filtersPanel.add(refreshFiltersButton);

        mainConfigPanel.add(filtersPanel, BorderLayout.SOUTH);

        return mainConfigPanel;
    }

    private JPanel createReportDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Generated Report"));

        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Left: Status
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reportStatusLabel.setFont(reportStatusLabel.getFont().deriveFont(Font.ITALIC));
        statusPanel.add(reportStatusLabel);
        panel.add(statusPanel, BorderLayout.WEST);

        // Right: Action Buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        // Style buttons
        generateReportButton.setPreferredSize(new Dimension(160, 30));
        printReportButton.setPreferredSize(new Dimension(100, 30));
        exportPdfButton.setPreferredSize(new Dimension(100, 30));
        exportExcelButton.setPreferredSize(new Dimension(110, 30));

        generateReportButton.setBackground(new Color(70, 130, 180));
        generateReportButton.setFont(generateReportButton.getFont().deriveFont(Font.BOLD));

        buttonsPanel.add(generateReportButton);
        buttonsPanel.add(Box.createHorizontalStrut(16));
        buttonsPanel.add(printReportButton);
        buttonsPanel.add(exportPdfButton);
        buttonsPanel.add(exportExcelButton);

        panel.add(buttonsPanel, BorderLayout.EAST);

        return panel;
    }

    private void setInitialStates() {
        // Initially disable some components
        reportTypeComboBox.setEnabled(true);
        generateReportButton.setEnabled(true);
        printReportButton.setEnabled(false);
        exportPdfButton.setEnabled(false);
        exportExcelButton.setEnabled(false);

        // Set default selections
        reportCategoryComboBox.setSelectedItem("Financial Reports");
        periodComboBox.setSelectedItem("This Month");

        // Update status with current timestamp and user
        setReportStatus("Ready to generate reports - isharax9 - 2025-08-30 18:42:43 UTC");
    }

    // --- Public Methods for Dynamic Updates ---

    public void updateReportTypes(String category) {
        reportTypeComboBox.removeAllItems();

        switch (category) {
            case "Financial Reports":
                reportTypeComboBox.addItem("Comprehensive Financial Summary");
                reportTypeComboBox.addItem("Revenue Analysis Report");
                reportTypeComboBox.addItem("Outstanding Payments Report");
                reportTypeComboBox.addItem("Payment Collection Report");
                reportTypeComboBox.addItem("Service Revenue Breakdown");
                reportTypeComboBox.addItem("Doctor Revenue Performance");
                reportTypeComboBox.addItem("Insurance vs Patient Payments");
                reportTypeComboBox.addItem("Monthly Revenue Trends");
                reportTypeComboBox.addItem("Aged Receivables Report");
                reportTypeComboBox.addItem("Payment Methods Analysis");
                break;

            case "Patient Reports":
                reportTypeComboBox.addItem("Individual Patient Financial Summary");
                reportTypeComboBox.addItem("Patient Payment History");
                reportTypeComboBox.addItem("Patient's Service Utilization");
                break;
        }

        if (reportTypeComboBox.getItemCount() > 0) {
            reportTypeComboBox.setSelectedIndex(0);
        }
    }

    public void updateDoctorFilter(java.util.List<String> doctors) {
        doctorFilterComboBox.removeAllItems();
        doctorFilterComboBox.addItem("All Doctors");
        for (String doctor : doctors) {
            doctorFilterComboBox.addItem(doctor);
        }
    }

    public void updateServiceFilter(java.util.List<String> services) {
        serviceFilterComboBox.removeAllItems();
        serviceFilterComboBox.addItem("All Services");
        for (String service : services) {
            serviceFilterComboBox.addItem(service);
        }
    }

    public void setReportContent(String content) {
        reportArea.setText(content);
        reportArea.setCaretPosition(0); // Scroll to top
    }

    public void setReportStatus(String status) {
        reportStatusLabel.setText(status);
    }

    public void enableExportButtons(boolean enabled) {
        printReportButton.setEnabled(enabled);
        exportPdfButton.setEnabled(enabled);
        exportExcelButton.setEnabled(enabled);
    }

    // --- Getters for Controller ---

    public String getSelectedReportCategory() {
        return (String) reportCategoryComboBox.getSelectedItem();
    }

    public String getSelectedReportType() {
        return (String) reportTypeComboBox.getSelectedItem();
    }

    public String getSelectedPeriod() {
        return (String) periodComboBox.getSelectedItem();
    }

    public Date getFromDate() {
        return (Date) fromDateSpinner.getValue();
    }

    public Date getToDate() {
        return (Date) toDateSpinner.getValue();
    }

    public String getSelectedDoctor() {
        return (String) doctorFilterComboBox.getSelectedItem();
    }

    public String getSelectedService() {
        return (String) serviceFilterComboBox.getSelectedItem();
    }

    public String getSelectedPaymentStatus() {
        return (String) paymentStatusFilterComboBox.getSelectedItem();
    }

    public String getPatientId() {
        return patientIdField.getText().trim();
    }

    public String getReportContent() {
        return reportArea.getText();
    }

    // --- Optional: Methods to enable/disable faded components when needed ---

    /**
     * Enable the faded dropdowns for specific report types that need them
     */
    public void enableDateFilters(boolean enabled) {
        periodComboBox.setEnabled(enabled);
        fromDateSpinner.setEnabled(enabled);
        toDateSpinner.setEnabled(enabled);

        if (enabled) {
            // Restore normal appearance
            periodComboBox.setBackground(Color.WHITE);
            periodComboBox.setForeground(Color.BLACK);
        } else {
            // Apply faded appearance
            fadeDropdown(periodComboBox);
        }
    }

    /**
     * Enable the doctor filter for specific report types that need it
     */
    public void enableDoctorFilter(boolean enabled) {
        doctorFilterComboBox.setEnabled(enabled);

        if (enabled) {
            doctorFilterComboBox.setBackground(Color.WHITE);
            doctorFilterComboBox.setForeground(Color.BLACK);
        } else {
            fadeDropdown(doctorFilterComboBox);
        }
    }

    /**
     * Enable the payment status filter for specific report types that need it
     */
    public void enablePaymentStatusFilter(boolean enabled) {
        paymentStatusFilterComboBox.setEnabled(enabled);

        if (enabled) {
            paymentStatusFilterComboBox.setBackground(Color.WHITE);
            paymentStatusFilterComboBox.setForeground(Color.BLACK);
        } else {
            fadeDropdown(paymentStatusFilterComboBox);
        }
    }
}