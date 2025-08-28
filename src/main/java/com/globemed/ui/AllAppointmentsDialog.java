package com.globemed.ui;

import com.globemed.appointment.Appointment;
import com.globemed.db.SchedulingDAO; // Will be used to update status
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

    // This formatter is useful for displaying LocalDateTime objects
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AllAppointmentsDialog(Frame parent, List<Appointment> appointments, boolean canMarkAppointmentDone) {
        super(parent, "All Appointments", true); // Modal dialog
        this.schedulingDAO = new SchedulingDAO();
        this.currentAppointments = appointments; // Store the initial list

        // Create the table model
        String[] columnNames = {"ID", "Patient ID", "Doctor ID", "Date/Time", "Reason", "Status"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make all cells non-editable except for the "Status" if we want to add an inline editor later
                return column == 5; // Status column (index 5) could be editable
            }
        };

        // Populate the table with initial data
        populateTable(model, appointments);
        appointmentsTable = new JTable(model);
        appointmentsTable.setFillsViewportHeight(true);

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(appointmentsTable);

        // --- Buttons ---
        JButton markAsDoneButton = new JButton("Mark as Done");
        JButton closeButton = new JButton("Close");

        // Action listener for Mark as Done button
        markAsDoneButton.addActionListener(e -> markAppointmentAsDone());

        // Action listener for Close button
        closeButton.addActionListener(e -> dispose());

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(markAsDoneButton);
        buttonPanel.add(closeButton);

        // Set up the dialog layout
        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // Initial button state
        markAsDoneButton.setEnabled(false);

        // Enable/disable "Mark as Done" based on table selection
        appointmentsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = appointmentsTable.getSelectedRow() != -1;
            markAsDoneButton.setEnabled(rowSelected);
        });

        setSize(900, 600); // A good size for listing all appointments
        setLocationRelativeTo(parent);
    }

    private void populateTable(DefaultTableModel model, List<Appointment> appointments) {
        model.setRowCount(0); // Clear existing rows
        for (Appointment appt : appointments) {
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

    private void markAppointmentAsDone() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to mark as done.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the Appointment object from our list using the selected row index
        Appointment selectedAppt = currentAppointments.get(selectedRow);

        if ("Done".equalsIgnoreCase(selectedAppt.getStatus())) {
            JOptionPane.showMessageDialog(this, "Appointment is already marked as Done.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Mark appointment #" + selectedAppt.getAppointmentId() + " for Patient " + selectedAppt.getPatientId() + " as Done?",
                "Confirm Status Change", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            selectedAppt.setStatus("Done"); // Update the object in memory
            boolean success = schedulingDAO.updateAppointment(selectedAppt); // Persist to DB

            if (success) {
                JOptionPane.showMessageDialog(this, "Appointment marked as Done successfully.");
                // Refresh the table with updated status
                populateTable((DefaultTableModel)appointmentsTable.getModel(), currentAppointments);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update appointment status.", "Error", JOptionPane.ERROR_MESSAGE);
                // Optionally revert status if DB update fails
                selectedAppt.setStatus("Scheduled");
            }
        }
    }
}