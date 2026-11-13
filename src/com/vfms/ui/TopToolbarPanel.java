package com.vfms.ui;

import com.vfms.ui.theme.UITheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.util.function.Consumer;

/**
 * Top action toolbar providing quick access to folder/file operations and live search.
 */
public class TopToolbarPanel extends JPanel {

    public interface ToolbarListener {
        void onNewFolder();
        void onNewFile();
        void onImportFile();
        void onExportFile();
        void onDeleteSelected();
        void onRefresh();
        void onSearch(String query);
    }

    private final JButton newFolderBtn;
    private final JButton newFileBtn;
    private final JButton importBtn;
    private final JButton exportBtn;
    private final JButton deleteBtn;
    private final JButton refreshBtn;
    private final JTextField searchField;
    private ToolbarListener listener;

    public TopToolbarPanel() {
        setLayout(new BorderLayout(12, 0));
        setBackground(UITheme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        // Action buttons (Left)
        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftGroup.setBackground(UITheme.SURFACE);

        newFolderBtn = UITheme.createPrimaryButton("+ New Folder");
        newFolderBtn.setToolTipText("Create a new folder in the current directory");

        newFileBtn = UITheme.createSecondaryButton("+ New File");
        newFileBtn.setToolTipText("Create a new virtual text/code file");

        importBtn = UITheme.createSecondaryButton("📥 Import File");
        importBtn.setToolTipText("Upload a file from your computer into this virtual folder");

        exportBtn = UITheme.createSecondaryButton("📤 Export");
        exportBtn.setToolTipText("Export selected virtual file to your computer");
        exportBtn.setEnabled(false);

        deleteBtn = UITheme.createSecondaryButton("🗑️ Delete");
        deleteBtn.setToolTipText("Delete selected folder or file");
        deleteBtn.setEnabled(false);

        refreshBtn = UITheme.createSecondaryButton("🔄 Refresh");
        refreshBtn.setToolTipText("Reload files and folders");

        leftGroup.add(newFolderBtn);
        leftGroup.add(newFileBtn);
        leftGroup.add(importBtn);
        leftGroup.add(exportBtn);
        leftGroup.add(deleteBtn);
        leftGroup.add(refreshBtn);

        // Search bar (Right)
        JPanel rightGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightGroup.setBackground(UITheme.SURFACE);

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(UITheme.FONT_REGULAR);

        searchField = new JTextField(16);
        searchField.setFont(UITheme.FONT_REGULAR);
        searchField.setPreferredSize(new Dimension(200, 34));
        searchField.putClientProperty("JTextField.placeholderText", "Search files...");

        JButton clearSearchBtn = new JButton("✕");
        clearSearchBtn.setFont(UITheme.FONT_SMALL);
        clearSearchBtn.setMargin(new Insets(2, 6, 2, 6));
        clearSearchBtn.setFocusPainted(false);
        clearSearchBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearSearchBtn.addActionListener(e -> {
            searchField.setText("");
            if (listener != null) listener.onSearch("");
        });

        rightGroup.add(searchIcon);
        rightGroup.add(searchField);
        rightGroup.add(clearSearchBtn);

        add(leftGroup, BorderLayout.WEST);
        add(rightGroup, BorderLayout.EAST);

        setupEventHandlers();
    }

    public void setListener(ToolbarListener listener) {
        this.listener = listener;
    }

    public void updateSelectionState(boolean hasSelection, boolean hasFileSelected) {
        deleteBtn.setEnabled(hasSelection);
        exportBtn.setEnabled(hasFileSelected);
    }

    private void setupEventHandlers() {
        newFolderBtn.addActionListener(e -> { if (listener != null) listener.onNewFolder(); });
        newFileBtn.addActionListener(e -> { if (listener != null) listener.onNewFile(); });
        importBtn.addActionListener(e -> { if (listener != null) listener.onImportFile(); });
        exportBtn.addActionListener(e -> { if (listener != null) listener.onExportFile(); });
        deleteBtn.addActionListener(e -> { if (listener != null) listener.onDeleteSelected(); });
        refreshBtn.addActionListener(e -> { if (listener != null) listener.onRefresh(); });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { triggerSearch(); }
            @Override
            public void removeUpdate(DocumentEvent e) { triggerSearch(); }
            @Override
            public void changedUpdate(DocumentEvent e) { triggerSearch(); }

            private void triggerSearch() {
                if (listener != null) {
                    listener.onSearch(searchField.getText().trim());
                }
            }
        });
    }
}

