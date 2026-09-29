package com.cash_shop.customer;

import java.awt.BorderLayout;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.cash_shop.common.StyleManager;

public class CustomerView extends JFrame {
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final JTextField idField = StyleManager.createField();
    private final JTextField nameField = StyleManager.createField();
    private final JTextField emailField = StyleManager.createField();
    private final JTextField phoneField = StyleManager.createField();
    private final JTextField spentField = StyleManager.createField();
    private final DefaultTableModel tableModel = StyleManager.createReadOnlyModel(
            "ID", "Name", "Phone", "Total Spent", "Loyalty Points");
    private final JTable table = StyleManager.createTable(tableModel);

    public CustomerView() {
        setTitle("Customer Management");
        setSize(850, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUI();
        refreshTable();
    }

    private void buildUI() {
        JPanel form = StyleManager.createForm("Customer details");
        StyleManager.addRow(form, 0, "Customer ID:", idField);
        StyleManager.addRow(form, 1, "Name:", nameField);
        StyleManager.addRow(form, 2, "Email:", emailField);
        StyleManager.addRow(form, 3, "Phone:", phoneField);
        StyleManager.addRow(form, 4, "Total Spent:", spentField);
        JPanel left = new JPanel(new BorderLayout());
        left.add(form, BorderLayout.NORTH);

        JButton addButton = new JButton("Add");
        JButton pointsButton = new JButton("Add Loyalty Points");
        JButton deleteButton = new JButton("Delete");
        JButton closeButton = new JButton("Close");
        addButton.addActionListener(event -> addCustomer());
        pointsButton.addActionListener(event -> addLoyaltyPoints());
        deleteButton.addActionListener(event -> deleteCustomer());
        closeButton.addActionListener(event -> dispose());

        JPanel page = StyleManager.createPage();
        page.add(StyleManager.createTitle("Customers"), BorderLayout.NORTH);
        page.add(left, BorderLayout.WEST);
        page.add(new JScrollPane(table), BorderLayout.CENTER);
        page.add(StyleManager.createButtonBar(addButton, pointsButton, deleteButton, closeButton),
                BorderLayout.SOUTH);
        setContentPane(page);
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
