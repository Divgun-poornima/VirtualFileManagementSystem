package com.vfms.ui;

import com.vfms.model.VirtualFolder;
import com.vfms.ui.theme.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.util.List;
import java.util.function.Consumer;

/**
 * Clickable breadcrumb path navigation component.
 */
public class BreadcrumbPanel extends JPanel {

    private final Consumer<Integer> onFolderSelected;
    private final Runnable onNavigateUp;

    public BreadcrumbPanel(Consumer<Integer> onFolderSelected, Runnable onNavigateUp) {
        this.onFolderSelected = onFolderSelected;
        this.onNavigateUp = onNavigateUp;

        setLayout(new FlowLayout(FlowLayout.LEFT, 4, 6));
        setBackground(UITheme.SURFACE);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER));
    }

    public void updatePath(List<VirtualFolder> breadcrumbs, Integer currentFolderId) {
        removeAll();

        // Up button
        JButton upBtn = new JButton("↑ Up");
        upBtn.setFont(UITheme.FONT_SMALL);
        upBtn.setEnabled(currentFolderId != null);
        upBtn.setMargin(new Insets(2, 6, 2, 6));
        upBtn.setFocusPainted(false);
        upBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        upBtn.addActionListener(e -> onNavigateUp.run());
        add(upBtn);

        addSeparator();

        // Root crumb
        JButton rootBtn = createCrumbButton("📁 Home / Root", currentFolderId == null);
        rootBtn.addActionListener(e -> onFolderSelected.accept(null));
        add(rootBtn);

        // Subfolders crumbs
        if (breadcrumbs != null) {
            for (int i = 0; i < breadcrumbs.size(); i++) {
                addSeparator();
                VirtualFolder folder = breadcrumbs.get(i);
                boolean isLast = (i == breadcrumbs.size() - 1);
                JButton crumbBtn = createCrumbButton(folder.getName(), isLast);
                int fid = folder.getId();
                crumbBtn.addActionListener(e -> onFolderSelected.accept(fid));
                add(crumbBtn);
            }
        }

        revalidate();
        repaint();
    }

    private void addSeparator() {
        JLabel sep = new JLabel("›");
        sep.setFont(UITheme.FONT_HEADER);
        sep.setForeground(UITheme.TEXT_MUTED);
        add(sep);
    }

    private JButton createCrumbButton(String text, boolean isCurrent) {
        JButton btn = new JButton(text);
        btn.setFont(isCurrent ? UITheme.FONT_BOLD : UITheme.FONT_REGULAR);
        btn.setForeground(isCurrent ? UITheme.PRIMARY : UITheme.TEXT_PRIMARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(2, 4, 2, 4));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}

