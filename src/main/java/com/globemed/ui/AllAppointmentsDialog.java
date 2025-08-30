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
    private final boolean canMarkAppointmentDone; // Permission to mark as done (implies notes editable)

    // --- Filter Components ---
    private final JTextField filterDoctorIdField = new JTextField(10);
    private final JButton filterButton = new JButton("Filter by Doctor ID");
    private final JButton clearFilterButton = new JButton("Clear Filter");

    // --- NEW: Doctor Notes Component ---
    private final JTextArea doctorNotesArea = new JTextArea(4, 30); // Rows, cols for notes
    private final JScrollPane doctorNotesScrollPane; // For the notes area

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

        // --- NEW: Notes Panel ---
        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.setBorder(BorderFactory.createTitledBorder("Doctor Notes/Prescription"));
        doctorNotesArea.setLineWrap(true);
        doctorNotesArea.setWrapStyleWord(true);
        doctorNotesScrollPane = new JScrollPane(doctorNotesArea); // Initialize scroll pane
        notesPanel.add(doctorNotesScrollPane, BorderLayout.CENTER);

        // --- Central Panel to combine table and notes ---
        JPanel centerCombinedPanel = new JPanel(new BorderLayout(10, 10));
        centerCombinedPanel.add(scrollPane, BorderLayout.CENTER);
        centerCombinedPanel.add(notesPanel, BorderLayout.SOUTH); // Notes below the table

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(markAsDoneButton);
        buttonPanel.add(closeButton);

        // Set up the dialog layout
        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(filterPanel, BorderLayout.NORTH);
        getContentPane().add(centerCombinedPanel, BorderLayout.CENTER); // Use the combined panel
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // Initial states
        markAsDoneButton.setEnabled(false); // Managed by selection listener and permission
        doctorNotesArea.setEditable(false); // Initially read-only

        appointmentsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = appointmentsTable.getSelectedRow() != -1;
            markAsDoneButton.setEnabled(rowSelected && canMarkAppointmentDone);
            populateNotesArea(rowSelected); // Populate notes area on selection
            doctorNotesArea.setEditable(rowSelected && canMarkAppointmentDone); // Notes editable based on permission and selection
            doctorNotesArea.setBackground(doctorNotesArea.isEditable() ? Color.WHITE : UIManager.getColor("Panel.background"));
        });

        setSize(900, 750); // Increased height to accommodate notes area
        setLocationRelativeTo(parent);
    }

    // --- NEW: Populate Notes Area Method ---
    private void populateNotesArea(boolean isSelected) {
        if (isSelected) {
            int selectedRow = appointmentsTable.getSelectedRow();
            if (selectedRow != -1 && selectedRow < currentAppointments.size()) {
                Appointment selectedAppt = currentAppointments.get(selectedRow);
                doctorNotesArea.setText(selectedAppt.getDoctorNotes() != null ? selectedAppt.getDoctorNotes() : "");
                doctorNotesArea.setCaretPosition(0); // Scroll to top
            }
        } else {
            doctorNotesArea.setText("");
        }
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
            selectedAppt.setDoctorNotes(doctorNotesArea.getText().trim()); // Save notes from the area <-- NEW
            boolean success = schedulingDAO.updateAppointment(selectedAppt);

            if (success) {
                JOptionPane.showMessageDialog(this, "Appointment marked as Done successfully.");
                populateTable((DefaultTableModel)appointmentsTable.getModel(), currentAppointments);
                populateNotesArea(true); // Refresh notes area just in case (e.g., clears scroll)
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update appointment status.", "Error", JOptionPane.ERROR_MESSAGE);
                selectedAppt.setStatus("Scheduled");
            }
        }
    }
}