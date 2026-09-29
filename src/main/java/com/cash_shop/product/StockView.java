package com.cash_shop.product;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.cash_shop.common.StyleManager;

public class StockView extends JFrame {
    private static final int MINIMUM_STOCK = 5;

    private final ProductDAO productDAO = new ProductDAO();
    private final boolean readOnly;
    private final List<Product> products = new ArrayList<>();
    private final DefaultTableModel tableModel = StyleManager.createReadOnlyModel(
            "Reference", "Product", "Available", "Minimum", "Stock Status", "Category");
    private final JTable table = StyleManager.createTable(tableModel);
    private final Timer refreshTimer = new Timer(5000, event -> refreshStock(false));

    public StockView() {
        this(false);
    }

    public StockView(boolean readOnly) {
        this.readOnly = readOnly;
        setTitle("Stock Management");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        refreshStock();
        refreshTimer.start();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                refreshTimer.stop();
            }
        });
    }

    private void buildUI() {
        // Le statut du stock est la seule colonne colorée
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value, boolean selected,
                    boolean focused, int row, int column) {
                Component component = super.getTableCellRendererComponent(source, value, selected, focused,
                        row, column);
                if (!selected) {
                    String status = value == null ? "" : value.toString();
                    component.setForeground(status.equals("LOW") ? StyleManager.DANGER
                            : status.equals("MEDIUM") ? StyleManager.WARNING : StyleManager.SUCCESS);
                }
                return component;
            }
        });

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.add(StyleManager.createTitle("Stock"), BorderLayout.NORTH);
        top.add(new JLabel("Status:  LOW (5 or less)   |   MEDIUM (6 to 20)   |   OK (more than 20)"),
                BorderLayout.SOUTH);

        JButton stockIn = new JButton("Stock In");
        JButton stockOut = new JButton("Stock Out");
        JButton refresh = new JButton("Refresh");
        JButton alerts = new JButton("View Alerts");
        JButton close = new JButton("Close");
        stockIn.setEnabled(!readOnly);
        stockOut.setEnabled(!readOnly);
        stockIn.addActionListener(event -> changeStock(true));
        stockOut.addActionListener(event -> changeStock(false));
        refresh.addActionListener(event -> refreshStock());
        alerts.addActionListener(event -> showAlerts());
        close.addActionListener(event -> dispose());

        JPanel page = StyleManager.createPage();
        page.add(top, BorderLayout.NORTH);
        page.add(new JScrollPane(table), BorderLayout.CENTER);
        page.add(StyleManager.createButtonBar(stockIn, stockOut, refresh, alerts, close), BorderLayout.SOUTH);
        setContentPane(page);
    }

    private void refreshStock() {
        refreshStock(true);
    }

    private void refreshStock(boolean showError) {
        try {
            products.clear();
            products.addAll(productDAO.getAllProducts());
            tableModel.setRowCount(0);
            for (Product product : products) {
                int quantity = product.getStockQuantity();
                String status = quantity <= MINIMUM_STOCK ? "LOW" : quantity <= 20 ? "MEDIUM" : "OK";
                tableModel.addRow(new Object[] { product.getReference(), product.getDesignation(), quantity,
                        MINIMUM_STOCK, status, productCategory(product) });
            }
        } catch (IllegalStateException exception) {
            if (showError) showError(exception.getMessage());
        }
    }

    private String productCategory(Product product) {
        if (product instanceof FreshProduct) return "Fresh";
        if (product instanceof ElectronicProduct) return "Electronic";
        if (product instanceof ArtisanalProduct) return "Artisanal";
        return "Standard";
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
