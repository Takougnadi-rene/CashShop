package com.cash_shop.sale;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import com.cash_shop.bill.BillView;
import com.cash_shop.common.StyleManager;
import com.cash_shop.customer.Customer;
import com.cash_shop.customer.CustomerDAO;
import com.cash_shop.employee.Employee;
import com.cash_shop.employee.EmployeeDAO;
import com.cash_shop.payment.Payment;
import com.cash_shop.product.Product;
import com.cash_shop.product.ProductDAO;

public class SaleView extends JFrame {
    private final List<Product> catalog = new ArrayList<>();
    private final Employee cashier;
    private final List<CartItem> basket = new ArrayList<>();
    private final JComboBox<Customer> customerCombo = new JComboBox<>();
    private final JComboBox<Product> productCombo = new JComboBox<>();
    private final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JRadioButton cashRadio = new JRadioButton("Cash", true);
    private final JRadioButton cardRadio = new JRadioButton("Bank card");
    private final JRadioButton mobileRadio = new JRadioButton("Mobile Money");
    private final JLabel totalLabel = StyleManager.createLabel("TOTAL DUE: 0 FCFA",
            StyleManager.ACCENT_GREEN, StyleManager.FONT_HEADER);
    private final DefaultTableModel basketModel = new DefaultTableModel(
            new String[] { "Product", "Qty", "Unit Price (FCFA)", "Subtotal (FCFA)" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable basketTable = StyleManager.createTable(basketModel);
    public SaleView() {
        this(null);
    }

    public SaleView(String cashierEmail) {
        cashier = findCashier(cashierEmail);
        loadCatalog();
        loadCustomers();
        setTitle("Register - Sales Module");
        setSize(1120, 720);
        setMinimumSize(new java.awt.Dimension(900, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        refreshProducts();
    }

    private Employee findCashier(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        try {
            return new EmployeeDAO().getAllEmployees().stream()
                    .filter(employee -> email.equalsIgnoreCase(employee.getEmail()))
                    .findFirst()
                    .orElse(null);
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Cashier Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void loadCatalog() {
        try {
            catalog.addAll(new ProductDAO().getAllProducts());
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Catalog Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCustomers() {
        customerCombo.addItem(null);
        try {
            for (Customer customer : new CustomerDAO().getAllCustomers()) {
                customerCombo.addItem(customer);
            }
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(this, "The customer list is unavailable.", "Customers",
                    JOptionPane.WARNING_MESSAGE);
        }
        customerCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value instanceof Customer ? ((Customer) value).getName() : "Walk-in customer");
                return this;
            }
        });
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(StyleManager.BG_DARK);
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(StyleManager.BG_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(2, 4, 4, 4));
        header.add(StyleManager.createLabel("REGISTER", StyleManager.ACCENT_GOLD, StyleManager.FONT_TITLE),
                BorderLayout.WEST);
        header.add(StyleManager.createLabel("SALES MODULE", StyleManager.TEXT_MUTED,
                StyleManager.FONT_SMALL), BorderLayout.EAST);

        JPanel columns = new JPanel(new BorderLayout(12, 0));
        columns.setOpaque(false);
        columns.add(buildEntryPanel(), BorderLayout.WEST);
        columns.add(buildBasketPanel(), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        actions.setBackground(StyleManager.BG_DARK);
        actions.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        JButton validateButton = StyleManager.createButton("COMPLETE SALE", StyleManager.ACCENT_GREEN);
        JButton cancelButton = StyleManager.createButton("CANCEL SALE", StyleManager.ACCENT_RED);
        JButton closeButton = StyleManager.createButton("Close", new Color(90, 90, 110));
        validateButton.addActionListener(event -> validateSale());
        cancelButton.addActionListener(event -> cancelSale());
        closeButton.addActionListener(event -> dispose());
        actions.add(validateButton);
        actions.add(cancelButton);
        actions.add(closeButton);

        root.add(header, BorderLayout.NORTH);
        root.add(columns, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildEntryPanel() {
        JPanel panel = StyleManager.createCard();
        panel.setLayout(new BorderLayout(0, 12));
        panel.setPreferredSize(new java.awt.Dimension(420, 0));

        JPanel entry = new JPanel(new GridBagLayout());
        entry.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(7, 4, 7, 4);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        entry.add(StyleManager.createLabel("[ CUSTOMER & ITEM ENTRY ]", StyleManager.ACCENT_BLUE,
                StyleManager.FONT_HEADER), constraints);
        constraints.gridwidth = 1;
        addFormRow(entry, constraints, 1, "Customer:", createCustomerControl());
        addFormRow(entry, constraints, 2, "Product:", productCombo);
        addFormRow(entry, constraints, 3, "Quantity:", quantitySpinner);

        JButton addButton = StyleManager.createButton("ADD TO CART", StyleManager.ACCENT_GREEN);
        addButton.addActionListener(event -> addToBasket());
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 2;
        entry.add(addButton, constraints);

        JPanel paymentPanel = new JPanel();
        paymentPanel.setOpaque(false);
        paymentPanel.setLayout(new BoxLayout(paymentPanel, BoxLayout.Y_AXIS));
        paymentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, StyleManager.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 2, 0, 2)));
        paymentPanel.add(StyleManager.createLabel("[ PAYMENT METHOD ]", StyleManager.ACCENT_BLUE,
                StyleManager.FONT_HEADER));
        paymentPanel.add(Box.createVerticalStrut(8));
        ButtonGroup paymentGroup = new ButtonGroup();
        stylePaymentOption(cashRadio);
        stylePaymentOption(cardRadio);
        stylePaymentOption(mobileRadio);
        paymentGroup.add(cashRadio);
        paymentGroup.add(cardRadio);
        paymentGroup.add(mobileRadio);
        paymentPanel.add(cashRadio);
        paymentPanel.add(Box.createVerticalStrut(4));
        paymentPanel.add(cardRadio);
        paymentPanel.add(Box.createVerticalStrut(4));
        paymentPanel.add(mobileRadio);

        panel.add(entry, BorderLayout.NORTH);
        panel.add(paymentPanel, BorderLayout.CENTER);
        styleInput(customerCombo);
        styleInput(productCombo);
        styleInput(quantitySpinner);
        return panel;
    }

    private JPanel createCustomerControl() {
        JPanel control = new JPanel(new BorderLayout(6, 0));
        control.setOpaque(false);
        JButton newCustomerButton = StyleManager.createButton("+ New", StyleManager.ACCENT_BLUE);
        newCustomerButton.setPreferredSize(new java.awt.Dimension(85, 32));
        newCustomerButton.addActionListener(event -> addCustomer());
        control.add(customerCombo, BorderLayout.CENTER);
        control.add(newCustomerButton, BorderLayout.EAST);
        return control;
    }

    private void addCustomer() {
        JTextField idField = StyleManager.createField();
        idField.setText(Integer.toString(nextCustomerId()));
        JTextField nameField = StyleManager.createField();
        JTextField phoneField = StyleManager.createField();

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(StyleManager.BG_DARK);
        form.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        addCustomerField(form, constraints, 0, "Customer ID:", idField);
        addCustomerField(form, constraints, 1, "Name:", nameField);
        addCustomerField(form, constraints, 2, "Phone:", phoneField);

        int choice = JOptionPane.showConfirmDialog(this, form, "Add New Customer",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int customerId = Integer.parseInt(idField.getText().trim());
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            if (name.isEmpty() || phone.isEmpty()) {
                throw new IllegalArgumentException("Name and phone are required.");
            }

            new CustomerDAO().insertCustomer(customerId, name, phone, 0);
            Customer customer = new Customer(customerId, name, "", phone, 0);
            customerCombo.addItem(customer);
            customerCombo.setSelectedItem(customer);
        } catch (NumberFormatException exception) {
            showCustomerError("Customer ID must be a number.");
        } catch (SQLException | IllegalArgumentException exception) {
            showCustomerError(exception.getMessage());
        }
    }

    private int nextCustomerId() {
        int largestId = 0;
        for (int index = 0; index < customerCombo.getItemCount(); index++) {
            Customer customer = customerCombo.getItemAt(index);
            if (customer != null) {
                largestId = Math.max(largestId, customer.getCustomerId());
            }
        }
        return largestId + 1;
    }

    private void addCustomerField(JPanel form, GridBagConstraints constraints, int row,
            String label, JTextField field) {
        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.weightx = 0;
        form.add(StyleManager.createLabel(label, StyleManager.TEXT_MUTED, StyleManager.FONT_BODY), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        form.add(field, constraints);
    }

    private void showCustomerError(String message) {
        JOptionPane.showMessageDialog(this, message, "Customer Error", JOptionPane.ERROR_MESSAGE);
    }

    private void addFormRow(JPanel panel, GridBagConstraints constraints, int row, String label,
            java.awt.Component input) {
        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.weightx = 0;
        panel.add(StyleManager.createLabel(label, StyleManager.TEXT_MUTED, StyleManager.FONT_BODY), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        panel.add(input, constraints);
    }

    private JPanel buildBasketPanel() {
        JPanel panel = StyleManager.createCard();
        panel.setLayout(new BorderLayout(0, 10));
        JPanel title = new JPanel(new BorderLayout());
        title.setOpaque(false);
        title.add(StyleManager.createLabel("[ CUSTOMER CART ]", StyleManager.ACCENT_BLUE,
                StyleManager.FONT_HEADER), BorderLayout.WEST);
        JButton removeButton = StyleManager.createButton("Remove", StyleManager.ACCENT_RED);
        removeButton.setPreferredSize(new java.awt.Dimension(105, 30));
        removeButton.addActionListener(event -> removeSelectedItem());
        title.add(removeButton, BorderLayout.EAST);

        basketTable.setFillsViewportHeight(true);
        basketTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(basketTable);
        scrollPane.getViewport().setBackground(StyleManager.BG_PANEL);
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 2));
        totalPanel.setOpaque(false);
        totalPanel.add(totalLabel);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(totalPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void stylePaymentOption(JRadioButton option) {
        option.setOpaque(false);
        option.setForeground(StyleManager.TEXT_PRIMARY);
        option.setFont(StyleManager.FONT_BODY);
        option.setFocusPainted(false);
    }

    private void styleInput(JComboBox<?> combo) {
        combo.setBackground(StyleManager.BG_DARK);
        combo.setForeground(StyleManager.TEXT_PRIMARY);
        combo.setFont(StyleManager.FONT_BODY);
    }

    private void styleInput(JSpinner spinner) {
        spinner.setFont(StyleManager.FONT_BODY);
        spinner.setBorder(BorderFactory.createLineBorder(StyleManager.BORDER_COLOR));
        spinner.getEditor().getComponent(0).setBackground(StyleManager.BG_DARK);
        spinner.getEditor().getComponent(0).setForeground(StyleManager.TEXT_PRIMARY);
    }

    private void refreshProducts() {
        productCombo.removeAllItems();
        for (Product product : catalog) {
            productCombo.addItem(product);
        }
        productCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Product product) {
                    setText(product.getDesignation() + " - " + formatMoney(product.getSellingPrice()));
                }
                return this;
            }
        });
    }

    private void addToBasket() {
        Product product = (Product) productCombo.getSelectedItem();
        if (product == null) {
            JOptionPane.showMessageDialog(this, "No products are available in the catalog.", "Product",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantity = (Integer) quantitySpinner.getValue();
        CartItem existing = basket.stream()
                .filter(item -> item.product.getReference() == product.getReference())
                .findFirst().orElse(null);
        int currentQuantity = existing == null ? 0 : existing.quantity;
        if (currentQuantity + quantity > product.getStockQuantity()) {
            JOptionPane.showMessageDialog(this, "There is not enough stock for the requested quantity.", "Stock",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (existing == null) {
            basket.add(new CartItem(product, quantity));
        } else {
            existing.quantity += quantity;
        }
        refreshBasket();
    }

    private void removeSelectedItem() {
        int selectedRow = basketTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        basket.remove(basketTable.convertRowIndexToModel(selectedRow));
        refreshBasket();
    }

    private void validateSale() {
        if (basket.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Add at least one item before completing the sale.",
                    "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Double cashReceived = null;
        if (cashRadio.isSelected()) {
            cashReceived = requestCashReceived();
            if (cashReceived == null) {
                return;
            }
        }

        Sale sale = createSaleFromBasket();
        Customer customer = (Customer) customerCombo.getSelectedItem();
        Payment.PaymentMode paymentMode = selectedPaymentMode();
        try {
            int billNumber = new SaleDAO().persistCompletedSale(sale, customer, paymentMode);
            for (CartItem item : basket) {
                item.product.setStockQuantity(item.product.getStockQuantity() - item.quantity);
            }
            new BillView(sale, customer, paymentMode, cashReceived, billNumber).setVisible(true);
            clearBasket();
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Sale Not Saved",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Sale createSaleFromBasket() {
        Sale sale = new Sale(0, LocalDate.now(), cashier);
        List<Product> soldProducts = new ArrayList<>();
        for (CartItem item : basket) {
            for (int index = 0; index < item.quantity; index++) {
                soldProducts.add(item.product);
            }
        }
        sale.setProductsList(new ArrayList<>(soldProducts));
        return sale;
    }

    private Payment.PaymentMode selectedPaymentMode() {
        if (cardRadio.isSelected()) {
            return Payment.PaymentMode.CREDIT_CARD;
        }
        if (mobileRadio.isSelected()) {
            return Payment.PaymentMode.MOBILE_PAYMENT;
        }
        return Payment.PaymentMode.CASH;
    }

    private void cancelSale() {
        if (!basket.isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(this, "Cancel all items in the cart?",
                    "Cancel Sale", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }
        clearBasket();
    }

    private void clearBasket() {
        basket.clear();
        refreshBasket();
    }

    private Double requestCashReceived() {
        JTextField amountField = StyleManager.createField();
        JLabel changeAmountLabel = StyleManager.createLabel("Change: -- FCFA",
                StyleManager.TEXT_MUTED, StyleManager.FONT_BODY);
        JPanel cashDialog = new JPanel(new GridBagLayout());
        cashDialog.setBackground(StyleManager.BG_DARK);
        cashDialog.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        cashDialog.add(StyleManager.createLabel("Amount due: " + formatMoney(currentTotal()),
                StyleManager.TEXT_PRIMARY, StyleManager.FONT_HEADER), constraints);
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        cashDialog.add(StyleManager.createLabel("Cash received (FCFA):", StyleManager.TEXT_MUTED,
                StyleManager.FONT_BODY), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        cashDialog.add(amountField, constraints);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        cashDialog.add(changeAmountLabel, constraints);

        amountField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                refreshDialogChange(amountField, changeAmountLabel);
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                refreshDialogChange(amountField, changeAmountLabel);
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                refreshDialogChange(amountField, changeAmountLabel);
            }
        });

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, cashDialog, "Cash Payment",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return null;
            }
            Double received = parseAmount(amountField.getText());
            if (received != null && received >= currentTotal()) {
                return received;
            }
            JOptionPane.showMessageDialog(this, "Cash received must be a valid amount at least equal to the total due.",
                    "Insufficient Cash", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refreshDialogChange(JTextField amountField, JLabel changeAmountLabel) {
        Double received = parseAmount(amountField.getText());
        if (received == null) {
            changeAmountLabel.setText("Change: -- FCFA");
            changeAmountLabel.setForeground(StyleManager.TEXT_MUTED);
            return;
        }
        double difference = received - currentTotal();
        changeAmountLabel.setText(difference >= 0
                ? "Change: " + formatMoney(difference)
                : "Amount due: " + formatMoney(-difference));
        changeAmountLabel.setForeground(difference >= 0 ? StyleManager.ACCENT_GREEN : StyleManager.ACCENT_RED);
    }

    private Double parseAmount(String amount) {
        String normalized = amount.trim().replace(" ", "").replace(",", "");
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            double value = Double.parseDouble(normalized);
            return value >= 0 ? value : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private double currentTotal() {
        return basket.stream().mapToDouble(item -> item.product.getSellingPrice() * item.quantity).sum();
    }

    private void refreshBasket() {
        basketModel.setRowCount(0);
        double total = 0;
        for (CartItem item : basket) {
            double subtotal = item.product.getSellingPrice() * item.quantity;
            total += subtotal;
            basketModel.addRow(new Object[] { item.product.getDesignation(), item.quantity,
                    formatMoney(item.product.getSellingPrice()), formatMoney(subtotal) });
        }
        totalLabel.setText("TOTAL DUE: " + formatMoney(total));
    }

    private String formatMoney(double amount) {
        return String.format("%.0f FCFA", amount);
    }

    private static final class CartItem {
        private final Product product;
        private int quantity;

        private CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new SaleView().setVisible(true));
    }
}