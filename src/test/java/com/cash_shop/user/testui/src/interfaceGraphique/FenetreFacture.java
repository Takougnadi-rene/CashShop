package interfaceGraphique;

import javax.swing.*;
import java.awt.*;
import static interfaceGraphique.StyleManager.*;

/**
 * Fenêtre affichant le reçu (facture) remis au client après un paiement
 * validé en caisse.
 */
public class FenetreFacture extends JFrame {

    private final model.Facture facture;

    public FenetreFacture(model.Facture derniereFacture) {
        this.facture = derniereFacture;
        setTitle("Facture N°" + String.format("%04d", derniereFacture.getNumeroFacture()));
        setSize(375, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(0);
        setResizable(false);
        buildUI();
    }


    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_DARK);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setBackground(BG_PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        header.add(createLabel("🧾 FACTURE", ACCENT_GOLD, FONT_TITLE));

        JTextArea ta = new JTextArea(facture.genererFacture());
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setEditable(false);
        ta.setBackground(new Color(15, 15, 20));
        ta.setForeground(Color.WHITE);
        ta.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(ta);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        actions.setBackground(BG_PANEL);
        JButton btnImprimer = createButton("🖨 IMPRIMER", ACCENT_BLUE);
        JButton btnFermer   = createButton("Fermer", new Color(80,80,100));

        btnImprimer.addActionListener(e -> {
            facture.imprimerFacture();
            JOptionPane.showMessageDialog(this,
                "Facture envoyée à l'impression.",
                "Impression", JOptionPane.INFORMATION_MESSAGE);
        });
        btnFermer.addActionListener(e -> dispose());

        actions.add(btnImprimer);
        actions.add(btnFermer);

        main.add(header, BorderLayout.NORTH);
        main.add(scroll, BorderLayout.CENTER);
        main.add(actions, BorderLayout.SOUTH);
        setContentPane(main);
    }
}