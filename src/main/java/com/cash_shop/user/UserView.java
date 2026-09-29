package com.cash_shop.user;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.cash_shop.Home;
import com.cash_shop.aisle.AisleView;
import com.cash_shop.common.StyleManager;
import com.cash_shop.employee.Employee.Role;
import com.cash_shop.sale.SaleView;

public class UserView extends JFrame {

    private final JTextField tfLogin = new JTextField(18);
    private final JPasswordField tfPassword = new JPasswordField(18);

    private final UserService authService = new UserService();
    private final AccessService accessService = new AccessService();

    public UserView() {
        setTitle("Cash Shop - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildUI();
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void buildUI() {
        JPanel form = StyleManager.createForm("Sign in");
        StyleManager.addRow(form, 0, "Username:", tfLogin);
        StyleManager.addRow(form, 1, "Password:", tfPassword);

        JButton btnLogin = new JButton("Login");
        btnLogin.addActionListener(e -> login());
        tfPassword.addActionListener(e -> login());
        getRootPane().setDefaultButton(btnLogin);

        JPanel page = StyleManager.createPage();
        page.add(StyleManager.createTitle("Cash Shop"), BorderLayout.NORTH);
        page.add(form, BorderLayout.CENTER);
        page.add(StyleManager.createButtonBar(btnLogin), BorderLayout.SOUTH);
        setContentPane(page);
    }

    private void login() {
        String login = tfLogin.getText().trim();
        String password = new String(tfPassword.getPassword());

        if (login.isEmpty() && password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        } else if (login.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter your username.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        } else if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter your password.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User authenticatedUser = authService.authenticate(login, password);
            if (authenticatedUser == null) {
                JOptionPane.showMessageDialog(this,
                        "Username, password, or linked employee account is incorrect.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (accessService.getAllowedModules(authenticatedUser.getRole()).isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Your account does not have access to any application module.",
                        "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dispose();
            SwingUtilities.invokeLater(() -> openHome(authenticatedUser));
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openHome(User authenticatedUser) {
        Role role = authenticatedUser.getRole();
        JFrame landingWindow = switch (role) {
            case CASHIER, COUNTER -> new SaleView(authenticatedUser.getEmail());
            case AISLE_MANAGER -> new AisleView(authenticatedUser.getEmployeeMatricule());
            default -> null;
        };
        if (landingWindow == null) {
            new Home(authenticatedUser.getUsername(), role, authenticatedUser.getEmployeeMatricule()).setVisible(true);
            return;
        }
        landingWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                SwingUtilities.invokeLater(() -> new UserView().setVisible(true));
            }
        });
        landingWindow.setVisible(true);
    }

    public static void main(String[] args) {
        StyleManager.applyLookAndFeel();
        SwingUtilities.invokeLater(() -> new UserView().setVisible(true));
    }
}
