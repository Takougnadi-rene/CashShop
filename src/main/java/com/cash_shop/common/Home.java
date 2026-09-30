package com.cash_shop.common;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.cash_shop.aisle.AisleView;
import com.cash_shop.customer.CustomerView;
import com.cash_shop.employee.Employee.Role;
import com.cash_shop.employee.EmployeeView;
import com.cash_shop.product.Product;
import com.cash_shop.product.ProductDAO;
import com.cash_shop.product.ProductView;
import com.cash_shop.product.StockView;
import com.cash_shop.sale.SaleView;
import com.cash_shop.sale.SaleHistoryView;
import com.cash_shop.sale.RegisterSessionDAO;
import com.cash_shop.supplier.SupplierView;
import com.cash_shop.user.AccessService;
import com.cash_shop.user.AccessService.Module;
import com.cash_shop.user.AccountManagementView;
import com.cash_shop.user.UserManagementView;
import com.cash_shop.user.UserView;

public class Home extends JFrame {
    private final String username;
    private final String employeeMatricule;
    private final Role role;
    private final AccessService accessService = new AccessService();
    private JLabel dateLabel;
    private JLabel productCountLabel;
    private JLabel lowStockLabel;
    private JLabel stockValueLabel;
    private JLabel activeRegistersLabel;
    private Timer dateTimer;
    private Timer refreshTimer;
    private final RegisterSessionDAO registerSessionDAO = new RegisterSessionDAO();

    public Home(String username, Role role) {
        this(username, role, "");
    }

    public Home(String username, Role role, String employeeMatricule) {
        this.username = username;
        this.role = role;
        this.employeeMatricule = employeeMatricule;
        setTitle("Supermarket Manager");
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(StyleManager.BG_SURFACE);
        buildUI();
        refreshOverview();
        dateTimer = new Timer(1000, event -> dateLabel.setText(currentDateTime()));
        dateTimer.start();
        refreshTimer = new Timer(5000, event -> refreshOverview());
        refreshTimer.start();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent event) {
                dateTimer.stop();
                refreshTimer.stop();
            }
        });
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(StyleManager.BG_SURFACE);

        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildCenter(),  BorderLayout.CENTER);

        setContentPane(root);
    }

    // ── En-tête ──────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, StyleManager.BG_DEEP, getWidth(), 0,
                        new Color(0x1A, 0x14, 0x40)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Ligne inférieure accent
                g2.setColor(StyleManager.ACCENT_PRIMARY);
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        // Logo + titre
        JLabel logo = new JLabel("Supermarket Manager");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(StyleManager.TEXT_PRIMARY);

        // Horloge
        dateLabel = new JLabel(currentDateTime());
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(StyleManager.TEXT_SECONDARY);

        header.add(logo, BorderLayout.WEST);
        header.add(dateLabel, BorderLayout.EAST);
        return header;
    }

    // ── Sidebar ──────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(StyleManager.BG_DEEP);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Bordure droite
                g2.setColor(StyleManager.BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 16, 10));

        // Profil utilisateur
        JLabel userLabel = new JLabel(username);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLabel.setForeground(StyleManager.TEXT_PRIMARY);
        userLabel.setAlignmentX(LEFT_ALIGNMENT);
        userLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 4, 0));

        JLabel roleLabel = new JLabel(role.name().replace("_", " "));
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        roleLabel.setForeground(StyleManager.ACCENT_PRIMARY);
        roleLabel.setAlignmentX(LEFT_ALIGNMENT);
        roleLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        JPanel divider = new JPanel() {
            { setOpaque(false); setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
              setPreferredSize(new Dimension(0, 1)); }
            @Override protected void paintComponent(Graphics g) {
                g.setColor(StyleManager.BORDER_COLOR);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        sidebar.add(userLabel);
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(roleLabel);
        sidebar.add(Box.createVerticalStrut(16));
        sidebar.add(divider);
        sidebar.add(Box.createVerticalStrut(16));

        // Navigation
        JLabel navTitle = new JLabel("NAVIGATION");
        navTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        navTitle.setForeground(new Color(0x55, 0x59, 0x7A));
        navTitle.setAlignmentX(LEFT_ALIGNMENT);
        navTitle.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 0));
        sidebar.add(navTitle);

        if (canAccess(Module.PRODUCTS))
            sidebar.add(buildNavButton("Products", () -> new ProductView(!canEditInventory())));
        if (canAccess(Module.STOCK))
            sidebar.add(buildNavButton("Stock", () -> new StockView(!canEditInventory())));
        if (canAccess(Module.SUPPLIERS))
            sidebar.add(buildNavButton("Suppliers", () -> new SupplierView(role)));
        if (canAccess(Module.SALES))
            sidebar.add(buildNavButton("Checkout",       SaleView::new));
        if (canAccess(Module.CUSTOMERS))
            sidebar.add(buildNavButton("Customers",      CustomerView::new));
        if (canAccess(Module.EMPLOYEES))
            sidebar.add(buildNavButton("Employees",    () -> new EmployeeView(role != Role.ADMIN)));
        if (canAccess(Module.SALES_HISTORY))
            sidebar.add(buildNavButton("Sales history", () -> new SaleHistoryView(role)));
        if (canAccess(Module.DASHBOARD))
            sidebar.add(buildNavButton("Dashboard", () -> new Dashboard(role)));
        if (canAccess(Module.AISLES))
            sidebar.add(buildNavButton("Aisles",
                () -> new AisleView(!canEditInventory(),
                            role == Role.AISLE_MANAGER ? employeeMatricule : null)));
        if (canAccess(Module.USERS))
            sidebar.add(buildNavButton("Users", UserManagementView::new));

        sidebar.add(Box.createVerticalGlue());

        sidebar.add(buildNavButton("Manage my account", () -> new AccountManagementView(this, username)));
        sidebar.add(Box.createVerticalStrut(8));

        // Bouton déconnexion
        JButton logout = buildLogoutButton();
        sidebar.add(logout);
        return sidebar;
    }

    private JButton buildNavButton(String label, Supplier<? extends Window> factory) {
        JButton btn = new JButton(label) {
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
                if (hovered) {
                    g2.setColor(new Color(0x6C, 0x63, 0xFF, 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(StyleManager.TEXT_SECONDARY);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(JButton.LEFT);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleManager.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 9, 8, 9)));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.addActionListener(e -> factory.get().setVisible(true));
        return btn;
    }

    private JButton buildLogoutButton() {
        JButton btn = new JButton("Logout") {
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
                Color bg = hovered ? new Color(0x5A, 0x20, 0x2E) : new Color(0x3D, 0x1A, 0x22);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(StyleManager.DANGER);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleManager.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(9, 9, 9, 9)));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.addActionListener(event -> {
            dispose();
            SwingUtilities.invokeLater(() -> new UserView().setVisible(true));
        });
        return btn;
    }

    // ── Zone centrale ────────────────────────────────────────────────────────

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setBackground(StyleManager.BG_SURFACE);
        center.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Titre de bienvenue
        JPanel welcome = new JPanel();
        welcome.setOpaque(false);
        welcome.setLayout(new BoxLayout(welcome, BoxLayout.Y_AXIS));
        JLabel greet = new JLabel("Hello, " + username);
        greet.setFont(new Font("Segoe UI", Font.BOLD, 22));
        greet.setForeground(StyleManager.TEXT_PRIMARY);
        JLabel hint = new JLabel("Select a module from the navigation bar on the left.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hint.setForeground(StyleManager.TEXT_SECONDARY);
        welcome.add(greet);
        welcome.add(Box.createVerticalStrut(4));
        welcome.add(hint);

        center.add(welcome, BorderLayout.NORTH);

        // Cartes de statistiques
        if (canAccess(Module.PRODUCTS)) {
            JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
            cards.setOpaque(false);

            productCountLabel = new JLabel("--");
            lowStockLabel     = new JLabel("--");
            stockValueLabel   = new JLabel("--");
            activeRegistersLabel = new JLabel("--");

            cards.add(buildStatCard("Products in catalog", productCountLabel,
                    StyleManager.ACCENT_PRIMARY));
            cards.add(buildStatCard("Low stock",          lowStockLabel,
                    StyleManager.WARNING));
            cards.add(buildStatCard("Stock value",       stockValueLabel,
                    StyleManager.ACCENT_TEAL));
                cards.add(buildStatCard("Active registers", activeRegistersLabel,
                    StyleManager.ACCENT_ORANGE));

            center.add(cards, BorderLayout.CENTER);
        }

        return center;
    }

    private JPanel buildStatCard(String caption, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(StyleManager.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(StyleManager.BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                // Accent sur le dessus
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(20, 0, getWidth() - 20, 0);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        captionLabel.setForeground(StyleManager.TEXT_SECONDARY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accent);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        card.add(top, BorderLayout.NORTH);
        card.add(captionLabel, BorderLayout.CENTER);
        card.add(valueLabel, BorderLayout.SOUTH);

        return card;
    }

    // ── Données ──────────────────────────────────────────────────────────────

    private void refreshOverview() {
        refreshActiveRegisters();
        if (!canAccess(Module.PRODUCTS)) return;
        try {
            List<Product> products = new ProductDAO().getAllProducts();
            long lowStock   = products.stream().filter(p -> p.getStockQuantity() <= 5).count();
            double value    = products.stream()
                    .mapToDouble(p -> p.getSellingPrice() * p.getStockQuantity()).sum();
            productCountLabel.setText(String.valueOf(products.size()));
            lowStockLabel.setText(String.valueOf(lowStock));
            stockValueLabel.setText(String.format("%.0f USD", value));
        } catch (IllegalStateException exception) {
            productCountLabel.setText("N/A");
            lowStockLabel.setText("N/A");
            stockValueLabel.setText("N/A");
        }
    }

    private void refreshActiveRegisters() {
        if (activeRegistersLabel == null) {
            return;
        }
        try {
            activeRegistersLabel.setText(Integer.toString(registerSessionDAO.getActiveSessionCount()));
        } catch (IllegalStateException exception) {
            activeRegistersLabel.setText("N/A");
        }
    }

    private boolean canAccess(Module module) {
        return accessService.canAccess(role, module);
    }

    private boolean canEditInventory() {
        return role == Role.MANAGER;
    }

    private String currentDateTime() {
        return new SimpleDateFormat("dd/MM/yyyy  |  HH:mm:ss").format(new Date());
    }
}

