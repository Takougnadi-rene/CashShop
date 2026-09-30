package com.cash_shop.supplier;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import com.cash_shop.common.StyleManager;
import com.cash_shop.employee.Employee.Role;
import com.cash_shop.product.Product;
import com.cash_shop.product.ProductDAO;

public class SupplierView extends JFrame {
    private final Role role;
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final List<Supplier> suppliers = new ArrayList<>();
    private final List<SupplierOrder> orders = new ArrayList<>();
    private final List<Product> orderLines = new ArrayList<>();
    private final DefaultTableModel supplierModel = StyleManager.createReadOnlyModel(
            "Code", "Supplier", "Phone", "Address");
    private final DefaultTableModel orderModel = StyleManager.createReadOnlyModel(
            "Order #", "Date", "Supplier", "Status", "Total (USD)");
    private final DefaultTableModel lineModel = StyleManager.createReadOnlyModel(
            "Reference", "Product", "Quantity", "Purchase Price (USD)", "Line Total (USD)");
    private final JTable supplierTable = StyleManager.createTable(supplierModel);
    private final JTable orderTable = StyleManager.createTable(orderModel);
    private final JTable lineTable = StyleManager.createTable(lineModel);
    private final JButton addSupplierButton = new JButton("Add supplier");
    private final JButton deleteSupplierButton = new JButton("Delete supplier");
    private final JButton newOrderButton = new JButton("New order");
    private final JButton addProductButton = new JButton("Add product");
    private final JButton removeProductButton = new JButton("Remove product");
    private final JButton approveOrderButton = new JButton("Approve order");
    private final JButton refuseOrderButton = new JButton("Refuse order");
    private final JButton deliveredOrderButton = new JButton("Mark delivered");
    private final Timer refreshTimer = new Timer(5000, event -> refreshSuppliers(false));

    public SupplierView() {
        this(Role.ADMIN);
    }

    public SupplierView(Role role) {
        this.role = role;
        setTitle("Supplier Management");
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
        setMinimumSize(new java.awt.Dimension(Toolkit.getDefaultToolkit().getScreenSize()));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        updateActionState();
        try {
            supplierDAO.initializeSchema();
            refreshSuppliers();
            refreshTimer.start();
        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                refreshTimer.stop();
            }
        });
    }

    private void buildUI() {
        JPanel supplierPanel = new JPanel(new BorderLayout(0, 8));
        supplierPanel.setBorder(BorderFactory.createTitledBorder("Suppliers"));
        supplierPanel.add(new JScrollPane(supplierTable), BorderLayout.CENTER);
        addSupplierButton.addActionListener(event -> addSupplier());
        deleteSupplierButton.addActionListener(event -> deleteSupplier());
        addSupplierButton.setVisible(canManageSuppliers());
        deleteSupplierButton.setVisible(canManageSuppliers());
        if (canManageSuppliers()) {
            supplierPanel.add(StyleManager.createButtonBar(addSupplierButton, deleteSupplierButton), BorderLayout.SOUTH);
        }

        JPanel ordersPanel = new JPanel(new BorderLayout(0, 8));
        ordersPanel.setBorder(BorderFactory.createTitledBorder("Orders"));
        JPanel orderListPanel = new JPanel(new BorderLayout());
        orderListPanel.setBorder(BorderFactory.createTitledBorder("Supplier orders"));
        orderListPanel.add(new JScrollPane(orderTable), BorderLayout.CENTER);
        JPanel orderLinesPanel = new JPanel(new BorderLayout());
        orderLinesPanel.setBorder(BorderFactory.createTitledBorder("Products in selected order"));
        orderLinesPanel.add(new JScrollPane(lineTable), BorderLayout.CENTER);
        JSplitPane orderDetails = new JSplitPane(JSplitPane.VERTICAL_SPLIT, orderListPanel, orderLinesPanel);
        orderDetails.setResizeWeight(0.52);
        orderDetails.setBorder(null);
        ordersPanel.add(orderDetails, BorderLayout.CENTER);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, supplierPanel, ordersPanel);
        mainSplit.setResizeWeight(0.28);
        mainSplit.setDividerLocation(300);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> refreshSuppliers());
        newOrderButton.addActionListener(event -> createOrder());
        addProductButton.addActionListener(event -> addProductToOrder());
        removeProductButton.addActionListener(event -> removeProductFromOrder());
        approveOrderButton.addActionListener(event -> reviewOrder(SupplierOrder.OrderStatus.APPROUVED));
        refuseOrderButton.addActionListener(event -> reviewOrder(SupplierOrder.OrderStatus.DENIDED));
        deliveredOrderButton.addActionListener(event -> markOrderDelivered());

        List<JButton> availableActions = new ArrayList<>();
        if (canManageOrders()) {
            availableActions.add(newOrderButton);
            availableActions.add(addProductButton);
            availableActions.add(removeProductButton);
            availableActions.add(deliveredOrderButton);
        }
        if (canReviewOrders()) {
            availableActions.add(approveOrderButton);
            availableActions.add(refuseOrderButton);
        }
        availableActions.add(refreshButton);
        JPanel orderActions = StyleManager.createButtonBar(availableActions.toArray(JButton[]::new));
        JPanel page = StyleManager.createPage();
        page.add(StyleManager.createTitle("Suppliers and Orders"), BorderLayout.NORTH);
        page.add(mainSplit, BorderLayout.CENTER);
        page.add(orderActions, BorderLayout.SOUTH);
        setContentPane(page);

        supplierTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                refreshOrders();
            }
        });
        orderTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                refreshOrderLines();
            }
        });
        lineTable.getSelectionModel().addListSelectionListener(event -> updateActionState());
    }

    private void refreshSuppliers() {
        refreshSuppliers(true);
    }

    private void refreshSuppliers(boolean showError) {
        try {
            Supplier previousSelection = getSelectedSupplier();
            suppliers.clear();
            suppliers.addAll(supplierDAO.getSuppliers());
            supplierModel.setRowCount(0);
            for (Supplier supplier : suppliers) {
                supplierModel.addRow(new Object[] { supplier.getCode(), supplier.getName(),
                        supplier.getTelephone(), supplier.getAddress() });
            }
            orders.clear();
            orderLines.clear();
            orderModel.setRowCount(0);
            lineModel.setRowCount(0);
            if (!suppliers.isEmpty()) {
                int selectedIndex = 0;
                if (previousSelection != null) {
                    for (int index = 0; index < suppliers.size(); index++) {
                        if (suppliers.get(index).getCode() == previousSelection.getCode()) {
                            selectedIndex = index;
                            break;
                        }
                    }
                }
                supplierTable.setRowSelectionInterval(selectedIndex, selectedIndex);
            } else {
                updateActionState();
            }
        } catch (IllegalStateException exception) {
            if (showError) {
                showError(exception.getMessage());
            }
        }
    }

    private void refreshOrders() {
        SupplierOrder previousOrder = getSelectedOrder();
        int supplierIndex = supplierTable.getSelectedRow();
        orders.clear();
        orderLines.clear();
        orderModel.setRowCount(0);
        lineModel.setRowCount(0);
        if (supplierIndex < 0) {
            updateActionState();
            return;
        }
        Supplier selectedSupplier = suppliers.get(supplierTable.convertRowIndexToModel(supplierIndex));
        try {
            orders.addAll(supplierDAO.getOrders(selectedSupplier.getCode()));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            for (SupplierOrder order : orders) {
                orderModel.addRow(new Object[] { order.getOrderNumber(), dateFormat.format(order.getOrderDate()),
                        order.getSupplierName(), formatStatus(order.getStatus()), formatMoney(order.getTotalAmount()) });
            }
            if (!orders.isEmpty()) {
                int selectedIndex = 0;
                if (previousOrder != null) {
                    for (int index = 0; index < orders.size(); index++) {
                        if (orders.get(index).getOrderNumber() == previousOrder.getOrderNumber()) {
                            selectedIndex = index;
                            break;
                        }
                    }
                }
                orderTable.setRowSelectionInterval(selectedIndex, selectedIndex);
            } else {
                updateActionState();
            }
        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void refreshOrderLines() {
        orderLines.clear();
        lineModel.setRowCount(0);
        int orderIndex = orderTable.getSelectedRow();
        if (orderIndex < 0) {
            updateActionState();
            return;
        }
        SupplierOrder selectedOrder = orders.get(orderTable.convertRowIndexToModel(orderIndex));
        try {
            orderLines.addAll(supplierDAO.getOrderLines(selectedOrder.getOrderNumber()));
            for (Product line : orderLines) {
                BigDecimal unitPrice = BigDecimal.valueOf(line.getPurchasePrice());
                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(line.getStockQuantity()));
                lineModel.addRow(new Object[] { line.getReference(), line.getDesignation(),
                        line.getStockQuantity(), formatMoney(unitPrice), formatMoney(lineTotal) });
            }
            updateActionState();
        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void addSupplier() {
        if (!canManageSuppliers()) {
            return;
        }
        JTextField codeField = StyleManager.createField();
        JTextField nameField = StyleManager.createField();
        JTextField phoneField = StyleManager.createField();
        JTextField addressField = StyleManager.createField();
        JPanel form = StyleManager.createForm("Supplier details");
        StyleManager.addRow(form, 0, "Code:", codeField);
        StyleManager.addRow(form, 1, "Name:", nameField);
        StyleManager.addRow(form, 2, "Phone:", phoneField);
        StyleManager.addRow(form, 3, "Address:", addressField);
        if (JOptionPane.showConfirmDialog(this, form, "Add supplier", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            int code = Integer.parseInt(codeField.getText().trim());
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();
            if (code <= 0 || name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                throw new IllegalArgumentException("Enter a positive code and complete all supplier details.");
            }
            supplierDAO.addSupplier(new Supplier(code, name, phone, address));
            refreshSuppliers();
        } catch (NumberFormatException exception) {
            showError("Supplier code must be numeric.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void deleteSupplier() {
        if (!canManageSuppliers()) {
            return;
        }
        Supplier supplier = getSelectedSupplier();
        if (supplier == null) {
            showError("Select a supplier first.");
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Delete supplier " + supplier.getName() + "? Suppliers with orders cannot be deleted.",
                "Confirm deletion", JOptionPane.YES_NO_OPTION);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            supplierDAO.deleteSupplier(supplier.getCode());
            refreshSuppliers();
        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void createOrder() {
        if (!canCreateOrders()) {
            return;
        }
        Supplier supplier = getSelectedSupplier();
        if (supplier == null) {
            showError("Select a supplier first.");
            return;
        }
        try {
            int orderNumber = supplierDAO.createOrder(supplier.getCode(), role);
            refreshOrders();
            for (int index = 0; index < orders.size(); index++) {
                if (orders.get(index).getOrderNumber() == orderNumber) {
                    orderTable.setRowSelectionInterval(index, index);
                    break;
                }
            }
        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void addProductToOrder() {
        if (!canCreateOrders()) {
            return;
        }
        SupplierOrder order = getSelectedOrder();
        if (order == null || order.getStatus() != SupplierOrder.OrderStatus.PENDING) {
            showError("Select a pending order first.");
            return;
        }
        try {
            List<Product> products = productDAO.getAllProducts();
            if (products.isEmpty()) {
                showError("There are no products to order.");
                return;
            }
            JComboBox<Product> productSelector = new JComboBox<>(products.toArray(Product[]::new));
            productSelector.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(
                            list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Product product) {
                        label.setText(product.getReference() + " - " + product.getDesignation()
                                + " (" + formatMoney(BigDecimal.valueOf(product.getPurchasePrice())) + ")");
                    }
                    return label;
                }
            });
            JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));
            JLabel totalLabel = new JLabel();
            totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            Runnable updateTotal = () -> {
                Product product = (Product) productSelector.getSelectedItem();
                int quantity = (Integer) quantitySpinner.getValue();
                BigDecimal total = product == null ? BigDecimal.ZERO
                        : BigDecimal.valueOf(product.getPurchasePrice()).multiply(BigDecimal.valueOf(quantity));
                totalLabel.setText("Line total: " + formatMoney(total));
            };
            productSelector.addActionListener(event -> updateTotal.run());
            quantitySpinner.addChangeListener(event -> updateTotal.run());
            updateTotal.run();
            JPanel form = StyleManager.createForm("Order item");
            StyleManager.addRow(form, 0, "Product:", productSelector);
            StyleManager.addRow(form, 1, "Quantity:", quantitySpinner);
            StyleManager.addRow(form, 2, "Calculated total:", totalLabel);
            if (JOptionPane.showConfirmDialog(this, form, "Add product to order", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
                return;
            }
            Product product = (Product) productSelector.getSelectedItem();
                supplierDAO.addOrderLine(order.getOrderNumber(), product.getReference(),
                    (Integer) quantitySpinner.getValue(), role);
            refreshOrders();
            selectOrder(order.getOrderNumber());
        } catch (IllegalStateException | IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    private void removeProductFromOrder() {
        if (!canCreateOrders()) {
            return;
        }
        SupplierOrder order = getSelectedOrder();
        int lineIndex = lineTable.getSelectedRow();
        if (order == null || lineIndex < 0) {
            showError("Select an order item first.");
            return;
        }
        if (order.getStatus() != SupplierOrder.OrderStatus.PENDING) {
            showError("Only items from pending orders can be removed.");
            return;
        }
        Product line = orderLines.get(lineTable.convertRowIndexToModel(lineIndex));
        try {
            supplierDAO.removeOrderLine(order.getOrderNumber(), line.getReference(), role);
            refreshOrders();
            selectOrder(order.getOrderNumber());
        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void reviewOrder(SupplierOrder.OrderStatus status) {
        if (!canReviewOrders()) {
            return;
        }
        SupplierOrder order = getSelectedOrder();
        if (order == null || order.getStatus() != SupplierOrder.OrderStatus.PENDING) {
            showError("Select a pending order first.");
            return;
        }
        String action = status == SupplierOrder.OrderStatus.APPROUVED ? "approve" : "refuse";
        int confirmation = JOptionPane.showConfirmDialog(this, "Do you want to " + action + " this order?",
                "Review order", JOptionPane.YES_NO_OPTION);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            supplierDAO.updateOrderStatus(order.getOrderNumber(), status, role);
            refreshOrders();
            selectOrder(order.getOrderNumber());
        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void markOrderDelivered() {
        if (!canMarkDelivered()) {
            return;
        }
        SupplierOrder order = getSelectedOrder();
        if (order == null || order.getStatus() != SupplierOrder.OrderStatus.APPROUVED) {
            showError("Select an approved order first.");
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Confirm receipt of this order and add its quantities to stock?", "Mark order delivered",
                JOptionPane.YES_NO_OPTION);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            supplierDAO.updateOrderStatus(order.getOrderNumber(), SupplierOrder.OrderStatus.DELIVERED, role);
            refreshOrders();
            selectOrder(order.getOrderNumber());
        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void selectOrder(int orderNumber) {
        for (int index = 0; index < orders.size(); index++) {
            if (orders.get(index).getOrderNumber() == orderNumber) {
                orderTable.setRowSelectionInterval(index, index);
                return;
            }
        }
    }

    private void updateActionState() {
        SupplierOrder order = getSelectedOrder();
        boolean hasSupplier = getSelectedSupplier() != null;
        boolean pending = order != null && order.getStatus() == SupplierOrder.OrderStatus.PENDING;
        boolean approved = order != null && order.getStatus() == SupplierOrder.OrderStatus.APPROUVED;
        addSupplierButton.setEnabled(canManageSuppliers());
        deleteSupplierButton.setEnabled(canManageSuppliers() && hasSupplier);
        newOrderButton.setEnabled(canCreateOrders() && hasSupplier);
        addProductButton.setEnabled(canCreateOrders() && pending);
        removeProductButton.setEnabled(canCreateOrders() && pending && lineTable.getSelectedRow() >= 0);
        approveOrderButton.setEnabled(canReviewOrders() && pending);
        refuseOrderButton.setEnabled(canReviewOrders() && pending);
        deliveredOrderButton.setEnabled(canMarkDelivered() && approved);
    }

    private boolean canManageSuppliers() {
        return false;
    }

    private boolean canCreateOrders() {
        return canManageOrders();
    }

    private boolean canReviewOrders() {
        return role == Role.COUNTER;
    }

    private boolean canMarkDelivered() {
        return canManageOrders();
    }

    private boolean canManageOrders() {
        return role == Role.MANAGER;
    }

    private String formatStatus(SupplierOrder.OrderStatus status) {
        return switch (status) {
            case PENDING -> "Pending";
            case APPROUVED -> "Approved";
            case DENIDED -> "Refused";
            case DELIVERED -> "Delivered";
        };
    }

    private Supplier getSelectedSupplier() {
        int row = supplierTable.getSelectedRow();
        return row < 0 ? null : suppliers.get(supplierTable.convertRowIndexToModel(row));
    }

    private SupplierOrder getSelectedOrder() {
        int row = orderTable.getSelectedRow();
        return row < 0 ? null : orders.get(orderTable.convertRowIndexToModel(row));
    }

    private String formatMoney(BigDecimal amount) {
        return amount == null ? "0.00 USD" : String.format("%.2f USD", amount);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Supplier management", JOptionPane.ERROR_MESSAGE);
    }
}