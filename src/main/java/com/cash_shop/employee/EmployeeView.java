package com.cash_shop.employee;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
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
import static com.cash_shop.common.StyleManager.FONT_BODY;
import static com.cash_shop.common.StyleManager.FONT_HEADER;
import static com.cash_shop.common.StyleManager.FONT_SMALL;
import static com.cash_shop.common.StyleManager.FONT_TITLE;
import static com.cash_shop.common.StyleManager.TEXT_MUTED;
import static com.cash_shop.common.StyleManager.TEXT_PRIMARY;
import static com.cash_shop.common.StyleManager.createButton;
import static com.cash_shop.common.StyleManager.createCard;
import static com.cash_shop.common.StyleManager.createField;
import static com.cash_shop.common.StyleManager.createLabel;
import static com.cash_shop.common.StyleManager.createTable;

public class EmployeeView extends JFrame {
    private JTextField tfMatricule, tfPrenom, tfNom, tfEmail, tfSalaire, tfRecherche;
    private JComboBox<Employee.Role> cbRole;
    private DefaultTableModel tableModel;
    private JTable table;
    private Employee employeeSelectionne;
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final List<Employee> employees = new ArrayList<>();

    public EmployeeView() {
        setTitle("Gestion des Employés");
        setSize(900, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        chargerEmployes();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_DARK);

        JPanel titrePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titrePanel.setBackground(BG_PANEL);
        titrePanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        titrePanel.add(createLabel("========== ⚙ GESTION DES EMPLOYÉS ==========", ACCENT_GOLD, FONT_TITLE));

        JPanel corps = new JPanel(new GridLayout(1, 2, 10, 0));
        corps.setBackground(BG_DARK);
        corps.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JPanel formPanel = createCard();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        formPanel.add(createLabel("[ Formulaire Employé ]", ACCENT_BLUE, FONT_HEADER), g);
        g.gridwidth = 1;

        ajouterChamp(formPanel, g, 1, "Matricule :", tfMatricule = createField());
        ajouterChamp(formPanel, g, 2, "Prénom :", tfPrenom = createField());
        ajouterChamp(formPanel, g, 3, "Nom :", tfNom = createField());
        ajouterChamp(formPanel, g, 4, "Email :", tfEmail = createField());
        ajouterChamp(formPanel, g, 5, "Salaire :", tfSalaire = createField());

        g.gridy = 6;
        g.gridx = 0;
        formPanel.add(createLabel("Rôle :", TEXT_MUTED, FONT_SMALL), g);
        g.gridx = 1;
        cbRole = new JComboBox<>(Employee.Role.values());
        cbRole.setBackground(BG_DARK);
        cbRole.setForeground(TEXT_PRIMARY);
        cbRole.setFont(FONT_BODY);
        formPanel.add(cbRole, g);

        g.gridy = 7;
        g.gridx = 0;
        formPanel.add(createLabel("Recherche :", TEXT_MUTED, FONT_SMALL), g);
        g.gridx = 1;
        tfRecherche = createField();
        formPanel.add(tfRecherche, g);

        JPanel listePanel = createCard();
        listePanel.setLayout(new BorderLayout());
        listePanel.add(createLabel("[ Liste des Employés ]", ACCENT_BLUE, FONT_HEADER), BorderLayout.NORTH);
        tableModel = new DefaultTableModel(
                new String[] { "Matricule", "Prénom", "Nom", "Email", "Salaire", "Rôle" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = createTable(tableModel);
        table.getSelectionModel().addListSelectionListener(event -> selectionnerEmploye());
        listePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        corps.add(formPanel);
        corps.add(listePanel);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        actionsPanel.setBackground(BG_PANEL);
        actionsPanel.add(createLabel("[ Actions ]", TEXT_MUTED, FONT_SMALL));
        javax.swing.JButton btnAjouter = createButton("+ Add", ACCENT_GREEN);
        javax.swing.JButton btnModifier = createButton("✎ Modify", ACCENT_BLUE);
        javax.swing.JButton btnSupprimer = createButton("✕ Delete", ACCENT_RED);
        javax.swing.JButton btnRecherche = createButton("⚲ Search", ACCENT_GOLD);
        javax.swing.JButton btnFermer = createButton("Close", new Color(80, 80, 100));
        btnAjouter.addActionListener(event -> ajouterEmploye());
        btnModifier.addActionListener(event -> modifierEmploye());
        btnSupprimer.addActionListener(event -> supprimerEmployee());
        btnRecherche.addActionListener(event -> rechercherEmploye());
        btnFermer.addActionListener(event -> dispose());
        actionsPanel.add(btnAjouter);
        actionsPanel.add(btnModifier);
        actionsPanel.add(btnSupprimer);
        actionsPanel.add(btnRecherche);
        actionsPanel.add(btnFermer);

        main.add(titrePanel, BorderLayout.NORTH);
        main.add(corps, BorderLayout.CENTER);
        main.add(actionsPanel, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private void ajouterChamp(JPanel panel, GridBagConstraints constraints, int row, String label,
            JTextField field) {
        constraints.gridy = row;
        constraints.gridx = 0;
        panel.add(createLabel(label, TEXT_MUTED, FONT_SMALL), constraints);
        constraints.gridx = 1;
        panel.add(field, constraints);
    }

    private void chargerEmployes() {
        employees.clear();
        employees.addAll(employeeDAO.getAllEmployees());
        afficherEmployes(employees);
    }

    private void afficherEmployes(List<Employee> employes) {
        tableModel.setRowCount(0);
        for (Employee employee : employes) {
            tableModel.addRow(new Object[] { employee.getMatricule(), employee.getFirstName(),
                    employee.getLastName(), employee.getEmail(), String.format("%.2f", employee.getSalary()),
                    employee.getRole() });
        }
    }

    private void selectionnerEmploye() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= employees.size()) {
            return;
        }
        employeeSelectionne = employees.get(row);
        tfMatricule.setText(employeeSelectionne.getMatricule());
        tfPrenom.setText(employeeSelectionne.getFirstName());
        tfNom.setText(employeeSelectionne.getLastName());
        tfEmail.setText(employeeSelectionne.getEmail());
        tfSalaire.setText(String.valueOf(employeeSelectionne.getSalary()));
        cbRole.setSelectedItem(employeeSelectionne.getRole());
    }

    private void ajouterEmploye() {
        try {
            Employee employee = lireEmploye();
            employeeDAO.addEmployee(employee);
            employees.add(employee);
            chargerEmployes();
            viderFormulaire();
            afficherSucces("Employé ajouté avec succès.");
        } catch (IllegalArgumentException exception) {
            afficherErreur(exception.getMessage());
        }
    }

    private void modifierEmploye() {
        if (employeeSelectionne == null) {
            afficherErreur("Sélectionnez un employé.");
            return;
        }
        try {
            Employee employee = lireEmploye();
            int index = employees.indexOf(employeeSelectionne);
            employeeDAO.updateEmployee(employee);
            employees.set(index, employee);
            chargerEmployes();
            viderFormulaire();
            afficherSucces("Employé modifié.");
        } catch (IllegalArgumentException exception) {
            afficherErreur(exception.getMessage());
        }
    }

    private void supprimerEmployee() {
        if (employeeSelectionne == null) {
            afficherErreur("Sélectionnez un employé.");
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Supprimer " + employeeSelectionne.getFirstName() + " " + employeeSelectionne.getLastName() + " ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            employeeDAO.removeEmployee(employeeSelectionne);
            employees.remove(employeeSelectionne);
            chargerEmployes();
            viderFormulaire();
        }
    }

    private Employee lireEmploye() {
        String matricule = tfMatricule.getText().trim();
        String prenom = tfPrenom.getText().trim();
        String nom = tfNom.getText().trim();
        String email = tfEmail.getText().trim();
        if (matricule.isEmpty() || prenom.isEmpty() || nom.isEmpty() || email.isEmpty()) {
            throw new IllegalArgumentException("Remplissez tous les champs.");
        }
        try {
            double salaire = Double.parseDouble(tfSalaire.getText().trim());
            return new Employee(matricule, prenom, nom, email, salaire,
                    (Employee.Role) cbRole.getSelectedItem());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Vérifiez la valeur du salaire.");
        }
    }

    private void rechercherEmploye() {
        String terme = tfRecherche.getText().trim().toLowerCase();
        List<Employee> resultats = new ArrayList<>();
        for (Employee employee : employees) {
            if (employee.getMatricule().toLowerCase().contains(terme)
                    || employee.getFirstName().toLowerCase().contains(terme)
                    || employee.getLastName().toLowerCase().contains(terme)
                    || employee.getEmail().toLowerCase().contains(terme)) {
                resultats.add(employee);
            }
        }
        afficherEmployes(resultats);
    }

    private void viderFormulaire() {
        tfMatricule.setText("");
        tfPrenom.setText("");
        tfNom.setText("");
        tfEmail.setText("");
        tfSalaire.setText("");
        tfRecherche.setText("");
        cbRole.setSelectedIndex(0);
        employeeSelectionne = null;
        table.clearSelection();
    }

    private void afficherErreur(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private void afficherSucces(String message) {
        JOptionPane.showMessageDialog(this, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new EmployeeView().setVisible(true));
    }
}