package interfaceGraphique;

import model.Caissier;
import model.ChefRayon;
import model.Employe;
import service.AuthentificationService;
import javax.swing.*;
import java.awt.*;
import static interfaceGraphique.StyleManager.*;

public class FenetreConnexion extends JFrame {

    private JTextField tfLogin;
    private JPasswordField tfPassword;

    private final AuthentificationService authService = new AuthentificationService();

    public FenetreConnexion() {
        setTitle("SUPERMARKET MANAGER - Connexion");
        setSize(480, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG_DARK);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        // En-tête
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setBackground(BG_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        JLabel titre = createLabel("⚙ SUPERMARKET MANAGER", ACCENT_GOLD, FONT_TITLE);
        header.add(titre);

        // Panneau central
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(BG_DARK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel card = createCard();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(420, 240));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 10, 8, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        JLabel titreForm = createLabel("[ CONNEXION ]", TEXT_PRIMARY, FONT_HEADER);
        titreForm.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(titreForm, c);

        c.gridwidth = 1;

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.0;
        card.add(createLabel("Identifiant / Login :", TEXT_MUTED, FONT_SMALL), c);

        c.gridx = 1;
        c.weightx = 1.0;
        tfLogin = createField();
        tfLogin.setForeground(Color.WHITE);
        tfLogin.setCaretColor(Color.WHITE);
        card.add(tfLogin, c);

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.0;
        card.add(createLabel("Mot de passe :", TEXT_MUTED, FONT_SMALL), c);

        c.gridx = 1;
        c.weightx = 1.0;
        tfPassword = new JPasswordField();
        tfPassword.setBackground(BG_DARK);
        tfPassword.setForeground(Color.WHITE);
        tfPassword.setCaretColor(Color.WHITE);
        tfPassword.setFont(FONT_BODY);
        tfPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        card.add(tfPassword, c);

        // Bouton connexion
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.weightx = 0.0; // Reset
        c.insets = new Insets(16, 10, 8, 10);
        JButton btnConnexion = createButton(" ▶ SE CONNECTER", ACCENT_BLUE);
        btnConnexion.setPreferredSize(new Dimension(220, 38));
        btnConnexion.setFont(new Font("Consolas", Font.BOLD, 13));
        card.add(btnConnexion, c);

        center.add(card, gbc);

        // Info comptes de test
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setBackground(BG_DARK);
        
        infoPanel.add(createLabel(
                "admin/admin | comptable/1234 | magasinier/1235 | caissier1/1234 | chef1/1234",
                TEXT_MUTED, FONT_SMALL));

        // Action connexion
        btnConnexion.addActionListener(e -> seConnecter());
        tfPassword.addActionListener(e -> seConnecter());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_DARK);
        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(center, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    private void seConnecter() {
        String login = tfLogin.getText().trim();
        String mdp = new String(tfPassword.getPassword());

        Employe employe = authService.authentifier(login, mdp);
        if (employe != null) {
            dispose();
            ouvrirFenetrePourEmploye(employe);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Identifiant ou mot de passe incorrect.",
                    "Échec de connexion", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ouvrirFenetrePourEmploye(Employe employe) {
        if (employe instanceof Caissier) {
            new FenetreCaisse((Caissier) employe).setVisible(true);
        } else if (employe instanceof ChefRayon) {
            new FenetreRayon(employe, true).setVisible(true);
        } else {
            new Accueil(employe).setVisible(true);
        }
    }
}