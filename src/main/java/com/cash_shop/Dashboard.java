package com.cash_shop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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

import com.cash_shop.common.StyleManager;
import com.cash_shop.employee.Employee.Role;
import com.cash_shop.employee.EmployeeView;
import com.cash_shop.payment.PaymentDAO;
import com.cash_shop.product.FreshProduct;
import com.cash_shop.product.Product;
import com.cash_shop.product.ProductDAO;
import com.cash_shop.product.ProductView;
import com.cash_shop.product.StockView;
import com.cash_shop.sale.SaleDAO;
import com.cash_shop.sale.SaleView;
import com.cash_shop.user.AccessService;
import com.cash_shop.user.AccessService.Module;

public class Dashboard extends JFrame {
    private static final int LOW_STOCK_LIMIT = 5;
    private final Role role;
    private final AccessService accessService = new AccessService();
    private JLabel dateLabel;
    private JLabel revenueValueLabel;
    private JLabel salesCountValueLabel;
    private JLabel productCountValueLabel;
    private JLabel inventoryValueLabel;
    private JLabel expiredCountLabel;
    private JLabel lowStockCountLabel;

    public Dashboard(Role role) {
        this.role = role;
        setTitle("Supermarket Manager - Dashboard");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(StyleManager.BG_DARK);
        buildUI();
        refreshDashboard();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout(0, 14));
        main.setBackground(StyleManager.BG_DARK);
        main.setBorder(BorderFactory.createEmptyBorder(12, 16, 14, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(StyleManager.BG_DARK);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, StyleManager.BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 4, 10, 4)));
        header.add(StyleManager.createLabel("GLOBAL DASHBOARD", StyleManager.ACCENT_GOLD,
                StyleManager.FONT_TITLE), BorderLayout.WEST);
        dateLabel = StyleManager.createLabel("As of " + currentDate(), StyleManager.TEXT_MUTED,
                StyleManager.FONT_BODY);
        header.add(dateLabel, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        JPanel metrics = new JPanel(new GridLayout(1, 2, 14, 0));
        metrics.setOpaque(false);

        revenueValueLabel = metricValue("-- USD");
        salesCountValueLabel = metricValue("-- transactions");
        productCountValueLabel = metricValue("-- references");
        inventoryValueLabel = metricValue("-- USD");
        metrics.add(createSection("FINANCIAL PERFORMANCE", StyleManager.ACCENT_BLUE,
                createMetricRows("Total revenue:", revenueValueLabel,
                        "Number of sales made:", salesCountValueLabel)));
        metrics.add(createSection("INVENTORY STATUS", StyleManager.ACCENT_GREEN,
                createMetricRows("Total number of products:", productCountValueLabel,
                        "Total stock value:", inventoryValueLabel)));

        expiredCountLabel = metricValue("-- items");
        lowStockCountLabel = metricValue("-- items");
        JPanel alertRows = new JPanel();
        alertRows.setOpaque(false);
        alertRows.setLayout(new BoxLayout(alertRows, BoxLayout.Y_AXIS));
        alertRows.add(createAlertRow("Expired products:", expiredCountLabel, "View products",
                () -> openWindow(new ProductView(role != Role.ADMIN))));
        alertRows.add(Box.createVerticalStrut(8));
        alertRows.add(createAlertRow("Low stock:", lowStockCountLabel, "View stock",
                () -> openWindow(new StockView())));
        JPanel alerts = createSection("ALERTS & WATCH POINTS", StyleManager.ACCENT_RED, alertRows);

        JPanel shortcuts = new JPanel(new BorderLayout(0, 8));
        shortcuts.setOpaque(false);
        shortcuts.add(StyleManager.createLabel("NAVIGATION SHORTCUTS", StyleManager.TEXT_MUTED,
                StyleManager.FONT_HEADER), BorderLayout.NORTH);
        JPanel shortcutButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        shortcutButtons.setOpaque(false);
        if (canAccess(Module.SALES)) {
            shortcutButtons.add(createShortcut("View Sales", StyleManager.ACCENT_GREEN,
                    () -> openWindow(new SaleView())));
        }
        if (canAccess(Module.EMPLOYEES)) {
            shortcutButtons.add(createShortcut("View Employees", StyleManager.ACCENT_BLUE,
                    () -> openWindow(new EmployeeView(role != Role.ADMIN))));
        }
        if (canAccess(Module.PRODUCTS) && !canAccess(Module.SALES)) {
            shortcutButtons.add(createShortcut("View Products", StyleManager.ACCENT_GOLD,
                    () -> openWindow(new ProductView(role != Role.ADMIN))));
        }
        shortcutButtons.add(createShortcut("Main Menu", new Color(100, 100, 120), this::dispose));
        shortcuts.add(shortcutButtons, BorderLayout.CENTER);

        content.add(metrics, BorderLayout.NORTH);
        content.add(alerts, BorderLayout.CENTER);
        content.add(shortcuts, BorderLayout.SOUTH);
        main.add(header, BorderLayout.NORTH);
        main.add(content, BorderLayout.CENTER);
        setContentPane(main);
    }

    private JPanel createSection(String title, Color accent, Component content) {
        JPanel section = new JPanel(new BorderLayout(0, 7));
        section.setOpaque(false);
        section.add(StyleManager.createLabel(title, accent, StyleManager.FONT_HEADER), BorderLayout.NORTH);
        JPanel card = StyleManager.createCard();
        card.setLayout(new BorderLayout());
        card.add(content, BorderLayout.CENTER);
        section.add(card, BorderLayout.CENTER);
        return section;
    }

    private JPanel createMetricRows(String firstCaption, JLabel firstValue,
            String secondCaption, JLabel secondValue) {
        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.add(StyleManager.createLabel(firstCaption, StyleManager.TEXT_MUTED, StyleManager.FONT_BODY));
        rows.add(firstValue);
        rows.add(Box.createVerticalStrut(8));
        rows.add(StyleManager.createLabel(secondCaption, StyleManager.TEXT_MUTED, StyleManager.FONT_BODY));
        rows.add(secondValue);
        return rows;
    }

    private JPanel createAlertRow(String caption, JLabel count, String buttonText, Runnable action) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        JPanel description = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        description.setOpaque(false);
        description.add(StyleManager.createLabel(caption, StyleManager.TEXT_PRIMARY, StyleManager.FONT_BODY));
        description.add(count);
        row.add(description, BorderLayout.CENTER);
        row.add(createShortcut(buttonText, StyleManager.ACCENT_BLUE, action), BorderLayout.EAST);
        return row;
    }

    private JLabel metricValue(String text) {
        return StyleManager.createLabel(">> " + text, StyleManager.TEXT_PRIMARY, StyleManager.FONT_BODY);
    }

    private JButton createShortcut(String text, Color color, Runnable action) {
        JButton button = StyleManager.createButton(text, color);
        button.addActionListener(event -> action.run());
        return button;
    }

    private void refreshDashboard() {
        try {
            List<Product> products = new ProductDAO().getAllProducts();
            long expiredCount = products.stream()
                    .filter(FreshProduct.class::isInstance)
                    .map(FreshProduct.class::cast)
                    .filter(this::isExpired)
                    .count();
            long lowStockCount = products.stream()
                    .filter(product -> product.getStockQuantity() <= LOW_STOCK_LIMIT)
                    .count();
            double stockValue = products.stream()
                    .mapToDouble(product -> product.getSellingPrice() * product.getStockQuantity())
                    .sum();
            productCountValueLabel.setText(">> " + products.size() + " references");
            inventoryValueLabel.setText(String.format(">> %.2f USD", stockValue));
            expiredCountLabel.setText(expiredCount + " items");
            lowStockCountLabel.setText(lowStockCount + " items");
        } catch (IllegalStateException exception) {
            productCountValueLabel.setText(">> Unavailable");
            inventoryValueLabel.setText(">> Unavailable");
            expiredCountLabel.setText("Unavailable");
            lowStockCountLabel.setText("Unavailable");
        }

        try {
            salesCountValueLabel.setText(">> " + new SaleDAO().getSalesCount() + " transactions");
        } catch (IllegalStateException exception) {
            salesCountValueLabel.setText(">> Unavailable");
        }
        try {
            revenueValueLabel.setText(String.format(">> %.2f USD", new PaymentDAO().getTotalRevenue()));
        } catch (IllegalStateException exception) {
            revenueValueLabel.setText(">> Unavailable");
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
        return new SimpleDateFormat("dd/MM/yyyy").format(new Date());
    }

    private void openWindow(JFrame window) {
        if (window != null) {
            window.setVisible(true);
        }
    }

    private boolean canAccess(Module module) {
        return accessService.canAccess(role, module);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new com.cash_shop.user.UserView().setVisible(true));
    }
}
