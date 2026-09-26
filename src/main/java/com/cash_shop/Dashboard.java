package com.cash_shop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.cash_shop.common.StyleManager;
import com.cash_shop.customer.CustomerView;
import com.cash_shop.employee.EmployeeView;
import com.cash_shop.product.ProductView;
import com.cash_shop.sale.SaleView;
import com.cash_shop.supplier.SupplierView;
import com.cash_shop.employee.Employee.Role;
import com.cash_shop.user.AccessService;
import com.cash_shop.user.AccessService.Module;

public class Dashboard extends JFrame {
    private final Role role;
    private final AccessService accessService = new AccessService();

    public Dashboard(Role role) {
        this.role = role;
        setTitle("Cash Shop - Dashboard");
        setSize(980, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(StyleManager.BG_DARK);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(18, 18));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setBackground(StyleManager.BG_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        JLabel title = StyleManager.createLabel("=== CASH SHOP MANAGEMENT ===", StyleManager.ACCENT_GOLD, StyleManager.FONT_TITLE);
        header.add(title);

        JPanel cards = new JPanel(new GridLayout(0, 3, 14, 20));
        cards.setBackground(StyleManager.BG_DARK);
        cards.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        if (canAccess(Module.EMPLOYEES)) cards.add(createCard("Employees", StyleManager.ACCENT_BLUE, "Staff management", () -> openWindow(new EmployeeView())));
        if (canAccess(Module.CUSTOMERS)) cards.add(createCard("Customers", StyleManager.ACCENT_GREEN, "Customer records and loyalty", () -> openWindow(new CustomerView())));
        if (canAccess(Module.PRODUCTS)) cards.add(createCard("Products", StyleManager.ACCENT_GOLD, "Catalog and pricing", () -> openWindow(new ProductView())));
        if (canAccess(Module.STOCK)) cards.add(createCard("Stock", StyleManager.ACCENT_RED, "Inventory levels and alerts", () -> openWindow(new com.cash_shop.product.StockView())));
        if (canAccess(Module.AISLES)) cards.add(createCard("Aisles", StyleManager.ACCENT_BLUE, "Aisle and product assignments", () -> openWindow(new com.cash_shop.aisle.AisleView(false))));
        if (canAccess(Module.SALES)) cards.add(createCard("Sales", StyleManager.ACCENT_GREEN, "Checkout and payments", () -> openWindow(new SaleView())));
        if (canAccess(Module.SUPPLIERS)) cards.add(createCard("Suppliers", StyleManager.ACCENT_GOLD, "Supplier records", () -> openWindow(new SupplierView())));
        cards.add(createCard("Exit", new Color(120, 120, 140), "Close the dashboard", () -> dispose()));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(StyleManager.BG_DARK);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 20));
        JButton logoutBtn = StyleManager.createButton("Log Out", new Color(244, 67, 54));
        logoutBtn.setPreferredSize(new Dimension(160, 36));
        logoutBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new com.cash_shop.user.UserView().setVisible(true));
        });
        footer.add(logoutBtn);

        add(header, BorderLayout.NORTH);
        add(cards, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createCard(String title, Color color, String subtitle, Runnable action) {
        JPanel panel = StyleManager.createCard();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(250, 160));

        JLabel titleLabel = StyleManager.createLabel(title, color, StyleManager.FONT_HEADER);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        JLabel subtitleLabel = StyleManager.createLabel(subtitle, StyleManager.TEXT_MUTED, StyleManager.FONT_SMALL);
        subtitleLabel.setHorizontalAlignment(JLabel.CENTER);

        JButton button = StyleManager.createButton("Open", color);
        button.setPreferredSize(new Dimension(140, 36));
        button.addActionListener(e -> action.run());

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(titleLabel, BorderLayout.NORTH);
        content.add(subtitleLabel, BorderLayout.CENTER);
        content.add(button, BorderLayout.SOUTH);

        panel.add(content, BorderLayout.CENTER);
        return panel;
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
