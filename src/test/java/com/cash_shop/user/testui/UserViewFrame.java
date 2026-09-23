package com.cash_shop.user.testui;

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
import com.cash_shop.user.UserService;

public class UserViewFrame extends JFrame implements UserView {

    private final JTextField tfLogin = createField();
    private final JPasswordField tfPassword = new JPasswordField();
    private final UserService authService = new UserService();

    public UserViewFrame() {
        setTitle("SUPERMARKET MANAGER - Connexion");
        setSize(480, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG_DARK);
        buildUI();
    }

    @Override
    public JFrame createWindow() {
        return this;
    }

    @Override
    public boolean login(String username, String password) {
        return authService.login(username.trim(), password);
    }

    private void buildUI() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setBackground(BG_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        header.add(createLabel("⚙ SUPERMARKET MANAGER", ACCENT_GOLD, FONT_TITLE));

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

        JLabel title = createLabel("[ CONNEXION ]", TEXT_PRIMARY, FONT_HEADER);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title, c);

        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.0;
        card.add(createLabel("Identifiant / Login :", TEXT_MUTED, FONT_SMALL), c);

        c.gridx = 1;
        c.weightx = 1.0;
        tfLogin.setForeground(Color.WHITE);
        tfLogin.setCaretColor(Color.WHITE);
        card.add(tfLogin, c);

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.0;
        card.add(createLabel("Mot de passe :", TEXT_MUTED, FONT_SMALL), c);

        c.gridx = 1;
        c.weightx = 1.0;
        tfPassword.setBackground(BG_DARK);
        tfPassword.setForeground(Color.WHITE);
        tfPassword.setCaretColor(Color.WHITE);
        tfPassword.setFont(FONT_BODY);
        tfPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        card.add(tfPassword, c);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.weightx = 0.0;
        c.insets = new Insets(16, 10, 8, 10);
        JButton loginButton = createButton(" ▶ SE CONNECTER", ACCENT_BLUE);
        loginButton.setPreferredSize(new Dimension(220, 38));
        loginButton.setFont(new Font("Consolas", Font.BOLD, 13));
        card.add(loginButton, c);
        center.add(card, gbc);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setBackground(BG_DARK);
        infoPanel.add(createLabel(
                "admin/admin | comptable/1234 | magasinier/1235 | caissier1/1234 | chef1/1234",
                TEXT_MUTED, FONT_SMALL));

        loginButton.addActionListener(event -> submitLogin());
        tfPassword.addActionListener(event -> submitLogin());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_DARK);
        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(center, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    private void submitLogin() {
        if (login(tfLogin.getText(), new String(tfPassword.getPassword()))) {
            JOptionPane.showMessageDialog(this, "Connexion réussie !", "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
            setVisible(false);
        } else {
            JOptionPane.showMessageDialog(this, "Identifiant ou mot de passe incorrect.",
                    "Échec de connexion", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserViewFrame().setVisible(true));
    }
}
