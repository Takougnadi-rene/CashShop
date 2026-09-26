package com.cash_shop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.cash_shop.aisle.AisleView;
import com.cash_shop.employee.Employee.Role;
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
import static com.cash_shop.common.StyleManager.createLabel;
import com.cash_shop.customer.CustomerView;
import com.cash_shop.employee.EmployeeView;
import com.cash_shop.product.Product;
import com.cash_shop.product.ProductDAO;
import com.cash_shop.product.ProductView;
import com.cash_shop.product.StockView;
import com.cash_shop.sale.SaleView;
import com.cash_shop.supplier.SupplierView;
import com.cash_shop.user.UserView;
import com.cash_shop.user.AccessService;
import com.cash_shop.user.AccessService.Module;

public class Home extends JFrame {
	private final String username;
	private final Role role;
	private final AccessService accessService = new AccessService();
	private JLabel dateLabel;
	private JLabel productCountLabel;
	private JLabel lowStockLabel;
	private JLabel stockValueLabel;

	public Home(String username, Role role) {
		this.username = username;
		this.role = role;
		setTitle("Cash Shop - Home");
		setSize(1050, 660);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		buildUI();
		refreshOverview();
		new Timer(1000, event -> dateLabel.setText(currentDateTime())).start();
	}

	private void buildUI() {
		JPanel main = new JPanel(new BorderLayout());
		main.setBackground(BG_DARK);

		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(BG_PANEL);
		header.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 18, 12, 18));
		header.add(createLabel("CASH SHOP", ACCENT_GOLD, FONT_TITLE), BorderLayout.WEST);
		dateLabel = createLabel(currentDateTime(), TEXT_MUTED, FONT_BODY);
		header.add(dateLabel, BorderLayout.EAST);

		JPanel body = new JPanel(new BorderLayout(12, 0));
		body.setBackground(BG_DARK);
		body.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));

		JPanel navigation = new JPanel();
		navigation.setLayout(new BoxLayout(navigation, BoxLayout.Y_AXIS));
		navigation.setBackground(BG_PANEL);
		navigation.setBorder(javax.swing.BorderFactory.createCompoundBorder(
				javax.swing.BorderFactory.createLineBorder(com.cash_shop.common.StyleManager.BORDER_COLOR),
				javax.swing.BorderFactory.createEmptyBorder(12, 10, 12, 10)));
		navigation.setPreferredSize(new Dimension(210, 0));
		navigation.add(createLabel("NAVIGATION", ACCENT_GOLD, FONT_HEADER));
		navigation.add(Box.createVerticalStrut(12));
		if (canAccess(Module.DASHBOARD)) {
			addNavigationButton(navigation, "Dashboard", () -> new Dashboard(role));
		}
		if (canAccess(Module.EMPLOYEES)) addNavigationButton(navigation, "Employees", EmployeeView::new);
		if (canAccess(Module.CUSTOMERS)) addNavigationButton(navigation, "Customers", CustomerView::new);
		if (canAccess(Module.PRODUCTS)) addNavigationButton(navigation, "Products", ProductView::new);
		if (canAccess(Module.STOCK)) addNavigationButton(navigation, "Stock", StockView::new);
		if (canAccess(Module.AISLES)) {
			addNavigationButton(navigation, "Aisles", () -> new AisleView(role == Role.AISLE_MANAGER));
		}
		if (canAccess(Module.SALES)) addNavigationButton(navigation, "Sales", SaleView::new);
		if (canAccess(Module.SUPPLIERS)) addNavigationButton(navigation, "Suppliers", SupplierView::new);
		navigation.add(Box.createVerticalGlue());
		javax.swing.JButton logout = createButton("Log Out", ACCENT_RED);
		logout.setAlignmentX(Component.LEFT_ALIGNMENT);
		logout.setMaximumSize(new Dimension(185, 38));
		logout.addActionListener(event -> {
			dispose();
			SwingUtilities.invokeLater(() -> new UserView().setVisible(true));
		});
		navigation.add(logout);

		JPanel overview = new JPanel(new BorderLayout(0, 12));
		overview.setBackground(BG_DARK);
		JPanel welcome = createCard();
		welcome.setLayout(new BoxLayout(welcome, BoxLayout.Y_AXIS));
		welcome.add(createLabel("[ WELCOME TO CASH SHOP ]", ACCENT_GOLD, FONT_HEADER));
		welcome.add(Box.createVerticalStrut(10));
		welcome.add(createLabel("Welcome, " + username, TEXT_PRIMARY, FONT_TITLE));
		welcome.add(Box.createVerticalStrut(6));
		welcome.add(createLabel("Choose a module from the navigation menu.", TEXT_MUTED, FONT_SMALL));

		if (canAccess(Module.PRODUCTS)) {
			JPanel summary = createCard();
			summary.setLayout(new GridLayout(3, 1, 8, 8));
			summary.add(createLabel("QUICK OVERVIEW", ACCENT_BLUE, FONT_HEADER));
			productCountLabel = createLabel("Products in catalog: --", TEXT_PRIMARY, FONT_BODY);
			lowStockLabel = createLabel("Low-stock products: --", ACCENT_RED, FONT_BODY);
			stockValueLabel = createLabel("Inventory value: --", ACCENT_GREEN, FONT_BODY);
			summary.add(productCountLabel);
			summary.add(lowStockLabel);
			summary.add(stockValueLabel);
			overview.add(summary, BorderLayout.CENTER);
		} else {
			overview.add(createLabel("Select your assigned module from the navigation menu.", TEXT_MUTED, FONT_BODY),
					BorderLayout.CENTER);
		}

		JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		quickActions.setOpaque(false);
		quickActions.add(createLabel("QUICK ACCESS", TEXT_MUTED, FONT_HEADER));
		if (canAccess(Module.STOCK)) quickActions.add(quickButton("Open Stock", ACCENT_BLUE, StockView::new));
		if (canAccess(Module.SALES)) quickActions.add(quickButton("Start Sale", ACCENT_GREEN, SaleView::new));
		overview.add(welcome, BorderLayout.NORTH);
		overview.add(quickActions, BorderLayout.SOUTH);

		body.add(navigation, BorderLayout.WEST);
		body.add(overview, BorderLayout.CENTER);
		main.add(header, BorderLayout.NORTH);
		main.add(body, BorderLayout.CENTER);
		setContentPane(main);
	}

	private void addNavigationButton(JPanel navigation, String label, Supplier<? extends JFrame> windowFactory) {
		javax.swing.JButton button = new javax.swing.JButton(label);
		button.setBackground(BG_DARK);
		button.setForeground(TEXT_PRIMARY);
		button.setFont(FONT_BODY);
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setAlignmentX(Component.LEFT_ALIGNMENT);
		button.setMaximumSize(new Dimension(185, 38));
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.addActionListener(event -> windowFactory.get().setVisible(true));
		navigation.add(button);
		navigation.add(Box.createVerticalStrut(4));
	}

	private javax.swing.JButton quickButton(String label, Color color, Supplier<? extends JFrame> windowFactory) {
		javax.swing.JButton button = createButton(label, color);
		button.addActionListener(event -> windowFactory.get().setVisible(true));
		return button;
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
			stockValueLabel.setText(String.format("Inventory value: %.0f FCFA", stockValue));
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
		return new SimpleDateFormat("yyyy-MM-dd  |  HH:mm:ss").format(new Date());
	}
}
