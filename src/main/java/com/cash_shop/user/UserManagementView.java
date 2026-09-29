package com.cash_shop.user;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.cash_shop.common.StyleManager;

public class UserManagementView extends JFrame {
    private final UserService userService = new UserService();
    private final DefaultTableModel tableModel = StyleManager.createReadOnlyModel("Login", "Email");
    private final JTable table = StyleManager.createTable(tableModel);
    private final JPasswordField passwordField = new JPasswordField(16);

    public UserManagementView() {
        setTitle("User Management");
        setSize(650, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        refreshUsers();
    }

    private void buildUI() {
        JButton updatePassword = new JButton("Reset Password");
        JButton refresh = new JButton("Refresh");
        JButton close = new JButton("Close");
        updatePassword.addActionListener(event -> resetPassword());
        refresh.addActionListener(event -> refreshUsers());
        close.addActionListener(event -> dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actions.add(new JLabel("New password:"));
        actions.add(passwordField);
        actions.add(updatePassword);
        actions.add(refresh);
        actions.add(close);

        JPanel page = StyleManager.createPage();
        page.add(StyleManager.createTitle("User management"), BorderLayout.NORTH);
        page.add(new JScrollPane(table), BorderLayout.CENTER);
        page.add(actions, BorderLayout.SOUTH);
        setContentPane(page);
    }

    private void refreshUsers() {
        try {
            tableModel.setRowCount(0);
            for (User user : userService.getAllUsers()) {
                tableModel.addRow(new Object[] { user.getUsername(), user.getEmail() });
            }
        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

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

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "User Management", JOptionPane.ERROR_MESSAGE);
    }
}
