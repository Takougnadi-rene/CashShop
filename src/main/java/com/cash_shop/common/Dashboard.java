package com.cash_shop.common;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.Timer;

import com.cash_shop.aisle.AisleDAO;
import com.cash_shop.employee.Employee.Role;
import com.cash_shop.employee.EmployeeDAO;
import com.cash_shop.employee.EmployeeView;
import com.cash_shop.payment.PaymentDAO;
import com.cash_shop.product.FreshProduct;
import com.cash_shop.product.Product;
import com.cash_shop.product.ProductDAO;
import com.cash_shop.product.ProductView;
import com.cash_shop.product.StockView;
import com.cash_shop.sale.RegisterSessionDAO;
import com.cash_shop.sale.SaleDAO;
import com.cash_shop.sale.SaleDAO.DailySalesSummary;
import com.cash_shop.sale.SaleHistoryView;
import com.cash_shop.supplier.SupplierDAO;
import com.cash_shop.user.AccessService;
import com.cash_shop.user.AccessService.Module;

/**
 * Dashboard with key indicators: revenue, number of sales, product references and alerts (expired products,
 * low stock).
 */
public class Dashboard extends JFrame {
    // Stock at or below this quantity counts as low stock.
    private static final int LOW_STOCK_LIMIT = 5;
    private final Role role;
    private final AccessService accessService = new AccessService();

    // KPI labels
    private final JLabel revenueValueLabel      = kpiLabel("--");
    private final JLabel salesCountValueLabel   = kpiLabel("--");
    private final JLabel todaySalesCountLabel  = kpiLabel("--");
    private final JLabel activeCashRegisterCountLabel = kpiLabel("--");
    private final JLabel todaySalesTotalLabel  = kpiLabel("--");
    private final JLabel estimatedProfitLabel = kpiLabel("--");
    private final JLabel averageSaleLabel = kpiLabel("--");
    private final JLabel productCountValueLabel = kpiLabel("--");
    private final JLabel inventoryValueLabel    = kpiLabel("--");
    private final JLabel aisleCountLabel = kpiLabel("--");
    private final JLabel employeeCountLabel = kpiLabel("--");
    private final JLabel deliveredOrderCountLabel = kpiLabel("--");
    private final JLabel expiredCountLabel      = alertLabel("--");
    private final JLabel lowStockCountLabel     = alertLabel("--");
    private final JSpinner reportDate = new JSpinner(new SpinnerDateModel());
    private final Timer dailyRefreshTimer = new Timer(60_000, event -> {
        refreshDailySalesSummary();
        refreshActiveCashRegisterCount();
    });

    /** Builds the window and loads the figures. */
    public Dashboard(Role role) {
        this.role = role;
        setTitle("Supermarket Manager \u2013 Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        refreshDashboard();
        dailyRefreshTimer.start();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent event) {
                dailyRefreshTimer.stop();
            }
        });
    }

    // ---- Construction ----

    /** Assembles header, content and footer. */
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(StyleManager.BG_SURFACE);

        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildContent(), BorderLayout.CENTER);
        root.add(buildFooter(),  BorderLayout.SOUTH);

        setContentPane(root);
    }

    /** Builds the gradient header. */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, StyleManager.BG_DEEP,
                        getWidth(), 0, new Color(0x1A, 0x14, 0x40)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(StyleManager.ACCENT_PRIMARY);
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(StyleManager.TEXT_PRIMARY);

        JLabel dateLabel = StyleManager.createLabel("Report date:");
        reportDate.setEditor(new JSpinner.DateEditor(reportDate, "yyyy-MM-dd"));
        reportDate.setValue(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        reportDate.setPreferredSize(new Dimension(140, 34));
        reportDate.setBackground(StyleManager.BG_INPUT);
        reportDate.setForeground(StyleManager.TEXT_PRIMARY);
        JSpinner.DateEditor dateEditor = (JSpinner.DateEditor) reportDate.getEditor();
        StyleManager.styleInput(dateEditor.getTextField());
        reportDate.addChangeListener(event -> refreshDailySalesSummary());

        header.add(title, BorderLayout.WEST);
        JPanel dateControl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        dateControl.setOpaque(false);
        dateControl.add(dateLabel);
        dateControl.add(reportDate);
        header.add(dateControl, BorderLayout.EAST);
        return header;
    }

    /** Builds the dashboard indicator cards. */
    private JPanel buildContent() {
        JPanel content = new JPanel(new GridLayout(3, 5, 16, 16));
        content.setBackground(StyleManager.BG_SURFACE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 12, 20));

        content.add(buildKpiCard("Lifetime Revenue", revenueValueLabel, StyleManager.ACCENT_TEAL));
        content.add(buildKpiCard("Lifetime Sales", salesCountValueLabel, StyleManager.ACCENT_PRIMARY));
        content.add(buildKpiCard("Sales on selected date", todaySalesCountLabel, StyleManager.ACCENT_ORANGE));
        content.add(buildKpiCard("Active cash registers", activeCashRegisterCountLabel,
                StyleManager.ACCENT_ORANGE));
        content.add(buildKpiCard("Revenue on selected date", todaySalesTotalLabel, StyleManager.ACCENT_TEAL));
        content.add(buildKpiCard("Estimated gross profit", estimatedProfitLabel, StyleManager.SUCCESS));
        content.add(buildKpiCard("Average sale", averageSaleLabel, StyleManager.ACCENT_PRIMARY));
        content.add(buildKpiCard("Product References", productCountValueLabel, new Color(0x8A, 0x63, 0xFF)));
        content.add(buildKpiCard("Current stock value (cost)", inventoryValueLabel, StyleManager.ACCENT_TEAL));
        content.add(buildKpiCard("Aisles", aisleCountLabel, StyleManager.ACCENT_ORANGE));
        content.add(buildKpiCard("Employees", employeeCountLabel, StyleManager.ACCENT_PRIMARY));
        content.add(buildKpiCard("Orders delivered", deliveredOrderCountLabel, StyleManager.SUCCESS));
        content.add(buildAlertsCard());

        return content;
    }

    /** Builds the bottom buttons (shortcuts depend on the role). */
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(StyleManager.BG_SURFACE);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 20, 16, 20));

        JPanel btnBar = new JPanel();
        btnBar.setOpaque(false);
        btnBar.setLayout(new BoxLayout(btnBar, BoxLayout.X_AXIS));
        btnBar.add(Box.createHorizontalGlue());

        if (canAccess(Module.SALES_HISTORY)) {
            btnBar.add(buildFooterButton("Sales history", StyleManager.BG_CARD,
                () -> openWindow(new SaleHistoryView(role))));
            btnBar.add(Box.createHorizontalStrut(10));
        }
        if (canAccess(Module.EMPLOYEES)) {
            btnBar.add(buildFooterButton("Employees", StyleManager.BG_CARD,
                    () -> openWindow(new EmployeeView(role != Role.ADMIN))));
            btnBar.add(Box.createHorizontalStrut(10));
        }
        if (canAccess(Module.PRODUCTS) && !canAccess(Module.SALES)) {
            btnBar.add(buildFooterButton("Products", StyleManager.BG_CARD,
                    () -> openWindow(new ProductView(true))));
            btnBar.add(Box.createHorizontalStrut(10));
        }
        btnBar.add(buildFooterButton("Close", new Color(0x3D, 0x1A, 0x22), this::dispose));

        footer.add(btnBar, BorderLayout.EAST);
        return footer;
    }

    // ---- KPI cards ----

    /** Creates an indicator card. */
    private JPanel buildKpiCard(String caption, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(StyleManager.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(StyleManager.BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(20, 2, getWidth() - 20, 2);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel captionLabel = new JLabel(caption.toUpperCase());
        captionLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        captionLabel.setForeground(StyleManager.TEXT_SECONDARY);

        valueLabel.setForeground(accent);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(captionLabel, BorderLayout.NORTH);

        card.add(top, BorderLayout.CENTER);
        card.add(valueLabel, BorderLayout.SOUTH);
        return card;
    }

    /** Creates the alerts card (expired products and low stock). */
    private JPanel buildAlertsCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(StyleManager.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(StyleManager.BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(StyleManager.DANGER);
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(20, 2, getWidth() - 20, 2);
                g2.dispose();
            }
        };
        
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel captionLabel = new JLabel("ALERTS");
        captionLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        captionLabel.setForeground(StyleManager.TEXT_SECONDARY);

        JPanel alertRows = new JPanel(new GridBagLayout());
        alertRows.setOpaque(false);
        addAlertRow(alertRows, 0, "Expired products:", expiredCountLabel, "View products",
            () -> openWindow(new ProductView(true)));
        addAlertRow(alertRows, 1, "Low stock:", lowStockCountLabel, "View stock",
            () -> openWindow(new StockView(true)));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(captionLabel, BorderLayout.CENTER);

        card.add(top, BorderLayout.NORTH);
        card.add(alertRows, BorderLayout.CENTER);
        return card;
    }

    /** Adds a row to the alerts card: caption, value and a shortcut button. */
    private void addAlertRow(JPanel panel, int row, String caption, JLabel value,
            String btnText, Runnable action) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(4, 0, 4, 8);

        c.gridx = 0; c.anchor = GridBagConstraints.WEST; c.weightx = 0;
        JLabel lbl = new JLabel(caption);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(StyleManager.TEXT_SECONDARY);
        panel.add(lbl, c);

        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        value.setForeground(StyleManager.DANGER);
        panel.add(value, c);

        c.gridx = 2; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        panel.add(buildSmallButton(btnText, action), c);
    }

    // ---- Visual helpers ----

    /** Creates a large label for an indicator value. */
    private static JLabel kpiLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 24));
        return l;
    }

    /** Creates a label for an alert value. */
    private static JLabel alertLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return l;
    }

    /** Creates a small shortcut button. */
    private JButton buildSmallButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(StyleManager.ACCENT_PRIMARY);
        btn.setBackground(new Color(0x6C, 0x63, 0xFF, 30));
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleManager.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(4, 9, 4, 9)));
        btn.setPreferredSize(new Dimension(95, 28));
        btn.addMouseListener(new MouseAdapter() {
            private final Color bg = new Color(0x6C, 0x63, 0xFF, 30);
            private final Color hover = new Color(0x6C, 0x63, 0xFF, 70);
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        btn.addActionListener(e -> action.run());
        return btn;
    }

    /** Creates a footer button; the dark-red background marks the "Close" button. */
    private JButton buildFooterButton(String text, Color bg, Runnable action) {
        boolean isDanger = bg.equals(new Color(0x3D, 0x1A, 0x22));
        Color fg = isDanger ? StyleManager.DANGER : StyleManager.TEXT_PRIMARY;
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleManager.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 17, 8, 17)));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        btn.addActionListener(e -> action.run());
        return btn;
    }

    // ---- Data ----

    /** Loads all indicators; a failing query only shows "Unavailable" for its own indicator. */
    private void refreshDashboard() {
        refreshDailySalesSummary();

        try {
            List<Product> products = new ProductDAO().getAllProducts();
            long expiredCount = products.stream()
                    .filter(FreshProduct.class::isInstance)
                    .map(FreshProduct.class::cast)
                    .filter(this::isExpired)
                    .count();
            long lowStockCount = products.stream()
                    .filter(p -> p.getStockQuantity() <= LOW_STOCK_LIMIT)
                    .count();
            double stockValue = products.stream()
                    .mapToDouble(p -> p.getPurchasePrice() * p.getStockQuantity())
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
            aisleCountLabel.setText(Integer.toString(new AisleDAO().getAisleCount()));
        } catch (IllegalStateException exception) {
            aisleCountLabel.setText("Unavailable");
        }
        try {
            employeeCountLabel.setText(Integer.toString(new EmployeeDAO().getEmployeeCount()));
        } catch (IllegalStateException exception) {
            employeeCountLabel.setText("Unavailable");
        }
        try {
            deliveredOrderCountLabel.setText(Integer.toString(new SupplierDAO().getDeliveredOrderCount()));
        } catch (IllegalStateException exception) {
            deliveredOrderCountLabel.setText("Unavailable");
        }
        refreshActiveCashRegisterCount();

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

    /** Refreshes sales and revenue for the date currently selected in the report control. */
    private void refreshDailySalesSummary() {
        try {
            LocalDate selectedDate = ((Date) reportDate.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            DailySalesSummary summary = new SaleDAO().getDailySalesSummary(selectedDate);
            todaySalesCountLabel.setText(Integer.toString(summary.saleCount()));
            todaySalesTotalLabel.setText(String.format("%.2f USD", summary.totalRevenue()));
            estimatedProfitLabel.setText(String.format("%.2f USD", summary.estimatedGrossProfit()));
            averageSaleLabel.setText(summary.saleCount() == 0 ? "0.00 USD"
                    : String.format("%.2f USD", summary.totalRevenue()
                            .divide(java.math.BigDecimal.valueOf(summary.saleCount()),
                                    java.math.RoundingMode.HALF_UP)));
        } catch (IllegalStateException exception) {
            todaySalesCountLabel.setText("Unavailable");
            todaySalesTotalLabel.setText("Unavailable");
            estimatedProfitLabel.setText("Unavailable");
            averageSaleLabel.setText("Unavailable");
        }
    }

    /** Refreshes the number of cash registers with a recent heartbeat. */
    private void refreshActiveCashRegisterCount() {
        try {
            activeCashRegisterCountLabel.setText(Integer.toString(
                    new RegisterSessionDAO().getActiveSessionCount()));
        } catch (IllegalStateException exception) {
            activeCashRegisterCountLabel.setText("Unavailable");
        }
    }

    /**
     * A fresh product is expired when its ISO expiration date is before today (invalid or missing dates are
     * ignored).
     */
    private boolean isExpired(FreshProduct product) {
        try {
            return LocalDate.parse(product.getExpirationDate()).isBefore(LocalDate.now());
        } catch (DateTimeParseException | NullPointerException exception) {
            return false;
        }
    }

    /** Shows the window if it is not null. */
    private void openWindow(JFrame window) {
        if (window != null) window.setVisible(true);
    }

    /** Tells whether the current role may open the module. */
    private boolean canAccess(Module module) {
        return accessService.canAccess(role, module);
    }
}
