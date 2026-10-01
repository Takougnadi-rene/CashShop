package com.cash_shop.common;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
 * Central place for the application's visual identity (dark theme).
 *
 * It exposes the color palette plus small factory/helper methods that build
 * consistently styled Swing components (pages, forms, tables, buttons, labels),
 * so the view classes do not have to repeat styling code.
 */
public class StyleManager {

    // ---- Color palette ----
    public static final Color BG_DEEP        = new Color(0x0F, 0x11, 0x17);  // main background
    public static final Color BG_SURFACE     = new Color(0x16, 0x18, 0x22);  // card background
    public static final Color BG_CARD        = new Color(0x1E, 0x21, 0x30);  // panel background
    public static final Color BG_INPUT       = new Color(0x25, 0x28, 0x3A);  // input field background
    public static final Color ACCENT_PRIMARY = new Color(0x6C, 0x63, 0xFF);  // purple
    public static final Color ACCENT_TEAL    = new Color(0x00, 0xD4, 0xAA);  // teal
    public static final Color ACCENT_ORANGE  = new Color(0xFF, 0x8C, 0x42);  // orange
    public static final Color TEXT_PRIMARY   = new Color(0xF0, 0xF2, 0xFF);  // soft white
    public static final Color TEXT_SECONDARY = new Color(0x8A, 0x8F, 0xB5);  // bluish gray
    public static final Color BORDER_COLOR   = new Color(0x2E, 0x32, 0x4A);  // subtle border

    // ---- Semantic colors ----
    public static final Color SUCCESS = new Color(0x00, 0xD4, 0xAA);
    public static final Color WARNING = new Color(0xFF, 0xB8, 0x00);
    public static final Color DANGER  = new Color(0xFF, 0x4D, 0x6D);

    // ---- Fonts ----
    private static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BUTTON   = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_INPUT    = new Font("Segoe UI", Font.PLAIN, 13);

    // Global initialisation

    /**
     * Applies the dark theme to the global Swing defaults (dialogs, option panes,
     * tooltips, scroll bars...). Must be called once, before any window is created.
     */
    public static void applyLookAndFeel() {
        try {
            // Cross-platform look and feel: it honors the colors we set below on every OS.
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception exception) {
            // Not fatal: Swing simply keeps its default look and feel.
        }

        UIManager.put("Panel.background", BG_DEEP);
        UIManager.put("OptionPane.background", BG_DEEP);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("OptionPane.messageFont", FONT_BODY);
        UIManager.put("OptionPane.buttonFont", FONT_BUTTON);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("ToolTip.background", BG_CARD);
        UIManager.put("ToolTip.foreground", TEXT_PRIMARY);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(BORDER_COLOR));
        UIManager.put("ScrollBar.thumb", BORDER_COLOR);
        UIManager.put("ScrollBar.track", BG_SURFACE);
    }


    // Component factories

    /** Creates a styled text field (dark input background, rounded border, visible caret). */
    public static JTextField createField() {
        JTextField field = new JTextField();
        field.setFont(FONT_INPUT);
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER_COLOR, 8),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return field;
    }

    /** Creates a regular (secondary color) label. */
    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    /** Creates an emphasized (bold, primary color) label. */
    public static JLabel createBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SUBTITLE);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    /** Creates a page title label, with some padding around it. */
    public static JLabel createTitle(String text) {
        JLabel title = new JLabel(text);
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return title;
    }

    /** Creates the root panel of a window: BorderLayout, dark background and outer padding. */
    public static JPanel createPage() {
        JPanel page = new JPanel(new BorderLayout(12, 12));
        page.setBackground(BG_DEEP);
        page.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return page;
    }

    /**
     * Creates a form panel (GridBagLayout) with a titled border.
     */
    public static JPanel createForm(String title) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        title,
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        FONT_SUBTITLE,
                        ACCENT_PRIMARY),
                BorderFactory.createEmptyBorder(6, 8, 8, 8)));
        return form;
    }

    /**
     * Adds a "label + component" row to a form created by {@link #createForm(String)}.
     */
    
    public static void addRow(JPanel form, int row, String labelText, JComponent component) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = row;
        constraints.insets = new Insets(5, 6, 5, 6);
        constraints.anchor = GridBagConstraints.WEST;

        // Left column: fixed-width label.
        constraints.gridx = 0;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        form.add(createLabel(labelText), constraints);

        // Right column: the input component takes all the remaining width.
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        component.setPreferredSize(new Dimension(Math.max(component.getPreferredSize().width, 220), 30));
        form.add(component, constraints);
    }

    /** Creates a table model whose cells cannot be edited by the user. */
    public static DefaultTableModel createReadOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /** Creates a styled table (single selection, dark colors, styled header). */
    public static JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(FONT_BODY);
        table.setRowHeight(28);
        table.setBackground(BG_SURFACE);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(ACCENT_PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_SUBTITLE);
        header.setBackground(BG_CARD);
        header.setForeground(TEXT_PRIMARY);
        header.setReorderingAllowed(false);
        return table;
    }

    /** Wraps a table in a scroll pane that matches the dark theme. */
    public static JScrollPane createScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(BG_SURFACE);
        return scrollPane;
    }

    /** Creates a right-aligned, transparent button bar and styles every button it receives. */
    public static JPanel createButtonBar(JButton... buttons) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bar.setOpaque(false);
        for (JButton button : buttons) {
            styleButton(button);
            bar.add(button);
        }
        return bar;
    }

    /** Applies the primary button style (accent color, hand cursor, hover effect). */
    public static void styleButton(JButton button) {
        button.setFont(FONT_BUTTON);
        button.setBackground(ACCENT_PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));

        // Hover effect: slightly brighter background while the mouse is over the button.
        // The listener is added only once, even if the button is styled several times.
        if (!Boolean.TRUE.equals(button.getClientProperty("styled"))) {
            button.putClientProperty("styled", Boolean.TRUE);
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent event) {
                    if (button.isEnabled()) {
                        button.setBackground(ACCENT_PRIMARY.brighter());
                    }
                }

                @Override
                public void mouseExited(MouseEvent event) {
                    button.setBackground(ACCENT_PRIMARY);
                }
            });
        }
    }

    // Helper classes
    
    /** Border with rounded corners, used by the text fields. */
    private static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;

        /** Creates a border with the given color and corner radius. */
        RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component component, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        /** Returns the border insets. */
        @Override
        public Insets getBorderInsets(Component component) {
            return new Insets(2, 2, 2, 2);
        }
    }

    /** Panel painted with a diagonal two-color gradient background. */
    public static class GradientPanel extends JPanel {
        private final Color color1;
        private final Color color2;

        /** Creates a panel painted with a gradient from c1 (top-left) to c2 (bottom-right). */
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
