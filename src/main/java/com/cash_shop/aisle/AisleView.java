package com.cash_shop.aisle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import static com.cash_shop.common.StyleManager.ACCENT_BLUE;
import static com.cash_shop.common.StyleManager.ACCENT_GOLD;
import static com.cash_shop.common.StyleManager.ACCENT_GREEN;
import static com.cash_shop.common.StyleManager.ACCENT_RED;
import static com.cash_shop.common.StyleManager.BG_DARK;
import static com.cash_shop.common.StyleManager.BG_PANEL;
import static com.cash_shop.common.StyleManager.FONT_BODY;
import static com.cash_shop.common.StyleManager.FONT_HEADER;
import static com.cash_shop.common.StyleManager.FONT_SMALL;
import static com.cash_shop.common.StyleManager.FONT_TITLE;
import static com.cash_shop.common.StyleManager.TEXT_MUTED;
import static com.cash_shop.common.StyleManager.TEXT_PRIMARY;
import static com.cash_shop.common.StyleManager.createButton;
import static com.cash_shop.common.StyleManager.createCard;
import static com.cash_shop.common.StyleManager.createField;
import static com.cash_shop.common.StyleManager.createLabel;
import static com.cash_shop.common.StyleManager.createTable;
import com.cash_shop.product.Product;
import com.cash_shop.product.ProductDAO;

public class AisleView extends JFrame {
    private final boolean readOnly;
    private final AisleDAO aisleDAO = new AisleDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final List<Aisle> aisles = new ArrayList<>();
    private final List<Product> products = new ArrayList<>();
    private final JTextField codeField = createField();
    private final JTextField nameField = createField();
    private final JTextField categoryField = createField();
    private final JTextField managerField = createField();
    private final JComboBox<String> productCombo = new JComboBox<>();
    private final DefaultTableModel aisleModel = new DefaultTableModel(
            new String[] { "Code", "Aisle Name", "Category", "Manager" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel productModel = new DefaultTableModel(
            new String[] { "Reference", "Product", "Stock", "Price" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable aisleTable = createTable(aisleModel);
    private final JTable productTable = createTable(productModel);
    private Aisle selectedAisle;

    public AisleView() {
        this(false);
    }

    public AisleView(boolean readOnly) {
        this.readOnly = readOnly;
        setTitle(readOnly ? "Aisle Overview" : "Aisle Management");
        setSize(980, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        refreshAisles();
        if (!readOnly) refreshProductChoices();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_DARK);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setBackground(BG_PANEL);
        header.add(createLabel("AISLE MANAGEMENT", ACCENT_GOLD, FONT_TITLE));

        JPanel content = new JPanel(new GridLayout(1, readOnly ? 1 : 2, 10, 0));
        content.setBackground(BG_DARK);
        content.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JPanel form = createCard();
        form.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 5, 6, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        form.add(createLabel("[ Aisle Details ]", ACCENT_BLUE, FONT_HEADER), constraints);
        addField(form, constraints, 1, "Aisle Code:", codeField);
        addField(form, constraints, 2, "Aisle Name:", nameField);
        addField(form, constraints, 3, "Category:", categoryField);
        addField(form, constraints, 4, "Aisle Manager:", managerField);
        constraints.gridy = 5;
        constraints.gridx = 0;
        constraints.gridwidth = 1;
        form.add(createLabel("Add Product:", TEXT_MUTED, FONT_SMALL), constraints);
        constraints.gridx = 1;
        productCombo.setBackground(BG_DARK);
        productCombo.setForeground(TEXT_PRIMARY);
        productCombo.setFont(FONT_BODY);
        form.add(productCombo, constraints);

        JPanel lists = new JPanel(new GridLayout(2, 1, 0, 10));
        lists.setBackground(BG_DARK);
        JPanel aisleList = createCard();
        aisleList.setLayout(new BorderLayout());
        aisleList.add(createLabel("[ Aisle List ]", ACCENT_BLUE, FONT_HEADER), BorderLayout.NORTH);
        aisleList.add(new JScrollPane(aisleTable), BorderLayout.CENTER);
        JPanel productList = createCard();
        productList.setLayout(new BorderLayout());
        productList.add(createLabel("[ Selected Aisle Products ]", ACCENT_BLUE, FONT_HEADER), BorderLayout.NORTH);
        productList.add(new JScrollPane(productTable), BorderLayout.CENTER);
        lists.add(aisleList);
        lists.add(productList);
        aisleTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                selectAisle();
            }
        });
        if (!readOnly) content.add(form);
        content.add(lists);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        actions.setBackground(BG_PANEL);
        javax.swing.JButton refreshButton = createButton("Refresh", ACCENT_GOLD);
        javax.swing.JButton closeButton = createButton("Close", new Color(80, 80, 100));
        refreshButton.addActionListener(event -> refreshAisles());
        closeButton.addActionListener(event -> dispose());
        if (readOnly) {
            javax.swing.JButton stockValueButton = createButton("Stock Value", ACCENT_BLUE);
            stockValueButton.addActionListener(event -> showStockValue());
            actions.add(stockValueButton);
        } else {
            javax.swing.JButton addButton = createButton("+ Add Aisle", ACCENT_GREEN);
            javax.swing.JButton assignButton = createButton("Assign Product", ACCENT_BLUE);
            javax.swing.JButton deleteButton = createButton("Delete Aisle", ACCENT_RED);
            addButton.addActionListener(event -> addAisle());
            assignButton.addActionListener(event -> assignProduct());
            deleteButton.addActionListener(event -> deleteAisle());
            actions.add(addButton);
            actions.add(assignButton);
            actions.add(deleteButton);
        }
        actions.add(refreshButton);
        actions.add(closeButton);

        main.add(header, BorderLayout.NORTH);
        main.add(content, BorderLayout.CENTER);
        main.add(actions, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private void addField(JPanel panel, GridBagConstraints constraints, int row, String label, JTextField field) {
        constraints.gridy = row;
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        panel.add(createLabel(label, TEXT_MUTED, FONT_SMALL), constraints);
        constraints.gridx = 1;
        panel.add(field, constraints);
    }

    private void refreshAisles() {
        try {
            aisles.clear();
            aisles.addAll(aisleDAO.getAllAisles());
            aisleModel.setRowCount(0);
            for (Aisle aisle : aisles) {
                aisleModel.addRow(new Object[] { aisle.getAisleCode(), aisle.getAisleName(), aisle.getCategory(),
                        aisle.getAisleChief() });
            }
            selectedAisle = null;
            productModel.setRowCount(0);
        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

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

    private void addAisle() {
        try {
            int code = Integer.parseInt(codeField.getText().trim());
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            String manager = managerField.getText().trim();
            if (name.isEmpty() || category.isEmpty()) {
                throw new IllegalArgumentException("Aisle name and category are required.");
            }
            aisleDAO.addAisle(new Aisle(code, name, category, manager));
            clearForm();
            refreshAisles();
        } catch (NumberFormatException exception) {
            showError("Aisle code must be numeric.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

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

    private void showStockValue() {
        if (selectedAisle == null) {
            showError("Select an aisle first.");
            return;
        }
        try {
            double total = aisleDAO.getProductsForAisle(selectedAisle.getAisleCode()).stream()
                    .mapToDouble(product -> product.getSellingPrice() * product.getStockQuantity()).sum();
            JOptionPane.showMessageDialog(this,
                    String.format("Inventory value for %s: %.0f FCFA", selectedAisle.getAisleName(), total),
                    "Stock Value", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void clearForm() {
        codeField.setText("");
        nameField.setText("");
        categoryField.setText("");
        managerField.setText("");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
