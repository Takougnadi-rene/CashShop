package com.cash_shop;

import com.cash_shop.common.StyleManager;
import com.cash_shop.user.UserView;

/**
 * Application entry point: applies the visual theme, then opens the login window on the Swing Event Dispatch
 * Thread.
 */
public class App {
    /** Starts the Cash Shop desktop application. */
    public static void main(String[] args) {
        // Apply the custom UI styling before creating the main window.
        StyleManager.applyLookAndFeel();

        // Start the Swing UI on the Event Dispatch Thread (EDT).
        javax.swing.SwingUtilities.invokeLater(() -> {
            UserView userView = new UserView();
            userView.setVisible(true);
        });
    }
}
