package com.cash_shop.product;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.cash_shop.common.StyleManager;

/**
 * Swing window to follow stock levels with LOW / MEDIUM / OK status, record stock in/out and list low-stock
 * alerts.
 */
public class StockView extends JFrame {
    // Stock at or below this quantity is considered LOW.
    private static final int MINIMUM_STOCK = 5;

    private final ProductDAO productDAO = new ProductDAO();
    private final boolean readOnly;
    private final List<Product> products = new ArrayList<>();
    private final DefaultTableModel tableModel = StyleManager.createReadOnlyModel(
            "Reference", "Product", "Available", "Minimum", "Stock Status");
    private final JTable table = StyleManager.createTable(tableModel);
    private final Timer refreshTimer = new Timer(5000, event -> refreshStock(false));

    /** Editable window. */
    public StockView() {
        this(false);
    }

    /** Window that can be opened read-only. */
    public StockView(boolean readOnly) {
        this.readOnly = readOnly;
        setTitle("Stock Management");
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
        setMinimumSize(Toolkit.getDefaultToolkit().getScreenSize());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        refreshStock();
        refreshTimer.start();
        addWindowListener(new WindowAdapter() {
            // Stop the periodic refresh once the window is closed.
            @Override
            public void windowClosed(WindowEvent event) {
                refreshTimer.stop();
            }
        });
    }

    /** Builds the table (with a colored status column), the legend and the buttons. */
    private void buildUI() {
        // The stock status is the only colored column
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            // Colors the status text: red for LOW, orange for MEDIUM, green for OK.
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

        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.setOpaque(false);
        top.add(StyleManager.createTitle("Stock"), BorderLayout.NORTH);

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
        page.add(StyleManager.createScrollPane(table), BorderLayout.CENTER);
        page.add(StyleManager.createButtonBar(stockIn, stockOut, refresh, alerts, close), BorderLayout.SOUTH);
        setContentPane(page);
    }

    /** Reloads the stock table and shows errors. */
    private void refreshStock() {
        refreshStock(true);
    }

    /** Reloads the stock table; errors are only shown when requested. */
    private void refreshStock(boolean showError) {
        try {
            products.clear();
            products.addAll(productDAO.getAllProducts());
            tableModel.setRowCount(0);
            for (Product product : products) {
                int quantity = product.getStockQuantity();
                // LOW: at or below the minimum; MEDIUM: up to 20 units; OK: more than 20.
                String status = quantity <= MINIMUM_STOCK ? "LOW" : quantity <= 20 ? "MEDIUM" : "OK";
                tableModel.addRow(new Object[] { product.getReference(), product.getDesignation(), quantity,
                        MINIMUM_STOCK, status });
            }
        } catch (IllegalStateException exception) {
            if (showError) showError(exception.getMessage());
        }
    }


    /** Asks for a quantity and adds it to (stock in) or removes it from (stock out) the selected product. */
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

    /** Lists the products whose stock is at or below the minimum. */
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

    /** Displays an error dialog. */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
