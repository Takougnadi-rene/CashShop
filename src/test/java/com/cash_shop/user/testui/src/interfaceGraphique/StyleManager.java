package interfaceGraphique;

import java.awt.*;

public class StyleManager {
    // Couleurs thème sombre professionnel
    public static final Color BG_DARK      = new Color(18, 18, 28);
    public static final Color BG_PANEL     = new Color(28, 28, 42);
    public static final Color BG_CARD      = new Color(38, 38, 56);
    public static final Color ACCENT_GOLD  = new Color(255, 193, 7);
    public static final Color ACCENT_BLUE  = new Color(33, 150, 243);
    public static final Color ACCENT_GREEN = new Color(76, 175, 80);
    public static final Color ACCENT_RED   = new Color(244, 67, 54);
    public static final Color TEXT_PRIMARY = new Color(236, 236, 240);
    public static final Color TEXT_MUTED   = new Color(140, 140, 160);
    public static final Color BORDER_COLOR = new Color(55, 55, 75);

    public static final Font FONT_TITLE  = new Font("Consolas", Font.BOLD, 18);
    public static final Font FONT_HEADER = new Font("Consolas", Font.BOLD, 14);
    public static final Font FONT_BODY   = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_SMALL  = new Font("Consolas", Font.PLAIN, 11);

    public static javax.swing.JButton createButton(String text, Color bg) {
        javax.swing.JButton btn = new javax.swing.JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BODY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 34));
        return btn;
    }

    public static javax.swing.JTextField createField() {
        javax.swing.JTextField tf = new javax.swing.JTextField();
        tf.setBackground(BG_DARK);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_GOLD);
        tf.setFont(FONT_BODY);
        tf.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(BORDER_COLOR),
            javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return tf;
    }

    public static javax.swing.JLabel createLabel(String text, Color color, Font font) {
        javax.swing.JLabel lbl = new javax.swing.JLabel(text);
        lbl.setForeground(color);
        lbl.setFont(font);
        return lbl;
    }

    public static javax.swing.JPanel createCard() {
        javax.swing.JPanel p = new javax.swing.JPanel();
        p.setBackground(BG_CARD);
        p.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(BORDER_COLOR),
            javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return p;
    }

    public static javax.swing.JTable createTable(javax.swing.table.DefaultTableModel model) {
        javax.swing.JTable table = new javax.swing.JTable(model);
        table.setBackground(BG_PANEL);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER_COLOR);
        table.setFont(FONT_BODY);
        table.setRowHeight(26);
        table.getTableHeader().setBackground(BG_CARD);
        table.getTableHeader().setForeground(ACCENT_GOLD);
        table.getTableHeader().setFont(FONT_HEADER);
        table.setSelectionBackground(new Color(60, 60, 100));
        table.setSelectionForeground(TEXT_PRIMARY);
        return table;
    }
}
