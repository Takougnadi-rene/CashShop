package com.cash_shop.user;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.cash_shop.aisle.AisleView;
import com.cash_shop.common.Home;
import com.cash_shop.common.StyleManager;
import com.cash_shop.employee.Employee.Role;
import com.cash_shop.sale.SaleView;

public class UserView extends JFrame {

    private final JTextField tfLogin    = createStyledField();
    private final JPasswordField tfPassword = createStyledPasswordField();

    private final UserService    authService   = new UserService();
    private final AccessService  accessService = new AccessService();

    public UserView() {
        setTitle("Cash Shop – Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        buildUI();
        pack();
        setSize(new Dimension(780, 480));
        setLocationRelativeTo(null);
    }

    // ─── Construction de l'interface ────────────────────────────────────────

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(StyleManager.BG_SURFACE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setPreferredSize(new Dimension(780, 480));

        root.add(buildBrandPanel(), BorderLayout.WEST);
        root.add(buildLoginPanel(), BorderLayout.CENTER);
        setContentPane(root);
    }

    /** Panneau gauche avec gradient et branding. */
    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(
                        0, 0,   new Color(0x2D, 0x1B, 0x69),
                        0, getHeight(), new Color(0x11, 0x0D, 0x30)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Cercles décoratifs
                g2.setColor(new Color(0x6C, 0x63, 0xFF, 40));
                g2.fillOval(-60, -60, 220, 220);
                g2.setColor(new Color(0x00, 0xD4, 0xAA, 25));
                g2.fillOval(getWidth() - 100, getHeight() - 120, 180, 180);
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(310, 480));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(60, 40, 60, 40));

        // Icône / logo
        JLabel icon = new JLabel("") {
            { setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
              setForeground(Color.WHITE);
              setAlignmentX(CENTER_ALIGNMENT); }
        };

        JLabel title = new JLabel("Cash Shop");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Point of Supermarket System");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(0xB0, 0xA8, 0xFF));
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        // Séparateur décoratif
        JPanel sep = new JPanel() {
            { setOpaque(false); setMaximumSize(new Dimension(60, 3)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, new Color(0x6C, 0x63, 0xFF),
                        getWidth(), 0, new Color(0x00, 0xD4, 0xAA)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 3, 3);
                g2.dispose();
            }
        };

        JLabel tagline = new JLabel("<html><center>Manage the supermarket<br>with elegance</center></html>");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tagline.setForeground(new Color(0x88, 0x80, 0xCC));
        tagline.setAlignmentX(CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(icon);
        panel.add(Box.createVerticalStrut(16));
        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(20));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(20));
        panel.add(tagline);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    /** Panneau droit avec formulaire de connexion. */
    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(StyleManager.BG_SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);

        // Titre
        JLabel heading = new JLabel("Welcome!");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        heading.setForeground(StyleManager.TEXT_PRIMARY);
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 4, 0);
        panel.add(heading, gbc);

        JLabel sub = new JLabel("Sign in to your account");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(StyleManager.TEXT_SECONDARY);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 28, 0);
        panel.add(sub, gbc);

        // Champ username
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 4, 0);
        panel.add(fieldLabel("Username"), gbc);
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 16, 0);
        panel.add(tfLogin, gbc);

        // Champ password
        gbc.gridy = 4; gbc.insets = new Insets(0, 0, 4, 0);
        panel.add(fieldLabel("Password"), gbc);
        gbc.gridy = 5; gbc.insets = new Insets(0, 0, 28, 0);
        panel.add(tfPassword, gbc);

        // Bouton login
        JButton btnLogin = buildLoginButton();
        btnLogin.addActionListener(e -> login());
        tfPassword.addActionListener(e -> login());
        getRootPane().setDefaultButton(btnLogin);
        gbc.gridy = 6; gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(btnLogin, gbc);

        return panel;
    }

    // ─── Helpers visuels ────────────────────────────────────────────────────

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(StyleManager.TEXT_SECONDARY);
        return label;
    }

    private static JTextField createStyledField() {
        JTextField f = new JTextField(20);
        f.setBackground(StyleManager.BG_INPUT);
        f.setForeground(StyleManager.TEXT_PRIMARY);
        f.setCaretColor(StyleManager.ACCENT_PRIMARY);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleManager.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        f.setPreferredSize(new Dimension(300, 42));
        return f;
    }

    private static JPasswordField createStyledPasswordField() {
        JPasswordField f = new JPasswordField(20);
        f.setBackground(StyleManager.BG_INPUT);
        f.setForeground(StyleManager.TEXT_PRIMARY);
        f.setCaretColor(StyleManager.ACCENT_PRIMARY);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleManager.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        f.setPreferredSize(new Dimension(300, 42));
        return f;
    }

    private JButton buildLoginButton() {
        JButton btn = new JButton("Sign In") {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color from = hovered ? new Color(0x8A, 0x82, 0xFF) : StyleManager.ACCENT_PRIMARY;
                Color to   = hovered ? new Color(0x00, 0xFF, 0xCC) : StyleManager.ACCENT_TEAL;
                g2.setPaint(new GradientPaint(0, 0, from, getWidth(), 0, to));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(300, 44));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleManager.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(9, 19, 9, 19)));
        return btn;
    }



    // ─── Logique de connexion ────────────────────────────────────────────────

    private void login() {
        String login    = tfLogin.getText().trim();
        String password = new String(tfPassword.getPassword());

        if (login.isEmpty() && password.isEmpty()) {
            showWarning("Please enter your username and password.");
            return;
        } else if (login.isEmpty()) {
            showWarning("Please enter your username.");
            return;
        } else if (password.isEmpty()) {
            showWarning("Please enter your password.");
            return;
        }

        try {
            User authenticatedUser = authService.authenticate(login, password);
            if (authenticatedUser == null) {
                JOptionPane.showMessageDialog(this,
                        "Incorrect username, password, or employee account.",
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
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid Input", JOptionPane.WARNING_MESSAGE);
    }

    private void openHome(User authenticatedUser) {
        Role role = authenticatedUser.getRole();
        JFrame landingWindow = switch (role) {
                case CASHIER -> new SaleView(authenticatedUser.getEmail(), authenticatedUser.getUsername());
                case AISLE_MANAGER -> new AisleView(authenticatedUser.getEmployeeMatricule(),
                    authenticatedUser.getUsername());
            default               -> null;
        };
        if (landingWindow == null) {
            new Home(authenticatedUser.getUsername(), role,
                    authenticatedUser.getEmployeeMatricule()).setVisible(true);
            return;
        }
        landingWindow.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent event) {
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

