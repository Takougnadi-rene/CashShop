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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.cash_shop.employee.Employee.Role;
import com.cash_shop.employee.EmployeeView;
import com.cash_shop.payment.PaymentDAO;
import com.cash_shop.product.FreshProduct;
import com.cash_shop.product.Product;
import com.cash_shop.product.ProductDAO;
import com.cash_shop.product.ProductView;
import com.cash_shop.product.StockView;
import com.cash_shop.sale.SaleDAO;
import com.cash_shop.sale.SaleHistoryView;
import com.cash_shop.user.AccessService;
import com.cash_shop.user.AccessService.Module;

public class Dashboard extends JFrame {
    private static final int LOW_STOCK_LIMIT = 5;
    private final Role role;
    private final AccessService accessService = new AccessService();

    // Labels des KPI
    private final JLabel revenueValueLabel      = kpiLabel("--");
    private final JLabel salesCountValueLabel   = kpiLabel("--");
    private final JLabel productCountValueLabel = kpiLabel("--");
    private final JLabel inventoryValueLabel    = kpiLabel("--");
    private final JLabel expiredCountLabel      = alertLabel("--");
    private final JLabel lowStockCountLabel     = alertLabel("--");

    public Dashboard(Role role) {
        this.role = role;
        setTitle("Supermarket Manager – Dashboard");
        setSize(720, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        refreshDashboard();
    }

    // ── Construction ─────────────────────────────────────────────────────────

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(StyleManager.BG_SURFACE);

        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildContent(), BorderLayout.CENTER);
        root.add(buildFooter(),  BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, StyleManager.BG_DEEP,
                        getWidth(), 0, new Color(0x1A, 0x14, 0x40)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(StyleManager.ACCENT_PRIMARY);
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(StyleManager.TEXT_PRIMARY);

        JLabel date = new JLabel("As of " + currentDate());
        date.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        date.setForeground(StyleManager.TEXT_SECONDARY);

        header.add(title, BorderLayout.WEST);
        header.add(date,  BorderLayout.EAST);
        return header;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new GridLayout(2, 2, 16, 16));
        content.setBackground(StyleManager.BG_SURFACE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 12, 20));

        content.add(buildKpiCard("Total Revenue",  revenueValueLabel,    StyleManager.ACCENT_TEAL));
        content.add(buildKpiCard("Number of Sales",    salesCountValueLabel, StyleManager.ACCENT_PRIMARY));
        content.add(buildKpiCard("Product References", productCountValueLabel, new Color(0x8A, 0x63, 0xFF)));
        content.add(buildAlertsCard());

        return content;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(StyleManager.BG_SURFACE);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 20, 16, 20));

        JPanel btnBar = new JPanel();
        btnBar.setOpaque(false);
        btnBar.setLayout(new BoxLayout(btnBar, BoxLayout.X_AXIS));
        btnBar.add(Box.createHorizontalGlue());

        if (canAccess(Module.SALES_HISTORY)) {
            btnBar.add(buildFooterButton("Sales history", StyleManager.BG_CARD,
                () -> openWindow(new SaleHistoryView(role))));
            btnBar.add(Box.createHorizontalStrut(10));
        }
        if (canAccess(Module.EMPLOYEES)) {
            btnBar.add(buildFooterButton("Employees", StyleManager.BG_CARD,
                    () -> openWindow(new EmployeeView(role != Role.ADMIN))));
            btnBar.add(Box.createHorizontalStrut(10));
        }
        if (canAccess(Module.PRODUCTS) && !canAccess(Module.SALES)) {
            btnBar.add(buildFooterButton("Products", StyleManager.BG_CARD,
                    () -> openWindow(new ProductView(true))));
            btnBar.add(Box.createHorizontalStrut(10));
        }
        btnBar.add(buildFooterButton("Close", new Color(0x3D, 0x1A, 0x22), this::dispose));

        footer.add(btnBar, BorderLayout.EAST);
        return footer;
    }

    // ── Cartes KPI ───────────────────────────────────────────────────────────

    private JPanel buildKpiCard(String caption, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(StyleManager.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(StyleManager.BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(20, 2, getWidth() - 20, 2);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel captionLabel = new JLabel(caption.toUpperCase());
        captionLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        captionLabel.setForeground(StyleManager.TEXT_SECONDARY);

        valueLabel.setForeground(accent);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(captionLabel, BorderLayout.NORTH);

        card.add(top, BorderLayout.CENTER);
        card.add(valueLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildAlertsCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(StyleManager.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(StyleManager.BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(StyleManager.DANGER);
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(20, 2, getWidth() - 20, 2);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel captionLabel = new JLabel("ALERTS");
        captionLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        captionLabel.setForeground(StyleManager.TEXT_SECONDARY);

        JPanel alertRows = new JPanel(new GridBagLayout());
        alertRows.setOpaque(false);
        addAlertRow(alertRows, 0, "Expired products:", expiredCountLabel, "View products",
            () -> openWindow(new ProductView(true)));
        addAlertRow(alertRows, 1, "Low stock:", lowStockCountLabel, "View stock",
            () -> openWindow(new StockView(true)));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(captionLabel, BorderLayout.CENTER);

        card.add(top, BorderLayout.NORTH);
        card.add(alertRows, BorderLayout.CENTER);
        return card;
    }

    private void addAlertRow(JPanel panel, int row, String caption, JLabel value,
            String btnText, Runnable action) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(4, 0, 4, 8);

        c.gridx = 0; c.anchor = GridBagConstraints.WEST; c.weightx = 0;
        JLabel lbl = new JLabel(caption);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(StyleManager.TEXT_SECONDARY);
        panel.add(lbl, c);

        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        value.setForeground(StyleManager.DANGER);
        panel.add(value, c);

        c.gridx = 2; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        panel.add(buildSmallButton(btnText, action), c);
    }

    // ── Utilitaires visuels ──────────────────────────────────────────────────

    private static JLabel kpiLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 24));
        return l;
    }

    private static JLabel alertLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return l;
    }

    private JButton buildSmallButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(StyleManager.ACCENT_PRIMARY);
        btn.setBackground(new Color(0x6C, 0x63, 0xFF, 30));
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleManager.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(4, 9, 4, 9)));
        btn.setPreferredSize(new Dimension(95, 28));
        btn.addMouseListener(new MouseAdapter() {
            private final Color bg = new Color(0x6C, 0x63, 0xFF, 30);
            private final Color hover = new Color(0x6C, 0x63, 0xFF, 70);
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private JButton buildFooterButton(String text, Color bg, Runnable action) {
        boolean isDanger = bg.equals(new Color(0x3D, 0x1A, 0x22));
        Color fg = isDanger ? StyleManager.DANGER : StyleManager.TEXT_PRIMARY;
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleManager.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 17, 8, 17)));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        btn.addActionListener(e -> action.run());
        return btn;
    }

    // ── Données ──────────────────────────────────────────────────────────────

    private void refreshDashboard() {
        try {
            List<Product> products = new ProductDAO().getAllProducts();
            long expiredCount = products.stream()
                    .filter(FreshProduct.class::isInstance)
                    .map(FreshProduct.class::cast)
                    .filter(this::isExpired)
                    .count();
            long lowStockCount = products.stream()
                    .filter(p -> p.getStockQuantity() <= LOW_STOCK_LIMIT)
                    .count();
            double stockValue = products.stream()
                    .mapToDouble(p -> p.getSellingPrice() * p.getStockQuantity())
                    .sum();
            productCountValueLabel.setText(products.size() + " references");
            inventoryValueLabel.setText(String.format("%.2f USD", stockValue));
            expiredCountLabel.setText(expiredCount + " items");
            lowStockCountLabel.setText(lowStockCount + " items");
        } catch (IllegalStateException exception) {
            productCountValueLabel.setText("Unavailable");
            inventoryValueLabel.setText("Unavailable");
            expiredCountLabel.setText("Unavailable");
            lowStockCountLabel.setText("Unavailable");
        }

        try {
            salesCountValueLabel.setText(new SaleDAO().getSalesCount() + " transactions");
        } catch (IllegalStateException exception) {
            salesCountValueLabel.setText("Unavailable");
        }
        try {
            revenueValueLabel.setText(String.format("%.2f USD", new PaymentDAO().getTotalRevenue()));
        } catch (IllegalStateException exception) {
            revenueValueLabel.setText("Unavailable");
        }
    }

    private boolean isExpired(FreshProduct product) {
        try {
            return LocalDate.parse(product.getExpirationDate()).isBefore(LocalDate.now());
        } catch (DateTimeParseException | NullPointerException exception) {
            return false;
        }
    }

    private String currentDate() {
        return new SimpleDateFormat("dd/MM/yyyy  |  HH:mm").format(new Date());
    }

    private void openWindow(JFrame window) {
        if (window != null) window.setVisible(true);
    }

    private boolean canAccess(Module module) {
        return accessService.canAccess(role, module);
    }

    public static void main(String[] args) {
        StyleManager.applyLookAndFeel();
        SwingUtilities.invokeLater(() -> new com.cash_shop.user.UserView().setVisible(true));
    }
}
