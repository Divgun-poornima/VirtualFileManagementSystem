package com.vfms.ui.dialogs;

import com.vfms.model.VirtualFile;
import com.vfms.ui.theme.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

/**
 * Dialog for previewing virtual file contents and basic statistics.
 */
public class FilePreviewDialog extends JDialog {

    public FilePreviewDialog(JFrame parent, VirtualFile file) {
        super(parent, "Preview: " + file.getName(), true);
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setBackground(UITheme.SIDEBAR_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel titleLabel = new JLabel(file.getName());
        titleLabel.setFont(UITheme.FONT_HEADER);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel metaLabel = new JLabel(String.format("%s | %s | Created: %s", 
            file.getFileType(), file.getFormattedSize(), com.vfms.util.FileUtils.formatDate(file.getCreatedAt())));
        metaLabel.setFont(UITheme.FONT_SMALL);
        metaLabel.setForeground(UITheme.TEXT_MUTED);

        JPanel headerText = new JPanel(new BorderLayout(0, 4));
        headerText.setBackground(UITheme.SIDEBAR_BG);
        headerText.add(titleLabel, BorderLayout.NORTH);
        headerText.add(metaLabel, BorderLayout.SOUTH);
        headerPanel.add(headerText, BorderLayout.CENTER);

        // Content Area
        String textContent = file.getContentAsString();
        boolean isText = file.isTextBased() || (!textContent.isEmpty() && !textContent.contains("\0"));

        JTextArea previewArea = new JTextArea();
        previewArea.setFont(UITheme.FONT_MONO);
        previewArea.setEditable(false);
        previewArea.setBackground(UITheme.SURFACE);
        previewArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        if (isText) {
            previewArea.setText(textContent.isEmpty() ? "(File is empty)" : textContent);
            previewArea.setCaretPosition(0);
        } else {
            previewArea.setText(String.format(
                "[Binary File Preview Not Available]\n\n" +
                "File Name: %s\n" +
                "Type: %s\n" +
                "Size: %s (%d bytes)\n\n" +
                "To view or open this file, export it to your computer using 'Export File'.",
                file.getName(), file.getFileType(), file.getFormattedSize(), file.getFileSize()
            ));
        }

        JScrollPane scrollPane = new JScrollPane(previewArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Footer buttons
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(UITheme.SIDEBAR_BG);
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        // Stats on left
        int lineCount = isText && !textContent.isEmpty() ? textContent.split("\r\n|\r|\n").length : 0;
        int charCount = isText ? textContent.length() : 0;
        JLabel statsLabel = new JLabel(String.format(" Lines: %d | Characters: %d", lineCount, charCount));
        statsLabel.setFont(UITheme.FONT_SMALL);
        statsLabel.setForeground(UITheme.TEXT_MUTED);
        statsLabel.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnGroup.setBackground(UITheme.SIDEBAR_BG);

        if (isText && !textContent.isEmpty()) {
            JButton copyBtn = UITheme.createSecondaryButton("Copy Text");
            copyBtn.addActionListener(e -> {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(textContent), null);
                JOptionPane.showMessageDialog(this, "Content copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
            });
            btnGroup.add(copyBtn);
        }

        JButton closeBtn = UITheme.createPrimaryButton("Close");
        closeBtn.addActionListener(e -> dispose());
        btnGroup.add(closeBtn);

        footerPanel.add(statsLabel, BorderLayout.WEST);
        footerPanel.add(btnGroup, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(650, 480));
        pack();
        setLocationRelativeTo(parent);
    }
}

