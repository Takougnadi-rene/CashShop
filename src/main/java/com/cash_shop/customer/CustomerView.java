package com.cash_shop.customer;

import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;

public class CustomerView extends JFrame {
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final JTextField nameField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField spentField = new JTextField();
    private final JTable table;
    private final DefaultTableModel tableModel;

    public CustomerView() {
        setTitle("Gestion des Clients");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Fields Panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2));
        formPanel.add(new JLabel("Nom:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Téléphone:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Dépenses totales:"));
        formPanel.add(spentField);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Ajouter");
        JButton deleteButton = new JButton("Supprimer");
        JButton fidelityButton = new JButton("Gérer Points Fidélité");

        btnPanel.add(addButton);
        btnPanel.add(deleteButton);
        btnPanel.add(fidelityButton);

        // Table
        String[] columns = {"ID", "Nom", "Téléphone", "Dépenses", "Points Fidélité"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);

        add(formPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        // Actions
        addButton.addActionListener(e -> {
            try {
                String name = nameField.getText();
                String phone = phoneField.getText();
                double spent = Double.parseDouble(spentField.getText());
                customerDAO.addCustomer(name, phone, spent);
                refreshTable();
                JOptionPane.showMessageDialog(this, "Client ajouté");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            }
        });

        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int id = (int) table.getValueAt(row, 0);
                try {
                    customerDAO.deleteCustomer(id);
                    refreshTable();
                    JOptionPane.showMessageDialog(this, "Client supprimé");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
                }
            }
        });

        fidelityButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int id = (int) table.getValueAt(row, 0);
                new FidelityView(id).setVisible(true);
            }
        });

        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        try {
            ResultSet rs = customerDAO.getAllCustomers();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("phone"),
                    rs.getDouble("total_spent"),
                    rs.getInt("fidelity_points")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
        }
    }
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new CustomerView().setVisible(true);
            }
        });
    }
}
