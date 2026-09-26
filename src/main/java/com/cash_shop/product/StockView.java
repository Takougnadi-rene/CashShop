package com.cash_shop.product;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import static com.cash_shop.common.StyleManager.ACCENT_BLUE;
import static com.cash_shop.common.StyleManager.ACCENT_GOLD;
import static com.cash_shop.common.StyleManager.ACCENT_GREEN;
import static com.cash_shop.common.StyleManager.ACCENT_RED;
import static com.cash_shop.common.StyleManager.BG_DARK;
import static com.cash_shop.common.StyleManager.BG_PANEL;
import static com.cash_shop.common.StyleManager.FONT_SMALL;
import static com.cash_shop.common.StyleManager.FONT_TITLE;
import static com.cash_shop.common.StyleManager.createButton;
import static com.cash_shop.common.StyleManager.createLabel;
import static com.cash_shop.common.StyleManager.createTable;

public class StockView extends JFrame {
    private static final int MINIMUM_STOCK = 5;

    private final ProductDAO productDAO = new ProductDAO();
    private final List<Product> products = new ArrayList<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[] { "Reference", "Product", "Available", "Minimum", "Stock Status", "Category" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = createTable(tableModel);

    public StockView() {
        setTitle("Stock Management");
        setSize(850, 540);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        refreshStock();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_DARK);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setBackground(BG_PANEL);
        header.add(createLabel("STOCK MANAGEMENT", ACCENT_GOLD, FONT_TITLE));

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT));
        legend.setBackground(BG_DARK);
        legend.add(createLabel("LOW (<= 5)", ACCENT_RED, FONT_SMALL));
        legend.add(createLabel("  MEDIUM (6-20)", ACCENT_GOLD, FONT_SMALL));
        legend.add(createLabel("  OK (> 20)", ACCENT_GREEN, FONT_SMALL));

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value, boolean selected,
                    boolean focused, int row, int column) {
                Component component = super.getTableCellRendererComponent(source, value, selected, focused, row,
                        column);
                if (!selected) {
                    String status = value == null ? "" : value.toString();
                    component.setForeground(status.equals("LOW") ? ACCENT_RED
                            : status.equals("MEDIUM") ? ACCENT_GOLD : ACCENT_GREEN);
                    component.setBackground(com.cash_shop.common.StyleManager.BG_PANEL);
                }
                return component;
            }
        });

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG_DARK);
        center.add(legend, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        actions.setBackground(BG_PANEL);
        javax.swing.JButton stockIn = createButton("+ Stock In", ACCENT_GREEN);
        javax.swing.JButton stockOut = createButton("- Stock Out", ACCENT_RED);
        javax.swing.JButton refresh = createButton("Refresh", ACCENT_BLUE);
        javax.swing.JButton alerts = createButton("View Alerts", ACCENT_GOLD);
        javax.swing.JButton close = createButton("Close", new Color(80, 80, 100));
        stockIn.addActionListener(event -> changeStock(true));
        stockOut.addActionListener(event -> changeStock(false));
        refresh.addActionListener(event -> refreshStock());
        alerts.addActionListener(event -> showAlerts());
        close.addActionListener(event -> dispose());
        actions.add(stockIn);
        actions.add(stockOut);
        actions.add(refresh);
        actions.add(alerts);
        actions.add(close);

        main.add(header, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);
        main.add(actions, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private void refreshStock() {
        try {
            products.clear();
            products.addAll(productDAO.getAllProducts());
            tableModel.setRowCount(0);
            for (Product product : products) {
                int quantity = product.getStockQuantity();
                String status = quantity <= MINIMUM_STOCK ? "LOW" : quantity <= 20 ? "MEDIUM" : "OK";
                tableModel.addRow(new Object[] { product.getReference(), product.getDesignation(), quantity,
                        MINIMUM_STOCK, status, product.getCategory() });
            }
        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void changeStock(boolean stockIn) {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Select a product first.");
            return;
        }
        String input = JOptionPane.showInputDialog(this, "Quantity:");
        if (input == null) {
            return;
        }
        try {
            int quantity = Integer.parseInt(input.trim());
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero.");
            }
            Product product = products.get(table.convertRowIndexToModel(row));
            if (!stockIn && quantity > product.getStockQuantity()) {
                throw new IllegalArgumentException("Not enough stock is available.");
            }
            if (stockIn) {
                productDAO.addQuantity(product.getReference(), quantity);
            } else {
                productDAO.removeQuantity(product.getReference(), quantity);
            }
            refreshStock();
        } catch (NumberFormatException exception) {
            showError("Quantity must be numeric.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void showAlerts() {
        StringBuilder message = new StringBuilder("LOW STOCK PRODUCTS\n\n");
        for (Product product : products) {
            if (product.getStockQuantity() <= MINIMUM_STOCK) {
                message.append(product.getDesignation()).append(" - ")
                        .append(product.getStockQuantity()).append(" available\n");
            }
        }
        if (message.length() == "LOW STOCK PRODUCTS\n\n".length()) {
            message.append("No low-stock products.");
        }
        JOptionPane.showMessageDialog(this, message.toString(), "Stock Alerts", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
