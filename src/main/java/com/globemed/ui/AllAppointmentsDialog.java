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
    private final boolean canMarkAppointmentDone;

    // --- NEW: Filter Components ---
    private final JTextField filterDoctorIdField = new JTextField(10);
    private final JButton filterButton = new JButton("Filter by Doctor ID");
    private final JButton clearFilterButton = new JButton("Clear Filter");

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AllAppointmentsDialog(Frame parent, List<Appointment> appointments, boolean canMarkAppointmentDone) {
        super(parent, "All Appointments", true);
        this.schedulingDAO = new SchedulingDAO();
        this.currentAppointments = appointments; // Initial list of all appointments
        this.canMarkAppointmentDone = canMarkAppointmentDone;

        // Create the table model
        String[] columnNames = {"ID", "Patient ID", "Doctor ID", "Date/Time", "Reason", "Status"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5 && AllAppointmentsDialog.this.canMarkAppointmentDone;
            }
        };

        populateTable(model, this.currentAppointments); // Populate with the initial list
        appointmentsTable = new JTable(model);
        appointmentsTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(appointmentsTable);

        // --- Buttons ---
        JButton markAsDoneButton = new JButton("Mark as Done");
        JButton closeButton = new JButton("Close");

        markAsDoneButton.addActionListener(e -> markAppointmentAsDone());
        closeButton.addActionListener(e -> dispose());

        // --- Filter Panel (NEW) ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Appointments"));
        filterPanel.add(new JLabel("Doctor ID:"));
        filterPanel.add(filterDoctorIdField);
        filterPanel.add(filterButton);
        filterPanel.add(clearFilterButton);

        // Action listeners for filter buttons (NEW)
        filterButton.addActionListener(e -> filterAppointments());
        clearFilterButton.addActionListener(e -> clearFilter());


        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(markAsDoneButton);
        buttonPanel.add(closeButton);

        // Set up the dialog layout
        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(filterPanel, BorderLayout.NORTH); // <-- ADD FILTER PANEL
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // Initial button state
        markAsDoneButton.setEnabled(false && canMarkAppointmentDone);

        appointmentsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = appointmentsTable.getSelectedRow() != -1;
            markAsDoneButton.setEnabled(rowSelected && canMarkAppointmentDone);
        });

        setSize(900, 600);
        setLocationRelativeTo(parent);
    }

    private void populateTable(DefaultTableModel model, List<Appointment> appointmentsToDisplay) {
        model.setRowCount(0); // Clear existing rows
        if (appointmentsToDisplay == null || appointmentsToDisplay.isEmpty()) {
            // Optionally add a "No appointments found" message
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

    // --- NEW METHOD: Filter Appointments ---
    private void filterAppointments() {
        String doctorId = filterDoctorIdField.getText().trim();
        if (doctorId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Doctor ID to filter.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Fetch filtered appointments from the DAO
        List<Appointment> filteredList = schedulingDAO.getAppointmentsByDoctorId(doctorId);
        currentAppointments = filteredList; // Update currentAppointments to reflect the filtered list
        populateTable((DefaultTableModel)appointmentsTable.getModel(), filteredList);

        if (filteredList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No appointments found for Doctor ID: " + doctorId, "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // --- NEW METHOD: Clear Filter ---
    private void clearFilter() {
        filterDoctorIdField.setText(""); // Clear the text field
        currentAppointments = schedulingDAO.getAllAppointments(); // Re-fetch all appointments
        populateTable((DefaultTableModel)appointmentsTable.getModel(), currentAppointments);
    }

    private void markAppointmentAsDone() {
        if (!canMarkAppointmentDone) {
            JOptionPane.showMessageDialog(this, "You do not have permission to mark appointments as done.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to mark as done.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the Appointment object from our list using the selected row index
        // This list might be filtered, so ensure we get the correct one
        Appointment selectedAppt = currentAppointments.get(selectedRow);

        if ("Done".equalsIgnoreCase(selectedAppt.getStatus())) {
            JOptionPane.showMessageDialog(this, "Appointment is already marked as Done.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Mark appointment #" + selectedAppt.getAppointmentId() + " for Patient " + selectedAppt.getPatientId() + " as Done?",
                "Confirm Status Change", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            selectedAppt.setStatus("Done");
            boolean success = schedulingDAO.updateAppointment(selectedAppt);

            if (success) {
                JOptionPane.showMessageDialog(this, "Appointment marked as Done successfully.");
                populateTable((DefaultTableModel)appointmentsTable.getModel(), currentAppointments); // Refresh with potentially filtered list
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update appointment status.", "Error", JOptionPane.ERROR_MESSAGE);
                selectedAppt.setStatus("Scheduled");
            }
        }
    }
}