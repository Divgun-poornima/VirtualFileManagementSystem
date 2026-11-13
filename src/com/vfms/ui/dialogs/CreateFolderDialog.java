package com.vfms.ui.dialogs;

import com.vfms.ui.theme.UITheme;
import com.vfms.util.ValidationUtils;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * Modern modal dialog for creating a new virtual folder.
 */
public class CreateFolderDialog extends JDialog {

    private final JTextField nameField;
    private boolean confirmed = false;
    private String folderName = "";

    public CreateFolderDialog(JFrame parent) {
        super(parent, "New Folder", true);
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        contentPanel.setBackground(UITheme.SURFACE);

        JLabel titleLabel = new JLabel("Create New Folder");
        titleLabel.setFont(UITheme.FONT_HEADER);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel descLabel = new JLabel("Enter a name for the folder:");
        descLabel.setFont(UITheme.FONT_REGULAR);
        descLabel.setForeground(UITheme.TEXT_MUTED);

        nameField = new JTextField("New Folder", 22);
        nameField.setFont(UITheme.FONT_REGULAR);
        nameField.setPreferredSize(new Dimension(300, 36));
        nameField.selectAll();

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(descLabel);
        contentPanel.add(Box.createVerticalStrut(14));
        contentPanel.add(nameField);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBackground(UITheme.SIDEBAR_BG);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton createBtn = UITheme.createPrimaryButton("Create Folder");
        createBtn.addActionListener(e -> handleCreate());

        // Enter key shortcut
        nameField.addActionListener(e -> handleCreate());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(createBtn);

        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    private void handleCreate() {
        String input = nameField.getText().trim();
        String error = ValidationUtils.validateName(input);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Validation Error", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }
        this.folderName = input;
        this.confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getFolderName() {
        return folderName;
    }
}

