package com.cash_shop.customer;

import javax.swing.*;
import java.awt.*;

public class FidelityView extends JFrame {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final int customerId;
    private final JTextField pointsField = new JTextField(10);
    private final JLabel statusLabel = new JLabel(" ");

    public FidelityView(int customerId) {
        this.customerId = customerId;

        setTitle("Gérer Points Fidélité - Client #" + customerId);
        setSize(350, 220);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        formPanel.add(new JLabel("Nombre de points :"));
        formPanel.add(pointsField);

        // Buttons panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton addBtn = new JButton("Ajouter Points");
        JButton removeBtn = new JButton("Retirer Points");

        btnPanel.add(addBtn);
        btnPanel.add(removeBtn);

        // Status label
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(new Color(0, 128, 0));

        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(btnPanel, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        add(mainPanel);

        // Actions
        addBtn.addActionListener(e -> {
            try {
                int points = Integer.parseInt(pointsField.getText().trim());
                if (points <= 0) throw new NumberFormatException();
                customerDAO.addFidelityPoint(customerId, points);
                statusLabel.setForeground(new Color(0, 128, 0));
                statusLabel.setText(points + " point(s) ajouté(s) avec succès.");
                pointsField.setText("");
            } catch (NumberFormatException ex) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Veuillez entrer un nombre entier valide.");
            } catch (Exception ex) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Erreur: " + ex.getMessage());
            }
        });

        removeBtn.addActionListener(e -> {
            try {
                int points = Integer.parseInt(pointsField.getText().trim());
                if (points <= 0) throw new NumberFormatException();
                customerDAO.removeFidelityPoint(customerId, points);
                statusLabel.setForeground(new Color(0, 128, 0));
                statusLabel.setText(points + " point(s) retiré(s) avec succès.");
                pointsField.setText("");
            } catch (NumberFormatException ex) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Veuillez entrer un nombre entier valide.");
            } catch (Exception ex) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Erreur: " + ex.getMessage());
            }
        });
    }
}
