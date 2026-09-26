package interfaceGraphique;

import model.*;
import service.EmployeService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import static interfaceGraphique.StyleManager.*;

public class FenetreEmploye extends JFrame {

    private JTextField tfMatricule, tfNom, tfPrenom, tfSalaire, tfLogin;
    private JPasswordField tfMotDePasse;
    private JComboBox<String> cbRole;
    private DefaultTableModel tableModel;
    private Employe employeSelectionne;

    private final EmployeService employeService = new EmployeService();

    public FenetreEmploye() {
        setTitle("Gestion du Personnel");
        setSize(880, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        chargerEmployes();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_DARK);

        JPanel titre = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titre.setBackground(BG_PANEL);
        titre.setBorder(BorderFactory.createEmptyBorder(8,0,8,0));
        titre.add(createLabel("========== 👔 GESTION DU PERSONNEL ==========", ACCENT_GOLD, FONT_TITLE));

        JPanel corps = new JPanel(new GridLayout(1,2,10,0));
        corps.setBackground(BG_DARK);
        corps.setBorder(BorderFactory.createEmptyBorder(10,10,5,10));

        // Formulaire
        JPanel form = createCard();
        form.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,5,6,5);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx=0; g.gridy=0; g.gridwidth=2;
        form.add(createLabel("• INFORMATIONS DE L'EMPLOYÉ", TEXT_PRIMARY, FONT_HEADER), g);
        g.gridwidth=1;

        g.gridy=1; g.gridx=0; form.add(createLabel("Matricule :", TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1; tfMatricule = createField(); form.add(tfMatricule, g);

        g.gridy=2; g.gridx=0; form.add(createLabel("Nom :", TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1; tfNom = createField(); form.add(tfNom, g);

        g.gridy=3; g.gridx=0; form.add(createLabel("Prénom :", TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1; tfPrenom = createField(); form.add(tfPrenom, g);

        g.gridy=4; g.gridx=0; form.add(createLabel("Salaire (F CFA) :", TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1; tfSalaire = createField(); form.add(tfSalaire, g);

        g.gridy=5; g.gridx=0; form.add(createLabel("Rôle :", TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1;
        cbRole = new JComboBox<>(new String[]{"Caissier","Magasinier","ChefRayon","Comptable","Directeur"});
        cbRole.setBackground(BG_DARK); cbRole.setForeground(TEXT_PRIMARY); cbRole.setFont(FONT_BODY);
        form.add(cbRole, g);

        g.gridy=6; g.gridx=0; form.add(createLabel("Login :", TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1; tfLogin = createField(); form.add(tfLogin, g);

        g.gridy=7; g.gridx=0; form.add(createLabel("Mot de passe :", TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1;
        tfMotDePasse = new JPasswordField();
        tfMotDePasse.setBackground(BG_DARK);
        tfMotDePasse.setForeground(TEXT_PRIMARY);
        tfMotDePasse.setCaretColor(ACCENT_GOLD);
        tfMotDePasse.setFont(FONT_BODY);
        tfMotDePasse.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        form.add(tfMotDePasse, g);

        // Liste
        JPanel liste = createCard();
        liste.setLayout(new BorderLayout());
        liste.add(createLabel("■ REGISTRE DU PERSONNEL", ACCENT_BLUE, FONT_HEADER), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Matricule","Nom & Prénom","Fonction","Login","Salaire"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = createTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            try {
                int mat = Integer.parseInt(tableModel.getValueAt(row,0).toString());
                employeSelectionne = Supermarche.getInstance().getEmployes().stream()
                    .filter(emp -> emp.getMatricule() == mat).findFirst().orElse(null);
                if (employeSelectionne != null) {
                    tfMatricule.setText(String.valueOf(employeSelectionne.getMatricule()));
                    tfNom.setText(employeSelectionne.getNom());
                    tfPrenom.setText(employeSelectionne.getPrenom());
                    tfSalaire.setText(String.valueOf((int) employeSelectionne.getSalaire()));
                    tfLogin.setText(employeSelectionne.getLogin());
                    cbRole.setSelectedItem(employeSelectionne.afficherRole().replace(" de Rayon","Rayon").replace("Chef Rayon","ChefRayon"));
                }
            } catch (Exception ex) { /* ignore */ }
        });
        liste.add(new JScrollPane(table), BorderLayout.CENTER);

        corps.add(form);
        corps.add(liste);

        // Boutons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        actions.setBackground(BG_PANEL);
        JButton btnAjouter  = createButton("+ Ajouter",   ACCENT_GREEN);
        JButton btnModifier = createButton("✎ Modifier",   ACCENT_BLUE);
        JButton btnFermer   = createButton("Fermer",        new Color(80,80,100));

        btnAjouter.addActionListener(e -> ajouterEmploye());
        btnModifier.addActionListener(e -> modifierEmploye());
        btnFermer.addActionListener(e -> dispose());

        actions.add(btnAjouter); actions.add(btnModifier); actions.add(btnFermer);

        main.add(titre, BorderLayout.NORTH);
        main.add(corps, BorderLayout.CENTER);
        main.add(actions, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private void chargerEmployes() {
        tableModel.setRowCount(0);
        for (Employe e : Supermarche.getInstance().getEmployes()) {
            tableModel.addRow(new Object[]{
                String.format("%03d", e.getMatricule()),
                e.getNomComplet(),
                e.afficherRole(),
                e.getLogin(),
                String.format("%.0f F", e.getSalaire())
            });
        }
    }

    private void ajouterEmploye() {
        try {
            int mat     = Integer.parseInt(tfMatricule.getText().trim());
            String nom  = tfNom.getText().trim();
            String prn  = tfPrenom.getText().trim();
            double sal  = Double.parseDouble(tfSalaire.getText().trim());
            String role = (String) cbRole.getSelectedItem();
            String login = tfLogin.getText().trim();
            String mdp   = new String(tfMotDePasse.getPassword());

            if (nom.isEmpty() || login.isEmpty() || mdp.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Remplissez tous les champs (y compris login et mot de passe).");
                return;
            }
            employeService.creerEmploye(role, mat, nom, prn, sal, login, mdp);
            chargerEmployes();
            JOptionPane.showMessageDialog(this, "Employé ajouté.", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Vérifiez les valeurs numériques.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifierEmploye() {
        if (employeSelectionne == null) { JOptionPane.showMessageDialog(this, "Sélectionnez un employé."); return; }
        try {
            employeSelectionne.setNom(tfNom.getText().trim());
            employeSelectionne.setPrenom(tfPrenom.getText().trim());
            employeSelectionne.setSalaire(Double.parseDouble(tfSalaire.getText().trim()));
            if (!tfLogin.getText().trim().isEmpty()) employeSelectionne.setLogin(tfLogin.getText().trim());
            String mdp = new String(tfMotDePasse.getPassword());
            if (!mdp.isEmpty()) employeSelectionne.setMotDePasse(mdp);
            chargerEmployes();
            JOptionPane.showMessageDialog(this, "Employé modifié.", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Salaire invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}