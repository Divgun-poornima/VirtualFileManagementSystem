package com.vfms.ui.dialogs;

import com.vfms.ui.theme.UITheme;
import com.vfms.util.ValidationUtils;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * Modal dialog for creating a new virtual file with initial text content.
 */
public class CreateFileDialog extends JDialog {

    private final JTextField nameField;
    private final JComboBox<String> extensionCombo;
    private final JTextArea contentArea;
    private boolean confirmed = false;
    private String fullFileName = "";
    private String fileContent = "";

    public CreateFileDialog(JFrame parent) {
        super(parent, "New File", true);
        setLayout(new BorderLayout());
        setResizable(true);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        mainPanel.setBackground(UITheme.SURFACE);

        JLabel titleLabel = new JLabel("Create New Virtual File");
        titleLabel.setFont(UITheme.FONT_HEADER);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        // Name and Extension Row
        JPanel nameRow = new JPanel(new BorderLayout(8, 0));
        nameRow.setBackground(UITheme.SURFACE);
        nameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        nameField = new JTextField("document", 18);
        nameField.setFont(UITheme.FONT_REGULAR);
        nameField.setPreferredSize(new Dimension(240, 34));

        String[] popularExtensions = { ".txt", ".md", ".java", ".sql", ".json", ".xml", ".html", ".csv", ".py" };
        extensionCombo = new JComboBox<>(popularExtensions);
        extensionCombo.setFont(UITheme.FONT_REGULAR);
        extensionCombo.setEditable(true);
        extensionCombo.setPreferredSize(new Dimension(90, 34));

        nameRow.add(nameField, BorderLayout.CENTER);
        nameRow.add(extensionCombo, BorderLayout.EAST);

        // Content Area
        JLabel contentLabel = new JLabel("File Content (Optional):");
        contentLabel.setFont(UITheme.FONT_REGULAR);
        contentLabel.setForeground(UITheme.TEXT_MUTED);

        contentArea = new JTextArea(10, 35);
        contentArea.setFont(UITheme.FONT_MONO);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(new JLabel("File Name & Extension:"));
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(nameRow);
        mainPanel.add(Box.createVerticalStrut(14));
        mainPanel.add(contentLabel);
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(scrollPane);

        // Bottom action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBackground(UITheme.SIDEBAR_BG);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton createBtn = UITheme.createPrimaryButton("Create File");
        createBtn.addActionListener(e -> handleCreate());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(createBtn);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(480, 420));
        pack();
        setLocationRelativeTo(parent);
    }

    private void handleCreate() {
        String baseName = nameField.getText().trim();
        String ext = (String) extensionCombo.getSelectedItem();
        if (ext == null) ext = ".txt";
        ext = ext.trim();
        if (!ext.startsWith(".")) {
            ext = "." + ext;
        }

        // If user already typed the extension in baseName, don't duplicate
        String finalName;
        if (baseName.toLowerCase().endsWith(ext.toLowerCase())) {
            finalName = baseName;
        } else {
            finalName = baseName + ext;
        }

        String error = ValidationUtils.validateName(finalName);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Validation Error", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        this.fullFileName = finalName;
        this.fileContent = contentArea.getText();
        this.confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getFullFileName() {
        return fullFileName;
    }

    public String getFileContent() {
        return fileContent;
    }
}

