package com.vfms.ui;

import com.vfms.ui.theme.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * Bottom status bar providing summary counts, feedback messages, and system state.
 */
public class StatusBarPanel extends JPanel {

    private final JLabel itemsLabel;
    private final JLabel statusMessageLabel;

    public StatusBarPanel() {
        setLayout(new BorderLayout(16, 0));
        setBackground(UITheme.SIDEBAR_BG);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER),
            BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));
        setPreferredSize(new Dimension(800, 32));

        itemsLabel = new JLabel("0 items");
        itemsLabel.setFont(UITheme.FONT_SMALL);
        itemsLabel.setForeground(UITheme.TEXT_MUTED);

        statusMessageLabel = new JLabel("Ready");
        statusMessageLabel.setFont(UITheme.FONT_SMALL);
        statusMessageLabel.setForeground(UITheme.TEXT_MUTED);

        add(itemsLabel, BorderLayout.WEST);
        add(statusMessageLabel, BorderLayout.EAST);
    }

    public void updateItemCount(int folderCount, int fileCount, int selectedCount) {
        StringBuilder sb = new StringBuilder();
        sb.append(folderCount).append(folderCount == 1 ? " folder, " : " folders, ");
        sb.append(fileCount).append(fileCount == 1 ? " file" : " files");
        if (selectedCount > 0) {
            sb.append(" | ").append(selectedCount).append(" item selected");
        }
        itemsLabel.setText(sb.toString());
    }

    public void setStatusMessage(String message) {
        statusMessageLabel.setText(message != null ? message : "Ready");
    }
}

