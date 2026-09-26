package interfaceGraphique;

import model.*;
import service.RayonService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import static interfaceGraphique.StyleManager.*;

/**
 * Fenêtre de gestion des rayons.
 *
 * - Le Directeur voit la liste de TOUS les rayons, leur responsable et leur
 *   contenu ; il peut créer un rayon, y affecter des produits et
 *   nommer/changer son responsable.
 * - Le Chef de Rayon n'a accès qu'à la consultation des informations de
 *   SON propre rayon (lecture seule) ; c'est aussi sa fenêtre "d'accueil"
 *   (redirection directe après connexion, pas d'écran d'accueil général).
 */
public class FenetreRayon extends JFrame {

    private final Employe employe;
    private final boolean estDirecteur;
    private final boolean standalone;
    private final RayonService rayonService = new RayonService();

    private JTextField tfCode, tfNom;
    private JComboBox<String> cbResponsable, cbProduit;
    private DefaultTableModel tableRayons, tableProduits;
    private Rayon rayonSelectionne;

    /**
     * @param employe    l'employé connecté (Directeur ou ChefRayon)
     * @param standalone true si cette fenêtre sert de fenêtre "d'accueil"
     *                   directe (cas du Chef de Rayon) ; dans ce cas, la
     *                   fermer revient à se déconnecter.
     */
    public FenetreRayon(Employe employe, boolean standalone) {
        this.employe = employe;
        this.estDirecteur = employe instanceof Directeur;
        this.standalone = standalone;

        setTitle("Gestion des Rayons");
        setSize(900, 530);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        chargerRayons();

        if (standalone) {
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    new FenetreConnexion().setVisible(true);
                }
            });
        }
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_DARK);

        JPanel titre = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titre.setBackground(BG_PANEL);
        titre.setBorder(BorderFactory.createEmptyBorder(8,0,8,0));
        titre.add(createLabel("========== 🏪 GESTION DES RAYONS ==========", ACCENT_GOLD, FONT_TITLE));

        JPanel corps = new JPanel(new GridLayout(1,2,10,0));
        corps.setBackground(BG_DARK);
        corps.setBorder(BorderFactory.createEmptyBorder(10,10,5,10));

        // Partie droite : liste rayons + produits du rayon (toujours visible)
        JPanel rightPanel = new JPanel(new GridLayout(2,1,0,10));
        rightPanel.setBackground(BG_DARK);

        JPanel listeRayons = createCard();
        listeRayons.setLayout(new BorderLayout());
        listeRayons.add(createLabel(estDirecteur ? "[ Liste des Rayons ]" : "[ Mon Rayon ]", ACCENT_BLUE, FONT_HEADER), BorderLayout.NORTH);
        tableRayons = new DefaultTableModel(new String[]{"Code","Nom Rayon","Responsable","Nb Produits"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblRayons = createTable(tableRayons);
        tblRayons.getSelectionModel().addListSelectionListener(e -> selectionnerRayon(tblRayons));
        listeRayons.add(new JScrollPane(tblRayons), BorderLayout.CENTER);

        JPanel listeProduits = createCard();
        listeProduits.setLayout(new BorderLayout());
        listeProduits.add(createLabel("[ Contenu du Rayon sélectionné ]", ACCENT_BLUE, FONT_HEADER), BorderLayout.NORTH);
        tableProduits = new DefaultTableModel(new String[]{"Produit","Stock","Prix"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        listeProduits.add(new JScrollPane(createTable(tableProduits)), BorderLayout.CENTER);

        rightPanel.add(listeRayons);
        rightPanel.add(listeProduits);

        JPanel actions;

        if (estDirecteur) {
            // === Formulaire complet réservé au Directeur ===
            JPanel form = createCard();
            form.setLayout(new GridBagLayout());
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(6,5,6,5);
            g.fill = GridBagConstraints.HORIZONTAL;

            g.gridx=0; g.gridy=0; g.gridwidth=2;
            form.add(createLabel("[ Formulaire de Saisie ]", ACCENT_BLUE, FONT_HEADER), g);
            g.gridwidth=1;

            g.gridy=1; g.gridx=0; form.add(createLabel("Code rayon :", TEXT_MUTED, FONT_SMALL), g);
            g.gridx=1; tfCode = createField(); form.add(tfCode, g);

            g.gridy=2; g.gridx=0; form.add(createLabel("Nom rayon :", TEXT_MUTED, FONT_SMALL), g);
            g.gridx=1; tfNom = createField(); form.add(tfNom, g);

            g.gridy=3; g.gridx=0; form.add(createLabel("Responsable :", TEXT_MUTED, FONT_SMALL), g);
            g.gridx=1;
            cbResponsable = new JComboBox<>();
            cbResponsable.setBackground(BG_DARK); cbResponsable.setForeground(TEXT_PRIMARY); cbResponsable.setFont(FONT_BODY);
            cbResponsable.addItem("-- Aucun --");
            for (Employe e : Supermarche.getInstance().getEmployes())
                if (e instanceof ChefRayon) cbResponsable.addItem(e.getNomComplet());
            form.add(cbResponsable, g);

            g.gridy=4; g.gridx=0; form.add(createLabel("Associer un produit :", TEXT_MUTED, FONT_SMALL), g);
            g.gridx=1;
            cbProduit = new JComboBox<>();
            cbProduit.setBackground(BG_DARK); cbProduit.setForeground(TEXT_PRIMARY); cbProduit.setFont(FONT_BODY);
            cbProduit.addItem("-- Choisir un produit --");
            for (Produit p : Supermarche.getInstance().getProduits()) cbProduit.addItem(p.getDesignation());
            form.add(cbProduit, g);

            corps.add(form);
            corps.add(rightPanel);

            actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
            actions.setBackground(BG_PANEL);
            JButton btnCreer      = createButton("+ Créer Rayon",        ACCENT_GREEN);
            JButton btnAffecter   = createButton("⚙ Affecter Prod.",     ACCENT_BLUE);
            JButton btnResponsable= createButton("👤 Changer Responsable", ACCENT_GOLD);
            JButton btnStock      = createButton("📊 Stock Total",        ACCENT_GOLD);
            JButton btnFermer     = createButton("Fermer",                 new Color(80,80,100));
            btnCreer.addActionListener(e -> creerRayon());
            btnAffecter.addActionListener(e -> affecterProduit());
            btnResponsable.addActionListener(e -> changerResponsable());
            btnStock.addActionListener(e -> afficherStockTotal());
            btnFermer.addActionListener(e -> dispose());
            actions.add(btnCreer); actions.add(btnAffecter); actions.add(btnResponsable);
            actions.add(btnStock); actions.add(btnFermer);
        } else {
            // === Mode Chef de Rayon : consultation seule de son propre rayon ===
            corps.setLayout(new GridLayout(1,1));
            corps.add(rightPanel);

            actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
            actions.setBackground(BG_PANEL);
            JButton btnStock  = createButton("📊 Valeur du Stock", ACCENT_GOLD);
            JButton btnFermer = createButton(standalone ? "Se déconnecter" : "Fermer", new Color(80,80,100));
            btnStock.addActionListener(e -> afficherStockTotal());
            btnFermer.addActionListener(e -> dispose());
            actions.add(btnStock); actions.add(btnFermer);
        }

        main.add(titre, BorderLayout.NORTH);
        main.add(corps, BorderLayout.CENTER);
        main.add(actions, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private void chargerRayons() {
        tableRayons.setRowCount(0);
        List<Rayon> rayonsVisibles = rayonService.getRayonsVisibles(employe);
        for (Rayon r : rayonsVisibles) {
            tableRayons.addRow(new Object[]{
                String.format("RAY-%03d", r.getCodeRayon()),
                r.getNomRayon(),
                r.getResponsable() != null ? r.getResponsable().getNomComplet() : "N/A",
                r.getListeProduits().size()
            });
        }
        // Pour un chef de rayon, on affiche directement le contenu de son rayon.
        if (!estDirecteur && !rayonsVisibles.isEmpty()) {
            rayonSelectionne = rayonsVisibles.get(0);
            chargerProduitsDuRayon();
        }
    }

    private void selectionnerRayon(JTable tbl) {
        int row = tbl.getSelectedRow();
        if (row < 0) return;
        String code = tableRayons.getValueAt(row,0).toString().replace("RAY-","");
        try {
            int c = Integer.parseInt(code);
            rayonSelectionne = rayonService.getRayonsVisibles(employe).stream()
                .filter(r -> r.getCodeRayon() == c).findFirst().orElse(null);
            if (rayonSelectionne != null) chargerProduitsDuRayon();
        } catch (Exception ex) { /* ignore */ }
    }

    private void chargerProduitsDuRayon() {
        tableProduits.setRowCount(0);
        if (rayonSelectionne == null) return;
        for (Produit p : rayonSelectionne.getListeProduits()) {
            tableProduits.addRow(new Object[]{p.getDesignation(), p.getQuantiteStock(), String.format("%.0f F", p.getPrixVente())});
        }
    }

    private void creerRayon() {
        try {
            int code = Integer.parseInt(tfCode.getText().trim());
            String nom = tfNom.getText().trim();
            if (nom.isEmpty()) { JOptionPane.showMessageDialog(this, "Remplissez tous les champs."); return; }
            rayonService.creerRayon(code, nom);
            chargerRayons();
            JOptionPane.showMessageDialog(this, "Rayon créé.", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Code rayon invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void affecterProduit() {
        if (rayonSelectionne == null) { JOptionPane.showMessageDialog(this, "Sélectionnez un rayon."); return; }
        int idx = cbProduit.getSelectedIndex();
        if (idx <= 0) { JOptionPane.showMessageDialog(this, "Sélectionnez un produit."); return; }
        Produit p = Supermarche.getInstance().getProduits().get(idx - 1);
        rayonService.affecterProduit(rayonSelectionne, p);
        chargerProduitsDuRayon();
        chargerRayons();
        JOptionPane.showMessageDialog(this, "Produit affecté au rayon.", "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    private void changerResponsable() {
        if (rayonSelectionne == null) { JOptionPane.showMessageDialog(this, "Sélectionnez un rayon."); return; }
        int idx = cbResponsable.getSelectedIndex();
        if (idx <= 0) {
            rayonService.changerResponsable(rayonSelectionne, null);
        } else {
            String nomChoisi = (String) cbResponsable.getSelectedItem();
            ChefRayon chef = Supermarche.getInstance().getEmployes().stream()
                .filter(e -> e instanceof ChefRayon && e.getNomComplet().equals(nomChoisi))
                .map(e -> (ChefRayon) e)
                .findFirst().orElse(null);
            rayonService.changerResponsable(rayonSelectionne, chef);
        }
        chargerRayons();
        JOptionPane.showMessageDialog(this, "Responsable mis à jour.", "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    private void afficherStockTotal() {
        if (rayonSelectionne == null) { JOptionPane.showMessageDialog(this, "Sélectionnez un rayon."); return; }
        double val = rayonService.calculerValeurStock(rayonSelectionne);
        JOptionPane.showMessageDialog(this,
            String.format("📦 Valeur totale du stock du rayon %s :\n\n  >> %.0f F CFA", rayonSelectionne.getNomRayon(), val),
            "Stock Total", JOptionPane.INFORMATION_MESSAGE);
    }
}