package interfaceGraphique;

import model.Produit;
import model.Supermarche;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import static interfaceGraphique.StyleManager.*;

public class FenetreProduit extends JFrame {

    private JTextField tfRef, tfDes, tfPrixAchat, tfPrixVente, tfStock, tfRecherche;
    private JComboBox<String> cbCategorie;
    private DefaultTableModel tableModel;
    private JTable table;
    private Produit produitSelectionne;

    public FenetreProduit() {
        setTitle("Gestion des Produits");
        setSize(900, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        chargerProduits();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_DARK);

        // Titre
        JPanel titrePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titrePanel.setBackground(BG_PANEL);
        titrePanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        titrePanel.add(createLabel("========== ⚙ GESTION DES PRODUITS ==========", ACCENT_GOLD, FONT_TITLE));

        // Corps
        JPanel corps = new JPanel(new GridLayout(1, 2, 10, 0));
        corps.setBackground(BG_DARK);
        corps.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        // Formulaire
        JPanel formPanel = createCard();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx=0; g.gridy=0; g.gridwidth=2;
        formPanel.add(createLabel("[ Formulaire Produit ]", ACCENT_BLUE, FONT_HEADER), g);
        g.gridwidth=1;

        String[] labels = {"Référence :", "Désignation :", "Prix Achat :", "Prix Vente :", "Stock Initial :", "Catégorie :"};
        g.gridy=1; g.gridx=0; formPanel.add(createLabel(labels[0], TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1; tfRef = createField(); formPanel.add(tfRef, g);

        g.gridy=2; g.gridx=0; formPanel.add(createLabel(labels[1], TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1; tfDes = createField(); formPanel.add(tfDes, g);

        g.gridy=3; g.gridx=0; formPanel.add(createLabel(labels[2], TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1; tfPrixAchat = createField(); formPanel.add(tfPrixAchat, g);

        g.gridy=4; g.gridx=0; formPanel.add(createLabel(labels[3], TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1; tfPrixVente = createField(); formPanel.add(tfPrixVente, g);

        g.gridy=5; g.gridx=0; formPanel.add(createLabel(labels[4], TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1; tfStock = createField(); formPanel.add(tfStock, g);

        g.gridy=6; g.gridx=0; formPanel.add(createLabel(labels[5], TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1;
        cbCategorie = new JComboBox<>(new String[]{"Alimentaire","Frais","Electronique","Entretien","Boulangerie","Boucherie","Poissonnerie"});
        cbCategorie.setBackground(BG_DARK);
        cbCategorie.setForeground(TEXT_PRIMARY);
        cbCategorie.setFont(FONT_BODY);
        formPanel.add(cbCategorie, g);

        // Champ recherche
        g.gridy=7; g.gridx=0; formPanel.add(createLabel("Recherche :", TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1; tfRecherche = createField(); formPanel.add(tfRecherche, g);

        // Liste produits
        JPanel listePanel = createCard();
        listePanel.setLayout(new BorderLayout());
        listePanel.add(createLabel("[ Liste des Produits ]", ACCENT_BLUE, FONT_HEADER), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Réf", "Désignation", "Prix Vente", "Stock", "Catégorie"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = createTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> selectionnerProduit());
        listePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        corps.add(formPanel);
        corps.add(listePanel);

        // Boutons actions
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        actionsPanel.setBackground(BG_PANEL);
        actionsPanel.add(createLabel("[ Actions ]", TEXT_MUTED, FONT_SMALL));

        JButton btnAjouter   = createButton("+ Ajouter",    ACCENT_GREEN);
        JButton btnModifier  = createButton("✎ Modifier",   ACCENT_BLUE);
        JButton btnSupprimer = createButton("✕ Supprimer",  ACCENT_RED);
        JButton btnRecherche = createButton("⚲ Rechercher", ACCENT_GOLD);
        JButton btnFermer    = createButton("Fermer",        new Color(80,80,100));

        btnAjouter.addActionListener(e -> ajouterProduit());
        btnModifier.addActionListener(e -> modifierProduit());
        btnSupprimer.addActionListener(e -> supprimerProduit());
        btnRecherche.addActionListener(e -> rechercherProduit());
        btnFermer.addActionListener(e -> dispose());

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

    private void chargerProduits() {
        tableModel.setRowCount(0);
        for (Produit p : Supermarche.getInstance().getProduits()) {
            tableModel.addRow(new Object[]{
                String.format("%03d", p.getReference()),
                p.getDesignation(),
                String.format("%.0f F", p.getPrixVente()),
                p.getQuantiteStock(),
                p.getCategorie()
            });
        }
    }

    private void selectionnerProduit() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        try {
            int ref = Integer.parseInt(tableModel.getValueAt(row, 0).toString().trim());
            produitSelectionne = Supermarche.getInstance().rechercherProduit(ref);
            if (produitSelectionne != null) {
                tfRef.setText(String.valueOf(produitSelectionne.getReference()));
                tfDes.setText(produitSelectionne.getDesignation());
                tfPrixAchat.setText(String.valueOf((int)produitSelectionne.getPrixAchat()));
                tfPrixVente.setText(String.valueOf((int)produitSelectionne.getPrixVente()));
                tfStock.setText(String.valueOf(produitSelectionne.getQuantiteStock()));
            }
        } catch (Exception ex) { /* ignore */ }
    }

    private void ajouterProduit() {
        try {
            int ref       = Integer.parseInt(tfRef.getText().trim());
            String des    = tfDes.getText().trim();
            double achat  = Double.parseDouble(tfPrixAchat.getText().trim());
            double vente  = Double.parseDouble(tfPrixVente.getText().trim());
            int stock     = Integer.parseInt(tfStock.getText().trim());
            String cat    = (String) cbCategorie.getSelectedItem();

            if (des.isEmpty()) { JOptionPane.showMessageDialog(this, "Remplissez tous les champs."); return; }
            if (Supermarche.getInstance().rechercherProduit(ref) != null) {
                JOptionPane.showMessageDialog(this, "Référence déjà existante."); return;
            }
            Supermarche.getInstance().ajouterProduit(new Produit(ref, des, achat, vente, stock, cat));
            chargerProduits();
            viderFormulaire();
            JOptionPane.showMessageDialog(this, "Produit ajouté avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Vérifiez les valeurs numériques.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifierProduit() {
        if (produitSelectionne == null) { JOptionPane.showMessageDialog(this, "Sélectionnez un produit."); return; }
        try {
            produitSelectionne.setDesignation(tfDes.getText().trim());
            produitSelectionne.setPrixAchat(Double.parseDouble(tfPrixAchat.getText().trim()));
            produitSelectionne.modifierPrix(Double.parseDouble(tfPrixVente.getText().trim()));
            produitSelectionne.setQuantiteStock(Integer.parseInt(tfStock.getText().trim()));
            produitSelectionne.setCategorie((String) cbCategorie.getSelectedItem());
            chargerProduits();
            JOptionPane.showMessageDialog(this, "Produit modifié.", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de saisie.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimerProduit() {
        if (produitSelectionne == null) { JOptionPane.showMessageDialog(this, "Sélectionnez un produit."); return; }
        int conf = JOptionPane.showConfirmDialog(this, "Supprimer " + produitSelectionne.getDesignation() + " ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            Supermarche.getInstance().supprimerProduit(produitSelectionne.getReference());
            produitSelectionne = null;
            chargerProduits();
            viderFormulaire();
        }
    }

    private void rechercherProduit() {
        String terme = tfRecherche.getText().trim();
        tableModel.setRowCount(0);
        for (Produit p : Supermarche.getInstance().getProduits()) {
            if (p.getDesignation().toLowerCase().contains(terme.toLowerCase()) ||
                String.valueOf(p.getReference()).contains(terme)) {
                tableModel.addRow(new Object[]{
                    String.format("%03d", p.getReference()),
                    p.getDesignation(),
                    String.format("%.0f F", p.getPrixVente()),
                    p.getQuantiteStock(),
                    p.getCategorie()
                });
            }
        }
    }

    private void viderFormulaire() {
        tfRef.setText(""); tfDes.setText(""); tfPrixAchat.setText("");
        tfPrixVente.setText(""); tfStock.setText(""); tfRecherche.setText("");
        produitSelectionne = null;
    }
}
