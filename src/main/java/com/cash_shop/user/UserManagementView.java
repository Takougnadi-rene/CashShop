package com.cash_shop.user;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

import com.cash_shop.common.StyleManager;

/** Administrator window listing users and resetting their password. */
public class UserManagementView extends JFrame {
    private final UserService userService = new UserService();
    private final DefaultTableModel tableModel = StyleManager.createReadOnlyModel("Login", "Email");
    private final JTable table = StyleManager.createTable(tableModel);
    private final JPasswordField passwordField = new JPasswordField(16);
    private final Timer refreshTimer = new Timer(5000, event -> refreshUsers(false));

    /** Opens the window and starts the 5-second refresh. */
    public UserManagementView() {
        setTitle("User Management");
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        refreshUsers();
        refreshTimer.start();
        addWindowListener(new WindowAdapter() {
            // Stop the periodic refresh once the window is closed.
            @Override
            public void windowClosed(WindowEvent event) {
                refreshTimer.stop();
            }
        });
    }

    /** Builds the user table and the password-reset bar. */
    private void buildUI() {
        JButton updatePassword = new JButton("Reset Password");
        JButton refresh = new JButton("Refresh");
        JButton close = new JButton("Close");
        updatePassword.addActionListener(event -> resetPassword());
        refresh.addActionListener(event -> refreshUsers());
        close.addActionListener(event -> dispose());

        // Password field styling
        passwordField.setBackground(StyleManager.BG_INPUT);
        passwordField.setForeground(StyleManager.TEXT_PRIMARY);
        passwordField.setCaretColor(StyleManager.ACCENT_PRIMARY);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleManager.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        JLabel pwdLabel = StyleManager.createLabel("New password:");
        JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 4));
        actions.setOpaque(false);
        actions.add(pwdLabel);
        actions.add(passwordField);
        actions.add(updatePassword);
        actions.add(refresh);
        actions.add(close);
        StyleManager.styleButton(updatePassword);
        StyleManager.styleButton(refresh);
        StyleManager.styleButton(close);

        JPanel page = StyleManager.createPage();
        page.add(StyleManager.createTitle("User Management"), BorderLayout.NORTH);
        page.add(StyleManager.createScrollPane(table), BorderLayout.CENTER);
        page.add(actions, BorderLayout.SOUTH);
        setContentPane(page);
    }

    /** Reloads the users and shows errors. */
    private void refreshUsers() {
        refreshUsers(true);
    }

    /** Reloads the users, keeping the selection (silent for the periodic refresh). */
    private void refreshUsers(boolean showError) {
        try {
            int selectedRow = table.getSelectedRow();
            String selectedUsername = selectedRow < 0 ? null
                    : tableModel.getValueAt(table.convertRowIndexToModel(selectedRow), 0).toString();
            tableModel.setRowCount(0);
            for (User user : userService.getAllUsers()) {
                tableModel.addRow(new Object[] { user.getUsername(), user.getEmail() });
            }
            if (selectedUsername != null) {
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    if (selectedUsername.equals(tableModel.getValueAt(row, 0))) {
                        table.setRowSelectionInterval(row, row);
                        break;
                    }
                }
            }
        } catch (IllegalStateException exception) {
            if (showError) {
                showError(exception.getMessage());
            }
        }
    }

    /** Sets the typed password on the selected user. */
    private void resetPassword() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            showError("Select a user first.");
            return;
        }
        String username = tableModel.getValueAt(table.convertRowIndexToModel(selectedRow), 0).toString();
        try {
            if (!userService.updatePassword(username, new String(passwordField.getPassword()))) {
                showError("The selected user was not found.");
                return;
            }
            passwordField.setText("");
            JOptionPane.showMessageDialog(this, "Password updated successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    /** Displays an error dialog. */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "User Management", JOptionPane.ERROR_MESSAGE);
    }
}
