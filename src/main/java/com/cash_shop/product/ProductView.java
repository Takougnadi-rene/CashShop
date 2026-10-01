package com.cash_shop.product;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

import com.cash_shop.common.StyleManager;

/**
 * Swing window to manage the product catalog (add, delete, search). Fields adapt to the product type; read-
 * only mode is available. Refreshes every 5 seconds.
 */
public class ProductView extends JFrame {

    private final List<Product> products = new ArrayList<>();
    private final List<Product> visibleProducts = new ArrayList<>();
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
    private final JTextField tfSearch = new JTextField(20);
    private final ProductDAO productDAO = new ProductDAO();
    private final DefaultTableModel tableModel = StyleManager.createReadOnlyModel(
            "Reference", "Designation", "Purchase Price (USD)", "Selling Price (USD)",
            "Expiration Date", "Warranty (Months)", "Artisanal Type");
    private final JTable table = StyleManager.createTable(tableModel);
    private final boolean readOnly;
    private final Timer refreshTimer = new Timer(5000, event -> refreshTable(false));

    /** Editable window. */
    public ProductView() {
        this(false);
    }

    /** Window that can be opened read-only. */
    public ProductView(boolean readOnly) {
        this.readOnly = readOnly;
        setTitle("Product Management");
        setSize(new java.awt.Dimension(Toolkit.getDefaultToolkit().getScreenSize()));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
            // Stop the periodic refresh once the window is closed.
            @Override
            public void windowClosed(WindowEvent event) {
                refreshTimer.stop();
            }
        });
    }

    /** Builds the form, the search bar, the table and the buttons. */
    private void buildUI() {
        // Form on the left
        JPanel form = StyleManager.createForm("Product Details");
        StyleManager.addRow(form, 0, "Reference:", tfReference);
        StyleManager.addRow(form, 1, "Name:", tfDesignation);
        StyleManager.addRow(form, 2, "Purchase Price (USD):", tfPurchase);
        StyleManager.addRow(form, 3, "Selling Price (USD):", tfSelling);
        StyleManager.addRow(form, 4, "Product Type:", cbProductType);
        StyleManager.addRow(form, 5, "Expiration Date:", tfExpirationDate);
        StyleManager.addRow(form, 6, "Storage Temperature:", tfStorageTemperature);
        StyleManager.addRow(form, 7, "Brand:", tfBrand);
        StyleManager.addRow(form, 8, "Warranty (Months):", tfWarranty);
        StyleManager.addRow(form, 9, "Artisanal Type:", cbArtisanalType);
        cbProductType.addActionListener(e -> updateTypeFields());
        updateTypeFields();
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(form, BorderLayout.NORTH);

        // Styled search bar
        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> searchProducts());
        tfSearch.addActionListener(e -> searchProducts());
        tfSearch.setBackground(StyleManager.BG_INPUT);
        tfSearch.setForeground(StyleManager.TEXT_PRIMARY);
        tfSearch.setCaretColor(StyleManager.ACCENT_PRIMARY);
        tfSearch.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(StyleManager.BORDER_COLOR, 1),
                javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        JLabel searchLabel = StyleManager.createLabel("Search:");
        JPanel searchBar = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);
        searchBar.add(searchLabel);
        searchBar.add(tfSearch);
        searchBar.add(btnSearch);
        StyleManager.styleButton(btnSearch);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(searchBar, BorderLayout.NORTH);
        center.add(StyleManager.createScrollPane(table), BorderLayout.CENTER);

        // Buttons
        JButton btnAdd = new JButton("Add");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        JButton btnClose = new JButton("Close");
        btnAdd.setEnabled(!readOnly);
        btnDelete.setEnabled(!readOnly);
        btnAdd.addActionListener(e -> addProduct());
        btnDelete.addActionListener(e -> deleteSelectedProduct());
        btnClear.addActionListener(e -> clearForm());
        btnClose.addActionListener(e -> dispose());

        JPanel page = StyleManager.createPage();
        page.add(StyleManager.createTitle("Products"), BorderLayout.NORTH);
        page.add(left, BorderLayout.WEST);
        page.add(center, BorderLayout.CENTER);
        page.add(StyleManager.createButtonBar(btnAdd, btnDelete, btnClear, btnClose), BorderLayout.SOUTH);
        setContentPane(page);
    }

    /** Enables only the fields relevant to the selected product type. */
    private void updateTypeFields() {
        if (readOnly) {
            return;
        }
        String type = (String) cbProductType.getSelectedItem();
        boolean fresh = "Fresh".equals(type);
        boolean electronic = "Electronic".equals(type);
        boolean artisanal = "Artisanal".equals(type);
        tfExpirationDate.setEnabled(fresh);
        tfStorageTemperature.setEnabled(fresh);
        tfBrand.setEnabled(electronic);
        tfWarranty.setEnabled(electronic);
        cbArtisanalType.setEnabled(artisanal);
    }

    /** Creates the product (base row, then the type-specific row). */
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

    /** Deletes the selected product from the type-specific table and from the products table. */
    private void deleteSelectedProduct() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a product.");
            return;
        }
        Product product = visibleProducts.get(table.convertRowIndexToModel(selectedRow));
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

    /** Applies the search text to the table. */
    private void searchProducts() {
        applyFilter();
    }

    /**
     * Rebuilds the table from the products matching the search text, and remembers which products are
     * displayed.
     */
    private void applyFilter() {
        String keyword = tfSearch.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        visibleProducts.clear();
        for (Product product : products) {
            if (keyword.isEmpty()
                    || String.valueOf(product.getReference()).contains(keyword)
                    || product.getDesignation().toLowerCase().contains(keyword)) {
                visibleProducts.add(product);
                addProductRow(product);
            }
        }
    }

    /** Builds a Product (or subclass) from the form, validating the values. */
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

    /** Reloads the products and shows errors. */
    private void refreshTable() {
        refreshTable(true);
    }

    /** Reloads the products; errors are only shown when requested (silent for the periodic refresh). */
    private void refreshTable(boolean showError) {
        try {
            products.clear();
            products.addAll(productDAO.getAllProducts());
            applyFilter();
        } catch (IllegalStateException exception) {
            if (showError) {
                JOptionPane.showMessageDialog(this, exception.getMessage(), "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Adds one table row for the given product. */
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

    /** Empties the form fields. */
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
}
