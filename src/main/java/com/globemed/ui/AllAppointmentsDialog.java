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
    private final boolean canMarkAppointmentDone; // <-- NEW FIELD to store permission

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // MODIFIED CONSTRUCTOR: Store the permission
    public AllAppointmentsDialog(Frame parent, List<Appointment> appointments, boolean canMarkAppointmentDone) {
        super(parent, "All Appointments", true);
        this.schedulingDAO = new SchedulingDAO();
        this.currentAppointments = appointments;
        this.canMarkAppointmentDone = canMarkAppointmentDone; // <-- Store the permission

        String[] columnNames = {"ID", "Patient ID", "Doctor ID", "Date/Time", "Reason", "Status"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Status column (index 5) is editable only if user has permission
                return column == 5 && AllAppointmentsDialog.this.canMarkAppointmentDone;
            }
        };

        populateTable(model, appointments);
        appointmentsTable = new JTable(model);
        appointmentsTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(appointmentsTable);

        JButton markAsDoneButton = new JButton("Mark as Done");
        JButton closeButton = new JButton("Close");

        markAsDoneButton.addActionListener(e -> markAppointmentAsDone());
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(markAsDoneButton);
        buttonPanel.add(closeButton);

        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // --- MODIFIED: Initial button state depends on permission ---
        markAsDoneButton.setEnabled(false && canMarkAppointmentDone); // Initially false, but also requires permission

        // --- MODIFIED: Enable/disable "Mark as Done" based on selection AND permission ---
        appointmentsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = appointmentsTable.getSelectedRow() != -1;
            markAsDoneButton.setEnabled(rowSelected && canMarkAppointmentDone);
        });

        setSize(900, 600);
        setLocationRelativeTo(parent);
    }

    private void populateTable(DefaultTableModel model, List<Appointment> appointments) {
        model.setRowCount(0);
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
        if (!canMarkAppointmentDone) { // Extra check for robustness
            JOptionPane.showMessageDialog(this, "You do not have permission to mark appointments as done.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to mark as done.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

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
                populateTable((DefaultTableModel)appointmentsTable.getModel(), currentAppointments);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update appointment status.", "Error", JOptionPane.ERROR_MESSAGE);
                selectedAppt.setStatus("Scheduled");
            }
        }
    }
}