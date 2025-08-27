package com.globemed.ui;

import com.globemed.controller.PatientController;

import javax.swing.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("GlobeMed Healthcare Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // Center the window

        JTabbedPane tabbedPane = new JTabbedPane();

        // --- Patient Management Tab ---
        PatientPanel patientPanel = new PatientPanel();
        // The controller is created but not stored as a field here,
        // as its job is to just wire up the components. The listeners it sets
        // will keep it from being garbage collected.
        new PatientController(patientPanel);

        tabbedPane.addTab("Patient Management", patientPanel);

        // --- Add other tabs here later for Appointment, Billing, etc. ---
        JPanel appointmentPanel = new JPanel();
        appointmentPanel.add(new JLabel("Appointment Scheduling UI will go here."));
        tabbedPane.addTab("Appointments", appointmentPanel);

        JPanel billingPanel = new JPanel();
        billingPanel.add(new JLabel("Billing and Claims UI will go here."));
        tabbedPane.addTab("Billing", billingPanel);

        add(tabbedPane);
    }
}