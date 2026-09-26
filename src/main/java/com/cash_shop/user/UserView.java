package com.cash_shop.user;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.cash_shop.Home;
import static com.cash_shop.common.StyleManager.ACCENT_BLUE;
import static com.cash_shop.common.StyleManager.ACCENT_GOLD;
import static com.cash_shop.common.StyleManager.BG_DARK;
import static com.cash_shop.common.StyleManager.BORDER_COLOR;
import static com.cash_shop.common.StyleManager.FONT_BODY;
import static com.cash_shop.common.StyleManager.FONT_HEADER;
import static com.cash_shop.common.StyleManager.FONT_SMALL;
import static com.cash_shop.common.StyleManager.FONT_TITLE;
import static com.cash_shop.common.StyleManager.TEXT_MUTED;
import static com.cash_shop.common.StyleManager.TEXT_PRIMARY;
import static com.cash_shop.common.StyleManager.createButton;
import static com.cash_shop.common.StyleManager.createCard;
import static com.cash_shop.common.StyleManager.createField;
import static com.cash_shop.common.StyleManager.createLabel;

public class UserView extends JFrame {

    private JTextField tfLogin;
    private JPasswordField tfPassword;

    private final UserService authService = new UserService();
    private final AccessService accessService = new AccessService();

    public UserView() {
        setTitle("Cash Shop - Login");
        setSize(480, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG_DARK);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        // En-tête
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setBackground(BG_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        JLabel titre = createLabel("CASH SHOP", ACCENT_GOLD, FONT_TITLE);
        header.add(titre);

        // Panneau central
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(BG_DARK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel card = createCard();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(420, 240));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 10, 8, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        JLabel titreForm = createLabel("[ LOGIN ]", TEXT_PRIMARY, FONT_HEADER);
        titreForm.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(titreForm, c);

        c.gridwidth = 1;

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.0;
        card.add(createLabel("Username:", TEXT_MUTED, FONT_SMALL), c);

        c.gridx = 1;
        c.weightx = 1.0;
        tfLogin = createField();
        tfLogin.setForeground(Color.WHITE);
        tfLogin.setCaretColor(Color.WHITE);
        card.add(tfLogin, c);

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.0;
        card.add(createLabel("Password:", TEXT_MUTED, FONT_SMALL), c);

        c.gridx = 1;
        c.weightx = 1.0;
        tfPassword = new JPasswordField();
        tfPassword.setBackground(BG_DARK);
        tfPassword.setForeground(Color.WHITE);
        tfPassword.setCaretColor(Color.WHITE);
        tfPassword.setFont(FONT_BODY);
        tfPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        card.add(tfPassword, c);

        // Bouton connexion
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.weightx = 0.0;
        c.insets = new Insets(16, 10, 8, 10);
        JButton btnConnexion = createButton(" ▶ LOGIN", ACCENT_BLUE);
        btnConnexion.setPreferredSize(new Dimension(220, 38));
        btnConnexion.setFont(new Font("Consolas", Font.BOLD, 13));
        card.add(btnConnexion, c);

        center.add(card, gbc);

        // Action connexion
        btnConnexion.addActionListener(e -> login());
        tfPassword.addActionListener(e -> login());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_DARK);
        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(center, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private void login() {
        String login = tfLogin.getText().trim();
        String password = new String(tfPassword.getPassword());

        if (login.isEmpty() && password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        else if (login.isEmpty()) {
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
            SwingUtilities.invokeLater(() -> new Home(authenticatedUser.getUsername(), authenticatedUser.getRole())
                    .setVisible(true));
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserView().setVisible(true));
    }
}