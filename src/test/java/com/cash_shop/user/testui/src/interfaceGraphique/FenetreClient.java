package interfaceGraphique;

import model.Client;
import model.Supermarche;
import model.Vente;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import static interfaceGraphique.StyleManager.*;

public class FenetreClient extends JFrame {

    private JTextField tfNum, tfNom, tfTel;
    private DefaultTableModel tableModel;
    private Client clientSelectionne;

    public FenetreClient() {
        setTitle("Gestion des Clients");
        setSize(850, 530);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        chargerClients();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_DARK);

        JPanel titre = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titre.setBackground(BG_PANEL);
        titre.setBorder(BorderFactory.createEmptyBorder(8,0,8,0));
        titre.add(createLabel("========== 👥 GESTION DES CLIENTS ==========", ACCENT_GOLD, FONT_TITLE));

        JPanel corps = new JPanel(new GridLayout(1, 2, 10, 0));
        corps.setBackground(BG_DARK);
        corps.setBorder(BorderFactory.createEmptyBorder(10,10,5,10));

        // Formulaire
        JPanel form = createCard();
        form.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,5,6,5);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx=0; g.gridy=0; g.gridwidth=2;
        form.add(createLabel("[ Informations Client ]", ACCENT_BLUE, FONT_HEADER), g);
        g.gridwidth=1;

        g.gridy=1; g.gridx=0; form.add(createLabel("N° Client :", TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1; tfNum = createField(); form.add(tfNum, g);

        g.gridy=2; g.gridx=0; form.add(createLabel("Nom Complet :", TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1; tfNom = createField(); form.add(tfNom, g);

        g.gridy=3; g.gridx=0; form.add(createLabel("Téléphone :", TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1; tfTel = createField(); form.add(tfTel, g);

        // Panneau fidélité
        g.gridy=4; g.gridwidth=2;
        form.add(createLabel("[ Statut Fidélité ]", ACCENT_BLUE, FONT_HEADER), g);
        g.gridy=5;
        JLabel lblPoints = createLabel("  ★ Sélectionnez un client pour voir ses points.", TEXT_MUTED, FONT_SMALL);
        form.add(lblPoints, g);

        // Liste clients
        JPanel liste = createCard();
        liste.setLayout(new BorderLayout());
        liste.add(createLabel("[ Liste des Clients ]", ACCENT_BLUE, FONT_HEADER), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"N°","Nom","Téléphone","Points","Niveau"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = createTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            try {
                int num = Integer.parseInt(tableModel.getValueAt(row,0).toString());
                clientSelectionne = Supermarche.getInstance().getClients().stream()
                    .filter(c -> c.getNumeroClient() == num).findFirst().orElse(null);
                if (clientSelectionne != null) {
                    tfNum.setText(String.valueOf(clientSelectionne.getNumeroClient()));
                    tfNom.setText(clientSelectionne.getNom());
                    tfTel.setText(clientSelectionne.getTelephone());
                    lblPoints.setText("  ★ " + clientSelectionne.getPointsFidelite() + " Points | Niveau : " + clientSelectionne.getNiveauFidelite());
                }
            } catch (Exception ex) { /* ignore */ }
        });
        liste.add(new JScrollPane(table), BorderLayout.CENTER);

        corps.add(form);
        corps.add(liste);

        // Boutons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        actions.setBackground(BG_PANEL);

        JButton btnAjouter   = createButton("+ Ajouter",        ACCENT_GREEN);
        JButton btnModifier  = createButton("✎ Modifier",        ACCENT_BLUE);
        JButton btnHistorique= createButton("📋 Historique",     ACCENT_GOLD);
        JButton btnFermer    = createButton("Fermer",             new Color(80,80,100));

        btnAjouter.addActionListener(e -> ajouterClient());
        btnModifier.addActionListener(e -> modifierClient());
        btnHistorique.addActionListener(e -> voirHistorique());
        btnFermer.addActionListener(e -> dispose());

        actions.add(btnAjouter); actions.add(btnModifier); actions.add(btnHistorique); actions.add(btnFermer);

        main.add(titre, BorderLayout.NORTH);
        main.add(corps, BorderLayout.CENTER);
        main.add(actions, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private void chargerClients() {
        tableModel.setRowCount(0);
        for (Client c : Supermarche.getInstance().getClients()) {
            tableModel.addRow(new Object[]{c.getNumeroClient(), c.getNom(), c.getTelephone(), c.getPointsFidelite(), c.getNiveauFidelite()});
        }
    }

    private void ajouterClient() {
        try {
            int num = Integer.parseInt(tfNum.getText().trim());
            String nom = tfNom.getText().trim();
            String tel = tfTel.getText().trim();
            if (nom.isEmpty()) { JOptionPane.showMessageDialog(this, "Remplissez tous les champs."); return; }
            Supermarche.getInstance().ajouterClient(new Client(num, nom, tel));
            chargerClients();
            JOptionPane.showMessageDialog(this, "Client ajouté.", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "N° client invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifierClient() {
        if (clientSelectionne == null) { JOptionPane.showMessageDialog(this, "Sélectionnez un client."); return; }
        clientSelectionne.setNom(tfNom.getText().trim());
        clientSelectionne.setTelephone(tfTel.getText().trim());
        chargerClients();
        JOptionPane.showMessageDialog(this, "Client modifié.", "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    private void voirHistorique() {
        if (clientSelectionne == null) { JOptionPane.showMessageDialog(this, "Sélectionnez un client."); return; }
        StringBuilder sb = new StringBuilder("📋 HISTORIQUE D'ACHATS DE " + clientSelectionne.getNom() + "\n\n");
        for (Vente v : clientSelectionne.getHistoriqueAchats()) sb.append("  ").append(v).append("\n");
        if (clientSelectionne.getHistoriqueAchats().isEmpty()) sb.append("  Aucun achat enregistré.");
        JOptionPane.showMessageDialog(this, sb.toString(), "Historique", JOptionPane.INFORMATION_MESSAGE);
    }
}
