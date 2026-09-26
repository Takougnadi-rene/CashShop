package com.cash_shop.product;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.cash_shop.common.StyleManager;

public class ProductView extends JFrame {

    private final List<Product> products = new ArrayList<>();
    private final JTextField tfReference = StyleManager.createField();
    private final JTextField tfDesignation = StyleManager.createField();
    private final JTextField tfType = StyleManager.createField();
    private final JTextField tfCategory = StyleManager.createField();
    private final JTextField tfPurchase = StyleManager.createField();
    private final JTextField tfSelling = StyleManager.createField();
    private final JTextField tfStock = StyleManager.createField();
    private final JTextField tfSearch = StyleManager.createField();
    private JTable table;
    private DefaultTableModel tableModel;

    public ProductView() {
        initDemoProducts();
        setTitle("Product Management");
        setSize(980, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        buildUI();
        refreshTable();
    }

    private void initDemoProducts() {
        products.add(new Product(1001, "Whole grain bread", "Bakery", "Food", 230.0, 350.0, 25));
        products.add(new Product(1002, "Tomatoes", "Fresh", "Food", 180.0, 260.0, 40));
        products.add(new Product(1003, "Laptop X15", "Electronics", "Computers", 780000.0, 980000.0, 12));
        products.add(new Product(1004, "Wireless mouse", "Accessory", "Computers", 22000.0, 35000.0, 18));
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBackground(StyleManager.BG_DARK);
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setBackground(StyleManager.BG_DARK);
        header.add(StyleManager.createLabel("PRODUCT MANAGEMENT", StyleManager.ACCENT_GOLD, StyleManager.FONT_TITLE));

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBackground(StyleManager.BG_DARK);

        JPanel formPanel = StyleManager.createCard();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addFormField(formPanel, gbc, 0, "Reference:", tfReference);
        addFormField(formPanel, gbc, 1, "Name:", tfDesignation);
        addFormField(formPanel, gbc, 2, "Product Type:", tfType);
        addFormField(formPanel, gbc, 3, "Category:", tfCategory);
        addFormField(formPanel, gbc, 4, "Purchase Price:", tfPurchase);
        addFormField(formPanel, gbc, 5, "Selling Price:", tfSelling);
        addFormField(formPanel, gbc, 6, "Stock:", tfStock);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(formPanel, BorderLayout.CENTER);

        String[] columns = {"Reference", "Name", "Type", "Category", "Purchase Price", "Selling Price", "Stock"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = StyleManager.createTable(tableModel);
        table.setRowHeight(28);

        JPanel tablePanel = StyleManager.createCard();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.add(StyleManager.createLabel("Product List", StyleManager.ACCENT_BLUE, StyleManager.FONT_HEADER), BorderLayout.NORTH);
        rightPanel.add(tablePanel, BorderLayout.CENTER);

        JPanel centerSplit = new JPanel(new BorderLayout(12, 12));
        centerSplit.setOpaque(false);
        centerSplit.add(leftPanel, BorderLayout.WEST);
        centerSplit.add(rightPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setOpaque(false);
        JButton btnAdd = StyleManager.createButton("Add", StyleManager.ACCENT_GREEN);
        JButton btnDelete = StyleManager.createButton("Delete", StyleManager.ACCENT_RED);
        JButton btnSearch = StyleManager.createButton("Search", StyleManager.ACCENT_BLUE);
        JButton btnClear = StyleManager.createButton("Clear", new Color(100, 100, 120));

        btnAdd.addActionListener(e -> addProduct());
        btnDelete.addActionListener(e -> deleteSelectedProduct());
        btnSearch.addActionListener(e -> searchProducts());
        btnClear.addActionListener(e -> clearForm());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnSearch);
        buttonPanel.add(btnClear);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        searchPanel.add(StyleManager.createLabel("Search:", StyleManager.TEXT_MUTED, StyleManager.FONT_SMALL));
        searchPanel.add(tfSearch);
        tfSearch.setPreferredSize(new Dimension(220, 30));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(searchPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        main.add(header, BorderLayout.NORTH);
        main.add(centerSplit, BorderLayout.CENTER);
        main.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private void addFormField(JPanel formPanel, GridBagConstraints gbc, int row, String labelText, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(StyleManager.createLabel(labelText, StyleManager.TEXT_MUTED, StyleManager.FONT_SMALL), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        field.setPreferredSize(new Dimension(180, 28));
        formPanel.add(field, gbc);
        gbc.weightx = 0.0;
    }

    private void addProduct() {
        try {
            Product product = readProductFromFields();
            products.add(product);
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Product added successfully.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedProduct() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a product.");
            return;
        }
        products.remove(selectedRow);
        refreshTable();
    }

    private void searchProducts() {
        String keyword = tfSearch.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        if (keyword.isEmpty()) {
            refreshTable();
            return;
        }
        for (Product product : products) {
            if (String.valueOf(product.getReference()).contains(keyword)
                    || product.getDesignation().toLowerCase().contains(keyword)
                    || product.getType().toLowerCase().contains(keyword)
                    || product.getCategory().toLowerCase().contains(keyword)) {
                tableModel.addRow(new Object[]{
                    product.getReference(),
                    product.getDesignation(),
                    product.getType(),
                    product.getCategory(),
                    product.getPurchasePrice(),
                    product.getSellingPrice(),
                    product.getStockQuantity()
                });
            }
        }
    }

    private Product readProductFromFields() {
        String referenceStr = tfReference.getText().trim();
        String designation = tfDesignation.getText().trim();
        String type = tfType.getText().trim();
        String category = tfCategory.getText().trim();
        String purchaseStr = tfPurchase.getText().trim();
        String sellingStr = tfSelling.getText().trim();
        String stockStr = tfStock.getText().trim();

        if (referenceStr.isEmpty() || designation.isEmpty() || type.isEmpty() || category.isEmpty() || purchaseStr.isEmpty() || sellingStr.isEmpty() || stockStr.isEmpty()) {
            throw new IllegalArgumentException("All fields are required.");
        }

        try {
            int reference = Integer.parseInt(referenceStr);
            double purchase = Double.parseDouble(purchaseStr);
            double selling = Double.parseDouble(sellingStr);
            int stock = Integer.parseInt(stockStr);
            if (purchase <= 0 || selling <= 0 || stock < 0) {
                throw new IllegalArgumentException("Price or stock value is invalid.");
            }
            Product product = new Product(reference, designation, type, category, purchase, selling, stock);
            return product;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Check the numeric values.");
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Product product : products) {
            tableModel.addRow(new Object[]{
                product.getReference(),
                product.getDesignation(),
                product.getType(),
                product.getCategory(),
                product.getPurchasePrice(),
                product.getSellingPrice(),
                product.getStockQuantity()
            });
        }
    }

    private void clearForm() {
        tfReference.setText("");
        tfDesignation.setText("");
        tfType.setText("");
        tfCategory.setText("");
        tfPurchase.setText("");
        tfSelling.setText("");
        tfStock.setText("");
        tfSearch.setText("");
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new ProductView().setVisible(true));
    }
}
