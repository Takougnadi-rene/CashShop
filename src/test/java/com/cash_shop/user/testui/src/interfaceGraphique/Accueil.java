package interfaceGraphique;

import model.Employe;
import service.AccesService;
import service.AccesService.Module;
import service.StatistiqueService;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import static interfaceGraphique.StyleManager.*;

public class Accueil extends JFrame {

    private final Employe employe;
    private final AccesService accesService = new AccesService();
    private final StatistiqueService statistiqueService = new StatistiqueService();

    private JLabel lblDateHeure;
    private JLabel lblVentesJour;
    private JLabel lblAlerteStock;

    public Accueil(Employe employe) {
        this.employe = employe;
        setTitle("SUPERMARKET MANAGER");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUI();

        // Mise à jour horloge en temps réel
        Timer timer = new Timer(1000, e -> mettreAJourHeure());
        timer.start();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BG_DARK);

        // === EN-TÊTE ===
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        JLabel titreApp = createLabel("⚙ SUPERMARKET MANAGER", ACCENT_GOLD, FONT_TITLE);
        lblDateHeure = createLabel(getDateHeure(), TEXT_MUTED, FONT_BODY);

        header.add(titreApp, BorderLayout.WEST);
        header.add(lblDateHeure, BorderLayout.EAST);

        // === CORPS : NAVIGATION + CONTENU ===
        JPanel corps = new JPanel(new BorderLayout(10, 0));
        corps.setBackground(BG_DARK);
        corps.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- NAVIGATION GAUCHE (filtrée selon les droits de l'employé) ---
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(BG_PANEL);
        navPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        navPanel.setPreferredSize(new Dimension(190, 0));

        navPanel.add(createLabel("[ NAVIGATION ]", ACCENT_GOLD, FONT_HEADER));
        navPanel.add(Box.createVerticalStrut(10));

        List<Module> modulesAutorises = accesService.getModulesAutorises(employe);
        for (Module module : modulesAutorises) {
            JButton btn = new JButton(module.getLibelle());
            btn.setBackground(BG_CARD);
            btn.setForeground(TEXT_PRIMARY);
            btn.setFont(FONT_BODY);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setMaximumSize(new Dimension(170, 38));
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));

            btn.addActionListener(e -> ouvrirModule(module));
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new Color(55, 55, 80));
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(BG_CARD);
                }
            });
            navPanel.add(btn);
            navPanel.add(Box.createVerticalStrut(4));
        }

        navPanel.add(Box.createVerticalStrut(10));
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setMaximumSize(new Dimension(170, 2));
        navPanel.add(sep);
        navPanel.add(Box.createVerticalStrut(10));

        JButton btnQuitter = createButton("  ■ Quitter", ACCENT_RED);
        btnQuitter.setMaximumSize(new Dimension(170, 38));
        btnQuitter.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnQuitter.addActionListener(e -> System.exit(0));
        navPanel.add(btnQuitter);

        // --- ZONE DE BIENVENUE ---
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setBackground(BG_DARK);

        JPanel welcomeCard = createCard();
        welcomeCard.setLayout(new BoxLayout(welcomeCard, BoxLayout.Y_AXIS));
        welcomeCard.add(createLabel("[ BIENVENUE DANS VOTRE ESPACE DE GESTION ]", ACCENT_GOLD, FONT_HEADER));
        welcomeCard.add(Box.createVerticalStrut(8));
        welcomeCard.add(createLabel("Bonjour, " + employe.getNomComplet() +
                " (" + employe.afficherRole() + ")", TEXT_PRIMARY, FONT_BODY));
        welcomeCard.add(Box.createVerticalStrut(4));
        welcomeCard.add(createLabel("Sélectionnez une option dans le menu de gauche.", TEXT_MUTED, FONT_SMALL));

        JPanel apercu = createCard();
        apercu.setLayout(new GridLayout(3, 1, 5, 5));

        JLabel lblApercu = createLabel("📋 Aperçu rapide (Aujourd'hui) :", ACCENT_BLUE, FONT_HEADER);
        lblVentesJour = createLabel("  • Ventes du jour : "
                + String.format("%.0f", statistiqueService.calculerChiffreAffaires()) + " F CFA", TEXT_PRIMARY,
                FONT_BODY);
        lblAlerteStock = createLabel(
                "  • Alertes Stock : " + statistiqueService.getProduitsStockFaible().size() + " produits critiques",
                ACCENT_RED, FONT_BODY);

        apercu.add(lblApercu);
        apercu.add(lblVentesJour);
        apercu.add(lblAlerteStock);

        rightPanel.add(welcomeCard, BorderLayout.NORTH);
        rightPanel.add(apercu, BorderLayout.CENTER);

        corps.add(navPanel, BorderLayout.WEST);
        corps.add(rightPanel, BorderLayout.CENTER);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(corps, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void ouvrirModule(Module module) {
        switch (module) {
            case PRODUITS:
                new FenetreProduit().setVisible(true);
                break;
            case STOCK:
                new FenetreStock().setVisible(true);
                break;
            case FOURNISSEURS:
                new FenetreFournisseur().setVisible(true);
                break;
            case CLIENTS:
                new FenetreClient().setVisible(true);
                break;
            case EMPLOYES:
                new FenetreEmploye().setVisible(true);
                break;
            case RAYONS:
                new FenetreRayon(employe, false).setVisible(true);
                break;
            case STATISTIQUES:
                new Dashboard().setVisible(true);
                break;
            case CAISSE:
                /* non accessible depuis l'accueil : voir FenetreConnexion */ break;
        }
        rafraichirApercu();
    }

    private void rafraichirApercu() {
        lblVentesJour.setText("  • Ventes du jour : "
                + String.format("%.0f", statistiqueService.calculerChiffreAffaires()) + " F CFA");
        lblAlerteStock.setText(
                "  • Alertes Stock : " + statistiqueService.getProduitsStockFaible().size() + " produits critiques");
    }

    private void mettreAJourHeure() {
        lblDateHeure.setText(getDateHeure());
    }

    private String getDateHeure() {
        return new SimpleDateFormat("dd/MM/yyyy | HH:mm:ss").format(new Date());
    }
}