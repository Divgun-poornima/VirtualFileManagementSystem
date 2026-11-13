package com.vfms.ui.dialogs;

import com.vfms.model.VirtualFile;
import com.vfms.model.VirtualFolder;
import com.vfms.ui.theme.UITheme;
import com.vfms.util.FileUtils;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/**
 * Dialog displaying complete metadata properties for a virtual file or folder.
 */
public class FilePropertiesDialog extends JDialog {

    public FilePropertiesDialog(JFrame parent, VirtualFile file, String folderPath) {
        super(parent, "Properties: " + file.getName(), true);
        initUI(parent, file.getName(), file.getFileType(), 
               file.getFormattedSize() + " (" + file.getFileSize() + " bytes)",
               folderPath, 
               FileUtils.formatDate(file.getCreatedAt()), 
               FileUtils.formatDate(file.getUpdatedAt()),
               "File ID: #" + file.getId());
    }

    public FilePropertiesDialog(JFrame parent, VirtualFolder folder, String folderPath) {
        super(parent, "Properties: " + folder.getName(), true);
        initUI(parent, folder.getName(), "File Folder", 
               "-", 
               folderPath, 
               FileUtils.formatDate(folder.getCreatedAt()), 
               FileUtils.formatDate(folder.getUpdatedAt()),
               "Folder ID: #" + folder.getId());
    }

    private void initUI(JFrame parent, String name, String type, String size, String location, String created, String modified, String idTag) {
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        mainPanel.setBackground(UITheme.SURFACE);

        JLabel titleLabel = new JLabel(name);
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel idLabel = new JLabel(idTag);
        idLabel.setFont(UITheme.FONT_SMALL);
        idLabel.setForeground(UITheme.TEXT_MUTED);

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(2));
        mainPanel.add(idLabel);
        mainPanel.add(Box.createVerticalStrut(16));

        JPanel grid = new JPanel(new GridLayout(5, 2, 8, 12));
        grid.setBackground(UITheme.SURFACE);

        addPropertyRow(grid, "Type:", type);
        addPropertyRow(grid, "Location:", location != null ? location : "Root");
        addPropertyRow(grid, "Size:", size);
        addPropertyRow(grid, "Created:", created);
        addPropertyRow(grid, "Modified:", modified);

        mainPanel.add(grid);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.setBackground(UITheme.SIDEBAR_BG);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JButton closeBtn = UITheme.createPrimaryButton("Close");
        closeBtn.addActionListener(e -> dispose());
        buttonPanel.add(closeBtn);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(460, 340));
        pack();
        setLocationRelativeTo(parent);
    }

    private void addPropertyRow(JPanel panel, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_MUTED);

        JLabel v = new JLabel(value);
        v.setFont(UITheme.FONT_REGULAR);
        v.setForeground(UITheme.TEXT_PRIMARY);

        panel.add(l);
        panel.add(v);
    }
}

