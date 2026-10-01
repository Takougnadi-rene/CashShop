package com.cash_shop.aisle;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

import com.cash_shop.common.StyleManager;
import com.cash_shop.product.Product;
import com.cash_shop.product.ProductDAO;
import com.cash_shop.user.AccountManagementView;

/**
 * Swing window to browse aisles and their products. In read-only mode (e.g. for an aisle manager) it only
 * shows the aisles of the given manager and lets the user compute the stock value; otherwise it also allows
 * adding and deleting aisles and assigning products to them. The data is reloaded automatically every 5
 * seconds.
 */
public class AisleView extends JFrame {
    private final boolean readOnly;
    private final String assignedManagerMatricule;
    private final String accountUsername;
    private final AisleDAO aisleDAO = new AisleDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final List<Aisle> aisles = new ArrayList<>();
    private final List<Product> products = new ArrayList<>();
    private final JComboBox<String> productCombo = new JComboBox<>();
    private final DefaultTableModel aisleModel = StyleManager.createReadOnlyModel(
            "Code", "Aisle Name");
    private final DefaultTableModel productModel = StyleManager.createReadOnlyModel(
            "Reference", "Product", "Stock", "Price");
    private final JTable aisleTable = StyleManager.createTable(aisleModel);
    private final JTable productTable = StyleManager.createTable(productModel);
    private Aisle selectedAisle;
    private final Timer refreshTimer = new Timer(5000, event -> refreshAisles());

    /** Full-access window. */
    public AisleView() {
        this(false, null);
    }

    /** Window with an explicit read-only flag. */
    public AisleView(boolean readOnly) {
        this(readOnly, null);
    }

    /** Read-only window restricted to the aisles managed by the given matricule. */
    public AisleView(String assignedManagerMatricule) {
        this(true, assignedManagerMatricule);
    }

    /** Window with a read-only flag and an optional manager filter. */
    public AisleView(boolean readOnly, String assignedManagerMatricule) {
        this(readOnly, assignedManagerMatricule, null);
    }

    /** Read-only manager window that also shows the "Manage my account" button. */
    public AisleView(String assignedManagerMatricule, String accountUsername) {
        this(true, assignedManagerMatricule, accountUsername);
    }

    /** Main constructor shared by all the others. */
    private AisleView(boolean readOnly, String assignedManagerMatricule, String accountUsername) {
        this.readOnly = readOnly;
        this.assignedManagerMatricule = assignedManagerMatricule;
        this.accountUsername = accountUsername;
        setTitle(readOnly ? "Aisle Overview" : "Aisle Management");
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        refreshAisles();
        if (!readOnly) refreshProductChoices();
        refreshTimer.start();
        addWindowListener(new WindowAdapter() {
            // Stop the periodic refresh once the window is closed.
            @Override
            public void windowClosed(WindowEvent event) {
                refreshTimer.stop();
            }
        });
    }

    /** Builds the screen: aisle table, product table, action buttons and the optional account button. */
    private void buildUI() {
        JPanel aisleList = new JPanel(new BorderLayout());
        aisleList.setOpaque(false);
        aisleList.add(StyleManager.createScrollPane(aisleTable), BorderLayout.CENTER);

        JPanel productList = new JPanel(new BorderLayout());
        productList.setOpaque(false);
        productList.add(StyleManager.createScrollPane(productTable), BorderLayout.CENTER);

        JPanel lists = new JPanel(new GridLayout(1, 2, 10, 0));
        lists.setOpaque(false);
        lists.add(aisleList);
        lists.add(productList);
        aisleTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                selectAisle();
            }
        });

        // Buttons
        JButton refreshButton = new JButton("Refresh");
        JButton closeButton = new JButton("Close");
        refreshButton.addActionListener(event -> refreshAisles());
        closeButton.addActionListener(event -> dispose());

        JPanel actions;
        if (readOnly) {
            JButton stockValueButton = new JButton("Stock Value");
            stockValueButton.addActionListener(event -> showStockValue());
            actions = StyleManager.createButtonBar(stockValueButton, refreshButton, closeButton);
        } else {
            JButton addButton = new JButton("Add Aisle");
            JButton assignButton = new JButton("Assign Product");
            JButton deleteButton = new JButton("Delete Aisle");
            addButton.addActionListener(event -> addAisle());
            assignButton.addActionListener(event -> assignProduct());
            deleteButton.addActionListener(event -> deleteAisle());
            actions = StyleManager.createButtonBar(addButton, assignButton, deleteButton,
                    refreshButton, closeButton);
                actions.add(productCombo, 0);
        }

        JPanel page = StyleManager.createPage();
        page.add(StyleManager.createTitle("Aisles"), BorderLayout.NORTH);
        page.add(lists, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout());
        if (accountUsername != null && !accountUsername.isBlank()) {
            JButton accountButton = new JButton("Manage my account");
            accountButton.addActionListener(event ->
                new AccountManagementView(this, accountUsername).setVisible(true));
            footer.add(accountButton, BorderLayout.WEST);
        }
        footer.add(actions, BorderLayout.EAST);
        page.add(footer, BorderLayout.SOUTH);

        setContentPane(page);
    }

    /**
     * Reloads the aisles (filtered by manager when required) and keeps the previous selection when possible.
     */
    private void refreshAisles() {
        try {
            Integer selectedCode = selectedAisle == null ? null : selectedAisle.getAisleCode();
            aisles.clear();
            List<Aisle> loadedAisles = aisleDAO.getAllAisles();
            // An aisle manager only sees the aisles he is in charge of.
            if (assignedManagerMatricule != null) {
                String managerMatricule = assignedManagerMatricule.trim();
                for (Aisle aisle : loadedAisles) {
                    if (!managerMatricule.isEmpty() && aisle.getAisleChief() != null
                            && aisle.getAisleChief().trim().equalsIgnoreCase(managerMatricule)) {
                        aisles.add(aisle);
                    }
                }
            } else {
                aisles.addAll(loadedAisles);
            }
            aisleModel.setRowCount(0);
            for (Aisle aisle : aisles) {
                aisleModel.addRow(new Object[] { aisle.getAisleCode(), aisle.getAisleName() });
            }
            productModel.setRowCount(0);
            int selectedIndex = 0;
            if (selectedCode != null) {
                for (int index = 0; index < aisles.size(); index++) {
                    if (aisles.get(index).getAisleCode() == selectedCode) {
                        selectedIndex = index;
                        break;
                    }
                }
            }
            selectedAisle = null;
            if (!aisles.isEmpty()) {
                aisleTable.setRowSelectionInterval(selectedIndex, selectedIndex);
            } else {
                selectedAisle = null;
            }
        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    /** Fills the product combo box used to assign a product to an aisle. */
    private void refreshProductChoices() {
        try {
            products.clear();
            products.addAll(productDAO.getAllProducts());
            productCombo.removeAllItems();
            for (Product product : products) {
                productCombo.addItem(product.getReference() + " - " + product.getDesignation());
            }
        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    /** Loads the products of the selected aisle into the right-hand table. */
    private void selectAisle() {
        int row = aisleTable.getSelectedRow();
        if (row < 0) {
            return;
        }
        selectedAisle = aisles.get(aisleTable.convertRowIndexToModel(row));
        try {
            productModel.setRowCount(0);
            for (Product product : aisleDAO.getProductsForAisle(selectedAisle.getAisleCode())) {
                productModel.addRow(new Object[] { product.getReference(), product.getDesignation(),
                        product.getStockQuantity(), String.format("%.2f", product.getSellingPrice()) });
            }
        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    /** Asks for the aisle details in a dialog and saves the new aisle. */
    private void addAisle() {
        JTextField codeField = StyleManager.createField();
        JTextField nameField = StyleManager.createField();
        JTextField categoryField = StyleManager.createField();
        JTextField managerField = StyleManager.createField();
        JPanel form = StyleManager.createForm("Aisle details");
        StyleManager.addRow(form, 0, "Aisle Code:", codeField);
        StyleManager.addRow(form, 1, "Aisle Name:", nameField);
        StyleManager.addRow(form, 2, "Category:", categoryField);
        StyleManager.addRow(form, 3, "Aisle Manager:", managerField);
        if (JOptionPane.showConfirmDialog(this, form, "Add aisle", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            int code = Integer.parseInt(codeField.getText().trim());
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            String manager = managerField.getText().trim();
            if (name.isEmpty() || category.isEmpty()) {
                throw new IllegalArgumentException("Aisle name and category are required.");
            }
            aisleDAO.addAisle(new Aisle(code, name, category, manager));
            refreshAisles();
        } catch (NumberFormatException exception) {
            showError("Aisle code must be numeric.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    /** Assigns the product chosen in the combo box to the selected aisle. */
    private void assignProduct() {
        if (selectedAisle == null) {
            showError("Select an aisle first.");
            return;
        }
        int index = productCombo.getSelectedIndex();
        if (index < 0) {
            showError("Select a product first.");
            return;
        }
        try {
            aisleDAO.addProductToAisle(selectedAisle, products.get(index));
            selectAisle();
        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    /** Deletes the selected aisle after confirmation. */
    private void deleteAisle() {
        if (selectedAisle == null) {
            showError("Select an aisle first.");
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this, "Delete the selected aisle?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                aisleDAO.removeAisle(selectedAisle);
                refreshAisles();
            } catch (IllegalStateException exception) {
                showError(exception.getMessage());
            }
        }
    }

    /** Shows the inventory value (selling price x stock) of the selected aisle. */
    private void showStockValue() {
        if (selectedAisle == null) {
            showError("Select an aisle first.");
            return;
        }
        try {
            double total = aisleDAO.getProductsForAisle(selectedAisle.getAisleCode()).stream()
                    .mapToDouble(product -> product.getSellingPrice() * product.getStockQuantity()).sum();
            JOptionPane.showMessageDialog(this,
                    String.format("Inventory value for %s: %.2f USD", selectedAisle.getAisleName(), total),
                    "Stock Value", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    /** Displays an error dialog. */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
