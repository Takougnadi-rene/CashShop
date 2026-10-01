package com.cash_shop.user;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import com.cash_shop.common.StyleManager;

/** Modal dialog where the logged-in user changes his own password. */
public class AccountManagementView extends JDialog {
    private final UserService userService = new UserService();
    private final String username;
    private final JPasswordField currentPassword = new JPasswordField(18);
    private final JPasswordField newPassword = new JPasswordField(18);
    private final JPasswordField confirmPassword = new JPasswordField(18);

    /** Builds the dialog for the given user name. */
    public AccountManagementView(JFrame owner, String username) {
        super(owner, "Manage my account", true);
        this.username = username;
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(350, 400);
        setLocationRelativeTo(owner);

        JPanel form = StyleManager.createForm("Change password");
        StyleManager.addRow(form, 0, "Current password:", currentPassword);
        StyleManager.addRow(form, 1, "New password:", newPassword);
        StyleManager.addRow(form, 2, "Confirm password:", confirmPassword);

        JButton save = new JButton("Save password");
        JButton cancel = new JButton("Cancel");
        save.addActionListener(event -> changePassword());
        cancel.addActionListener(event -> dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(save);
        actions.add(cancel);

        JPanel page = StyleManager.createPage();
        page.add(StyleManager.createTitle("Manage my account"), BorderLayout.NORTH);
        page.add(form, BorderLayout.CENTER);
        page.add(actions, BorderLayout.SOUTH);
        setContentPane(page);
    }

    /** Checks that the new passwords match, then changes the password. */
    private void changePassword() {
        String current = new String(currentPassword.getPassword());
        String replacement = new String(newPassword.getPassword());
        if (!replacement.equals(new String(confirmPassword.getPassword()))) {
            showError("The new passwords do not match.");
            return;
        }
        try {
            if (!userService.changePassword(username, current, replacement)) {
                showError("The current password is incorrect.");
                return;
            }
            JOptionPane.showMessageDialog(this, "Password changed successfully.", "Account",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    /** Displays an error dialog. */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Account", JOptionPane.ERROR_MESSAGE);
    }
}