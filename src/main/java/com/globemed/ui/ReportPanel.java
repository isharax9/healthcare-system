package com.globemed.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ReportPanel extends JPanel {

    // --- Components ---
    public final JTextField patientIdField = new JTextField(15);
    public final JButton findPatientButton = new JButton("Find Patient");
    public final JLabel patientFoundLabel = new JLabel("Status: No patient loaded.");

    public final JComboBox<String> reportTypeComboBox = new JComboBox<>();
    public final JButton generateReportButton = new JButton("Generate Report");
    public final JButton printReportButton = new JButton("Print Report"); // <-- NEW BUTTON

    public final JTextArea reportArea = new JTextArea(25, 80);

    public ReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Top Panel: Controls ---
        JPanel controlsPanel = new JPanel(new BorderLayout());

        // Patient Selection
        JPanel patientPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        patientPanel.setBorder(new TitledBorder("1. Select Patient"));
        patientPanel.add(new JLabel("Patient ID:"));
        patientPanel.add(patientIdField);
        patientPanel.add(findPatientButton);
        patientPanel.add(patientFoundLabel);
        controlsPanel.add(patientPanel, BorderLayout.NORTH);

        // Report Generation
        JPanel generationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        generationPanel.setBorder(new TitledBorder("2. Generate Report"));
        generationPanel.add(new JLabel("Report Type:"));
        reportTypeComboBox.addItem("Patient Summary Report"); // Add report options
        reportTypeComboBox.addItem("Financial Report");
        generationPanel.add(reportTypeComboBox);
        generationPanel.add(generateReportButton);
        generationPanel.add(printReportButton); // <-- ADD THE NEW BUTTON HERE
        controlsPanel.add(generationPanel, BorderLayout.CENTER);

        add(controlsPanel, BorderLayout.NORTH);

        // --- Center Panel: Report Display ---
        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.setBorder(new TitledBorder("Generated Report"));
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setEditable(false);
        displayPanel.add(new JScrollPane(reportArea), BorderLayout.CENTER);

        add(displayPanel, BorderLayout.CENTER);

        // Initial state
        reportTypeComboBox.setEnabled(false);
        generateReportButton.setEnabled(false);
        printReportButton.setEnabled(false); // <-- NEW: Initially disabled
    }
}