package com.globemed;

import com.globemed.auth.AuthService;
import com.globemed.auth.IUser;
import com.globemed.ui.LoginDialog;
import com.globemed.ui.MainFrame;

import javax.swing.*;

public class Main {
    private AuthService authService;
    private MainFrame mainFrame; // Keep a reference to the main frame if needed, though often passed directly

    public static void main(String[] args) {
        // The static main method now just creates an instance and starts the app
        SwingUtilities.invokeLater(() -> {
            Main app = new Main();
            app.startApp();
        });
    }

    public Main() {
        this.authService = new AuthService();
    }

    public void startApp() {
        // This method contains our main application logic
        LoginDialog loginDialog = new LoginDialog(null, authService);
        loginDialog.setVisible(true);

        IUser currentUser = loginDialog.getAuthenticatedUser();

        if (currentUser != null) {
            // Pass 'this' (the Main instance) to the MainFrame
            mainFrame = new MainFrame(currentUser, this); // Store reference if needed
            mainFrame.setVisible(true);
        } else {
            // User closed the dialog or failed to log in
            System.out.println("Login canceled or failed. Exiting application.");
            System.exit(0);
        }
    }

    /**
     * This method is called by MainFrame to handle logout and restart the login process.
     */
    public void restart() {
        // Dispose of the old MainFrame if it's still around (should already be disposed by MainFrame itself)
        if (mainFrame != null) {
            mainFrame.dispose();
            mainFrame = null; // Clear reference
        }
        // Start the login process again
        startApp();
    }
}