package com.vfms.ui;

import com.vfms.config.DatabaseConfig;
import com.vfms.model.StorageStats;
import com.vfms.ui.theme.UITheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Clean modern sidebar with quick access links, storage metrics, and MySQL status.
 */
public class SidebarPanel extends JPanel {

    public interface SidebarListener {
        void onQuickFolderSelected(String folderName);
        void onOpenDatabaseSettings();
        void onStorageStatsClicked();
    }

    private final JLabel dbStatusBadge;
    private final JLabel totalFilesLabel;
    private final JLabel totalFoldersLabel;
    private final JLabel totalSizeLabel;
    private final JProgressBar storageProgressBar;
    private SidebarListener listener;

    public SidebarPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(240, 600));
        setBackground(UITheme.SIDEBAR_BG);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER));

        // Top App Header
        JPanel appHeader = new JPanel(new BorderLayout(8, 0));
        appHeader.setBackground(UITheme.SIDEBAR_BG);
        appHeader.setBorder(BorderFactory.createEmptyBorder(18, 16, 16, 16));

        JLabel logoLabel = new JLabel("🗂️ VFMS");
        logoLabel.setFont(UITheme.FONT_TITLE);
        logoLabel.setForeground(UITheme.PRIMARY);

        JLabel subLabel = new JLabel("Virtual File System");
        subLabel.setFont(UITheme.FONT_SMALL);
        subLabel.setForeground(UITheme.TEXT_MUTED);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setBackground(UITheme.SIDEBAR_BG);
        titleBlock.add(logoLabel);
        titleBlock.add(subLabel);
        appHeader.add(titleBlock, BorderLayout.CENTER);

        // Center: Navigation Items
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(UITheme.SIDEBAR_BG);
        navPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel navHeader = new JLabel("QUICK ACCESS");
        navHeader.setFont(new Font("Segoe UI", Font.BOLD, 10));
        navHeader.setForeground(UITheme.TEXT_MUTED);
        navHeader.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        navPanel.add(navHeader);

        navPanel.add(createNavItem("📁 All Files (Root)", e -> {
            if (listener != null) listener.onQuickFolderSelected(null);
        }));
        navPanel.add(createNavItem("📄 Documents", e -> {
            if (listener != null) listener.onQuickFolderSelected("Documents");
        }));
        navPanel.add(createNavItem("💻 College Projects", e -> {
            if (listener != null) listener.onQuickFolderSelected("College Projects");
        }));
        navPanel.add(createNavItem("🖼️ Pictures", e -> {
            if (listener != null) listener.onQuickFolderSelected("Pictures");
        }));
        navPanel.add(createNavItem("📊 Storage Overview", e -> {
            if (listener != null) listener.onStorageStatsClicked();
        }));

        // Bottom: Storage & Database Info
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(UITheme.SIDEBAR_BG);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 16, 12));

        // Storage Overview Card
        JPanel storageCard = new JPanel();
        storageCard.setLayout(new BoxLayout(storageCard, BoxLayout.Y_AXIS));
        storageCard.setBackground(UITheme.SURFACE);
        storageCard.setBorder(UITheme.createCardBorder());

        JLabel storageTitle = new JLabel("Storage Usage");
        storageTitle.setFont(UITheme.FONT_BOLD);
        storageTitle.setForeground(UITheme.TEXT_PRIMARY);

        totalSizeLabel = new JLabel("Used: 0 B");
        totalSizeLabel.setFont(UITheme.FONT_SMALL);
        totalSizeLabel.setForeground(UITheme.TEXT_MUTED);

        storageProgressBar = new JProgressBar(0, 100);
        storageProgressBar.setValue(15);
        storageProgressBar.setForeground(UITheme.PRIMARY);
        storageProgressBar.setBackground(UITheme.SIDEBAR_BG);
        storageProgressBar.setPreferredSize(new Dimension(190, 8));

        JPanel countsRow = new JPanel(new GridLayout(1, 2));
        countsRow.setBackground(UITheme.SURFACE);
        totalFilesLabel = new JLabel("Files: 0");
        totalFilesLabel.setFont(UITheme.FONT_SMALL);
        totalFilesLabel.setForeground(UITheme.TEXT_MUTED);

        totalFoldersLabel = new JLabel("Folders: 0");
        totalFoldersLabel.setFont(UITheme.FONT_SMALL);
        totalFoldersLabel.setForeground(UITheme.TEXT_MUTED);
        countsRow.add(totalFilesLabel);
        countsRow.add(totalFoldersLabel);

        storageCard.add(storageTitle);
        storageCard.add(Box.createVerticalStrut(4));
        storageCard.add(totalSizeLabel);
        storageCard.add(Box.createVerticalStrut(6));
        storageCard.add(storageProgressBar);
        storageCard.add(Box.createVerticalStrut(6));
        storageCard.add(countsRow);

        bottomPanel.add(storageCard);
        bottomPanel.add(Box.createVerticalStrut(12));

        // DB Status & Settings Button
        JPanel dbPanel = new JPanel(new BorderLayout(4, 4));
        dbPanel.setBackground(UITheme.SIDEBAR_BG);

        dbStatusBadge = new JLabel("● Connecting...", SwingConstants.LEFT);
        dbStatusBadge.setFont(UITheme.FONT_SMALL);
        dbStatusBadge.setForeground(UITheme.TEXT_MUTED);

        JButton dbSettingsBtn = new JButton("⚙ Database Settings");
        dbSettingsBtn.setFont(UITheme.FONT_SMALL);
        dbSettingsBtn.setForeground(UITheme.TEXT_PRIMARY);
        dbSettingsBtn.setBackground(UITheme.SURFACE);
        dbSettingsBtn.setFocusPainted(false);
        dbSettingsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dbSettingsBtn.addActionListener(e -> {
            if (listener != null) listener.onOpenDatabaseSettings();
        });

        dbPanel.add(dbStatusBadge, BorderLayout.NORTH);
        dbPanel.add(dbSettingsBtn, BorderLayout.SOUTH);
        bottomPanel.add(dbPanel);

        add(appHeader, BorderLayout.NORTH);
        add(navPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void setListener(SidebarListener listener) {
        this.listener = listener;
    }

    public void updateDatabaseStatus(boolean isConnected, boolean isMock) {
        if (isConnected) {
            dbStatusBadge.setText("● MySQL Connected");
            dbStatusBadge.setForeground(UITheme.SUCCESS);
        } else if (isMock) {
            dbStatusBadge.setText("○ Demo Mode (In-Memory)");
            dbStatusBadge.setForeground(UITheme.WARNING);
        } else {
            dbStatusBadge.setText("✕ Disconnected");
            dbStatusBadge.setForeground(UITheme.DANGER);
        }
    }

    public void updateStats(StorageStats stats) {
        if (stats == null) return;
        totalFilesLabel.setText("Files: " + stats.getTotalFiles());
        totalFoldersLabel.setText("Folders: " + stats.getTotalFolders());
        totalSizeLabel.setText("Used: " + stats.getFormattedTotalSize());

        // Approximate percentage assuming 100 MB demo limit
        long totalBytes = stats.getTotalBytes();
        int percent = (int) Math.min(100, Math.max(5, (totalBytes * 100) / (100 * 1024 * 1024)));
        storageProgressBar.setValue(percent);
    }

    private JPanel createNavItem(String text, Consumer<MouseEvent> onClick) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        item.setBackground(UITheme.SIDEBAR_BG);
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel label = new JLabel(text);
        label.setFont(UITheme.FONT_REGULAR);
        label.setForeground(UITheme.TEXT_PRIMARY);
        item.add(label);

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(UITheme.SIDEBAR_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(UITheme.SIDEBAR_BG);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.accept(e);
            }
        });

        return item;
    }
}

