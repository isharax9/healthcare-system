package com.globemed.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class BillingPanel extends JPanel {

    // --- Search and Results Components ---
    public final JTextField searchPatientIdField = new JTextField(15);
    public final JButton searchBillsButton = new JButton("Search Bills");
    public final JTable billsTable = new JTable();
    public final JButton viewLogButton = new JButton("View Processing Log");
    public final JButton printBillButton = new JButton("Print Selected Bill"); // NEW
    public final JButton deleteBillButton = new JButton("Delete Selected Bill");

    // --- Create New Bill Components ---
    public final JTextField createPatientIdField = new JTextField(15);
    public final JTextField serviceField = new JTextField(30);
    public final JTextField amountField = new JTextField(10);
    // The insurance field is now GONE
    public final JButton processBillButton = new JButton("Process New Bill");

    public BillingPanel() {
        setLayout(new BorderLayout(10, 20));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Top Panel: Search and Results ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));

        // Search bar
        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBarPanel.add(new JLabel("Search by Patient ID:"));
        searchBarPanel.add(searchPatientIdField);
        searchBarPanel.add(searchBillsButton);
        topPanel.add(searchBarPanel, BorderLayout.NORTH);

        // Results table
        JScrollPane scrollPane = new JScrollPane(billsTable);
        scrollPane.setBorder(new TitledBorder("Search Results"));
        topPanel.add(scrollPane, BorderLayout.CENTER);

        // Action buttons for results
        JPanel resultsActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        resultsActionPanel.add(viewLogButton);
        resultsActionPanel.add(printBillButton); // ADD THIS
        resultsActionPanel.add(deleteBillButton);
        topPanel.add(resultsActionPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.CENTER);

        // --- Bottom Panel: Create a New Bill Form ---
        JPanel createPanel = new JPanel(new GridBagLayout());
        createPanel.setBorder(new TitledBorder("Create and Process a New Bill"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0
        gbc.gridx = 0; gbc.gridy = 0; createPanel.add(new JLabel("Patient ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; createPanel.add(createPatientIdField, gbc);
        gbc.gridx = 2; gbc.gridy = 0; createPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; createPanel.add(amountField, gbc);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 1; createPanel.add(new JLabel("Service Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL; createPanel.add(serviceField, gbc);

        // Row 2 (Button)
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        createPanel.add(processBillButton, gbc);

        add(createPanel, BorderLayout.SOUTH);

        // Initial button states
        deleteBillButton.setEnabled(false);
        printBillButton.setEnabled(false);
        viewLogButton.setEnabled(false);
    }
}