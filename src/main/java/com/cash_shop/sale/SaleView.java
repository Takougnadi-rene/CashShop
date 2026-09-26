package com.cash_shop.sale;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.cash_shop.common.StyleManager;
import com.cash_shop.product.Product;

public class SaleView extends JFrame {

    private final List<Product> catalog = new ArrayList<>();
    private final List<Product> basket = new ArrayList<>();
    private final JComboBox<Product> cbProducts = new JComboBox<>();
        private final javax.swing.JLabel totalLabel = StyleManager.createLabel("TOTAL: 0.00 FCFA",
            StyleManager.ACCENT_GOLD, StyleManager.FONT_HEADER);
    private final DefaultTableModel basketModel = new DefaultTableModel(
            new String[]{"Product", "Unit Price", "Quantity", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable basketTable = new JTable(basketModel);

    public SaleView() {
        initCatalogue();
        setTitle("Sales and Checkout");
        setSize(920, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        buildUI();
        refreshCombo();
    }

    private void initCatalogue() {
        catalog.add(new Product(1001, "Pain complet", 230.0, 350.0, 25));
        catalog.add(new Product(1002, "Tomates", 180.0, 260.0, 40));
        catalog.add(new Product(1003, "Laptop X15", 780000.0, 980000.0, 12));
        catalog.add(new Product(1004, "Souris sans fil", 22000.0, 35000.0, 18));
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBackground(StyleManager.BG_DARK);
        main.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setBackground(StyleManager.BG_DARK);
        header.add(StyleManager.createLabel("SALES / CHECKOUT", StyleManager.ACCENT_GOLD, StyleManager.FONT_TITLE));

        JPanel formPanel = StyleManager.createCard();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(StyleManager.createLabel("Product:", StyleManager.TEXT_MUTED, StyleManager.FONT_SMALL), gbc);
        gbc.gridx = 1;
        formPanel.add(cbProducts, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnPanel.setOpaque(false);
        JButton btnAdd = StyleManager.createButton("Add to Cart", StyleManager.ACCENT_GREEN);
        JButton btnRemove = StyleManager.createButton("Remove", StyleManager.ACCENT_RED);
        JButton btnValidate = StyleManager.createButton("Complete Sale", StyleManager.ACCENT_BLUE);

        btnAdd.addActionListener(e -> addToBasket());
        btnRemove.addActionListener(e -> removeSelectedItem());
        btnValidate.addActionListener(e -> validateSale());

        btnPanel.add(btnAdd);
        btnPanel.add(btnRemove);
        btnPanel.add(btnValidate);

        JPanel basketPanel = StyleManager.createCard();
        basketPanel.setLayout(new BorderLayout());
        basketPanel.add(new JScrollPane(basketTable), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(StyleManager.BG_DARK);
        footer.add(totalLabel);

        main.add(header, BorderLayout.NORTH);
        main.add(formPanel, BorderLayout.CENTER);
        main.add(btnPanel, BorderLayout.SOUTH);
        add(main, BorderLayout.NORTH);
        add(basketPanel, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private void refreshCombo() {
        cbProducts.removeAllItems();
        for (Product product : catalog) {
            cbProducts.addItem(product);
        }
    }

    private void addToBasket() {
        Object selected = cbProducts.getSelectedItem();
        if (selected == null) {
            return;
        }
        Product product = (Product) selected;
        basket.add(product);
        basketModel.addRow(new Object[]{product.getDesignation(), product.getSellingPrice(), 1, product.getSellingPrice()});
        updateTotal();
    }

    private void removeSelectedItem() {
        int row = basketTable.getSelectedRow();
        if (row >= 0) {
            basket.remove(row);
            basketModel.removeRow(row);
            updateTotal();
        }
    }

    private void validateSale() {
        double total = 0.0;
        for (int i = 0; i < basketModel.getRowCount(); i++) {
            total += (double) basketModel.getValueAt(i, 3);
        }
        javax.swing.JOptionPane.showMessageDialog(this, "Sale completed. Total due: " + total + " FCFA");
        basket.clear();
        basketModel.setRowCount(0);
        updateTotal();
    }

    private void updateTotal() {
        double total = 0.0;
        for (int row = 0; row < basketModel.getRowCount(); row++) {
            total += ((Number) basketModel.getValueAt(row, 3)).doubleValue();
        }
        totalLabel.setText(String.format("TOTAL: %.2f FCFA", total));
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new SaleView().setVisible(true));
    }
}
