package com.globemed.ui;

import com.globemed.controller.AppointmentController;
import com.globemed.controller.PatientController;
import com.globemed.controller.BillingController;
import com.globemed.auth.IUser;

import javax.swing.*;

public class MainFrame extends JFrame {

    // --- ADD A FIELD TO HOLD THE CURRENT USER ---
    private final IUser currentUser;

    // --- MODIFY THE CONSTRUCTOR ---
    public MainFrame(IUser user) {
        this.currentUser = user;
        
        // Update the title to show who is logged in
        setTitle("GlobeMed HMS - Logged in as: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        
        // --- NOW, WE APPLY PERMISSIONS ---

        // Only add the Patient Management tab if the user has permission
        if (currentUser.hasPermission("can_access_patients")) {
            PatientPanel patientPanel = new PatientPanel();
            new PatientController(patientPanel);
            tabbedPane.addTab("Patient Management", patientPanel);
        }

        // Only add the Appointments tab if the user has permission
        if (currentUser.hasPermission("can_access_appointments")) {
            AppointmentPanel appointmentPanel = new AppointmentPanel();
            new AppointmentController(appointmentPanel);
            tabbedPane.addTab("Appointments", appointmentPanel);
        }

        // Only add the Billing tab if the user has permission (typically Admins)
        if (currentUser.hasPermission("can_access_billing")) {
            BillingPanel billingPanel = new BillingPanel();
            new BillingController(billingPanel);
            tabbedPane.addTab("Billing", billingPanel);
        }

        // What if a user has no permissions at all? Show a message.
        if (tabbedPane.getTabCount() == 0) {
            JLabel noAccessLabel = new JLabel("You do not have permission to access any modules.", SwingConstants.CENTER);
            add(noAccessLabel);
        } else {
            add(tabbedPane);
        }
    }
}