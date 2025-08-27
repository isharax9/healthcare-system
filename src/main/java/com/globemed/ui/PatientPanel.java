package com.globemed.ui;

import javax.swing.*;
import java.awt.*;
import java.text.BreakIterator;
import java.util.Arrays;
// Make sure this specific import is present
import java.util.List;
import com.globemed.insurance.InsurancePlan;

public class PatientPanel extends JPanel {
    // Search components
    private final JTextField searchIdField = new JTextField(15);
    public final JButton searchButton = new JButton("Search");

    // Display/Edit fields
    private final JTextField patientIdField = new JTextField(20);
    private final JTextField patientNameField = new JTextField(20);
    // ADD THIS NEW COMPONENT
    public final JComboBox<Object> insurancePlanComboBox = new JComboBox<>();
    private final JTextArea medicalHistoryArea = new JTextArea(10, 40);
    private final JTextArea treatmentPlansArea = new JTextArea(10, 40);

    // Action buttons
    public final JButton editButton = new JButton("Edit");
    public final JButton saveButton = new JButton("Save");
    public final JButton undoButton = new JButton("Undo");
    public final JButton newButton = new JButton("New");
    public final JButton deleteButton = new JButton("Delete");

    public PatientPanel() {
        setLayout(new BorderLayout(10, 10));

        // --- Search Panel (Top) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        searchPanel.add(new JLabel("Patient ID:"));
        searchPanel.add(searchIdField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        // --- Details Panel (Center) ---
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // --- Row 0: Patient ID ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        detailsPanel.add(new JLabel("Patient ID:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        detailsPanel.add(patientIdField, gbc);

        // --- Row 1: Full Name ---
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        detailsPanel.add(new JLabel("Full Name:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        detailsPanel.add(patientNameField, gbc);

        // ADD THE NEW INSURANCE PLAN ROW
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        detailsPanel.add(new JLabel("Insurance Plan:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        detailsPanel.add(insurancePlanComboBox, gbc);

        // SHIFT THE REMAINING ROWS DOWN
        // --- Row 3: Medical History (Previously Row 2) ---
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0; // Reset vertical weight before setting it for the text area
        detailsPanel.add(new JLabel("Medical History:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        detailsPanel.add(new JScrollPane(medicalHistoryArea), gbc);

        // --- Row 4: Treatment Plans (Previously Row 3) ---
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0; // Reset vertical weight
        detailsPanel.add(new JLabel("Treatment Plans:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        detailsPanel.add(new JScrollPane(treatmentPlansArea), gbc);

        add(detailsPanel, BorderLayout.CENTER);

        // --- Actions Panel (Bottom) ---
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionsPanel.add(newButton);
        actionsPanel.add(editButton);
        actionsPanel.add(saveButton);
        actionsPanel.add(deleteButton);
        actionsPanel.add(undoButton);
        add(actionsPanel, BorderLayout.SOUTH);

        // Initial state of the UI
        setFieldsEditable(false);
        patientIdField.setEditable(false); // ID is never editable
        saveButton.setEnabled(false);
        undoButton.setEnabled(false);
        editButton.setEnabled(false);
        deleteButton.setEnabled(false); // Can't delete until a patient is loaded
    }

    // --- Public methods for the Controller to interact with the View ---

    public String getSearchId() {
        return searchIdField.getText();
    }

    public void setPatientId(String id) { patientIdField.setText(id); }
    public String getPatientName() { return patientNameField.getText(); }
    public void setPatientName(String name) { patientNameField.setText(name); }

    // ADD THESE THREE NEW METHODS
    public void setInsurancePlans(List<InsurancePlan> plans) {
        insurancePlanComboBox.removeAllItems();
        // Add a "None" option as the first item for patients without insurance
        insurancePlanComboBox.addItem("None");
        for (InsurancePlan plan : plans) {
            insurancePlanComboBox.addItem(plan);
        }
    }

    public void setSelectedInsurancePlan(InsurancePlan plan) {
        if (plan == null) {
            insurancePlanComboBox.setSelectedItem("None");
        } else {
            // This is tricky. We need to find the matching object in the list.
            for (int i = 0; i < insurancePlanComboBox.getItemCount(); i++) {
                Object item = insurancePlanComboBox.getItemAt(i);
                if (item instanceof InsurancePlan && ((InsurancePlan) item).getPlanId() == plan.getPlanId()) {
                    insurancePlanComboBox.setSelectedItem(item);
                    return;
                }
            }
        }
    }

    public InsurancePlan getSelectedInsurancePlan() {
        Object selected = insurancePlanComboBox.getSelectedItem();
        if (selected instanceof InsurancePlan) {
            return (InsurancePlan) selected;
        }
        return null; // Return null if "None" or nothing is selected
    }


    public List<String> getMedicalHistory() {
        return Arrays.asList(medicalHistoryArea.getText().split("\\r?\\n"));
    }

    public void setMedicalHistory(List<String> history) {
        if (history != null) {
            medicalHistoryArea.setText(String.join("\n", history));
        } else {
            medicalHistoryArea.setText("");
        }
    }

    public List<String> getTreatmentPlans() {
        return Arrays.asList(treatmentPlansArea.getText().split("\\r?\\n"));
    }

    public void setTreatmentPlans(List<String> plans) {
        if (plans != null) {
            treatmentPlansArea.setText(String.join("\n", plans));
        } else {
            treatmentPlansArea.setText("");
        }
    }

    public void setFieldsEditable(boolean editable) {
        patientNameField.setEditable(editable);
        medicalHistoryArea.setEditable(editable);
        treatmentPlansArea.setEditable(editable);
        insurancePlanComboBox.setEnabled(editable); // <-- ADDED THIS

        saveButton.setEnabled(editable);
        undoButton.setEnabled(editable);
        editButton.setEnabled(!editable && !patientIdField.getText().isEmpty());
        deleteButton.setEnabled(!editable && !patientIdField.getText().isEmpty());
    }

    // Add a specific method to control the ID field's state
    public void setPatientIdEditable(boolean editable) {
        patientIdField.setEditable(editable);
    }

    public void clearFields() {
        searchIdField.setText("");
        patientIdField.setText("");
        patientNameField.setText("");
        insurancePlanComboBox.setSelectedItem("None");
        medicalHistoryArea.setText("");
        treatmentPlansArea.setText("");
    }

    public JTextField getPatientId() { return patientIdField; }
}