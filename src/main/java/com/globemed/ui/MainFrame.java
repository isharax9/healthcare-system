package com.globemed.ui;

import com.globemed.Main;
import com.globemed.auth.IUser;
import com.globemed.controller.*;
import javax.swing.*;

public class MainFrame extends JFrame {

    private final IUser currentUser;
    private final Main mainApp; // Reference to the main application instance for restarting

    /**
     * Constructor for the main application window.
     * @param user The currently logged-in user, decorated with their role.
     * @param mainApp The instance of the Main class to handle app lifecycle (e.g., logout/restart).
     */
    public MainFrame(IUser user, Main mainApp) {
        this.currentUser = user;
        this.mainApp = mainApp;

        // --- Frame Setup ---
        setTitle("GlobeMed HMS - Logged in as: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null); // Center the window

        // --- Create and add the Menu Bar ---
        createMenuBar();

        // --- Create the Tabbed Pane ---
        JTabbedPane tabbedPane = new JTabbedPane();

        // --- Apply Permissions to Tabs ---

        // 1. Patient Management Tab
        if (currentUser.hasPermission("can_access_patients")) {
            PatientPanel patientPanel = new PatientPanel();
            new PatientController(patientPanel, currentUser);
            tabbedPane.addTab("Patient Management", patientPanel);
        }

        // 2. Appointments Tab
        if (currentUser.hasPermission("can_access_appointments")) {
            AppointmentPanel appointmentPanel = new AppointmentPanel();
            new AppointmentController(appointmentPanel, currentUser);
            tabbedPane.addTab("Appointments", appointmentPanel);
        }

        // 3. Billing Tab
        if (currentUser.hasPermission("can_access_billing")) {
            BillingPanel billingPanel = new BillingPanel();
            new BillingController(billingPanel, currentUser);
            tabbedPane.addTab("Billing", billingPanel);
        }

        // 4. Reports Tab
        if (currentUser.hasPermission("can_generate_reports")) {
            ReportPanel reportPanel = new ReportPanel();
            new ReportController(reportPanel);
            tabbedPane.addTab("Reports", reportPanel);
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
        JMenuBar menuBar = new JMenuBar();

        // --- File Menu ---
        JMenu fileMenu = new JMenu("File");

        // Logout Menu Item
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());

        // Exit Menu Item
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(logoutItem);
        fileMenu.addSeparator(); // Adds a visual line between menu items
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        // Set the menu bar for this frame
        setJMenuBar(menuBar);
    }

    /**
     * Handles the logout process by disposing the current frame and
     * telling the main application class to restart the login sequence.
     */
    private void logout() {
        // Close the current MainFrame window
        this.dispose();
        // Call the restart method in the Main class
        mainApp.restart();
    }
}