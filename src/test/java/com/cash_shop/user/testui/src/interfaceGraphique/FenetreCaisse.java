package interfaceGraphique;

import model.*;
import service.FactureService;
import service.PaiementService;
import service.VenteService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import static interfaceGraphique.StyleManager.*;

/**
 * Fenêtre de caisse. C'est la fenêtre "d'accueil" du Caissier : il y est
 * dirigé directement après connexion (pas d'écran d'accueil général pour
 * lui). Fermer cette fenêtre revient donc à se déconnecter.
 */
public class FenetreCaisse extends JFrame {

    private final Caissier caissier;
    private final VenteService venteService = new VenteService();
    private final PaiementService paiementService = new PaiementService();
    private final FactureService factureService = new FactureService();

    private Vente venteEnCours;
    private Facture derniereFacture;

    private JComboBox<String> cbClient;
    private JComboBox<String> cbProduit;
    private JTextField tfQuantite;
    private JLabel lblTotal;
    private DefaultTableModel panierModel;
    private JRadioButton rbEspeces, rbCarte, rbMobile;

    public FenetreCaisse(Caissier caissier) {
        this.caissier = caissier;
        nouvelleVente();

        setTitle("Caisse " + caissier.getNumeroCaisse() + " - Module de Vente");
        setSize(900, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();

        // Fermer la caisse = se déconnecter (retour à l'écran de connexion)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                new FenetreConnexion().setVisible(true);
            }
        });
    }

    private void nouvelleVente() {
        venteEnCours = venteService.nouvelleVente(null, caissier);
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_DARK);

        // Titre
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        header.add(createLabel("💰 CAISSE " + caissier.getNumeroCaisse(), ACCENT_GOLD, FONT_TITLE), BorderLayout.WEST);
        header.add(createLabel("⚙ Écran d'Enregistrement", TEXT_MUTED, FONT_BODY), BorderLayout.EAST);

        // Corps
        JPanel corps = new JPanel(new GridLayout(1, 2, 10, 0));
        corps.setBackground(BG_DARK);
        corps.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        // Panneau SAISIE
        JPanel saisiePanel = createCard();
        saisiePanel.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,5,6,5);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx=0; g.gridy=0; g.gridwidth=2;
        saisiePanel.add(createLabel("[ Saisie Article ]", ACCENT_BLUE, FONT_HEADER), g);
        g.gridwidth=1;

        // Client
        g.gridy=1; g.gridx=0; saisiePanel.add(createLabel("Client :", TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1;
        cbClient = new JComboBox<>();
        cbClient.setBackground(BG_DARK); cbClient.setForeground(TEXT_PRIMARY); cbClient.setFont(FONT_BODY);
        cbClient.addItem("Tout-Venant");
        for (Client c : Supermarche.getInstance().getClients()) cbClient.addItem(c.getNom());
        saisiePanel.add(cbClient, g);

        // Produit
        g.gridy=2; g.gridx=0; saisiePanel.add(createLabel("Scanner / Sélectionner Produit :", TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1;
        cbProduit = new JComboBox<>();
        cbProduit.setBackground(BG_DARK); cbProduit.setForeground(TEXT_PRIMARY); cbProduit.setFont(FONT_BODY);
        for (Produit p : Supermarche.getInstance().getProduits())
            cbProduit.addItem(p.getDesignation() + " - " + String.format("%.0f F", p.getPrixVente()));
        saisiePanel.add(cbProduit, g);

        // Quantité
        g.gridy=3; g.gridx=0; saisiePanel.add(createLabel("Quantité :", TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1; tfQuantite = createField(); tfQuantite.setText("1"); saisiePanel.add(tfQuantite, g);

        // Bouton ajouter
        g.gridy=4; g.gridx=0; g.gridwidth=2;
        JButton btnAjouter = createButton("🛒 Ajouter au Panier", ACCENT_GREEN);
        btnAjouter.setPreferredSize(new Dimension(200, 36));
        btnAjouter.addActionListener(e -> ajouterAuPanier());
        saisiePanel.add(btnAjouter, g);

        // Séparateur
        g.gridy=5; g.gridwidth=2;
        saisiePanel.add(new JSeparator(), g);

        // Mode de règlement
        g.gridy=6; g.gridwidth=2;
        saisiePanel.add(createLabel("[ Mode de Règlement ]", ACCENT_BLUE, FONT_HEADER), g);

        rbEspeces = new JRadioButton("💵 Espèces");
        rbCarte   = new JRadioButton("💳 Carte Bancaire");
        rbMobile  = new JRadioButton("📱 Mobile Money");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbEspeces); bg.add(rbCarte); bg.add(rbMobile);
        rbEspeces.setSelected(true);

        for (JRadioButton rb : new JRadioButton[]{rbEspeces, rbCarte, rbMobile}) {
            rb.setBackground(BG_CARD); rb.setForeground(TEXT_PRIMARY); rb.setFont(FONT_BODY);
        }
        g.gridy=7; g.gridwidth=2; saisiePanel.add(rbEspeces, g);
        g.gridy=8; saisiePanel.add(rbCarte, g);
        g.gridy=9; saisiePanel.add(rbMobile, g);

        // Panneau PANIER
        JPanel panierPanel = createCard();
        panierPanel.setLayout(new BorderLayout(5,5));
        panierPanel.add(createLabel("[ 🛒 Panier Client ]", ACCENT_BLUE, FONT_HEADER), BorderLayout.NORTH);

        panierModel = new DefaultTableModel(new String[]{"Produit","Qté","Prix Unit.","Sous-Total"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablePanier = createTable(panierModel);
        panierPanel.add(new JScrollPane(tablePanier), BorderLayout.CENTER);

        lblTotal = createLabel("TOTAL À PAYER :    0 FCFA", ACCENT_GREEN, new Font("Consolas", Font.BOLD, 14));
        lblTotal.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        panierPanel.add(lblTotal, BorderLayout.SOUTH);

        corps.add(saisiePanel);
        corps.add(panierPanel);

        // Boutons bas
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        actionsPanel.setBackground(BG_PANEL);

        JButton btnValider  = createButton("✔ VALIDER LA VENTE",   ACCENT_GREEN);
        JButton btnFacture  = createButton("🖨 VOIR FACTURE",       ACCENT_BLUE);
        JButton btnAnnuler  = createButton("✕ Annuler Vente",       ACCENT_RED);
        JButton btnFermer   = createButton("Fermer",                 new Color(80,80,100));

        btnValider.setPreferredSize(new Dimension(180, 36));
        btnFacture.setPreferredSize(new Dimension(180, 36));

        btnValider.addActionListener(e -> validerVente());
        btnFacture.addActionListener(e -> voirDerniereFacture());
        btnAnnuler.addActionListener(e -> annulerVente());
        btnFermer.addActionListener(e -> dispose());

        actionsPanel.add(btnValider);
        actionsPanel.add(btnFacture);
        actionsPanel.add(btnAnnuler);
        actionsPanel.add(btnFermer);

        main.add(header, BorderLayout.NORTH);
        main.add(corps, BorderLayout.CENTER);
        main.add(actionsPanel, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private void ajouterAuPanier() {
        int idx = cbProduit.getSelectedIndex();
        if (idx < 0 || Supermarche.getInstance().getProduits().isEmpty()) return;
        Produit p = Supermarche.getInstance().getProduits().get(idx);
        try {
            int qte = Integer.parseInt(tfQuantite.getText().trim());
            if (!venteService.ajouterProduitAuPanier(venteEnCours, p, qte)) {
                JOptionPane.showMessageDialog(this, "Stock insuffisant ! Disponible : " + p.getQuantiteStock());
                return;
            }
            panierModel.addRow(new Object[]{
                p.getDesignation(), qte,
                String.format("%.0f F", p.getPrixVente()),
                String.format("%.0f F", p.getPrixVente() * qte)
            });
            lblTotal.setText(String.format("  🟢 TOTAL À PAYER :   %.0f FCFA", venteEnCours.calculerTotal()));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantité invalide.");
        }
    }

    /**
     * 
     */
    private void validerVente() {
        if (venteEnCours.getPanier().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le panier est vide.");
            return;
        }

        // Définir le client sélectionné (reconstruit le panier sur la nouvelle vente)
        int idxClient = cbClient.getSelectedIndex();
        if (idxClient > 0) {
            Client c = Supermarche.getInstance().getClients().get(idxClient - 1);
            Vente venteAvecClient = venteService.nouvelleVente(c, caissier);
            for (int i = 0; i < panierModel.getRowCount(); i++) {
                String nom = panierModel.getValueAt(i,0).toString();
                int qte = (int) panierModel.getValueAt(i,1);
                Produit p = Supermarche.getInstance().rechercherProduitParNom(nom);
                if (p != null) venteAvecClient.ajouterProduit(p, qte);
            }
            venteEnCours = venteAvecClient;
        }

        Paiement.ModePaiement mode = rbCarte.isSelected() ? Paiement.ModePaiement.CARTE_BANCAIRE :
                rbMobile.isSelected() ? Paiement.ModePaiement.MOBILE_MONEY : Paiement.ModePaiement.ESPECES;

        Paiement paiement;
        if (paiementService.necessiteMontantRecu(mode)) {
            // Règle métier : le montant reçu n'est demandé que pour un paiement en Espèces.
            String montantStr = JOptionPane.showInputDialog(this,
                    String.format("Montant reçu du client (Total: %.0f FCFA) :", venteEnCours.calculerTotal()));
            if (montantStr == null) return;
            try {
                double montantRecu = Double.parseDouble(montantStr.trim());
                paiement = paiementService.creerPaiement(montantRecu, mode, venteEnCours);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Montant invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            // Carte bancaire / Mobile Money : montant exact, pas de monnaie à rendre.
            paiement = paiementService.creerPaiementExact(mode, venteEnCours);
        }

        if (!paiement.validerPaiement()) {
            JOptionPane.showMessageDialog(this, "Montant insuffisant.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        venteService.finaliserVente(venteEnCours);
        derniereFacture = factureService.genererFacture(venteEnCours, paiement);

        JOptionPane.showMessageDialog(this,
            String.format("✅ Paiement validé avec succès.\n💳 Mode : %s\n💰 Monnaie : %.0f F CFA\n\nFacture prête à être imprimée.", mode, paiement.getMonnaie()),
            "Paiement validé", JOptionPane.INFORMATION_MESSAGE);

        new FenetreFacture(derniereFacture).setVisible(true);

        nouvelleVente();
        panierModel.setRowCount(0);
        lblTotal.setText("TOTAL À PAYER :    0 FCFA");
    }

    private void voirDerniereFacture() {
        if (derniereFacture == null) {
            JOptionPane.showMessageDialog(this, "Aucune facture à afficher. Validez d'abord une vente.");
            return;
        }
        new FenetreFacture(derniereFacture).setVisible(true);
    }

    private void annulerVente() {
        int conf = JOptionPane.showConfirmDialog(this, "Annuler la vente en cours ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            nouvelleVente();
            panierModel.setRowCount(0);
            lblTotal.setText("TOTAL À PAYER :    0 FCFA");
        }
    }
}