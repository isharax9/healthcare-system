package com.globemed.ui;

import com.globemed.Main; // Import Main class
import com.globemed.auth.IUser;
import com.globemed.controller.AppointmentController;
import com.globemed.controller.BillingController;
import com.globemed.controller.PatientController;
import com.globemed.controller.ReportController;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final IUser currentUser;
    private final Main appInstance; // Reference to the Main application instance

    // Constructor: Now accepts the Main app instance
    public MainFrame(IUser user, Main appInstance) {
        super("GlobeMed Healthcare Management System - Logged in as: " + user.getUsername() + " (" + user.getRole() + ")");
        this.currentUser = user;
        this.appInstance = appInstance; // Store the app instance

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        // Create menu bar first
        createMenuBar();

        JTabbedPane tabbedPane = new JTabbedPane();

        // --- Patient Management Tab ---
        if (currentUser.hasPermission("can_access_patients")) {
            PatientPanel patientPanel = new PatientPanel();
            new PatientController(patientPanel, this, currentUser);
            tabbedPane.addTab("Patient Management", patientPanel);
        } else {
            System.out.println("User " + currentUser.getUsername() + " does not have permission to access Patient Management.");
        }

        // --- Appointments Tab ---
        if (currentUser.hasPermission("can_access_appointments")) {
            AppointmentPanel appointmentPanel = new AppointmentPanel();
            new AppointmentController(appointmentPanel, this, currentUser);
            tabbedPane.addTab("Appointments", appointmentPanel);
        } else {
            System.out.println("User " + currentUser.getUsername() + " does not have permission to access Appointments.");
        }

        // --- Billing Tab ---
        if (currentUser.hasPermission("can_access_billing")) {
            BillingPanel billingPanel = new BillingPanel();
            new BillingController(billingPanel, this, currentUser);
            tabbedPane.addTab("Billing", billingPanel);
        } else {
            System.out.println("User " + currentUser.getUsername() + " does not have permission to access Billing.");
        }

        // --- Reports Tab ---
        if (currentUser.hasPermission("can_generate_reports")) {
            ReportPanel reportPanel = new ReportPanel();
            new ReportController(reportPanel, this, currentUser);
            tabbedPane.addTab("Reports", reportPanel);
        } else {
            System.out.println("User " + currentUser.getUsername() + " does not have permission to generate Reports.");
        }

        // --- Final Content Addition ---
        // If a user has no permissions, display a message instead of an empty tab pane.
        if (tabbedPane.getTabCount() == 0) {
            JLabel noAccessLabel = new JLabel("You do not have permission to access any modules.", SwingConstants.CENTER);
            add(noAccessLabel);
        } else {
            add(tabbedPane);
        }
    }

    /**
     * Creates and configures the main menu bar for the application.
     */
    private void createMenuBar() {
        // --- Menu Bar for Logout/Restart ---
        JMenuBar menuBar = new JMenuBar();

        // --- File Menu ---
        JMenu fileMenu = new JMenu("More");

        // Logout Menu Item
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());

        // Dark Mode Menu Item
        JMenuItem darkModeItem = new JMenuItem("Change Theme");
        darkModeItem.addActionListener(e -> toggleDarkMode());

        // Exit Menu Item
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(darkModeItem);
        fileMenu.addSeparator(); // Adds a visual line between menu items
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        // Set the menu bar for this frame
        setJMenuBar(menuBar);
    }

    /**
     * Toggles between dark and light themes for the application.
     */
    private void toggleDarkMode() {
        try {
            // Get current Look and Feel
            String currentLaF = UIManager.getLookAndFeel().getClass().getName();

            // Toggle between dark and light themes
            if (currentLaF.contains("Nimbus")) {
                // Switch to a light theme (system default)
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } else {
                // Switch to Nimbus (darker theme)
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            }

            // Update all components in the application
            SwingUtilities.updateComponentTreeUI(this);

            // Repaint to ensure changes are visible
            this.repaint();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to toggle dark mode: " + ex.getMessage(),
                    "Theme Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles the logout process by disposing the current frame and
     * telling the main application class to restart the login sequence.
     */
    private void logout() {
        // Close the current MainFrame window
        this.dispose();
        // Call the restart method in the Main class
        appInstance.restart(); // Fixed: use appInstance instead of mainApp
    }
}