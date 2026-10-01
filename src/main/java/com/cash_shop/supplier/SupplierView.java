package com.cash_shop.supplier;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

import com.cash_shop.common.StyleManager;
import com.cash_shop.employee.Employee.Role;
import com.cash_shop.product.Product;
import com.cash_shop.product.ProductDAO;

/**
 * Swing window to manage suppliers and their purchase orders. Buttons are shown according to the role:
 * administrators and managers manage suppliers, managers create/edit/deliver orders and counter staff approve
 * or refuse them. Database calls run in background workers to keep the UI responsive.
 */
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
    private final JCheckBox showAllOrdersCheckbox = new JCheckBox("All suppliers");
    private final JButton addSupplierButton = new JButton("Add supplier");
    private final JButton deleteSupplierButton = new JButton("Delete supplier");
    private final JButton newOrderButton = new JButton("New order");
    private final JButton addProductButton = new JButton("Add product");
    private final JButton removeProductButton = new JButton("Remove product");
    private final JButton approveOrderButton = new JButton("Approve order");
    private final JButton refuseOrderButton = new JButton("Refuse order");
    private final JButton deliveredOrderButton = new JButton("Mark delivered");
    private final JLabel actionStatusLabel = StyleManager.createLabel(" ");
    private final Timer refreshTimer = new Timer(30000, event -> refreshSuppliers(false));
    // True while a background action is running: tables and buttons are disabled.
    private boolean orderActionBusy;
    private boolean updatingOrderSelection;
    // Sequence numbers let a slow background load notice that a newer one was started, and drop its outdated
    // result.
    private int ordersLoadSequence;
    private int orderLinesLoadSequence;

    /** Opens the window with the manager role. */
    public SupplierView() {
        this(Role.MANAGER);
    }

    /** Opens the window for the given role. */
    public SupplierView(Role role) {
        this.role = role;
        setTitle("Supplier Management");
        setSize(1280, 800);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        updateActionState();
        loadInitialSuppliers();
        addWindowListener(new WindowAdapter() {
            // Stop the periodic refresh once the window is closed.
            @Override
            public void windowClosed(WindowEvent event) {
                refreshTimer.stop();
            }
        });
    }

    /** Initializes the schema and loads the suppliers in the background, then loads their orders. */
    private void loadInitialSuppliers() {
        new SwingWorker<List<Supplier>, Void>() {
            // Runs off the Event Dispatch Thread.
            @Override
            protected List<Supplier> doInBackground() {
                supplierDAO.initializeSchema();
                return supplierDAO.getSuppliers();
            }

            // Back on the Event Dispatch Thread: update the UI with the result.
            @Override
            protected void done() {
                try {
                    suppliers.clear();
                    suppliers.addAll(get());
                    supplierModel.setRowCount(0);
                    for (Supplier supplier : suppliers) {
                        supplierModel.addRow(new Object[] { supplier.getCode(), supplier.getName(),
                                supplier.getTelephone(), supplier.getAddress() });
                    }
                    if (!suppliers.isEmpty()) {
                        supplierTable.setRowSelectionInterval(0, 0);
                    }
                    refreshOrders();
                    refreshTimer.start();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("Supplier data loading was interrupted.");
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    showError(cause == null ? exception.getMessage() : cause.getMessage());
                }
            }
        }.execute();
    }

    /** Builds the suppliers table, the orders table, the order lines table and the role-dependent buttons. */
    private void buildUI() {
        // ---- Suppliers panel (left) ----
        JPanel supplierPanel = buildSectionPanel("Suppliers", StyleManager.ACCENT_PRIMARY, null);
        supplierPanel.add(StyleManager.createScrollPane(supplierTable), BorderLayout.CENTER);
        supplierPanel.setMinimumSize(new Dimension(280, 200));
        addSupplierButton.addActionListener(event -> addSupplier());
        deleteSupplierButton.addActionListener(event -> deleteSupplier());
        if (canManageSuppliers()) {
            supplierPanel.add(StyleManager.createButtonBar(addSupplierButton, deleteSupplierButton), BorderLayout.SOUTH);
        }

        // ---- Orders panel (right) ----
        showAllOrdersCheckbox.setOpaque(false);
        showAllOrdersCheckbox.setForeground(StyleManager.TEXT_SECONDARY);
        showAllOrdersCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showAllOrdersCheckbox.setFocusPainted(false);
        showAllOrdersCheckbox.setToolTipText("Check to display orders from all suppliers");
        showAllOrdersCheckbox.addActionListener(event -> refreshOrders());

        JPanel orderListPanel = buildSectionPanel("Supplier Orders", StyleManager.ACCENT_TEAL, showAllOrdersCheckbox);
        orderListPanel.add(StyleManager.createScrollPane(orderTable), BorderLayout.CENTER);
        orderListPanel.setMinimumSize(new Dimension(350, 150));

        JPanel orderLinesPanel = buildSectionPanel("Products in Selected Order", new Color(0x8A, 0x63, 0xFF), null);
        orderLinesPanel.add(StyleManager.createScrollPane(lineTable), BorderLayout.CENTER);
        orderLinesPanel.setMinimumSize(new Dimension(350, 150));

        JSplitPane orderDetails = new JSplitPane(JSplitPane.VERTICAL_SPLIT, orderListPanel, orderLinesPanel);
        orderDetails.setResizeWeight(0.52);
        orderDetails.setBorder(null);
        orderDetails.setDividerSize(6);
        orderDetails.setBackground(StyleManager.BG_SURFACE);

        JPanel ordersPanel = new JPanel(new BorderLayout(0, 0));
        ordersPanel.setOpaque(false);
        ordersPanel.setMinimumSize(new Dimension(450, 300));
        ordersPanel.add(orderDetails, BorderLayout.CENTER);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, supplierPanel, ordersPanel);
        mainSplit.setResizeWeight(0.28);
        mainSplit.setDividerSize(6);
        mainSplit.setBorder(null);
        mainSplit.setBackground(StyleManager.BG_SURFACE);

        SwingUtilities.invokeLater(() -> {
            mainSplit.setDividerLocation(0.28);
            orderDetails.setDividerLocation(0.52);
        });

        // ---- Actions ----
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> refreshSuppliers());
        newOrderButton.addActionListener(event -> createOrder());
        addProductButton.addActionListener(event -> addProductToOrder());
        removeProductButton.addActionListener(event -> removeProductFromOrder());
        approveOrderButton.addActionListener(event -> reviewOrder(SupplierOrder.OrderStatus.APPROUVED));
        refuseOrderButton.addActionListener(event -> reviewOrder(SupplierOrder.OrderStatus.DENIDED));
        deliveredOrderButton.addActionListener(event -> markOrderDelivered());

        List<JButton> availableActions = new ArrayList<>();
        if (canCreateOrders()) {
            availableActions.add(newOrderButton);
            availableActions.add(addProductButton);
            availableActions.add(removeProductButton);
        }
        if (canMarkDelivered()) {
            availableActions.add(deliveredOrderButton);
        }
        if (canReviewOrders()) {
            availableActions.add(approveOrderButton);
            availableActions.add(refuseOrderButton);
        }
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(event -> dispose());
        availableActions.add(refreshButton);
        availableActions.add(closeButton);
        JPanel orderActions = StyleManager.createButtonBar(availableActions.toArray(JButton[]::new));

        JPanel page = StyleManager.createPage();
        JPanel heading = new JPanel(new BorderLayout(0, 4));
        heading.setOpaque(false);
        heading.add(StyleManager.createTitle("Suppliers & Orders"), BorderLayout.NORTH);
        JLabel subtitle = StyleManager.createLabel("Role: " + role.name()
            + "  |  Manage supplier records, purchase orders, and incoming stock.");
        heading.add(subtitle, BorderLayout.CENTER);
        actionStatusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        heading.add(actionStatusLabel, BorderLayout.EAST);
        page.add(heading, BorderLayout.NORTH);
        page.add(mainSplit, BorderLayout.CENTER);
        page.add(orderActions, BorderLayout.SOUTH);
        setContentPane(page);

        supplierTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                refreshOrders();
            }
        });
        orderTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && !updatingOrderSelection) {
                refreshOrderLines();
            }
        });
        lineTable.getSelectionModel().addListSelectionListener(event -> updateActionState());
    }

    /**
     * Creates a rounded section panel with a colored top bar, a title and an optional component in the header.
     */
    private JPanel buildSectionPanel(String title, Color accent) {
        return buildSectionPanel(title, accent, null);
    }

    /** Creates a styled section panel with a colored bar on top. */
    private JPanel buildSectionPanel(String title, Color accent, JComponent extraComponent) {
        JPanel panel = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(StyleManager.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(StyleManager.BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(20, 2, getWidth() - 20, 2);
                g2.dispose();
            }
        };
        panel.setOpaque(false);

        // Section header
        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLabel.setForeground(accent);
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(10, 12, 4, 12));
        header.add(titleLabel, BorderLayout.WEST);
        if (extraComponent != null) {
            header.add(extraComponent, BorderLayout.EAST);
        }

        panel.add(header, BorderLayout.NORTH);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return panel;
    }

    /** Reloads suppliers and orders. */
    private void refreshSuppliers() {
        refreshSuppliers(true);
    }

    /** Reloads suppliers and orders, keeping the selection (silent for the periodic refresh). */
    private void refreshSuppliers(boolean showError) {
        if (orderActionBusy) {
            return;
        }
        try {
            Supplier previousSelection = getSelectedSupplier();
            List<Supplier> loaded = supplierDAO.getSuppliers();
            suppliers.clear();
            suppliers.addAll(loaded);
            supplierModel.setRowCount(0);
            for (Supplier supplier : suppliers) {
                supplierModel.addRow(new Object[] { supplier.getCode(), supplier.getName(),
                        supplier.getTelephone(), supplier.getAddress() });
            }
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
            }
            refreshOrders();
        } catch (IllegalStateException exception) {
            if (showError) {
                showError(exception.getMessage());
            }
        }
    }

    /**
     * Loads, in the background, the orders of the selected supplier (or of all suppliers); outdated results
     * are ignored.
     */
    private void refreshOrders() {
        SupplierOrder previousOrder = getSelectedOrder();
        int previousOrderNumber = previousOrder == null ? -1 : previousOrder.getOrderNumber();
        int supplierIndex = supplierTable.getSelectedRow();
        int loadSequence = ++ordersLoadSequence;
        boolean allOrders = showAllOrdersCheckbox.isSelected() || supplierIndex < 0;

        final int targetSupplierCode;
        final String supplierTitle;
        if (allOrders) {
            targetSupplierCode = 0;
            supplierTitle = "all suppliers";
        } else {
            Supplier selectedSupplier = suppliers.get(supplierTable.convertRowIndexToModel(supplierIndex));
            targetSupplierCode = selectedSupplier.getCode();
            supplierTitle = selectedSupplier.getName();
        }

        actionStatusLabel.setText("Loading orders for " + supplierTitle + "...");
        new SwingWorker<List<SupplierOrder>, Void>() {
            // Runs off the Event Dispatch Thread.
            @Override
            protected List<SupplierOrder> doInBackground() {
                return supplierDAO.getOrders(targetSupplierCode);
            }

            // Back on the Event Dispatch Thread: update the UI with the result.
            @Override
            protected void done() {
                if (loadSequence != ordersLoadSequence) {
                    return;
                }
                try {
                    List<SupplierOrder> loadedOrders = get();
                    orders.clear();
                    orders.addAll(loadedOrders);
                    updatingOrderSelection = true;
                    try {
                        orderModel.setRowCount(0);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        for (SupplierOrder order : orders) {
                            String dateStr = order.getOrderDate() != null
                                    ? dateFormat.format(order.getOrderDate()) : "-";
                            orderModel.addRow(new Object[] {
                                    order.getOrderNumber(),
                                    dateStr,
                                    order.getSupplierName(),
                                    formatStatus(order.getStatus()),
                                    formatMoney(order.getTotalAmount())
                            });
                        }
                    } finally {
                        updatingOrderSelection = false;
                    }
                    if (orders.isEmpty()) {
                        orderLines.clear();
                        lineModel.setRowCount(0);
                        actionStatusLabel.setText("No orders for " + supplierTitle + ".");
                        updateActionState();
                        return;
                    }
                    int selectedIndex = 0;
                    for (int index = 0; index < orders.size(); index++) {
                        if (orders.get(index).getOrderNumber() == previousOrderNumber) {
                            selectedIndex = index;
                            break;
                        }
                    }
                    orderTable.setRowSelectionInterval(selectedIndex, selectedIndex);
                    refreshOrderLines();
                    actionStatusLabel.setText(orders.size() + " order(s) for " + supplierTitle);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    actionStatusLabel.setText("Unable to load orders.");
                } catch (ExecutionException exception) {
                    actionStatusLabel.setText("Unable to load orders.");
                    Throwable cause = exception.getCause();
                    showError(cause == null ? exception.getMessage() : cause.getMessage());
                }
            }
        }.execute();
    }

    /** Loads, in the background, the lines of the selected order; outdated results are ignored. */
    private void refreshOrderLines() {
        int loadSequence = ++orderLinesLoadSequence;
        int orderIndex = orderTable.getSelectedRow();
        if (orderIndex < 0 || orderIndex >= orders.size()) {
            orderLines.clear();
            lineModel.setRowCount(0);
            lineTable.setEnabled(!orderActionBusy);
            actionStatusLabel.setText(" ");
            updateActionState();
            return;
        }
        SupplierOrder selectedOrder = orders.get(orderTable.convertRowIndexToModel(orderIndex));
        final int targetOrderNumber = selectedOrder.getOrderNumber();

        actionStatusLabel.setText("Loading products for order #" + targetOrderNumber + "...");
        lineTable.setEnabled(false);
        new SwingWorker<List<Product>, Void>() {
            // Runs off the Event Dispatch Thread.
            @Override
            protected List<Product> doInBackground() {
                return supplierDAO.getOrderLines(targetOrderNumber);
            }

            // Back on the Event Dispatch Thread: update the UI with the result.
            @Override
            protected void done() {
                if (loadSequence != orderLinesLoadSequence) {
                    return;
                }
                try {
                    List<Product> lines = get();
                    orderLines.clear();
                    orderLines.addAll(lines);
                    lineModel.setRowCount(0);
                    addOrderLinesToTable(orderLines);
                    actionStatusLabel.setText(orderLines.isEmpty()
                            ? "No products in order #" + targetOrderNumber + "."
                            : orderLines.size() + " product(s) in order #" + targetOrderNumber);
                    lineTable.setEnabled(!orderActionBusy);
                    updateActionState();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    actionStatusLabel.setText(" ");
                    lineTable.setEnabled(!orderActionBusy);
                } catch (ExecutionException exception) {
                    actionStatusLabel.setText(" ");
                    lineTable.setEnabled(!orderActionBusy);
                    Throwable cause = exception.getCause();
                    showError(cause == null ? exception.getMessage() : cause.getMessage());
                }
            }
        }.execute();
    }

    /** Fills the lines table. */
    private void addOrderLinesToTable(List<Product> lines) {
        for (Product line : lines) {
            BigDecimal unitPrice = BigDecimal.valueOf(line.getPurchasePrice());
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(line.getStockQuantity()));
            lineModel.addRow(new Object[] { line.getReference(), line.getDesignation(),
                    line.getStockQuantity(), formatMoney(unitPrice), formatMoney(lineTotal) });
        }
    }

    /** Asks for the supplier details and saves the supplier. */
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

    /** Deletes the selected supplier after confirmation. */
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

    /** Loads the catalog in the background, then opens the new-order dialog. */
    private void createOrder() {
        if (!canCreateOrders()) {
            return;
        }
        Supplier supplier = getSelectedSupplier();
        if (supplier == null) {
            showError("Select a supplier first.");
            return;
        }
        setOrderActionBusy(true, "Loading products catalog...");
        new SwingWorker<List<Product>, Void>() {
            // Runs off the Event Dispatch Thread.
            @Override
            protected List<Product> doInBackground() {
                return productDAO.getAllProducts();
            }

            // Back on the Event Dispatch Thread: update the UI with the result.
            @Override
            protected void done() {
                setOrderActionBusy(false, " ");
                try {
                    List<Product> products = get();
                    if (products.isEmpty()) {
                        showError("There are no products in the catalog to order.");
                        return;
                    }
                    showNewOrderDialog(supplier, products);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("Loading products was interrupted.");
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    showError(cause == null ? exception.getMessage() : cause.getMessage());
                }
            }
        }.execute();
    }

    /**
     * Dialog to build an order: filter the catalog, choose product and quantity, review the lines and the
     * total, then confirm.
     */
    private void showNewOrderDialog(Supplier supplier, List<Product> products) {
        JDialog dialog = new JDialog(this, "New Supplier Order - " + supplier.getName(), true);
        dialog.setSize(860, 680);
        dialog.setMinimumSize(new Dimension(760, 520));
        dialog.setLocationRelativeTo(this);

        List<Product> draftLines = new ArrayList<>();
        DefaultTableModel draftModel = StyleManager.createReadOnlyModel(
                "Reference", "Product Designation", "Unit Price", "Quantity", "Subtotal");
        JTable draftTable = StyleManager.createTable(draftModel);

        // Header info card
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 12, 6));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        infoPanel.add(StyleManager.createLabel("Supplier: " + supplier.getName() + " (Code: #" + supplier.getCode() + ")"));
        infoPanel.add(StyleManager.createLabel("Phone: " + supplier.getTelephone()));
        infoPanel.add(StyleManager.createLabel("Address: " + supplier.getAddress()));
        JLabel draftStatusLabel = StyleManager.createLabel("0 product(s) added");
        draftStatusLabel.setForeground(StyleManager.ACCENT_TEAL);
        infoPanel.add(draftStatusLabel);

        JPanel headerSection = buildSectionPanel("Order Supplier Info", StyleManager.ACCENT_PRIMARY);
        headerSection.add(infoPanel, BorderLayout.CENTER);

        // Product selection & filter bar
        JTextField filterField = StyleManager.createField();
        filterField.setToolTipText("Type to filter products by name or reference");
        DefaultComboBoxModel<Product> comboModel = new DefaultComboBoxModel<>();
        for (Product p : products) {
            comboModel.addElement(p);
        }
        JComboBox<Product> productSelector = new JComboBox<>(comboModel);
        productSelector.setRenderer(createProductSelectorRenderer());

        filterField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            /** Filters the product combo box by name or reference. */
            private void updateFilter() {
                String query = filterField.getText().trim().toLowerCase();
                comboModel.removeAllElements();
                for (Product p : products) {
                    if (query.isEmpty()
                            || p.getDesignation().toLowerCase().contains(query)
                            || String.valueOf(p.getReference()).contains(query)) {
                        comboModel.addElement(p);
                    }
                }
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateFilter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateFilter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateFilter(); }
        });

        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));
        JLabel linePreviewLabel = StyleManager.createLabel(" ");
        linePreviewLabel.setForeground(StyleManager.ACCENT_TEAL);
        linePreviewLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        Runnable updateLinePreview = () -> {
            Product selected = (Product) productSelector.getSelectedItem();
            int qty = (Integer) quantitySpinner.getValue();
            if (selected != null) {
                BigDecimal unitPrice = BigDecimal.valueOf(selected.getPurchasePrice());
                BigDecimal lineTot = unitPrice.multiply(BigDecimal.valueOf(qty));
                linePreviewLabel.setText("Unit: " + formatMoney(unitPrice) + "  |  Line: " + formatMoney(lineTot));
            } else {
                linePreviewLabel.setText(" ");
            }
        };
        productSelector.addActionListener(e -> updateLinePreview.run());
        quantitySpinner.addChangeListener(e -> updateLinePreview.run());
        updateLinePreview.run();

        JButton addProductToDraftButton = new JButton("Add product to order");
        JButton removeDraftProductButton = new JButton("Remove selected product");
        JButton clearDraftButton = new JButton("Clear list");
        removeDraftProductButton.setEnabled(false);
        clearDraftButton.setEnabled(false);

        JLabel totalOrderLabel = new JLabel("Total: 0.00 USD");
        totalOrderLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalOrderLabel.setForeground(StyleManager.ACCENT_TEAL);

        JButton confirmOrderButton = new JButton("Create order");
        confirmOrderButton.setEnabled(false);
        JButton cancelDialogButton = new JButton("Cancel");
        cancelDialogButton.addActionListener(e -> dialog.dispose());

        Runnable refreshDraftTable = () -> {
            draftModel.setRowCount(0);
            BigDecimal grandTotal = BigDecimal.ZERO;
            int totalUnits = 0;
            for (Product item : draftLines) {
                BigDecimal unitPrice = BigDecimal.valueOf(item.getPurchasePrice());
                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getStockQuantity()));
                grandTotal = grandTotal.add(lineTotal);
                totalUnits += item.getStockQuantity();
                draftModel.addRow(new Object[] {
                    item.getReference(),
                    item.getDesignation(),
                    formatMoney(unitPrice),
                    item.getStockQuantity(),
                    formatMoney(lineTotal)
                });
            }
            totalOrderLabel.setText("Total: " + formatMoney(grandTotal));
            draftStatusLabel.setText(draftLines.size() + " product(s) added (" + totalUnits + " total units)");
            boolean hasItems = !draftLines.isEmpty();
            confirmOrderButton.setEnabled(hasItems);
            clearDraftButton.setEnabled(hasItems);
            removeDraftProductButton.setEnabled(draftTable.getSelectedRow() >= 0);
        };

        draftTable.getSelectionModel().addListSelectionListener(e -> {
            removeDraftProductButton.setEnabled(draftTable.getSelectedRow() >= 0);
        });

        addProductToDraftButton.addActionListener(e -> {
            Product selected = (Product) productSelector.getSelectedItem();
            if (selected == null) {
                showError("Select a product to add.");
                return;
            }
            int qty = (Integer) quantitySpinner.getValue();
            if (qty <= 0) {
                showError("Quantity must be greater than zero.");
                return;
            }
            boolean exists = false;
            for (Product line : draftLines) {
                if (line.getReference() == selected.getReference()) {
                    line.setStockQuantity(line.getStockQuantity() + qty);
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                draftLines.add(new Product(selected.getReference(), selected.getDesignation(),
                        selected.getPurchasePrice(), selected.getSellingPrice(), qty));
            }
            quantitySpinner.setValue(1);
            refreshDraftTable.run();
        });

        removeDraftProductButton.addActionListener(e -> {
            int selectedRow = draftTable.getSelectedRow();
            if (selectedRow >= 0 && selectedRow < draftLines.size()) {
                draftLines.remove(draftTable.convertRowIndexToModel(selectedRow));
                refreshDraftTable.run();
            }
        });

        clearDraftButton.addActionListener(e -> {
            draftLines.clear();
            refreshDraftTable.run();
        });

        confirmOrderButton.addActionListener(e -> {
            if (draftLines.isEmpty()) {
                showError("Add at least one product before confirming the order.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Place this order with " + draftLines.size() + " product(s) for supplier " + supplier.getName() + "?",
                    "Confirm Order", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                saveNewOrder(supplier, draftLines, dialog);
            }
        });

        // Form for adding products
        JPanel addForm = StyleManager.createForm("Add products to this order");
        StyleManager.addRow(addForm, 0, "Search catalog:", filterField);
        StyleManager.addRow(addForm, 1, "Select product:", productSelector);
        StyleManager.addRow(addForm, 2, "Order quantity:", quantitySpinner);
        StyleManager.addRow(addForm, 3, "Price preview:", linePreviewLabel);

        JPanel addBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        addBar.setOpaque(false);
        StyleManager.styleButton(addProductToDraftButton);
        addBar.add(addProductToDraftButton);

        JPanel addContainer = new JPanel(new BorderLayout());
        addContainer.setOpaque(false);
        addContainer.add(addForm, BorderLayout.CENTER);
        addContainer.add(addBar, BorderLayout.SOUTH);

        // Table panel for draft items
        JPanel draftTablePanel = buildSectionPanel("Order Items (" + supplier.getName() + ")", StyleManager.ACCENT_TEAL);
        draftTablePanel.add(StyleManager.createScrollPane(draftTable), BorderLayout.CENTER);
        JPanel draftTableButtons = StyleManager.createButtonBar(removeDraftProductButton, clearDraftButton);
        draftTablePanel.add(draftTableButtons, BorderLayout.SOUTH);

        // Center split: top = add product, bottom = draft table
        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, addContainer, draftTablePanel);
        centerSplit.setResizeWeight(0.38);
        centerSplit.setDividerSize(6);
        centerSplit.setBorder(null);
        centerSplit.setBackground(StyleManager.BG_SURFACE);

        // Bottom summary & action bar
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 12, 16));

        JPanel totalBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        totalBox.setOpaque(false);
        totalBox.add(totalOrderLabel);

        JPanel actionBox = StyleManager.createButtonBar(confirmOrderButton, cancelDialogButton);
        footerPanel.add(totalBox, BorderLayout.WEST);
        footerPanel.add(actionBox, BorderLayout.EAST);

        JPanel dialogPage = StyleManager.createPage();
        dialogPage.add(headerSection, BorderLayout.NORTH);
        dialogPage.add(centerSplit, BorderLayout.CENTER);
        dialogPage.add(footerPanel, BorderLayout.SOUTH);

        dialog.setContentPane(dialogPage);
        dialog.setVisible(true);
    }

    /** Saves the order in the background and then displays it. */
    private void saveNewOrder(Supplier supplier, List<Product> draftLines, JDialog dialog) {
        setOrderActionBusy(true, "Saving order with " + draftLines.size() + " product(s)...");
        new SwingWorker<OrderLoad, Void>() {
            // Runs off the Event Dispatch Thread.
            @Override
            protected OrderLoad doInBackground() {
                int orderNumber = supplierDAO.createOrder(supplier.getCode(), draftLines, role);
                return new OrderLoad(orderNumber, supplierDAO.getOrders(supplier.getCode()),
                        supplierDAO.getOrderLines(orderNumber));
            }

            // Back on the Event Dispatch Thread: update the UI with the result.
            @Override
            protected void done() {
                setOrderActionBusy(false, " ");
                try {
                    OrderLoad loadedOrder = get();
                    if (dialog != null) {
                        dialog.dispose();
                    }
                    displayOrderLoad(loadedOrder);
                    actionStatusLabel.setText("Order #" + loadedOrder.orderNumber() + " created with "
                            + draftLines.size() + " product(s).");
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("Saving the order was interrupted.");
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    showError(cause == null ? exception.getMessage() : cause.getMessage());
                }
            }
        }.execute();
    }

    /** Displays the orders and lines returned by a save and selects the new order. */
    private void displayOrderLoad(OrderLoad loadedOrder) {
        orders.clear();
        orders.addAll(loadedOrder.orders());
        orderLines.clear();
        orderLines.addAll(loadedOrder.lines());
        updatingOrderSelection = true;
        try {
            orderTable.clearSelection();
            orderModel.setRowCount(0);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            for (SupplierOrder order : orders) {
                String dateStr = order.getOrderDate() != null
                        ? dateFormat.format(order.getOrderDate()) : "-";
                orderModel.addRow(new Object[] {
                        order.getOrderNumber(),
                        dateStr,
                        order.getSupplierName(),
                        formatStatus(order.getStatus()),
                        formatMoney(order.getTotalAmount())
                });
            }
            lineModel.setRowCount(0);
            addOrderLinesToTable(orderLines);
            for (int index = 0; index < orders.size(); index++) {
                if (orders.get(index).getOrderNumber() == loadedOrder.orderNumber()) {
                    orderTable.setRowSelectionInterval(index, index);
                    break;
                }
            }
        } finally {
            updatingOrderSelection = false;
        }
        updateActionState();
    }

    /** Disables/enables the tables and buttons while a background action is running. */
    private void setOrderActionBusy(boolean busy, String message) {
        orderActionBusy = busy;
        actionStatusLabel.setText(message);
        supplierTable.setEnabled(!busy);
        orderTable.setEnabled(!busy);
        lineTable.setEnabled(!busy);
        updateActionState();
    }

    /** Renderer showing reference, designation, purchase price and stock of a product. */
    private DefaultListCellRenderer createProductSelectorRenderer() {
        return new DefaultListCellRenderer() {
            /** Returns the list cell renderer component. */
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof Product product) {
                    label.setText(product.getReference() + " - " + product.getDesignation()
                            + " (Purchase: " + formatMoney(BigDecimal.valueOf(product.getPurchasePrice()))
                            + ", Stock: " + product.getStockQuantity() + ")");
                }
                return label;
            }
        };
    }

    /** Creates a product combo box. */
    private JComboBox<Product> createProductSelector(List<Product> products) {
        JComboBox<Product> productSelector = new JComboBox<>(products.toArray(Product[]::new));
        productSelector.setRenderer(createProductSelectorRenderer());
        return productSelector;
    }

    /** Adds a product to the selected pending order. */
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
            JComboBox<Product> productSelector = createProductSelector(products);
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

    /** Removes the selected line from the selected pending order. */
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

    /** Approves or refuses the selected pending order after confirmation. */
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

    /** Confirms the delivery of the selected approved order (the stock is increased). */
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

    /** Selects an order in the table by its number. */
    private void selectOrder(int orderNumber) {
        for (int index = 0; index < orders.size(); index++) {
            if (orders.get(index).getOrderNumber() == orderNumber) {
                orderTable.setRowSelectionInterval(index, index);
                return;
            }
        }
    }

    /** Enables or disables the buttons depending on the role, the selection and the order status. */
    private void updateActionState() {
        SupplierOrder order = getSelectedOrder();
        boolean hasSupplier = getSelectedSupplier() != null;
        boolean pending = order != null && order.getStatus() == SupplierOrder.OrderStatus.PENDING;
        boolean approved = order != null && order.getStatus() == SupplierOrder.OrderStatus.APPROUVED;
        addSupplierButton.setEnabled(canManageSuppliers());
        deleteSupplierButton.setEnabled(canManageSuppliers() && hasSupplier);
        newOrderButton.setEnabled(!orderActionBusy && canCreateOrders());
        newOrderButton.setToolTipText(hasSupplier ? "Create a supplier order" : "Select a supplier first");
        addProductButton.setEnabled(!orderActionBusy && canCreateOrders() && pending);
        removeProductButton.setEnabled(!orderActionBusy && canCreateOrders() && pending
            && lineTable.getSelectedRow() >= 0);
        approveOrderButton.setEnabled(!orderActionBusy && canReviewOrders() && pending);
        refuseOrderButton.setEnabled(!orderActionBusy && canReviewOrders() && pending);
        deliveredOrderButton.setEnabled(!orderActionBusy && canMarkDelivered() && approved);
    }

    /** Administrators and managers can add/delete suppliers. */
    private boolean canManageSuppliers() {
        return role == Role.ADMIN || role == Role.MANAGER;
    }

    /** Only managers create and edit orders. */
    private boolean canCreateOrders() {
        return role == Role.MANAGER;
    }

    /** Only counter staff approve or refuse orders. */
    private boolean canReviewOrders() {
        return role == Role.COUNTER;
    }

    /** Only managers confirm deliveries. */
    private boolean canMarkDelivered() {
        return role == Role.MANAGER;
    }

    /** Converts a status to the text displayed in the table. */
    private String formatStatus(SupplierOrder.OrderStatus status) {
        return switch (status) {
            case PENDING -> "Pending";
            case APPROUVED -> "Approved";
            case DENIDED -> "Refused";
            case DELIVERED -> "Delivered";
        };
    }

    /** Returns the selected supplier, or null. */
    private Supplier getSelectedSupplier() {
        int row = supplierTable.getSelectedRow();
        return row < 0 ? null : suppliers.get(supplierTable.convertRowIndexToModel(row));
    }

    /** Returns the selected order, or null. */
    private SupplierOrder getSelectedOrder() {
        int row = orderTable.getSelectedRow();
        return row < 0 ? null : orders.get(orderTable.convertRowIndexToModel(row));
    }

    /** Formats an amount in USD with 2 decimals. */
    private String formatMoney(BigDecimal amount) {
        return amount == null ? "0.00 USD" : String.format("%.2f USD", amount);
    }

    /** Result of saving an order: its number, the orders of the supplier and the lines of the new order. */
    private record OrderLoad(int orderNumber, List<SupplierOrder> orders, List<Product> lines) { }

    /** Displays an error dialog. */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Supplier management", JOptionPane.ERROR_MESSAGE);
    }
}