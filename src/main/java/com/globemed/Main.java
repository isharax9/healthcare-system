package com.globemed;

import com.globemed.auth.AuthService;
import com.globemed.auth.IUser;
import com.globemed.ui.LoginDialog;
import com.globemed.ui.MainFrame;

import javax.swing.*;

public class Main {
    private AuthService authService;

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
            MainFrame mainFrame = new MainFrame(currentUser, this);
            mainFrame.setVisible(true);
        } else {
            // User closed the dialog or failed to log in repeatedly
            System.exit(0);
        }
    }

    public void restart() {
        // This method is called by MainFrame to handle logout
        // It simply starts the login process again
        startApp();
    }
}