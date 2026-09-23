package com.cash_shop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.cash_shop.common.StyleManager;
import com.cash_shop.customer.CustomerView;
import com.cash_shop.employee.EmployeeView;
import com.cash_shop.product.ProductView;
import com.cash_shop.sale.SaleView;
import com.cash_shop.supplier.SupplierView;

public class Dashboard extends JFrame {

    public Dashboard() {
        setTitle("Cash Shop - Dashboard");
        setSize(980, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(StyleManager.BG_DARK);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(18, 18));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setBackground(StyleManager.BG_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        JLabel title = StyleManager.createLabel("=== CASH SHOP MANAGEMENT ===", StyleManager.ACCENT_GOLD, StyleManager.FONT_TITLE);
        header.add(title);

        JPanel cards = new JPanel(new GridLayout(2, 3, 20, 20));
        cards.setBackground(StyleManager.BG_DARK);
        cards.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        cards.add(createCard("Employés", StyleManager.ACCENT_BLUE, "Gestion du personnel", () -> openWindow(new EmployeeView())));
        cards.add(createCard("Clients", StyleManager.ACCENT_GREEN, "Fidélité et contacts", () -> openWindow(new CustomerView())));
        cards.add(createCard("Produits", StyleManager.ACCENT_GOLD, "Stock et prix", () -> openWindow(new ProductView())));
        cards.add(createCard("Ventes", StyleManager.ACCENT_BLUE, "Tickets et paiements", () -> openWindow(new SaleView())));
        cards.add(createCard("Fournisseurs", StyleManager.ACCENT_GREEN, "Commandes et approvisionnement", () -> openWindow(new SupplierView())));
        cards.add(createCard("Quitter", new Color(120, 120, 140), "Fermer le tableau de bord", () -> dispose()));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(StyleManager.BG_DARK);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 20));
        JButton logoutBtn = StyleManager.createButton("Déconnexion", new Color(244, 67, 54));
        logoutBtn.setPreferredSize(new Dimension(160, 36));
        logoutBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new com.cash_shop.user.UserView().setVisible(true));
        });
        footer.add(logoutBtn);

        add(header, BorderLayout.NORTH);
        add(cards, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createCard(String title, Color color, String subtitle, Runnable action) {
        JPanel panel = StyleManager.createCard();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(250, 160));

        JLabel titleLabel = StyleManager.createLabel(title, color, StyleManager.FONT_HEADER);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        JLabel subtitleLabel = StyleManager.createLabel(subtitle, StyleManager.TEXT_MUTED, StyleManager.FONT_SMALL);
        subtitleLabel.setHorizontalAlignment(JLabel.CENTER);

        JButton button = StyleManager.createButton("Ouvrir", color);
        button.setPreferredSize(new Dimension(140, 36));
        button.addActionListener(e -> action.run());

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(titleLabel, BorderLayout.NORTH);
        content.add(subtitleLabel, BorderLayout.CENTER);
        content.add(button, BorderLayout.SOUTH);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private void openWindow(JFrame window) {
        if (window != null) {
            window.setVisible(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Dashboard().setVisible(true));
    }
}
