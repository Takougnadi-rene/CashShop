package com.cash_shop.supplier;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.cash_shop.common.StyleManager;

public class SupplierView extends JFrame {

    private final List<Supplier> suppliers = new ArrayList<>();
    private final JTextField tfCode = StyleManager.createField();
    private final JTextField tfName = StyleManager.createField();
    private final JTextField tfPhone = StyleManager.createField();
    private final JTextField tfAddress = StyleManager.createField();
    private JTable table;
    private DefaultTableModel tableModel;

    public SupplierView() {
        initDemoSuppliers();
        setTitle("Supplier Management");
        setSize(820, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        buildUI();
        refreshTable();
    }

    private void initDemoSuppliers() {
        suppliers.add(new Supplier(1, "Achat Plus", "+221 77 000 00 00", "Dakar"));
        suppliers.add(new Supplier(2, "Fresh Supply", "+221 77 111 11 11", "Thiès"));
        suppliers.add(new Supplier(3, "TechMarket", "+221 77 222 22 22", "Saint-Louis"));
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBackground(StyleManager.BG_DARK);
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setBackground(StyleManager.BG_DARK);
        header.add(StyleManager.createLabel("SUPPLIERS", StyleManager.ACCENT_GOLD, StyleManager.FONT_TITLE));

        JPanel formPanel = StyleManager.createCard();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(StyleManager.createLabel("Code:", StyleManager.TEXT_MUTED, StyleManager.FONT_SMALL), gbc);
        gbc.gridx = 1;
        formPanel.add(tfCode, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(StyleManager.createLabel("Name:", StyleManager.TEXT_MUTED, StyleManager.FONT_SMALL), gbc);
        gbc.gridx = 1;
        formPanel.add(tfName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(StyleManager.createLabel("Phone:", StyleManager.TEXT_MUTED, StyleManager.FONT_SMALL), gbc);
        gbc.gridx = 1;
        formPanel.add(tfPhone, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(StyleManager.createLabel("Address:", StyleManager.TEXT_MUTED, StyleManager.FONT_SMALL), gbc);
        gbc.gridx = 1;
        formPanel.add(tfAddress, gbc);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        actionPanel.setOpaque(false);
        JButton addBtn = StyleManager.createButton("Add", StyleManager.ACCENT_GREEN);
        JButton deleteBtn = StyleManager.createButton("Delete", StyleManager.ACCENT_RED);
        JButton clearBtn = StyleManager.createButton("Clear", new java.awt.Color(100, 100, 120));

        addBtn.addActionListener(e -> addSupplier());
        deleteBtn.addActionListener(e -> deleteSupplier());
        clearBtn.addActionListener(e -> clearForm());

        actionPanel.add(addBtn);
        actionPanel.add(deleteBtn);
        actionPanel.add(clearBtn);

        String[] columns = {"Code", "Name", "Phone", "Address"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = StyleManager.createTable(tableModel);

        main.add(header, BorderLayout.NORTH);
        main.add(formPanel, BorderLayout.CENTER);
        main.add(actionPanel, BorderLayout.SOUTH);

        add(main, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void addSupplier() {
        try {
            int code = Integer.parseInt(tfCode.getText().trim());
            String name = tfName.getText().trim();
            String phone = tfPhone.getText().trim();
            String address = tfAddress.getText().trim();
            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                throw new IllegalArgumentException("Tous les champs doivent être remplis.");
            }
            suppliers.add(new Supplier(code, name, phone, address));
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Supplier added.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Code must be numeric.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSupplier() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a supplier.");
            return;
        }
        suppliers.remove(selectedRow);
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Supplier supplier : suppliers) {
            tableModel.addRow(new Object[]{supplier.getCode(), supplier.getName(), supplier.getTelephone(), supplier.getAddress()});
        }
    }

    private void clearForm() {
        tfCode.setText("");
        tfName.setText("");
        tfPhone.setText("");
        tfAddress.setText("");
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new SupplierView().setVisible(true));
    }
}
