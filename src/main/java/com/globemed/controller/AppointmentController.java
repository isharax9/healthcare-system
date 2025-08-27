package com.globemed.controller;

import com.globemed.appointment.AppointmentScheduler;
import com.globemed.appointment.Doctor;
import com.globemed.db.SchedulingDAO;
import com.globemed.ui.AppointmentPanel;

import javax.swing.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * The Controller for the Appointment Scheduling system.
 * It listens to user input from the View (AppointmentPanel) and uses the
 * Mediator (AppointmentScheduler) to perform actions.
 */
public class AppointmentController {
    private final AppointmentPanel view;
    private final SchedulingDAO dao;
    private final AppointmentScheduler scheduler; // The Mediator

    public AppointmentController(AppointmentPanel view) {
        this.view = view;
        this.dao = new SchedulingDAO();
        this.scheduler = new AppointmentScheduler();
        initController();
        loadInitialData();
    }

    /**
     * Wires up the listeners to the UI components.
     */
    private void initController() {
        // When the user selects a doctor or a date, update the schedule view
        view.doctorList.addListSelectionListener(e -> updateAppointmentView());
        view.dateSpinner.addChangeListener(e -> updateAppointmentView());

        // When the user clicks the "Book" button
        view.bookAppointmentButton.addActionListener(e -> bookNewAppointment());
    }

    /**
     * Loads the initial list of doctors into the UI.
     */
    private void loadInitialData() {
        List<Doctor> doctors = dao.getAllDoctors();
        view.setDoctorList(doctors);
    }

    /**
     * Fetches appointments for the selected doctor and date and updates the UI.
     */
    private void updateAppointmentView() {
        Doctor selectedDoctor = view.doctorList.getSelectedValue();
        Date selectedDate = (Date) view.dateSpinner.getValue();

        if (selectedDoctor == null || selectedDate == null) {
            return; // Do nothing if a doctor or date isn't selected
        }

        LocalDate localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        view.setAppointmentsList(dao.getAppointmentsForDoctorOnDate(selectedDoctor.getDoctorId(), localDate));
    }

    /**
     * Handles the logic for booking a new appointment.
     */
    private void bookNewAppointment() {
        // 1. Get all data from the view
        Doctor selectedDoctor = view.doctorList.getSelectedValue();
        String patientId = view.patientIdField.getText().trim();
        String reason = view.reasonField.getText().trim();

        // 2. Validate input
        if (selectedDoctor == null) {
            JOptionPane.showMessageDialog(view, "Please select a doctor.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (patientId.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter a Patient ID.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Combine selected date and time into a single LocalDateTime object
        Date selectedDate = (Date) view.dateSpinner.getValue();
        Date selectedTime = (Date) view.timeSpinner.getValue();

        LocalDate datePart = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime timePart = selectedTime.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        LocalDateTime requestedDateTime = LocalDateTime.of(datePart, timePart);

        // 4. Call the Mediator to perform the booking logic
        String resultMessage = scheduler.bookAppointment(patientId, selectedDoctor, requestedDateTime, reason);

        // 5. Display the result and refresh the view
        JOptionPane.showMessageDialog(view, resultMessage, "Booking Status", JOptionPane.INFORMATION_MESSAGE);
        updateAppointmentView(); // Refresh the list of appointments
    }
}