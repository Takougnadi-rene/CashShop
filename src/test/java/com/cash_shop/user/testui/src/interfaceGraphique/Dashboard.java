package interfaceGraphique;

import model.Produit;
import model.Supermarche;
import service.StatistiqueService;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import static interfaceGraphique.StyleManager.*;

public class Dashboard extends JFrame {

    private final StatistiqueService statistiqueService = new StatistiqueService();

    public Dashboard() {
        setTitle("Tableau de Bord - Direction");
        setSize(820, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout(0, 10));
        main.setBackground(BG_DARK);
        main.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // En-tête
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        header.add(createLabel("■ TABLEAU DE BORD GLOBAL", ACCENT_GOLD, FONT_TITLE), BorderLayout.WEST);
        header.add(createLabel("⚙ Situation au " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()), TEXT_MUTED, FONT_BODY), BorderLayout.EAST);

        // Corps en grille 2x2
        JPanel corps = new JPanel(new GridLayout(2, 2, 10, 10));
        corps.setBackground(BG_DARK);
        corps.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        Supermarche sm = Supermarche.getInstance();

        // Carte 1 : Performance financière
        JPanel finPanel = createCard();
        finPanel.setLayout(new BoxLayout(finPanel, BoxLayout.Y_AXIS));
        finPanel.add(createLabel("⚡ [ PERFORMANCE FINANCIÈRE ]", ACCENT_GOLD, FONT_HEADER));
        finPanel.add(Box.createVerticalStrut(8));
        finPanel.add(createLabel("Chiffre d'Affaires Total :", TEXT_MUTED, FONT_SMALL));
        finPanel.add(createLabel(String.format("  >> %.0f FCFA", statistiqueService.calculerChiffreAffaires()), ACCENT_GREEN, new Font("Consolas", Font.BOLD, 14)));
        finPanel.add(Box.createVerticalStrut(6));
        finPanel.add(createLabel("Nombre de Ventes Réalisées :", TEXT_MUTED, FONT_SMALL));
        finPanel.add(createLabel("  >> " + sm.getVentes().size() + " Transaction(s)", TEXT_PRIMARY, FONT_BODY));

        // Carte 2 : État de l'inventaire
        JPanel invPanel = createCard();
        invPanel.setLayout(new BoxLayout(invPanel, BoxLayout.Y_AXIS));
        invPanel.add(createLabel("⚙ [ ÉTAT DE L'INVENTAIRE ]", ACCENT_GOLD, FONT_HEADER));
        invPanel.add(Box.createVerticalStrut(8));
        invPanel.add(createLabel("Nombre Total de Produits :", TEXT_MUTED, FONT_SMALL));
        invPanel.add(createLabel("  >> " + sm.getProduits().size() + " Références", TEXT_PRIMARY, FONT_BODY));
        invPanel.add(Box.createVerticalStrut(6));
        invPanel.add(createLabel("Valeur Totale du Stock :", TEXT_MUTED, FONT_SMALL));
        invPanel.add(createLabel(String.format("  >> %.0f FCFA", statistiqueService.calculerValeurTotaleStock()), ACCENT_GREEN, new Font("Consolas", Font.BOLD, 14)));

        // Carte 3 : Alertes & Points de vigilance
        JPanel alertPanel = createCard();
        alertPanel.setLayout(new BoxLayout(alertPanel, BoxLayout.Y_AXIS));
        alertPanel.add(createLabel("▲ [ ALERTES & POINTS VIGILANCE ]", ACCENT_RED, FONT_HEADER));
        alertPanel.add(Box.createVerticalStrut(8));

        java.util.ArrayList<Produit> stockFaible = statistiqueService.getProduitsStockFaible();
        java.util.ArrayList<Produit> expires = statistiqueService.getProduitsExpires();

        JPanel ligneStock = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ligneStock.setBackground(BG_CARD);
        ligneStock.add(createLabel("🔴 Produits Expirés : " + expires.size() + " article(s)", ACCENT_RED, FONT_BODY));
        JButton btnVoirExpires = createButton("→ Voir", new Color(100,30,30));
        btnVoirExpires.setPreferredSize(new Dimension(90, 26));
        btnVoirExpires.addActionListener(e -> voirExpires(expires));
        ligneStock.add(btnVoirExpires);
        alertPanel.add(ligneStock);

        JPanel ligneExpire = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ligneExpire.setBackground(BG_CARD);
        ligneExpire.add(createLabel("♦ Stock Faible :    " + stockFaible.size() + " article(s)", ACCENT_GOLD, FONT_BODY));
        JButton btnVoirStock = createButton("→ Voir", new Color(100,80,0));
        btnVoirStock.setPreferredSize(new Dimension(90, 26));
        btnVoirStock.addActionListener(e -> voirStockFaible(stockFaible));
        ligneExpire.add(btnVoirStock);
        alertPanel.add(ligneExpire);

        // Carte 4 : Raccourcis de navigation
        JPanel navPanel = createCard();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.add(createLabel("[ Raccourcis de Navigation ]", TEXT_MUTED, FONT_HEADER));
        navPanel.add(Box.createVerticalStrut(10));

        JPanel btnRaccourcis = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRaccourcis.setBackground(BG_CARD);
        JButton btnVentes  = createButton("■ Voir Ventes", ACCENT_BLUE);
        JButton btnStats   = createButton("📊 Actualiser", ACCENT_GOLD);

        btnVentes.addActionListener(e -> voirVentes());
        btnStats.addActionListener(e -> { dispose(); new Dashboard().setVisible(true); });

        btnRaccourcis.add(btnVentes);
        btnRaccourcis.add(btnStats);
        navPanel.add(btnRaccourcis);

        corps.add(finPanel);
        corps.add(invPanel);
        corps.add(alertPanel);
        corps.add(navPanel);

        // Bouton fermer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(BG_DARK);
        JButton btnFermer = createButton("Fermer", new Color(80,80,100));
        btnFermer.addActionListener(e -> dispose());
        footer.add(btnFermer);

        main.add(header, BorderLayout.NORTH);
        main.add(corps, BorderLayout.CENTER);
        main.add(footer, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private void voirExpires(java.util.ArrayList<Produit> liste) {
        StringBuilder sb = new StringBuilder("🔴 PRODUITS EXPIRÉS :\n\n");
        for (Produit p : liste) sb.append("  • ").append(p.getDesignation()).append("\n");
        if (liste.isEmpty()) sb.append("  Aucun produit expiré.");
        JOptionPane.showMessageDialog(this, sb.toString(), "Produits Expirés", JOptionPane.WARNING_MESSAGE);
    }

    private void voirStockFaible(java.util.ArrayList<Produit> liste) {
        StringBuilder sb = new StringBuilder("♦ PRODUITS EN STOCK FAIBLE :\n\n");
        for (Produit p : liste) sb.append("  • ").append(p.getDesignation()).append(" → ").append(p.getQuantiteStock()).append(" unités\n");
        if (liste.isEmpty()) sb.append("  Tous les stocks sont satisfaisants.");
        JOptionPane.showMessageDialog(this, sb.toString(), "Stock Faible", JOptionPane.WARNING_MESSAGE);
    }

    private void voirVentes() {
        StringBuilder sb = new StringBuilder("■ HISTORIQUE DES VENTES :\n\n");
        for (model.Vente v : Supermarche.getInstance().getVentes())
            sb.append("  ").append(v).append("\n");
        if (Supermarche.getInstance().getVentes().isEmpty()) sb.append("  Aucune vente enregistrée.");
        JOptionPane.showMessageDialog(this, sb.toString(), "Ventes", JOptionPane.INFORMATION_MESSAGE);
    }
}