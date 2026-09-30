package com.cash_shop.sale;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
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
import javax.swing.Timer;
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
import com.cash_shop.user.AccountManagementView;

public class SaleView extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(SaleView.class.getName());
    private final List<Product> catalog = new ArrayList<>();
    private final Employee cashier;
    private final String accountUsername;
    private final RegisterSessionDAO registerSessionDAO = new RegisterSessionDAO();
    private String registerSessionId;
    private Timer registerHeartbeat;
    private final Timer dataRefreshTimer = new Timer(5000, event -> refreshSharedCatalog());
    private final List<CartItem> basket = new ArrayList<>();
    private final JComboBox<Customer> customerCombo = new JComboBox<>();
    private final JComboBox<Product> productCombo = new JComboBox<>();
    private final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JRadioButton cashRadio = new JRadioButton("Cash", true);
    private final JRadioButton cardRadio = new JRadioButton("Bank card");
    private final JRadioButton mobileRadio = new JRadioButton("Mobile Money");
    private final JLabel totalLabel = StyleManager.createBoldLabel("Total due: 0 $");
    private final DefaultTableModel basketModel = StyleManager.createReadOnlyModel(
            "Product", "Qty", "Unit Price ($)", "Subtotal ($)");
    private final JTable basketTable = StyleManager.createTable(basketModel);

    public SaleView() {
        this(null);
    }

    public SaleView(String cashierEmail) {
        this(cashierEmail, null);
    }

    public SaleView(String cashierEmail, String accountUsername) {
        cashier = findCashier(cashierEmail);
        this.accountUsername = accountUsername;
        loadCatalog();
        loadCustomers();
        setTitle("Register - Sales Module");
        setSize(new java.awt.Dimension(Toolkit.getDefaultToolkit().getScreenSize()));
        setMinimumSize(new java.awt.Dimension(Toolkit.getDefaultToolkit().getScreenSize()));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        refreshProducts();
        startRegisterSession();
        dataRefreshTimer.start();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                dataRefreshTimer.stop();
            }
        });
    }

    private void refreshSharedCatalog() {
        if (basket.isEmpty()) {
            try {
                catalog.clear();
                catalog.addAll(new ProductDAO().getAllProducts());
                refreshProducts();
            } catch (IllegalStateException exception) {
                LOGGER.log(Level.FINE, "Unable to refresh the product catalog.", exception);
            }
        }
        Customer selected = (Customer) customerCombo.getSelectedItem();
        Integer selectedId = selected == null ? null : selected.getCustomerId();
        try {
            customerCombo.removeAllItems();
            customerCombo.addItem(null);
            for (Customer customer : new CustomerDAO().getAllCustomers()) {
                customerCombo.addItem(customer);
                if (selectedId != null && selectedId == customer.getCustomerId()) {
                    customerCombo.setSelectedItem(customer);
                }
            }
        } catch (IllegalStateException exception) {
            LOGGER.log(Level.FINE, "Unable to refresh the customer list.", exception);
        }
    }

    private void startRegisterSession() {
        if (cashier == null || cashier.getRole() != Employee.Role.CASHIER) {
            return;
        }
        try {
            registerSessionId = registerSessionDAO.openSession(cashier.getMatricule());
            registerHeartbeat = new Timer(10_000, event -> registerSessionDAO.heartbeat(registerSessionId));
            registerHeartbeat.start();
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent event) {
                    registerHeartbeat.stop();
                    registerSessionDAO.closeSession(registerSessionId);
                }
            });
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Cash register status",
                    JOptionPane.WARNING_MESSAGE);
        }
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
        JButton validateButton = new JButton("Complete Sale");
        JButton cancelButton = new JButton("Cancel Sale");
        JButton closeButton = new JButton("Close");
        validateButton.addActionListener(event -> validateSale());
        cancelButton.addActionListener(event -> cancelSale());
        closeButton.addActionListener(event -> dispose());

        JPanel page = StyleManager.createPage();
        page.add(StyleManager.createTitle("Register"), BorderLayout.NORTH);
        page.add(buildEntryPanel(), BorderLayout.WEST);
        page.add(buildBasketPanel(), BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout());
        if (accountUsername != null && !accountUsername.isBlank()) {
            JButton accountButton = new JButton("Manage my account");
            accountButton.addActionListener(event ->
                new AccountManagementView(this, accountUsername).setVisible(true));
            footer.add(accountButton, BorderLayout.WEST);
        }
        footer.add(StyleManager.createButtonBar(validateButton, cancelButton, closeButton), BorderLayout.EAST);
        page.add(footer, BorderLayout.SOUTH);
        setContentPane(page);
    }

    private JPanel buildEntryPanel() {
        // Client, produit, quantité
        JPanel entry = StyleManager.createForm("Customer and item");
        StyleManager.addRow(entry, 0, "Customer:", createCustomerControl());
        StyleManager.addRow(entry, 1, "Product:", productCombo);
        StyleManager.addRow(entry, 2, "Quantity:", quantitySpinner);

        JButton addButton = new JButton("Add to cart");
        addButton.addActionListener(event -> addToBasket());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(8, 4, 4, 4);
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        entry.add(addButton, constraints);

        // Mode de paiement
        ButtonGroup paymentGroup = new ButtonGroup();
        paymentGroup.add(cashRadio);
        paymentGroup.add(cardRadio);
        paymentGroup.add(mobileRadio);
        JPanel payment = new JPanel(new GridLayout(0, 1, 0, 4));
        payment.setBorder(BorderFactory.createTitledBorder("Payment method"));
        payment.add(cashRadio);
        payment.add(cardRadio);
        payment.add(mobileRadio);

        JPanel stack = new JPanel(new BorderLayout(0, 10));
        stack.add(entry, BorderLayout.NORTH);
        stack.add(payment, BorderLayout.CENTER);

        JPanel left = new JPanel(new BorderLayout());
        left.add(stack, BorderLayout.NORTH);
        return left;
    }

    private JPanel createCustomerControl() {
        JPanel control = new JPanel(new BorderLayout(6, 0));
        JButton newCustomerButton = new JButton("New");
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
        StyleManager.addRow(form, 0, "Customer ID:", idField);
        StyleManager.addRow(form, 1, "Name:", nameField);
        StyleManager.addRow(form, 2, "Phone:", phoneField);

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

    private void showCustomerError(String message) {
        JOptionPane.showMessageDialog(this, message, "Customer Error", JOptionPane.ERROR_MESSAGE);
    }

    private JPanel buildBasketPanel() {
        JButton removeButton = new JButton("Remove selected item");
        removeButton.addActionListener(event -> removeSelectedItem());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.add(removeButton);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.add(totalLabel);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Cart"),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(basketTable), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
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
        Sale sale = new Sale(0, LocalDateTime.now(), cashier);
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
        JLabel changeAmountLabel = new JLabel("Change: -- $");
        JPanel cashDialog = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        cashDialog.add(StyleManager.createBoldLabel("Amount due: " + formatMoney(currentTotal())), constraints);
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        cashDialog.add(new JLabel("Cash received ($):"), constraints);
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
            changeAmountLabel.setText("Change: -- $");
            changeAmountLabel.setForeground(null);
            return;
        }
        double difference = received - currentTotal();
        changeAmountLabel.setText(difference >= 0
                ? "Change: " + formatMoney(difference)
                : "Still due: " + formatMoney(-difference));
        changeAmountLabel.setForeground(difference >= 0 ? StyleManager.SUCCESS : StyleManager.DANGER);
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
        totalLabel.setText("Total due: " + formatMoney(total));
    }

    private String formatMoney(double amount) {
        return String.format("%.0f $", amount);
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
        StyleManager.applyLookAndFeel();
        javax.swing.SwingUtilities.invokeLater(() -> new SaleView().setVisible(true));
    }
}
