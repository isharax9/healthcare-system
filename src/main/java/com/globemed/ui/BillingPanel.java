package com.globemed.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class BillingPanel extends JPanel {

    // --- Input Components ---
    public final JTextField patientIdField = new JTextField(15);
    public final JTextField serviceField = new JTextField(30);
    public final JTextField amountField = new JTextField(10);
    public final JTextField insuranceField = new JTextField(20); // Optional
    public final JButton processBillButton = new JButton("Process Bill");

    // --- Output Components ---
    public final JLabel statusLabel = new JLabel("Status: Waiting for submission.");
    public final JTextArea logArea = new JTextArea(15, 60);

    public BillingPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Input Form Panel (NORTH) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder("Create and Process a New Bill"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Patient ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; formPanel.add(patientIdField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Service Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; formPanel.add(serviceField, gbc);
        gbc.gridx = 2; gbc.gridy = 0; formPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; formPanel.add(amountField, gbc);
        gbc.gridx = 2; gbc.gridy = 1; formPanel.add(new JLabel("Insurance Policy # (Optional):"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; formPanel.add(insuranceField, gbc);

        // --- Button Panel (CENTER of formPanel's SOUTH) ---
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(processBillButton, gbc);
        add(formPanel, BorderLayout.NORTH);

        // --- Results Panel (CENTER) ---
        JPanel resultsPanel = new JPanel(new BorderLayout(5, 5));
        resultsPanel.setBorder(new TitledBorder("Processing Results"));
        statusLabel.setFont(new Font("Serif", Font.BOLD, 16));
        resultsPanel.add(statusLabel, BorderLayout.NORTH);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultsPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        add(resultsPanel, BorderLayout.CENTER);
    }

    // --- Public methods for the Controller ---

    public void setStatus(String status) {
        statusLabel.setText("Status: " + status);
    }

    public void setLog(String log) {
        logArea.setText(log);
    }

    public void clearForm() {
        patientIdField.setText("");
        serviceField.setText("");
        amountField.setText("");
        insuranceField.setText("");
    }
}