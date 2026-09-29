package com.cash_shop;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.cash_shop.aisle.AisleView;
import com.cash_shop.common.StyleManager;
import com.cash_shop.customer.CustomerView;
import com.cash_shop.employee.Employee.Role;
import com.cash_shop.employee.EmployeeView;
import com.cash_shop.product.Product;
import com.cash_shop.product.ProductDAO;
import com.cash_shop.product.ProductView;
import com.cash_shop.product.StockView;
import com.cash_shop.sale.SaleView;
import com.cash_shop.supplier.SupplierView;
import com.cash_shop.user.AccessService;
import com.cash_shop.user.AccessService.Module;
import com.cash_shop.user.UserManagementView;
import com.cash_shop.user.UserView;

public class Home extends JFrame {
    private final String username;
    private final String employeeMatricule;
    private final Role role;
    private final AccessService accessService = new AccessService();
    private JLabel dateLabel;
    private JLabel productCountLabel;
    private JLabel lowStockLabel;
    private JLabel stockValueLabel;

    public Home(String username, Role role) {
        this(username, role, "");
    }

    public Home(String username, Role role, String employeeMatricule) {
        this.username = username;
        this.role = role;
        this.employeeMatricule = employeeMatricule;
        setTitle("Supermarket Manager - Home");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildUI();
        refreshOverview();
        new Timer(1000, event -> dateLabel.setText(currentDateTime())).start();
        new Timer(30000, event -> refreshOverview()).start();
    }

    private void buildUI() {
        // En-tête : titre et date
        JPanel header = new JPanel(new BorderLayout());
        header.add(StyleManager.createTitle("Supermarket Manager"), BorderLayout.WEST);
        dateLabel = new JLabel(currentDateTime());
        header.add(dateLabel, BorderLayout.EAST);

        // Menu de gauche
        JPanel menu = new JPanel(new GridLayout(0, 1, 0, 6));
        menu.setBorder(BorderFactory.createTitledBorder("Menu"));
        if (canAccess(Module.PRODUCTS)) addMenuButton(menu, "Products", () -> new ProductView(role != Role.ADMIN));
        if (canAccess(Module.STOCK)) addMenuButton(menu, "Stock",
            () -> new StockView(role == Role.ACCOUNTANT));
        if (canAccess(Module.SUPPLIERS)) addMenuButton(menu, "Suppliers", () -> new SupplierView(role));
        if (canAccess(Module.SALES)) addMenuButton(menu, "Checkout", SaleView::new);
        if (canAccess(Module.CUSTOMERS)) addMenuButton(menu, "Customers", CustomerView::new);
        if (canAccess(Module.EMPLOYEES)) addMenuButton(menu, "Employees", () -> new EmployeeView(role != Role.ADMIN));
        if (canAccess(Module.DASHBOARD)) addMenuButton(menu, "Statistics", () -> new Dashboard(role));
        if (canAccess(Module.AISLES)) {
            addMenuButton(menu, "Aisles", () -> new AisleView(role != Role.ADMIN,
                    role == Role.AISLE_MANAGER ? employeeMatricule : null));
        }
        if (canAccess(Module.USERS)) addMenuButton(menu, "Users", UserManagementView::new);

        JButton logout = new JButton("Log out");
        logout.addActionListener(event -> {
            dispose();
            SwingUtilities.invokeLater(() -> new UserView().setVisible(true));
        });

        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.add(menu, BorderLayout.NORTH);
        left.add(logout, BorderLayout.SOUTH);

        // Zone centrale : bienvenue et aperçu
        JPanel welcome = new JPanel(new GridLayout(0, 1, 0, 4));
        welcome.add(StyleManager.createBoldLabel("Welcome, " + username));
        welcome.add(new JLabel("Select an option from the menu on the left to get started."));

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.add(welcome, BorderLayout.NORTH);
        if (canAccess(Module.PRODUCTS)) {
            JPanel overview = new JPanel(new GridLayout(0, 1, 0, 6));
            overview.setBorder(BorderFactory.createTitledBorder("Quick overview"));
            productCountLabel = new JLabel("Products in catalog: --");
            lowStockLabel = new JLabel("Low-stock products: --");
            stockValueLabel = new JLabel("Inventory value: --");
            overview.add(productCountLabel);
            overview.add(lowStockLabel);
            overview.add(stockValueLabel);
            JPanel overviewWrapper = new JPanel(new BorderLayout());
            overviewWrapper.add(overview, BorderLayout.NORTH);
            center.add(overviewWrapper, BorderLayout.CENTER);
        }

        JPanel page = StyleManager.createPage();
        page.add(header, BorderLayout.NORTH);
        page.add(left, BorderLayout.WEST);
        page.add(center, BorderLayout.CENTER);
        setContentPane(page);
    }

    private void addMenuButton(JPanel menu, String label, Supplier<? extends JFrame> windowFactory) {
        JButton button = new JButton(label);
        button.addActionListener(event -> windowFactory.get().setVisible(true));
        menu.add(button);
    }

    private void refreshOverview() {
        if (!canAccess(Module.PRODUCTS)) return;
        try {
            List<Product> products = new ProductDAO().getAllProducts();
            long lowStock = products.stream().filter(product -> product.getStockQuantity() <= 5).count();
            double stockValue = products.stream()
                    .mapToDouble(product -> product.getSellingPrice() * product.getStockQuantity()).sum();
            productCountLabel.setText("Products in catalog: " + products.size());
            lowStockLabel.setText("Low-stock products: " + lowStock);
            stockValueLabel.setText(String.format("Inventory value: %.2f USD", stockValue));
        } catch (IllegalStateException exception) {
            productCountLabel.setText("Products in catalog: unavailable");
            lowStockLabel.setText("Low-stock products: unavailable");
            stockValueLabel.setText("Inventory value: unavailable");
        }
    }

    private boolean canAccess(Module module) {
        return accessService.canAccess(role, module);
    }

    private String currentDateTime() {
        return new SimpleDateFormat("dd/MM/yyyy  |  HH:mm").format(new Date());
    }
}
