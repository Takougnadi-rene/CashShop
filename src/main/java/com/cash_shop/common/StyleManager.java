package com.cash_shop.common;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Gestionnaire de style – thème sombre moderne avec accents violets/teal.
 * Toutes les fenêtres utilisent ce gestionnaire pour une cohérence visuelle.
 */
public class StyleManager {

    // ── Palette de couleurs ──────────────────────────────────────────────────
    public static final Color BG_DEEP        = new Color(0x0F, 0x11, 0x17);  // fond principal
    public static final Color BG_SURFACE     = new Color(0x16, 0x18, 0x22);  // fond carte
    public static final Color BG_CARD        = new Color(0x1E, 0x21, 0x30);  // fond panneau
    public static final Color BG_INPUT       = new Color(0x25, 0x28, 0x3A);  // fond champ
    public static final Color ACCENT_PRIMARY = new Color(0x6C, 0x63, 0xFF);  // violet
    public static final Color ACCENT_TEAL    = new Color(0x00, 0xD4, 0xAA);  // teal
    public static final Color ACCENT_ORANGE  = new Color(0xFF, 0x8C, 0x42);  // orange
    public static final Color TEXT_PRIMARY   = new Color(0xF0, 0xF2, 0xFF);  // blanc doux
    public static final Color TEXT_SECONDARY = new Color(0x8A, 0x8F, 0xB5);  // gris bleuté
    public static final Color BORDER_COLOR   = new Color(0x2E, 0x32, 0x4A);  // bordure subtile

    // ── Couleurs sémantiques ─────────────────────────────────────────────────
    public static final Color SUCCESS = new Color(0x00, 0xD4, 0xAA);
    public static final Color WARNING = new Color(0xFF, 0xB8, 0x00);
    public static final Color DANGER  = new Color(0xFF, 0x4D, 0x6D);

    // ── Polices ──────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BUTTON   = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_INPUT    = new Font("Segoe UI", Font.PLAIN, 13);

    // ─────────────────────────────────────────────────────────────────────────
    // Initialisation globale
    // ─────────────────────────────────────────────────────────────────────────

    /** Applique le Look & Feel sombre au démarrage. */
    public static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) { }

        // Couleurs globales
        UIManager.put("Panel.background",            BG_SURFACE);
        UIManager.put("Frame.background",            BG_SURFACE);
        UIManager.put("OptionPane.background",       BG_CARD);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);

        // Champs de texte
        UIManager.put("TextField.background",        BG_INPUT);
        UIManager.put("TextField.foreground",        TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground",   ACCENT_PRIMARY);
        UIManager.put("TextField.selectionBackground", ACCENT_PRIMARY);
        UIManager.put("TextField.border",            BorderFactory.createEmptyBorder(6, 10, 6, 10));
        UIManager.put("PasswordField.background",    BG_INPUT);
        UIManager.put("PasswordField.foreground",    TEXT_PRIMARY);
        UIManager.put("PasswordField.caretForeground", ACCENT_PRIMARY);
        UIManager.put("PasswordField.selectionBackground", ACCENT_PRIMARY);
        UIManager.put("PasswordField.border",        BorderFactory.createEmptyBorder(6, 10, 6, 10));
        UIManager.put("TextArea.background",         BG_INPUT);
        UIManager.put("TextArea.foreground",         TEXT_PRIMARY);
        UIManager.put("TextArea.caretForeground",    ACCENT_PRIMARY);
        UIManager.put("ComboBox.background",         BG_INPUT);
        UIManager.put("ComboBox.foreground",         TEXT_PRIMARY);

        // Labels
        UIManager.put("Label.foreground",            TEXT_PRIMARY);

        // Tables
        UIManager.put("Table.background",            BG_CARD);
        UIManager.put("Table.foreground",            TEXT_PRIMARY);
        UIManager.put("Table.selectionBackground",   ACCENT_PRIMARY);
        UIManager.put("Table.selectionForeground",   Color.WHITE);
        UIManager.put("Table.gridColor",             BORDER_COLOR);
        UIManager.put("TableHeader.background",      BG_SURFACE);
        UIManager.put("TableHeader.foreground",      TEXT_SECONDARY);

        // ScrollPane
        UIManager.put("ScrollPane.background",       BG_CARD);
        UIManager.put("Viewport.background",         BG_CARD);
        UIManager.put("ScrollBar.background",        BG_SURFACE);
        UIManager.put("ScrollBar.thumb",             BORDER_COLOR);

        // Titres de bordure
        UIManager.put("TitledBorder.titleColor",     TEXT_SECONDARY);
        UIManager.put("TitledBorder.border",         BorderFactory.createLineBorder(BORDER_COLOR));

        // Boutons (dialogs)
        UIManager.put("Button.background",           BG_CARD);
        UIManager.put("Button.foreground",           TEXT_PRIMARY);
        UIManager.put("Button.border",               BorderFactory.createEmptyBorder(8, 16, 8, 16));
        UIManager.put("OptionPane.buttonFont",       FONT_BUTTON);
        UIManager.put("OptionPane.messageFont",      FONT_BODY);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Labels et titres
    // ─────────────────────────────────────────────────────────────────────────

    public static JLabel createTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_TITLE);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    public static JLabel createSubtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SUBTITLE);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    public static JLabel createBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SUBTITLE);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Panneaux principaux
    // ─────────────────────────────────────────────────────────────────────────

    /** Panneau principal de chaque fenêtre. */
    public static JPanel createPage() {
        JPanel page = new JPanel(new BorderLayout(12, 12));
        page.setBackground(BG_SURFACE);
        page.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return page;
    }

    /** Carte visuelle (panneau arrondi avec fond). */
    public static JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return card;
    }

    /** Panneau de formulaire avec titre et fond de carte. */
    public static JPanel createForm(String title) {
        JPanel form = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        form.setOpaque(false);
        form.setBorder(BorderFactory.createCompoundBorder(
                new SectionTitleBorder(title),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        return form;
    }

    public static void addRow(JPanel form, int row, String label, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 8, 5, 8);
        c.gridy = row;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_SECONDARY);
        form.add(lbl, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        field.setFont(FONT_INPUT);
        form.add(field, c);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Boutons
    // ─────────────────────────────────────────────────────────────────────────

    /** Barre de boutons centrée avec style automatique. */
    public static JPanel createButtonBar(JButton... buttons) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        bar.setOpaque(false);
        for (JButton button : buttons) {
            styleButton(button);
            bar.add(button);
        }
        return bar;
    }

    /** Applique le style approprié à un bouton selon son texte. */
    public static void styleButton(JButton button) {
        String text = button.getText().toLowerCase();
        if (text.equals("add") || text.equals("login") || text.equals("sign in")
                || text.equals("save") || text.startsWith("add ") || text.equals("stock in")) {
            applyButtonStyle(button, ACCENT_PRIMARY, ACCENT_PRIMARY.darker(), Color.WHITE);
        } else if (text.equals("delete") || text.contains("delet") || text.equals("remove")) {
            applyButtonStyle(button, new Color(0x3D, 0x1A, 0x22), new Color(0x5A, 0x20, 0x2E), DANGER);
        } else if (text.equals("close") || text.equals("cancel") || text.equals("log out")) {
            applyButtonStyle(button, BG_INPUT, BG_CARD, TEXT_SECONDARY);
        } else if (text.equals("stock out")) {
            applyButtonStyle(button, new Color(0x2E, 0x1A, 0x08), new Color(0x40, 0x28, 0x10), ACCENT_ORANGE);
        } else {
            applyButtonStyle(button, BG_CARD, BG_INPUT, TEXT_PRIMARY);
        }
    }

    private static void applyButtonStyle(JButton button, Color bg, Color hover, Color fg) {
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFont(FONT_BUTTON);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 17, 8, 17)));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { button.setBackground(bg); }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Champs de texte
    // ─────────────────────────────────────────────────────────────────────────

    public static JTextField createField() {
        JTextField field = new JTextField(15);
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_PRIMARY);
        field.setFont(FONT_INPUT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return field;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tables
    // ─────────────────────────────────────────────────────────────────────────

    public static DefaultTableModel createReadOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    public static JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER_COLOR);
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(ACCENT_PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        table.setFont(FONT_BODY);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_SURFACE);
        header.setForeground(TEXT_SECONDARY);
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_PRIMARY));
        header.setReorderingAllowed(false);

        return table;
    }

    public static JScrollPane createScrollPane(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(BG_CARD);
        scroll.setBackground(BG_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        return scroll;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Messages
    // ─────────────────────────────────────────────────────────────────────────

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Classes internes utilitaires
    // ─────────────────────────────────────────────────────────────────────────

    /** Bordure avec titre stylisé. */
    private static class SectionTitleBorder extends AbstractBorder {
        private final String title;
        private final Font font = new Font("Segoe UI", Font.BOLD, 11);

        SectionTitleBorder(String title) {
            this.title = title.toUpperCase();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(24, 10, 8, 10);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(ACCENT_PRIMARY);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(x, y + 20, x + width, y + 20);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(title);
            g2.setColor(BG_CARD);
            g2.fillRect(x + 10, y + 2, textW + 12, fm.getHeight() + 2);
            g2.setColor(ACCENT_PRIMARY);
            g2.drawString(title, x + 16, y + fm.getAscent() + 2);
            g2.dispose();
        }
    }

    /** Panneau avec dégradé de fond. */
    public static class GradientPanel extends JPanel {
        private final Color color1;
        private final Color color2;

        public GradientPanel(Color c1, Color c2) {
            this.color1 = c1;
            this.color2 = c2;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }
}

