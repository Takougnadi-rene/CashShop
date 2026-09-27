package com.cash_shop.user;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.cash_shop.common.StyleManager;

public class UserManagementView extends JFrame {
    private final UserService userService = new UserService();
    private final DefaultTableModel tableModel = new DefaultTableModel(new String[] { "Login", "Email" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = StyleManager.createTable(tableModel);
    private final JPasswordField passwordField = new JPasswordField();

    public UserManagementView() {
        setTitle("User Management");
        setSize(700, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        refreshUsers();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(StyleManager.BG_DARK);
        main.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        main.add(StyleManager.createLabel("USER MANAGEMENT", StyleManager.ACCENT_GOLD,
                StyleManager.FONT_TITLE), BorderLayout.NORTH);
        main.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actions.setOpaque(false);
        passwordField.setBackground(StyleManager.BG_DARK);
        passwordField.setForeground(StyleManager.TEXT_PRIMARY);
        passwordField.setCaretColor(StyleManager.ACCENT_GOLD);
        passwordField.setFont(StyleManager.FONT_BODY);
        passwordField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(StyleManager.BORDER_COLOR),
                javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        passwordField.setColumns(16);

        javax.swing.JButton updatePassword = StyleManager.createButton("Reset Password", StyleManager.ACCENT_BLUE);
        javax.swing.JButton refresh = StyleManager.createButton("Refresh", StyleManager.ACCENT_GREEN);
        javax.swing.JButton close = StyleManager.createButton("Close", new Color(80, 80, 100));
        updatePassword.addActionListener(event -> resetPassword());
        refresh.addActionListener(event -> refreshUsers());
        close.addActionListener(event -> dispose());
        actions.add(StyleManager.createLabel("New password:", StyleManager.TEXT_MUTED, StyleManager.FONT_SMALL));
        actions.add(passwordField);
        actions.add(updatePassword);
        actions.add(refresh);
        actions.add(close);
        main.add(actions, BorderLayout.SOUTH);
        setContentPane(main);
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