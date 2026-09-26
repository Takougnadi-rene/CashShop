package interfaceGraphique;

import model.Produit;
import model.Supermarche;
import service.StockService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import static interfaceGraphique.StyleManager.*;

public class FenetreStock extends JFrame {

    private DefaultTableModel tableModel;
    private StockService stockService = new StockService();

    public FenetreStock() {
        setTitle("Gestion du Stock");
        setSize(780, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        chargerStock();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_DARK);

        // Titre
        JPanel titrePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titrePanel.setBackground(BG_PANEL);
        titrePanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        titrePanel.add(createLabel("========== 📦 GESTION DU STOCK ==========", ACCENT_GOLD, FONT_TITLE));

        // Légende
        JPanel legendePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        legendePanel.setBackground(BG_DARK);
        legendePanel.add(createLabel("  🔴 STOCK FAIBLE (≤5)  ", ACCENT_RED, FONT_SMALL));
        legendePanel.add(createLabel("  🟡 STOCK MOYEN (6-20)  ", ACCENT_GOLD, FONT_SMALL));
        legendePanel.add(createLabel("  🟢 STOCK OK (>20)  ", ACCENT_GREEN, FONT_SMALL));

        // Table
        tableModel = new DefaultTableModel(new String[]{"Réf","Désignation","Qté Disponible","Seuil Min","État Stock","Catégorie"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = createTable(tableModel);
        // Colorier la colonne "État Stock"
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                String etat = val != null ? val.toString() : "";
                if (etat.contains("FAIBLE")) comp.setForeground(ACCENT_RED);
                else if (etat.contains("MOYEN")) comp.setForeground(ACCENT_GOLD);
                else comp.setForeground(ACCENT_GREEN);
                comp.setBackground(BG_PANEL);
                return comp;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(BG_PANEL);

        // Boutons
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        actionsPanel.setBackground(BG_PANEL);

        JButton btnEntree   = createButton("+ Entrée Stock",   ACCENT_GREEN);
        JButton btnSortie   = createButton("- Sortie Stock",   ACCENT_RED);
        JButton btnRefresh  = createButton("↻ Actualiser",     ACCENT_BLUE);
        JButton btnAlerte   = createButton("⚠ Voir Alertes",   ACCENT_GOLD);
        JButton btnFermer   = createButton("Fermer",            new Color(80,80,100));

        btnEntree.addActionListener(e -> mouvementStock(true));
        btnSortie.addActionListener(e -> mouvementStock(false));
        btnRefresh.addActionListener(e -> chargerStock());
        btnAlerte.addActionListener(e -> afficherAlertes());
        btnFermer.addActionListener(e -> dispose());

        actionsPanel.add(btnEntree);
        actionsPanel.add(btnSortie);
        actionsPanel.add(btnRefresh);
        actionsPanel.add(btnAlerte);
        actionsPanel.add(btnFermer);

        main.add(titrePanel, BorderLayout.NORTH);
        main.add(legendePanel, BorderLayout.CENTER);
        main.add(scroll, BorderLayout.CENTER);
        // Fix layout
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BG_DARK);
        centerPanel.add(legendePanel, BorderLayout.NORTH);
        centerPanel.add(scroll, BorderLayout.CENTER);
        main.add(centerPanel, BorderLayout.CENTER);
        main.add(actionsPanel, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void chargerStock() {
        tableModel.setRowCount(0);
        for (Produit p : Supermarche.getInstance().getProduits()) {
            String etat;
            if (p.getQuantiteStock() <= 5) etat = "🔴 STOCK FAIBLE";
            else if (p.getQuantiteStock() <= 20) etat = "🟡 STOCK MOYEN";
            else etat = "🟢 STOCK OK";

            tableModel.addRow(new Object[]{
                String.format("%03d", p.getReference()),
                p.getDesignation(),
                p.getQuantiteStock(),
                5,
                etat,
                p.getCategorie()
            });
        }
    }

    private void mouvementStock(boolean entree) {
        String refStr = JOptionPane.showInputDialog(this, "Référence du produit :");
        if (refStr == null) return;
        String qteStr = JOptionPane.showInputDialog(this, "Quantité :");
        if (qteStr == null) return;
        try {
            int ref = Integer.parseInt(refStr.trim());
            int qte = Integer.parseInt(qteStr.trim());
            if (entree) {
                stockService.entreeStock(ref, qte);
                JOptionPane.showMessageDialog(this, "Entrée de " + qte + " unité(s) enregistrée.", "Succès", JOptionPane.INFORMATION_MESSAGE);
            } else {
                boolean ok = stockService.sortieStock(ref, qte);
                if (ok) JOptionPane.showMessageDialog(this, "Sortie de " + qte + " unité(s) enregistrée.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                else JOptionPane.showMessageDialog(this, "Stock insuffisant.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
            chargerStock();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valeur invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void afficherAlertes() {
        StringBuilder sb = new StringBuilder("⚠ PRODUITS EN STOCK FAIBLE :\n\n");
        for (Produit p : stockService.verifierStockMinimum())
            sb.append("  • ").append(p.getDesignation()).append(" → ").append(p.getQuantiteStock()).append(" unités\n");
        JOptionPane.showMessageDialog(this, sb.toString(), "Alertes Stock", JOptionPane.WARNING_MESSAGE);
    }
}
