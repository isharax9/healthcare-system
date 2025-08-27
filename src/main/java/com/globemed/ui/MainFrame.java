package com.globemed.ui;

import javax.swing.*;
import java.awt.*;

/**
 * The main window (View) of our application.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("GlobeMed Healthcare Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // Center the window

        // Create a simple panel with a welcome message
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome to GlobeMed HMS", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Serif", Font.BOLD, 24));
        mainPanel.add(welcomeLabel, BorderLayout.CENTER);

        // Add the panel to the frame
        add(mainPanel);
    }
}