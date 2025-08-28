package com.globemed;

import com.globemed.auth.AuthService;
import com.globemed.auth.IUser;
import com.globemed.ui.LoginDialog;
import com.globemed.ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // We still use invokeLater to ensure all UI operations are on the EDT
        SwingUtilities.invokeLater(() -> {
            // 1. Create the authentication service
            AuthService authService = new AuthService();
            IUser currentUser = null;

            // 2. Show the login dialog
            LoginDialog loginDialog = new LoginDialog(null); // null parent
            loginDialog.setVisible(true);

            // This part of the code will only run AFTER the dialog is closed

            // 3. Attempt to authenticate
            String username = loginDialog.getUsername();
            // Don't try to log in if the user canceled
            if (username != null && !username.isEmpty()) {
                currentUser = authService.login(username, loginDialog.getPassword());
            }

            // 4. Check if authentication was successful
            if (currentUser != null) {
                // If successful, create and show the main application frame
                // We pass the logged-in user to the MainFrame so it can enforce permissions
                MainFrame mainFrame = new MainFrame(currentUser);
                mainFrame.setVisible(true);
            } else {
                // If login fails or was canceled, show a message and exit the application
                JOptionPane.showMessageDialog(null, "Login failed. Application will now exit.", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        });
    }
}