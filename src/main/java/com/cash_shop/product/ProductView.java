package com.cash_shop.product;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

import com.cash_shop.common.StyleManager;

public class ProductView extends JFrame {

    private final List<Product> products = new ArrayList<>();
    private final JTextField tfReference = StyleManager.createField();
    private final JTextField tfDesignation = StyleManager.createField();
    private final JTextField tfPurchase = StyleManager.createField();
    private final JTextField tfSelling = StyleManager.createField();
        private final JComboBox<String> cbProductType = new JComboBox<>(
            new String[] { "Standard", "Fresh", "Electronic", "Artisanal" });
        private final JTextField tfExpirationDate = StyleManager.createField();
        private final JTextField tfStorageTemperature = StyleManager.createField();
        private final JTextField tfBrand = StyleManager.createField();
        private final JTextField tfWarranty = StyleManager.createField();
        private final JComboBox<ArtisanalProduct.TypeArtisanal> cbArtisanalType =
            new JComboBox<>(ArtisanalProduct.TypeArtisanal.values());
    private final JTextField tfSearch = StyleManager.createField();
    private final ProductDAO productDAO = new ProductDAO();
    private JTable table;
    private DefaultTableModel tableModel;
    private final boolean readOnly;
    private final Timer refreshTimer = new Timer(5000, event -> refreshTable(false));

    public ProductView() {
        this(false);
    }

    public ProductView(boolean readOnly) {
        this.readOnly = readOnly;
        setTitle("Product Management");
        setSize(1050, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        buildUI();
        if (readOnly) {
            tfReference.setEditable(false);
            tfDesignation.setEditable(false);
            tfPurchase.setEditable(false);
            tfSelling.setEditable(false);
            tfExpirationDate.setEditable(false);
            tfStorageTemperature.setEditable(false);
            tfBrand.setEditable(false);
            tfWarranty.setEditable(false);
            cbProductType.setEnabled(false);
            cbArtisanalType.setEnabled(false);
        }
        refreshTable();
        refreshTimer.start();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                refreshTimer.stop();
            }
        });
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
        addFormField(formPanel, gbc, 2, "Purchase Price (USD):", tfPurchase);
        addFormField(formPanel, gbc, 3, "Selling Price (USD):", tfSelling);
        addFormField(formPanel, gbc, 4, "Product Type:", cbProductType);
        addFormField(formPanel, gbc, 5, "Expiration Date:", tfExpirationDate);
        addFormField(formPanel, gbc, 6, "Storage Temperature:", tfStorageTemperature);
        addFormField(formPanel, gbc, 7, "Brand:", tfBrand);
        addFormField(formPanel, gbc, 8, "Warranty (Months):", tfWarranty);
        addFormField(formPanel, gbc, 9, "Artisanal Type:", cbArtisanalType);
        cbProductType.setBackground(StyleManager.BG_DARK);
        cbProductType.setForeground(StyleManager.TEXT_PRIMARY);
        cbProductType.setFont(StyleManager.FONT_BODY);
        cbArtisanalType.setBackground(StyleManager.BG_DARK);
        cbArtisanalType.setForeground(StyleManager.TEXT_PRIMARY);
        cbArtisanalType.setFont(StyleManager.FONT_BODY);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(formPanel, BorderLayout.CENTER);

        String[] columns = {"Reference", "Designation", "Purchase Price (USD)", "Selling Price (USD)",
            "Expiration Date", "Warranty (Months)", "Artisanal Type"};
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
        btnAdd.setEnabled(!readOnly);
        btnDelete.setEnabled(!readOnly);

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

    private void addFormField(JPanel formPanel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
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
            productDAO.insertProduct(product);
            if (product instanceof FreshProduct freshProduct) productDAO.insertFreshProduct(freshProduct);
            if (product instanceof ElectronicProduct electronicProduct) {
                productDAO.insertElectronicProduct(electronicProduct);
            }
            if (product instanceof ArtisanalProduct artisanalProduct) {
                productDAO.insertArtisanalProduct(artisanalProduct);
            }
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Product added successfully.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedProduct() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a product.");
            return;
        }
        Product product = products.get(table.convertRowIndexToModel(selectedRow));
        try {
            if (product instanceof ElectronicProduct electronicProduct) {
                productDAO.deleteElectronicProduct(electronicProduct);
            }
            if (product instanceof FreshProduct freshProduct) productDAO.deleteFreshProduct(freshProduct);
            if (product instanceof ArtisanalProduct artisanalProduct) {
                productDAO.deleteArtisanalProduct(artisanalProduct);
            }
            productDAO.deleteProduct(product);
            refreshTable();
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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
                addProductRow(product);
            }
        }
    }

    private Product readProductFromFields() {
        String referenceStr = tfReference.getText().trim();
        String designation = tfDesignation.getText().trim();
        String purchaseStr = tfPurchase.getText().trim();
        String sellingStr = tfSelling.getText().trim();

        if (referenceStr.isEmpty() || designation.isEmpty() || purchaseStr.isEmpty() || sellingStr.isEmpty()) {
            throw new IllegalArgumentException("All fields are required.");
        }

        try {
            int reference = Integer.parseInt(referenceStr);
            double purchase = Double.parseDouble(purchaseStr);
            double selling = Double.parseDouble(sellingStr);
            if (purchase <= 0 || selling <= 0) {
                throw new IllegalArgumentException("Price value is invalid.");
            }
            String productType = (String) cbProductType.getSelectedItem();
            if ("Fresh".equals(productType)) {
                String expirationDate = tfExpirationDate.getText().trim();
                double temperature = Double.parseDouble(tfStorageTemperature.getText().trim());
                if (expirationDate.isEmpty()) throw new IllegalArgumentException("Expiration date is required.");
                return new FreshProduct(reference, designation, purchase, selling, 0, expirationDate, temperature);
            }
            if ("Electronic".equals(productType)) {
                String brand = tfBrand.getText().trim();
                int warranty = Integer.parseInt(tfWarranty.getText().trim());
                if (brand.isEmpty() || warranty < 0) throw new IllegalArgumentException("Brand and warranty are required.");
                return new ElectronicProduct(reference, designation, purchase, selling, 0, brand, warranty);
            }
            if ("Artisanal".equals(productType)) {
                return new ArtisanalProduct(reference, designation, purchase, selling, 0,
                        (ArtisanalProduct.TypeArtisanal) cbArtisanalType.getSelectedItem());
            }
            return new Product(reference, designation, purchase, selling, 0);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Check the numeric values.");
        }
    }

    private void refreshTable() {
        refreshTable(true);
    }

    private void refreshTable(boolean showError) {
        try {
            products.clear();
            products.addAll(productDAO.getAllProducts());
            tableModel.setRowCount(0);
            for (Product product : products) {
                addProductRow(product);
            }
        } catch (IllegalStateException exception) {
            if (showError) {
                JOptionPane.showMessageDialog(this, exception.getMessage(), "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addProductRow(Product product) {
        if (product == null) return;
        String expirationDate = product instanceof FreshProduct
                ? ((FreshProduct) product).getExpirationDate() : "";
        String warranty = product instanceof ElectronicProduct
                ? String.valueOf(((ElectronicProduct) product).getWarranty()) : "";
        String artisanalType = product instanceof ArtisanalProduct
                ? ((ArtisanalProduct) product).getType() : "";
        tableModel.addRow(new Object[] { product.getReference(), product.getDesignation(),
                String.format("%.2f USD", product.getPurchasePrice()),
                String.format("%.2f USD", product.getSellingPrice()), expirationDate, warranty, artisanalType });
    }

    private void clearForm() {
        tfReference.setText("");
        tfDesignation.setText("");
        tfPurchase.setText("");
        tfSelling.setText("");
        cbProductType.setSelectedItem("Standard");
        tfExpirationDate.setText("");
        tfStorageTemperature.setText("");
        tfBrand.setText("");
        tfWarranty.setText("");
        tfSearch.setText("");
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new ProductView().setVisible(true));
    }
}
