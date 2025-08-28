package com.globemed.ui;

import com.globemed.Main;
import com.globemed.auth.IUser;
import com.globemed.controller.AppointmentController;
import com.globemed.controller.BillingController;
import com.globemed.controller.PatientController;
import com.globemed.controller.ReportController;
import com.globemed.controller.StaffController; // <-- NEW IMPORT

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
        setSize(1250, 800);
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

        // --- NEW: Staff Management Tab ---
        if (currentUser.hasPermission("can_manage_staff")) { // Only Admins have this permission
            StaffPanel staffPanel = new StaffPanel();
            new StaffController(staffPanel, this, currentUser); // Instantiate the new controller
            tabbedPane.addTab("Staff Management", staffPanel); // Add the new tab
        } else {
            System.out.println("User " + currentUser.getUsername() + " does not have permission to manage staff.");
        }
        // --- END NEW TAB ---

        // --- Final Content Addition ---
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
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("More");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());
        JMenuItem darkModeItem = new JMenuItem("Change Theme");
        darkModeItem.addActionListener(e -> toggleDarkMode());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(darkModeItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    /**
     * Toggles between dark and light themes for the application.
     */
    private void toggleDarkMode() {
        try {
            String currentLaF = UIManager.getLookAndFeel().getClass().getName();
            if (currentLaF.contains("Nimbus")) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } else {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            }
            SwingUtilities.updateComponentTreeUI(this);
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
        this.dispose();
        appInstance.restart();
    }
}