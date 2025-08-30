package com.globemed.ui;

import com.globemed.billing.MedicalBill; // Import MedicalBill
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer; // For custom renderer
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime; // Import LocalDateTime
import java.time.format.DateTimeFormatter; // Import DateTimeFormatter
import java.util.List;

public class BillingPanel extends JPanel {

    // --- Search and Results Components ---
    public final JTextField searchPatientIdField = new JTextField(15);
    public final JButton searchBillsButton = new JButton("Search Bills");
    public final JTable billsTable = new JTable();
    public final DefaultTableModel billsTableModel = new DefaultTableModel();
    public final JButton viewLogButton = new JButton("View Processing Log");
    public final JButton printBillButton = new JButton("Print Selected Bill");
    public final JButton deleteBillButton = new JButton("Delete Selected Bill");
    public final JButton payNowButton = new JButton("Pay Now");

    // --- Create New Bill Components ---
    public final JTextField createPatientIdField = new JTextField(15);
    public final JTextField serviceField = new JTextField(30);
    public final JTextField amountField = new JTextField(10);
    public final JButton processBillButton = new JButton("Process New Bill");

    public BillingPanel() {
        setLayout(new BorderLayout(10, 20));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initializeTableModels(); // Initialize the table models

        // --- Top Panel: Search and Results ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));

        // Search bar
        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBarPanel.add(new JLabel("Search by Patient ID:"));
        searchBarPanel.add(searchPatientIdField);
        searchBarPanel.add(searchBillsButton);
        topPanel.add(searchBarPanel, BorderLayout.NORTH);

        // Results table
        billsTable.setModel(billsTableModel);
        billsTable.setFillsViewportHeight(true);
        billsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        billsTable.setRowSelectionAllowed(true);
        billsTable.setColumnSelectionAllowed(false);
        billsTable.getTableHeader().setReorderingAllowed(false);
        billsTable.setDefaultRenderer(LocalDateTime.class, new LocalDateTimeRenderer());

        // Set preferred column widths (adjust as needed)
        billsTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // Bill ID
        billsTable.getColumnModel().getColumn(1).setPreferredWidth(180); // Billed Date
        billsTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Service Description
        billsTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Original Amount
        billsTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Amount Paid
        billsTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Final Amount (Patient Owes)
        billsTable.getColumnModel().getColumn(6).setPreferredWidth(120); // Status

        JScrollPane scrollPane = new JScrollPane(billsTable);
        scrollPane.setBorder(new TitledBorder("Search Results"));
        topPanel.add(scrollPane, BorderLayout.CENTER);

        // Action buttons for results
        JPanel resultsActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        resultsActionPanel.add(payNowButton);
        resultsActionPanel.add(viewLogButton);
        resultsActionPanel.add(printBillButton);
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

        // --- Initial button states ---
        deleteBillButton.setEnabled(false);
        printBillButton.setEnabled(false);
        viewLogButton.setEnabled(false);
        payNowButton.setEnabled(false);
    }

    private void initializeTableModels() {
        billsTableModel.setColumnIdentifiers(new String[]{"Bill ID", "Billed Date", "Service", "Original Amt", "Amt Paid", "Final Amt", "Status"});
    }

    // FIXED: Method to set the data for the billsTable
    public void setBillsTableData(List<MedicalBill> bills) {
        billsTableModel.setRowCount(0); // Clear existing data
        if (bills == null || bills.isEmpty()) {
            billsTableModel.addRow(new Object[]{"", "", "", "", "", "No bills found.", ""});
            billsTable.setEnabled(false); // Make table appear unselectable
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (MedicalBill bill : bills) {
                // FIXED: Calculate correct patient amount
                double patientOwes = bill.getAmount() - bill.getInsurancePaidAmount();
                
                // DEBUG: Print to help understand the values
                System.out.println("BillingPanel - Bill " + bill.getBillId() + ":");
                System.out.println("  Original: " + bill.getAmount());
                System.out.println("  Insurance Paid: " + bill.getInsurancePaidAmount());
                System.out.println("  Final Amount (DB): " + bill.getFinalAmount());
                System.out.println("  Calculated Patient Owes: " + patientOwes);
                System.out.println("  Patient Paid: " + bill.getAmountPaid());
                
                billsTableModel.addRow(new Object[]{
                        bill.getBillId(),
                        bill.getBilledDateTime().format(formatter),
                        bill.getServiceDescription(),
                        String.format("%.2f", bill.getAmount()),                    // Original Amount
                        String.format("%.2f", bill.getAmountPaid()),               // Patient Paid
                        String.format("%.2f", patientOwes),                        // FIXED: Show what patient owes
                        bill.getStatus()
                });
            }
            billsTable.setEnabled(true); // Re-enable selection
        }
        billsTable.repaint(); // Ensure repaint after model change
    }

    // Helper method to get the selected MedicalBill from the table
    public MedicalBill getSelectedBillFromTable(List<MedicalBill> allBills) {
        int selectedRow = billsTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < allBills.size()) {
            // Check for the "No bills found" message row
            if (billsTable.getValueAt(selectedRow, 5).equals("No bills found.")) {
                return null;
            }
            return allBills.get(selectedRow);
        }
        return null;
    }

    public void clearCreateBillForm() {
        createPatientIdField.setText("");
        serviceField.setText("");
        amountField.setText("");
    }

    // Custom Renderer for LocalDateTime in JTable
    private static class LocalDateTimeRenderer extends DefaultTableCellRenderer {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof LocalDateTime) {
                value = ((LocalDateTime) value).format(formatter);
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}