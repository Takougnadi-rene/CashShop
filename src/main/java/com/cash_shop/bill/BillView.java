package com.cash_shop.bill;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent; // ✅ Added missing import
import java.util.Date; // ✅ Added missing import

public class BillView extends JFrame {

    // --- COMPONENTS ---
    private final JTextField billIdField = new JTextField(5);
    private final JTextField dateField = new JTextField(10);
    private final JTextField customerField = new JTextField(15);
    private final JTextField cashierField = new JTextField(15);
    private final JTextArea purchaseArea = new JTextArea(10, 30);

    private final JButton btnNew = new JButton("New Bill");
    private final JButton btnGenerate = new JButton("Generate Bill");

    public BillView() {
        setTitle("Bill Management");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- TOP PANEL (Bill Info) ---
        JPanel topPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        // Row 1
        topPanel.add(new JLabel("Bill ID:"));
        topPanel.add(billIdField);

        topPanel.add(new JLabel("Date:"));
        dateField.setText(new Date().toString()); // Default to current date
        topPanel.add(dateField);

        // Row 2
        topPanel.add(new JLabel("Customer:"));
        topPanel.add(customerField);

        topPanel.add(new JLabel("Cashier:"));
        topPanel.add(cashierField);

        // --- CENTER PANEL (Purchase Details) ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Purchase Details"));

        purchaseArea.setEditable(false);
        purchaseArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        centerPanel.add(new JScrollPane(purchaseArea), BorderLayout.CENTER);

        // --- BOTTOM PANEL (Buttons) ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnNew);
        bottomPanel.add(btnGenerate);

        // --- ADD PANELS TO MAIN ---
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // --- EVENT LISTENERS ---
        setupListeners();
    }

    private void setupListeners() {
        // Listener for New Bill button
        btnNew.addActionListener(this::onNewBillClick);

        // Listener for Generate Bill button
        btnGenerate.addActionListener(this::onGenerateBillClick);
    }

    private void onNewBillClick(ActionEvent e) {
        clearForm();
    }

    private void onGenerateBillClick(ActionEvent e) {
        // Placeholder for logic - implement later
        JOptionPane.showMessageDialog(this, "Generate Bill clicked!\nImplement your logic here.");
    }

    private void clearForm() {
        billIdField.setText("");
        dateField.setText(new Date().toString());
        customerField.setText("");
        cashierField.setText("");
        purchaseArea.setText("");
    }

    // --- MAIN METHOD FOR TESTING (Optional) ---
    public static void main(String[] args) {
        // Use SwingUtilities to ensure GUI updates are done on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            BillView billView = new BillView();
            billView.setVisible(true);
        });
    }
}
