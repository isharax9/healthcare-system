package com.globemed.ui;

import com.globemed.appointment.Appointment;
import com.globemed.db.SchedulingDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;

public class AllAppointmentsDialog extends JDialog {

    private final JTable appointmentsTable;
    private final SchedulingDAO schedulingDAO;
    private List<Appointment> currentAppointments; // To hold the list of appointments for reference
    private final boolean canMarkAppointmentDone; // Permission to mark as done

    // --- Filter Components ---
    private final JTextField filterDoctorIdField = new JTextField(10);
    private final JButton filterButton = new JButton("Filter by Doctor ID");
    private final JButton clearFilterButton = new JButton("Clear Filter");

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AllAppointmentsDialog(Frame parent, List<Appointment> appointments, boolean canMarkAppointmentDone) {
        super(parent, "All Appointments", true);
        this.schedulingDAO = new SchedulingDAO();
        this.currentAppointments = appointments;
        this.canMarkAppointmentDone = canMarkAppointmentDone;

        // Create the table model
        String[] columnNames = {"ID", "Patient ID", "Doctor ID", "Date/Time", "Reason", "Status"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Status column (index 5) is editable only if user has permission
                return column == 5 && AllAppointmentsDialog.this.canMarkAppointmentDone;
            }
        };

        populateTable(model, this.currentAppointments);
        appointmentsTable = new JTable(model);
        appointmentsTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(appointmentsTable);

        // --- Buttons ---
        JButton markAsDoneButton = new JButton("Mark as Done");
        JButton closeButton = new JButton("Close");

        markAsDoneButton.addActionListener(e -> markAppointmentAsDone());
        closeButton.addActionListener(e -> dispose());

        // --- Filter Panel ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Appointments"));
        filterPanel.add(new JLabel("Doctor ID:"));
        filterPanel.add(filterDoctorIdField);
        filterPanel.add(filterButton);
        filterPanel.add(clearFilterButton);

        filterButton.addActionListener(e -> filterAppointments());
        clearFilterButton.addActionListener(e -> clearFilter());

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(markAsDoneButton);
        buttonPanel.add(closeButton);

        // Set up the dialog layout (REMOVED NOTES SECTION)
        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(filterPanel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER); // Only the table now
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // Initial states
        markAsDoneButton.setEnabled(false); // Managed by selection listener and permission

        appointmentsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = appointmentsTable.getSelectedRow() != -1;
            boolean canMarkSelected = rowSelected && canMarkAppointmentDone && !isSelectedAppointmentCanceled();
            markAsDoneButton.setEnabled(canMarkSelected);
        });

        setSize(900, 600); // Reduced height since notes area is removed
        setLocationRelativeTo(parent);
    }

    // NEW: Check if selected appointment is canceled
    private boolean isSelectedAppointmentCanceled() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1 || selectedRow >= currentAppointments.size()) {
            return false;
        }

        Appointment selectedAppt = currentAppointments.get(selectedRow);
        return "Canceled".equalsIgnoreCase(selectedAppt.getStatus());
    }

    private void populateTable(DefaultTableModel model, List<Appointment> appointmentsToDisplay) {
        model.setRowCount(0);
        if (appointmentsToDisplay == null || appointmentsToDisplay.isEmpty()) {
            return;
        }
        for (Appointment appt : appointmentsToDisplay) {
            Vector<Object> row = new Vector<>();
            row.add(appt.getAppointmentId());
            row.add(appt.getPatientId());
            row.add(appt.getDoctorId());
            row.add(appt.getAppointmentDateTime().format(DATETIME_FORMATTER));
            row.add(appt.getReason());
            row.add(appt.getStatus());
            model.addRow(row);
        }
    }

    private void filterAppointments() {
        String doctorId = filterDoctorIdField.getText().trim();
        if (doctorId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Doctor ID to filter.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Appointment> filteredList = schedulingDAO.getAppointmentsByDoctorId(doctorId);
        currentAppointments = filteredList;
        populateTable((DefaultTableModel)appointmentsTable.getModel(), filteredList);

        if (filteredList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No appointments found for Doctor ID: " + doctorId, "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void clearFilter() {
        filterDoctorIdField.setText("");
        currentAppointments = schedulingDAO.getAllAppointments();
        populateTable((DefaultTableModel)appointmentsTable.getModel(), currentAppointments);
    }

    private void markAppointmentAsDone() {
        if (!canMarkAppointmentDone) {
            JOptionPane.showMessageDialog(this,
                    "You do not have permission to mark appointments as done.\nContact system administrator for access.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to mark as done.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Appointment selectedAppt = currentAppointments.get(selectedRow);

        // Check if appointment is canceled
        if ("Canceled".equalsIgnoreCase(selectedAppt.getStatus())) {
            JOptionPane.showMessageDialog(this,
                    "Cannot mark a canceled appointment as done.\nCanceled appointments cannot be completed.",
                    "Action Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Done".equalsIgnoreCase(selectedAppt.getStatus())) {
            JOptionPane.showMessageDialog(this,
                    "Appointment #" + selectedAppt.getAppointmentId() + " is already marked as Done.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Mark appointment #" + selectedAppt.getAppointmentId() +
                        " for Patient " + selectedAppt.getPatientId() +
                        " as Done?\n\nDate/Time: " + selectedAppt.getAppointmentDateTime().format(DATETIME_FORMATTER) +
                        "\nReason: " + selectedAppt.getReason() +
                        "\nCurrent Status: " + selectedAppt.getStatus(),
                "Confirm Status Change", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String previousStatus = selectedAppt.getStatus();
            selectedAppt.setStatus("Done");

            boolean success = schedulingDAO.updateAppointment(selectedAppt);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Appointment #" + selectedAppt.getAppointmentId() + " marked as Done successfully.\n" +
                                "Status changed from '" + previousStatus + "' to 'Done'\n" +
                                "Updated by: isharax9 at 2025-08-30 19:19:12 UTC",
                        "Update Successful", JOptionPane.INFORMATION_MESSAGE);

                // Refresh the table
                populateTable((DefaultTableModel)appointmentsTable.getModel(), currentAppointments);

                // Clear selection to prevent accidental double-updates
                appointmentsTable.clearSelection();

            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to update appointment status.\nPlease try again or contact system administrator.",
                        "Update Failed", JOptionPane.ERROR_MESSAGE);

                // Revert the status change
                selectedAppt.setStatus(previousStatus);
            }
        }
    }
}