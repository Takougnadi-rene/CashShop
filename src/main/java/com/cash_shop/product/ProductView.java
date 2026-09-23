package com.cash_shop.product;

import java.awt.BorderLayout;
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
    private final JTextField tfReference = new JTextField();
    private final JTextField tfDesignation = new JTextField();
    private final JTextField tfPurchase = new JTextField();
    private final JTextField tfSelling = new JTextField();
    private final JTextField tfStock = new JTextField();
    private final JTextField tfSearch = new JTextField();
    private JTable table;
    private DefaultTableModel tableModel;

    public ProductView() {
        initDemoProducts();
        setTitle("Gestion des Produits");
        setSize(980, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        buildUI();
        refreshTable();
    }

    private void initDemoProducts() {
        products.add(new Product(1001, "Pain complet", 230.0, 350.0, 25));
        products.add(new Product(1002, "Tomates", 180.0, 260.0, 40));
        products.add(new Product(1003, "Laptop X15", 780000.0, 980000.0, 12));
        products.add(new Product(1004, "Souris sans fil", 22000.0, 35000.0, 18));
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBackground(StyleManager.BG_DARK);
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setBackground(StyleManager.BG_DARK);
        header.add(StyleManager.createLabel("=== GESTION DES PRODUITS ===", StyleManager.ACCENT_GOLD, StyleManager.FONT_TITLE));

        JPanel formPanel = StyleManager.createCard();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(StyleManager.createLabel("Référence :", StyleManager.TEXT_MUTED, StyleManager.FONT_SMALL), gbc);
        gbc.gridx = 1;
        formPanel.add(tfReference, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(StyleManager.createLabel("Désignation :", StyleManager.TEXT_MUTED, StyleManager.FONT_SMALL), gbc);
        gbc.gridx = 1;
        formPanel.add(tfDesignation, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(StyleManager.createLabel("Prix achat :", StyleManager.TEXT_MUTED, StyleManager.FONT_SMALL), gbc);
        gbc.gridx = 1;
        formPanel.add(tfPurchase, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(StyleManager.createLabel("Prix vente :", StyleManager.TEXT_MUTED, StyleManager.FONT_SMALL), gbc);
        gbc.gridx = 1;
        formPanel.add(tfSelling, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(StyleManager.createLabel("Stock :", StyleManager.TEXT_MUTED, StyleManager.FONT_SMALL), gbc);
        gbc.gridx = 1;
        formPanel.add(tfStock, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setOpaque(false);
        JButton btnAdd = StyleManager.createButton("Ajouter", StyleManager.ACCENT_GREEN);
        JButton btnDelete = StyleManager.createButton("Supprimer", StyleManager.ACCENT_RED);
        JButton btnSearch = StyleManager.createButton("Rechercher", StyleManager.ACCENT_BLUE);
        JButton btnClear = StyleManager.createButton("Effacer", new java.awt.Color(100, 100, 120));

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
        searchPanel.add(StyleManager.createLabel("Recherche :", StyleManager.TEXT_MUTED, StyleManager.FONT_SMALL));
        searchPanel.add(tfSearch);
        tfSearch.setPreferredSize(new java.awt.Dimension(220, 30));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        String[] columns = {"Référence", "Désignation", "Achat", "Vente", "Stock"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);

        JPanel tablePanel = StyleManager.createCard();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        main.add(header, BorderLayout.NORTH);
        main.add(topPanel, BorderLayout.CENTER);
        main.add(buttonPanel, BorderLayout.SOUTH);

        add(main, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    private void addProduct() {
        try {
            Product product = readProductFromFields();
            products.add(product);
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Produit ajouté avec succès.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedProduct() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un produit.");
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
                    || product.getDesignation().toLowerCase().contains(keyword)) {
                tableModel.addRow(new Object[]{
                    product.getReference(),
                    product.getDesignation(),
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
        String purchaseStr = tfPurchase.getText().trim();
        String sellingStr = tfSelling.getText().trim();
        String stockStr = tfStock.getText().trim();

        if (referenceStr.isEmpty() || designation.isEmpty() || purchaseStr.isEmpty() || sellingStr.isEmpty() || stockStr.isEmpty()) {
            throw new IllegalArgumentException("Tous les champs sont obligatoires.");
        }

        try {
            int reference = Integer.parseInt(referenceStr);
            double purchase = Double.parseDouble(purchaseStr);
            double selling = Double.parseDouble(sellingStr);
            int stock = Integer.parseInt(stockStr);
            if (purchase <= 0 || selling <= 0 || stock < 0) {
                throw new IllegalArgumentException("Prix et stock invalides.");
            }
            return new Product(reference, designation, purchase, selling, stock);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Vérifiez les valeurs numériques.");
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Product product : products) {
            tableModel.addRow(new Object[]{
                product.getReference(),
                product.getDesignation(),
                product.getPurchasePrice(),
                product.getSellingPrice(),
                product.getStockQuantity()
            });
        }
    }

    private void clearForm() {
        tfReference.setText("");
        tfDesignation.setText("");
        tfPurchase.setText("");
        tfSelling.setText("");
        tfStock.setText("");
        tfSearch.setText("");
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new ProductView().setVisible(true));
    }
}
