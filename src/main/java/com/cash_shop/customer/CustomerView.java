package com.cash_shop.customer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;

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
import static com.cash_shop.common.StyleManager.FONT_HEADER;
import static com.cash_shop.common.StyleManager.FONT_SMALL;
import static com.cash_shop.common.StyleManager.FONT_TITLE;
import static com.cash_shop.common.StyleManager.TEXT_MUTED;
import static com.cash_shop.common.StyleManager.createButton;
import static com.cash_shop.common.StyleManager.createCard;
import static com.cash_shop.common.StyleManager.createField;
import static com.cash_shop.common.StyleManager.createLabel;
import static com.cash_shop.common.StyleManager.createTable;

public class CustomerView extends JFrame {
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final JTextField idField = createField();
    private final JTextField nameField = createField();
    private final JTextField emailField = createField();
    private final JTextField phoneField = createField();
    private final JTextField spentField = createField();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[] { "ID", "Name", "Phone", "Total Spent", "Loyalty Points" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = createTable(tableModel);

    public CustomerView() {
        setTitle("Customer Management");
        setSize(900, 560);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUI();
        refreshTable();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_DARK);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setBackground(BG_PANEL);
        header.add(createLabel("CUSTOMER MANAGEMENT", ACCENT_GOLD, FONT_TITLE));

        JPanel content = new JPanel(new GridLayout(1, 2, 10, 0));
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
        form.add(createLabel("[ Customer Details ]", ACCENT_BLUE, FONT_HEADER), constraints);
        addField(form, constraints, 1, "Customer ID:", idField);
        addField(form, constraints, 2, "Name:", nameField);
        addField(form, constraints, 3, "Email:", emailField);
        addField(form, constraints, 4, "Phone:", phoneField);
        addField(form, constraints, 5, "Total Spent:", spentField);

        JPanel list = createCard();
        list.setLayout(new BorderLayout());
        list.add(createLabel("[ Customer List ]", ACCENT_BLUE, FONT_HEADER), BorderLayout.NORTH);
        list.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(form);
        content.add(list);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        actions.setBackground(BG_PANEL);
        javax.swing.JButton addButton = createButton("+ Add", ACCENT_GREEN);
        javax.swing.JButton pointsButton = createButton("Add Loyalty Points", ACCENT_GOLD);
        javax.swing.JButton deleteButton = createButton("Delete", ACCENT_RED);
        javax.swing.JButton closeButton = createButton("Close", new Color(80, 80, 100));
        addButton.addActionListener(event -> addCustomer());
        pointsButton.addActionListener(event -> addLoyaltyPoints());
        deleteButton.addActionListener(event -> deleteCustomer());
        closeButton.addActionListener(event -> dispose());
        actions.add(addButton);
        actions.add(pointsButton);
        actions.add(deleteButton);
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

    private void addCustomer() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            double spent = Double.parseDouble(spentField.getText().trim());
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || spent < 0) {
                throw new IllegalArgumentException("Enter valid values for every field.");
            }
            customerDAO.insertCustomer(id, name, email, phone, spent);
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Customer added successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException exception) {
            showError("Customer ID and total spent must be numeric.");
        } catch (SQLException | IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    private void addLoyaltyPoints() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Select a customer first.");
            return;
        }
        String input = JOptionPane.showInputDialog(this, "Points to add:");
        if (input == null) {
            return;
        }
        try {
            int points = Integer.parseInt(input.trim());
            if (points <= 0) {
                throw new IllegalArgumentException("Enter a positive number of points.");
            }
            customerDAO.addFidelityPoint((int) tableModel.getValueAt(row, 0), points);
            refreshTable();
        } catch (NumberFormatException exception) {
            showError("Points must be numeric.");
        } catch (SQLException | IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    private void deleteCustomer() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Select a customer first.");
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this, "Delete the selected customer?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                customerDAO.deleteCustomer((int) tableModel.getValueAt(row, 0));
                refreshTable();
            } catch (SQLException exception) {
                showError(exception.getMessage());
            }
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        try {
            for (Customer customer : customerDAO.getAllCustomers()) {
                tableModel.addRow(new Object[] {
                        customer.getCustomerId(),
                        customer.getName(),
                        customer.getPhoneNumber(),
                        customer.getTotalSpent(),
                        customer.getLoyaltyPoints()
                });
            }
        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        spentField.setText("");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
