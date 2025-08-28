package com.globemed.ui;

import com.globemed.patient.PatientRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Vector;

/**
 * A dialog window that displays a list of all patients in a table.
 */
public class AllPatientsDialog extends JDialog {

    public AllPatientsDialog(Frame parent, List<PatientRecord> patients) {
        super(parent, "All Patients", true); // `true` for a modal dialog

        // --- Table Model Definition ---
        String[] columnNames = {"Patient ID", "Full Name", "Insurance Plan"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            // Make cells non-editable
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // --- Populate the Model ---
        for (PatientRecord patient : patients) {
            Vector<Object> row = new Vector<>();
            row.add(patient.getPatientId());
            row.add(patient.getName());

            // Safely get the insurance plan name, or show "None" if null
            String insuranceLevel = (patient.getInsurancePlan() != null)
                    ? patient.getInsurancePlan().getPlanName()
                    : "None";
            row.add(insuranceLevel);

            model.addRow(row);
        }

        // --- Create and Configure the Table ---
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // --- Layout ---
        setLayout(new BorderLayout());
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Dialog Properties ---
        setSize(700, 500);
        setLocationRelativeTo(parent); // Center relative to the main window
    }
}