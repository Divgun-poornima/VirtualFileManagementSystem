package com.vfms.ui.dialogs;

import com.vfms.config.DatabaseConfig;
import com.vfms.ui.theme.UITheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/**
 * Dialog for configuring MySQL connection credentials interactively.
 * Ensures passwords and credentials are never hardcoded.
 */
public class DatabaseConfigDialog extends JDialog {

    private final JTextField hostField;
    private final JTextField portField;
    private final JTextField dbNameField;
    private final JTextField userField;
    private final JPasswordField passwordField;
    private final JLabel statusLabel;
    private boolean saved = false;

    public DatabaseConfigDialog(JFrame parent) {
        super(parent, "Database Settings", true);
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        mainPanel.setBackground(UITheme.SURFACE);

        JLabel titleLabel = new JLabel("MySQL Database Configuration");
        titleLabel.setFont(UITheme.FONT_HEADER);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel descLabel = new JLabel("Configure connection settings for MySQL JDBC driver.");
        descLabel.setFont(UITheme.FONT_REGULAR);
        descLabel.setForeground(UITheme.TEXT_MUTED);

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(descLabel);
        mainPanel.add(Box.createVerticalStrut(16));

        // Form fields grid
        JPanel formGrid = new JPanel(new GridLayout(5, 2, 10, 10));
        formGrid.setBackground(UITheme.SURFACE);

        hostField = new JTextField(DatabaseConfig.getHost());
        portField = new JTextField(String.valueOf(DatabaseConfig.getPort()));
        dbNameField = new JTextField(DatabaseConfig.getDbName());
        userField = new JTextField(DatabaseConfig.getUser());
        passwordField = new JPasswordField(DatabaseConfig.getPassword());

        addFormField(formGrid, "Database Host:", hostField);
        addFormField(formGrid, "Port:", portField);
        addFormField(formGrid, "Database Name:", dbNameField);
        addFormField(formGrid, "Username:", userField);
        addFormField(formGrid, "Password:", passwordField);

        mainPanel.add(formGrid);
        mainPanel.add(Box.createVerticalStrut(14));

        // Status indicator
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createVerticalStrut(8));

        // Info box
        JLabel hintLabel = new JLabel("<html><i>Tip: Run <b>sql/schema.sql</b> in MySQL Workbench to initialize tables.</i></html>");
        hintLabel.setFont(UITheme.FONT_SMALL);
        hintLabel.setForeground(UITheme.TEXT_MUTED);
        mainPanel.add(hintLabel);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBackground(UITheme.SIDEBAR_BG);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JButton testBtn = UITheme.createSecondaryButton("Test Connection");
        testBtn.addActionListener(e -> runTestConnection());

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = UITheme.createPrimaryButton("Save & Connect");
        saveBtn.addActionListener(e -> handleSave());

        buttonPanel.add(testBtn);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(460, 410));
        pack();
        setLocationRelativeTo(parent);
    }

    private void addFormField(JPanel panel, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.FONT_REGULAR);
        label.setForeground(UITheme.TEXT_PRIMARY);
        field.setFont(UITheme.FONT_REGULAR);
        field.setPreferredSize(new Dimension(200, 32));
        panel.add(label);
        panel.add(field);
    }

    private void runTestConnection() {
        String host = hostField.getText().trim();
        int port = 3306;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException ignored) {}

        String db = dbNameField.getText().trim();
        String user = userField.getText().trim();
        String pass = new String(passwordField.getPassword());

        statusLabel.setText("Connecting to MySQL...");
        statusLabel.setForeground(UITheme.TEXT_MUTED);

        boolean success = DatabaseConfig.testConnection(host, port, db, user, pass);
        if (success) {
            statusLabel.setText("✓ Connection successful! Ready to use.");
            statusLabel.setForeground(UITheme.SUCCESS);
        } else {
            String err = DatabaseConfig.getLastErrorMessage();
            if (err == null || err.isEmpty()) err = "Check host, port, credentials and MySQL status.";
            statusLabel.setText("✗ Failed: " + (err.length() > 50 ? err.substring(0, 50) + "..." : err));
            statusLabel.setForeground(UITheme.DANGER);
            JOptionPane.showMessageDialog(this, 
                "Could not connect to MySQL server:\n" + err + 
                "\n\nPlease ensure MySQL is running on port " + port + 
                "\nand user '" + user + "' has privileges.",
                "Connection Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSave() {
        String host = hostField.getText().trim();
        int port = 3306;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Port must be a valid number (e.g. 3306).", "Invalid Port", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String db = dbNameField.getText().trim();
        String user = userField.getText().trim();
        String pass = new String(passwordField.getPassword());

        boolean ok = DatabaseConfig.saveConfiguration(host, port, db, user, pass);
        if (ok) {
            this.saved = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, DatabaseConfig.getLastErrorMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}

