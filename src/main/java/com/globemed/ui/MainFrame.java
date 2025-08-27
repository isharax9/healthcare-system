package com.globemed.ui;

import com.globemed.controller.AppointmentController;
import com.globemed.controller.PatientController;

import javax.swing.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("GlobeMed Healthcare Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768); // Increased size a bit to better fit the new layout
        setLocationRelativeTo(null); // Center the window

        JTabbedPane tabbedPane = new JTabbedPane();

        // --- Patient Management Tab (No changes here) ---
        PatientPanel patientPanel = new PatientPanel();
        new PatientController(patientPanel);
        tabbedPane.addTab("Patient Management", patientPanel);

        // --- Appointment Tab (THIS IS THE CHANGED SECTION) ---
        AppointmentPanel appointmentPanel = new AppointmentPanel();
        new AppointmentController(appointmentPanel); // Create the controller to make the panel functional
        tabbedPane.addTab("Appointments", appointmentPanel);


        // --- Billing Tab (Still a placeholder) ---
        JPanel billingPanel = new JPanel();
        billingPanel.add(new JLabel("Billing and Claims UI will go here."));
        tabbedPane.addTab("Billing", billingPanel);

        add(tabbedPane);
    }
}