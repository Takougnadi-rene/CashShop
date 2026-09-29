package com.cash_shop;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.cash_shop.common.StyleManager;
import com.cash_shop.employee.Employee.Role;
import com.cash_shop.employee.EmployeeView;
import com.cash_shop.payment.PaymentDAO;
import com.cash_shop.product.FreshProduct;
import com.cash_shop.product.Product;
import com.cash_shop.product.ProductDAO;
import com.cash_shop.product.ProductView;
import com.cash_shop.product.StockView;
import com.cash_shop.sale.SaleDAO;
import com.cash_shop.sale.SaleView;
import com.cash_shop.user.AccessService;
import com.cash_shop.user.AccessService.Module;

public class Dashboard extends JFrame {
    private static final int LOW_STOCK_LIMIT = 5;
    private final Role role;
    private final AccessService accessService = new AccessService();
    private final JLabel revenueValueLabel = new JLabel("--");
    private final JLabel salesCountValueLabel = new JLabel("--");
    private final JLabel productCountValueLabel = new JLabel("--");
    private final JLabel inventoryValueLabel = new JLabel("--");
    private final JLabel expiredCountLabel = new JLabel("--");
    private final JLabel lowStockCountLabel = new JLabel("--");

    public Dashboard(Role role) {
        this.role = role;
        setTitle("Supermarket Manager - Dashboard");
        setSize(650, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        refreshDashboard();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.add(StyleManager.createTitle("Dashboard"), BorderLayout.WEST);
        header.add(new JLabel("As of " + currentDate()), BorderLayout.EAST);

        JPanel financial = StyleManager.createForm("Financial performance");
        StyleManager.addRow(financial, 0, "Total revenue:", revenueValueLabel);
        StyleManager.addRow(financial, 1, "Number of sales made:", salesCountValueLabel);

        JPanel inventory = StyleManager.createForm("Inventory status");
        StyleManager.addRow(inventory, 0, "Total number of products:", productCountValueLabel);
        StyleManager.addRow(inventory, 1, "Total stock value:", inventoryValueLabel);

        JPanel alerts = StyleManager.createForm("Alerts");
        addAlertRow(alerts, 0, "Expired products:", expiredCountLabel, "View products",
                () -> openWindow(new ProductView(role != Role.ADMIN)));
        addAlertRow(alerts, 1, "Low stock:", lowStockCountLabel, "View stock",
                () -> openWindow(new StockView()));

        JPanel content = new JPanel(new GridLayout(0, 1, 0, 10));
        content.add(financial);
        content.add(inventory);
        content.add(alerts);

        JPanel shortcuts = new JPanel();
        if (canAccess(Module.SALES)) {
            shortcuts.add(createButton("View Sales", () -> openWindow(new SaleView())));
        }
        if (canAccess(Module.EMPLOYEES)) {
            shortcuts.add(createButton("View Employees", () -> openWindow(new EmployeeView(role != Role.ADMIN))));
        }
        if (canAccess(Module.PRODUCTS) && !canAccess(Module.SALES)) {
            shortcuts.add(createButton("View Products", () -> openWindow(new ProductView(role != Role.ADMIN))));
        }
        shortcuts.add(createButton("Close", this::dispose));

        JPanel page = StyleManager.createPage();
        page.add(header, BorderLayout.NORTH);
        page.add(content, BorderLayout.CENTER);
        page.add(shortcuts, BorderLayout.SOUTH);
        setContentPane(page);
    }

    private void addAlertRow(JPanel panel, int row, String caption, JLabel value, String buttonText,
            Runnable action) {
        StyleManager.addRow(panel, row, caption, value);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = row;
        constraints.insets = new Insets(4, 4, 4, 4);
        panel.add(createButton(buttonText, action), constraints);
    }

    private JButton createButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(event -> action.run());
        return button;
    }

    private void refreshDashboard() {
        try {
            List<Product> products = new ProductDAO().getAllProducts();
            long expiredCount = products.stream()
                    .filter(FreshProduct.class::isInstance)
                    .map(FreshProduct.class::cast)
                    .filter(this::isExpired)
                    .count();
            long lowStockCount = products.stream()
                    .filter(product -> product.getStockQuantity() <= LOW_STOCK_LIMIT)
                    .count();
            double stockValue = products.stream()
                    .mapToDouble(product -> product.getSellingPrice() * product.getStockQuantity())
                    .sum();
            productCountValueLabel.setText(products.size() + " references");
            inventoryValueLabel.setText(String.format("%.2f USD", stockValue));
            expiredCountLabel.setText(expiredCount + " items");
            lowStockCountLabel.setText(lowStockCount + " items");
        } catch (IllegalStateException exception) {
            productCountValueLabel.setText("Unavailable");
            inventoryValueLabel.setText("Unavailable");
            expiredCountLabel.setText("Unavailable");
            lowStockCountLabel.setText("Unavailable");
        }

        try {
            salesCountValueLabel.setText(new SaleDAO().getSalesCount() + " transactions");
        } catch (IllegalStateException exception) {
            salesCountValueLabel.setText("Unavailable");
        }
        try {
            revenueValueLabel.setText(String.format("%.2f USD", new PaymentDAO().getTotalRevenue()));
        } catch (IllegalStateException exception) {
            revenueValueLabel.setText("Unavailable");
        }
    }

    private boolean isExpired(FreshProduct product) {
        try {
            return LocalDate.parse(product.getExpirationDate()).isBefore(LocalDate.now());
        } catch (DateTimeParseException | NullPointerException exception) {
            return false;
        }
    }

    private String currentDate() {
        return new SimpleDateFormat("dd/MM/yyyy  |  HH:mm").format(new Date());
    }

    private void openWindow(JFrame window) {
        if (window != null) {
            window.setVisible(true);
        }
    }

    private boolean canAccess(Module module) {
        return accessService.canAccess(role, module);
    }

    public static void main(String[] args) {
        StyleManager.applyLookAndFeel();
        SwingUtilities.invokeLater(() -> new com.cash_shop.user.UserView().setVisible(true));
    }
}
