package interfaceGraphique;

import model.Fournisseur;
import model.Supermarche;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import static interfaceGraphique.StyleManager.*;

public class FenetreFournisseur extends JFrame {

    private JTextField tfCode, tfNom, tfTel, tfAdresse;
    private DefaultTableModel tableModel;

    public FenetreFournisseur() {
        setTitle("Gestion des Fournisseurs");
        setSize(820, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        charger();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_DARK);

        JPanel titre = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titre.setBackground(BG_PANEL);
        titre.setBorder(BorderFactory.createEmptyBorder(8,0,8,0));
        titre.add(createLabel("========== 🚚 GESTION DES FOURNISSEURS ==========", ACCENT_GOLD, FONT_TITLE));

        JPanel corps = new JPanel(new GridLayout(1,2,10,0));
        corps.setBackground(BG_DARK);
        corps.setBorder(BorderFactory.createEmptyBorder(10,10,5,10));

        JPanel form = createCard();
        form.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,5,6,5);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx=0; g.gridy=0; g.gridwidth=2;
        form.add(createLabel("[ Nouveau Fournisseur ]", ACCENT_BLUE, FONT_HEADER), g);
        g.gridwidth=1;

        g.gridy=1; g.gridx=0; form.add(createLabel("Code :", TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1; tfCode = createField(); form.add(tfCode, g);

        g.gridy=2; g.gridx=0; form.add(createLabel("Nom :", TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1; tfNom = createField(); form.add(tfNom, g);

        g.gridy=3; g.gridx=0; form.add(createLabel("Téléphone :", TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1; tfTel = createField(); form.add(tfTel, g);

        g.gridy=4; g.gridx=0; form.add(createLabel("Adresse :", TEXT_MUTED, FONT_SMALL), g);
        g.gridx=1; tfAdresse = createField(); form.add(tfAdresse, g);

        JPanel liste = createCard();
        liste.setLayout(new BorderLayout());
        liste.add(createLabel("[ Liste des Fournisseurs ]", ACCENT_BLUE, FONT_HEADER), BorderLayout.NORTH);
        tableModel = new DefaultTableModel(new String[]{"Code","Nom","Téléphone","Adresse"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        liste.add(new JScrollPane(createTable(tableModel)), BorderLayout.CENTER);

        corps.add(form);
        corps.add(liste);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        actions.setBackground(BG_PANEL);
        JButton btnAjouter = createButton("+ Ajouter", ACCENT_GREEN);
        JButton btnFermer  = createButton("Fermer",     new Color(80,80,100));
        btnAjouter.addActionListener(e -> ajouter());
        btnFermer.addActionListener(e -> dispose());
        actions.add(btnAjouter); actions.add(btnFermer);

        main.add(titre, BorderLayout.NORTH);
        main.add(corps, BorderLayout.CENTER);
        main.add(actions, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private void charger() {
        tableModel.setRowCount(0);
        for (Fournisseur f : Supermarche.getInstance().getFournisseurs())
            tableModel.addRow(new Object[]{f.getCode(), f.getNom(), f.getTelephone(), f.getAdresse()});
    }

    private void ajouter() {
        String code = tfCode.getText().trim();
        String nom  = tfNom.getText().trim();
        String tel  = tfTel.getText().trim();
        String adr  = tfAdresse.getText().trim();
        if (code.isEmpty() || nom.isEmpty()) { JOptionPane.showMessageDialog(this, "Code et Nom obligatoires."); return; }
        Supermarche.getInstance().ajouterFournisseur(new Fournisseur(code, nom, tel, adr));
        charger();
        tfCode.setText(""); tfNom.setText(""); tfTel.setText(""); tfAdresse.setText("");
        JOptionPane.showMessageDialog(this, "Fournisseur ajouté.", "Succès", JOptionPane.INFORMATION_MESSAGE);
    }
}
