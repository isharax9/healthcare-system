package com.globemed.ui;

import com.globemed.auth.AuthService;
import com.globemed.auth.IUser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginDialog extends JDialog {

    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JLabel statusLabel = new JLabel(" ");
    private final JButton loginButton = new JButton("Login");
    

    private IUser authenticatedUser = null;

    public LoginDialog(Frame parent, AuthService authService) {
        super(parent, "GlobeMed System Login", true);

        // --- Main Panel with 1 row, 2 columns ---
        setLayout(new GridLayout(1, 2));

        // --- 1. Left Panel (Image) ---
        add(createImagePanel());

        // --- 2. Right Panel (Login Form) ---
        add(createLoginPanel(authService));

        setSize(1024, 768);
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private JPanel createImagePanel() {
        JPanel imagePanel = new JPanel(new BorderLayout());
        try {
            java.net.URL imageUrl = getClass().getResource("/login_background.png");
            if (imageUrl != null) {
                ImageIcon imageIcon = new ImageIcon(new ImageIcon(imageUrl).getImage().getScaledInstance(512, 768, Image.SCALE_SMOOTH));
                imagePanel.add(new JLabel(imageIcon), BorderLayout.CENTER);
            } else {
                imagePanel.setBackground(new Color(20, 40, 80)); // Fallback color
                JLabel errorLabel = new JLabel("Image not found", SwingConstants.CENTER);
                errorLabel.setForeground(Color.WHITE);
                imagePanel.add(errorLabel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imagePanel;
    }

    // In com.globemed.ui.LoginDialog.java

    private JPanel createLoginPanel(AuthService authService) {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        // 1. Set the exact cyan background color
        loginPanel.setBackground(new Color(249, 249, 252)); // Bright Cyan
        loginPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Title and Subtitle Styling ---
        JLabel titleLabel = new JLabel("Welcome Back!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Helvetica Neue", Font.BOLD, 36)); // Modern font
        titleLabel.setForeground(Color.BLACK);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("Login to access the GlobeMed HMS", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.DARK_GRAY);
        gbc.gridy = 1; loginPanel.add(subtitleLabel, gbc);

        // --- Separator Styling ---
        JSeparator separator = new JSeparator();
        separator.setForeground(Color.LIGHT_GRAY);
        gbc.gridy = 2; gbc.insets = new Insets(20, 0, 20, 0); // Add more vertical spacing
        loginPanel.add(separator, gbc);
        gbc.insets = new Insets(10, 10, 10, 10); // Reset insets

        // --- Text Field Styling ---
        Font textFieldFont = new Font("Helvetica Neue", Font.PLAIN, 16);
        usernameField.setFont(textFieldFont);
        usernameField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        passwordField.setFont(textFieldFont);
        passwordField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        // --- Labels Styling ---
        Font labelFont = new Font("Helvetica Neue", Font.BOLD, 14);
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(labelFont);
        userLabel.setForeground(Color.BLACK);
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(labelFont);
        passLabel.setForeground(Color.BLACK);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 3; loginPanel.add(userLabel, gbc);
        gbc.gridx = 1; loginPanel.add(usernameField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; loginPanel.add(passLabel, gbc);
        gbc.gridx = 1; loginPanel.add(passwordField, gbc);

        // Status Label
        statusLabel.setForeground(Color.RED.darker());
        statusLabel.setFont(new Font("Helvetica Neue", Font.BOLD, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        loginPanel.add(statusLabel, gbc);

        // Login Button
        styleLoginButton();
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10); // Add top margin to button
        loginPanel.add(loginButton, gbc);

        // --- Action Listeners (No changes needed here) ---
        ActionListener loginAction = e -> {
            IUser user = authService.login(getUsername(), getPassword());
            if (user != null) {
                authenticatedUser = user;
                dispose();
            } else {
                statusLabel.setText("Invalid username or password.");
            }
        };
        loginButton.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);

        return loginPanel;
    }

    private void styleLoginButton() {
        loginButton.setFont(new Font("Helvetica Neue", Font.BOLD, 20));
        // 2. Set the exact red background
        loginButton.setBackground(new Color(255, 0, 0)); // Bright Red
        // 3. Set the text color
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        // 4. Create a rounded border
        loginButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 0, 0), 2, true), // Outer rounded border
                BorderFactory.createEmptyBorder(10, 30, 10, 30) // Inner padding
        ));
        loginButton.setContentAreaFilled(false);
        loginButton.setOpaque(true);
    }

    public String getUsername() { return usernameField.getText().trim(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public IUser getAuthenticatedUser() { return authenticatedUser; }
}