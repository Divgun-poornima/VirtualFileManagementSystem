package com.vfms.ui.theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern UI theme definition for Java Swing.
 * Uses FlatLaf if present, or cleanly styled Nimbus / System L&F.
 */
public class UITheme {

    // Modern color palette
    public static final Color PRIMARY = new Color(37, 99, 235);        // Royal Blue #2563EB
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216);  // #1D4ED8
    public static final Color ACCENT = new Color(59, 130, 246);        // #3B82F6
    public static final Color BACKGROUND = new Color(248, 250, 252);   // Slate 50 #F8FAFC
    public static final Color SURFACE = Color.WHITE;
    public static final Color SIDEBAR_BG = new Color(241, 245, 249);   // Slate 100 #F1F5F9
    public static final Color SIDEBAR_HOVER = new Color(226, 232, 240);// Slate 200 #E2E8F0
    public static final Color SIDEBAR_ACTIVE = new Color(219, 234, 254);// Blue 100
    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);     // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);   // Slate 500
    public static final Color BORDER = new Color(226, 232, 240);       // Slate 200
    public static final Color SUCCESS = new Color(16, 185, 129);       // Emerald 500
    public static final Color DANGER = new Color(239, 68, 68);         // Red 500
    public static final Color DANGER_HOVER = new Color(220, 38, 38);
    public static final Color WARNING = new Color(245, 158, 11);       // Amber 500

    // Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    /**
     * Attempts to install FlatLaf. If unavailable, falls back gracefully to Nimbus/System.
     */
    public static void initializeTheme() {
        try {
            // Attempt FlatLaf modern Light Look and Feel
            Class<?> flatLafClass = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            Object flatLafInstance = flatLafClass.getDeclaredConstructor().newInstance();
            UIManager.setLookAndFeel((javax.swing.LookAndFeel) flatLafInstance);
            
            // Modern tweaks for FlatLaf
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("Table.rowHeight", 34);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.gridColor", BORDER);
            return;
        } catch (Throwable ignored) {
            // FlatLaf not present on classpath, fall back to Nimbus or System
        }

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    UIManager.put("nimbusBase", PRIMARY);
                    UIManager.put("nimbusLightBackground", SURFACE);
                    UIManager.put("control", BACKGROUND);
                    return;
                }
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Notice: Using default Swing theme: " + e.getMessage());
        }
    }

    /**
     * Creates a styled modern button.
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 36));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(PRIMARY_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(PRIMARY);
            }
        });
        return btn;
    }

    /**
     * Creates a secondary/outline styled button.
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_REGULAR);
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(SURFACE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 10, 36));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(SIDEBAR_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(SURFACE);
            }
        });
        return btn;
    }

    /**
     * Creates a danger styled button (for delete actions).
     */
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(DANGER);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 36));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(DANGER_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(DANGER);
            }
        });
        return btn;
    }

    /**
     * Creates a rounded card panel border.
     */
    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );
    }
}

