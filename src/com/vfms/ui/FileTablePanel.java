package com.vfms.ui;

import com.vfms.model.VirtualFile;
import com.vfms.model.VirtualFolder;
import com.vfms.ui.theme.UITheme;
import com.vfms.util.FileUtils;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Main view displaying files and folders in a clean, modern table
 * with custom type icons, right-click context menu, and double-click actions.
 */
public class FileTablePanel extends JPanel {

    public interface FileTableListener {
        void onOpenFolder(int folderId);
        void onOpenFilePreview(int fileId);
        void onRenameRequested(boolean isFolder, int id, String currentName);
        void onDeleteRequested(boolean isFolder, int id, String name);
        void onExportRequested(int fileId, String fileName);
        void onPropertiesRequested(boolean isFolder, int id);
        void onSelectionChanged(int selectedCount, boolean hasFolderSelected, boolean hasFileSelected);
    }

    private final JTable table;
    private final FileSystemTableModel tableModel;
    private FileTableListener listener;
    private final JLabel emptyStateLabel;
    private final JScrollPane scrollPane;

    public FileTablePanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.SURFACE);

        tableModel = new FileSystemTableModel();
        table = new JTable(tableModel);
        table.setFont(UITheme.FONT_REGULAR);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(UITheme.FONT_BOLD);
        table.getTableHeader().setBackground(UITheme.SIDEBAR_BG);
        table.getTableHeader().setForeground(UITheme.TEXT_MUTED);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER));
        table.setFillsViewportHeight(true);

        setupRenderers();
        setupListeners();

        scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UITheme.SURFACE);

        emptyStateLabel = new JLabel("<html><div style='text-align: center; color: #94A3B8;'>"
            + "<h3 style='margin-bottom: 4px;'>This folder is empty</h3>"
            + "<p>Click <b>+ New Folder</b> or <b>+ New File</b> to get started.</p>"
            + "</div></html>", SwingConstants.CENTER);
        emptyStateLabel.setFont(UITheme.FONT_REGULAR);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void setListener(FileTableListener listener) {
        this.listener = listener;
    }

    public void setData(List<VirtualFolder> folders, List<VirtualFile> files) {
        tableModel.setData(folders, files);
        boolean isEmpty = (folders == null || folders.isEmpty()) && (files == null || files.isEmpty());
        if (isEmpty) {
            remove(scrollPane);
            add(emptyStateLabel, BorderLayout.CENTER);
        } else {
            remove(emptyStateLabel);
            add(scrollPane, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }

    public TableItem getSelectedItem() {
        int row = table.getSelectedRow();
        if (row >= 0 && row < tableModel.items.size()) {
            return tableModel.items.get(table.convertRowIndexToModel(row));
        }
        return null;
    }

    private void setupRenderers() {
        // Name & Icon renderer
        table.getColumnModel().getColumn(0).setPreferredWidth(260);
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                TableItem item = tableModel.items.get(table.convertRowIndexToModel(row));
                setIconTextGap(8);
                setFont(UITheme.FONT_REGULAR);
                if (item.isFolder) {
                    setText("📁 " + item.name);
                    setForeground(isSelected ? table.getSelectionForeground() : UITheme.TEXT_PRIMARY);
                } else {
                    setText(getFileIcon(item.extension) + " " + item.name);
                    setForeground(isSelected ? table.getSelectionForeground() : UITheme.TEXT_PRIMARY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                return this;
            }
        });

        // Type renderer
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setForeground(isSelected ? table.getSelectionForeground() : UITheme.TEXT_MUTED);
                setFont(UITheme.FONT_SMALL);
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
                return this;
            }
        });

        // Size renderer
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        DefaultTableCellRenderer sizeRenderer = new DefaultTableCellRenderer();
        sizeRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer((table1, value, isSelected, hasFocus, row, column) -> {
            Component c = sizeRenderer.getTableCellRendererComponent(table1, value, isSelected, hasFocus, row, column);
            c.setFont(UITheme.FONT_REGULAR);
            c.setForeground(isSelected ? table1.getSelectionForeground() : UITheme.TEXT_MUTED);
            return c;
        });

        // Date renderers
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
    }

    private String getFileIcon(String ext) {
        if (ext == null) return "📄";
        switch (ext.toLowerCase()) {
            case "txt":
            case "md": return "📝";
            case "pdf": return "📑";
            case "doc":
            case "docx": return "📘";
            case "xls":
            case "xlsx":
            case "csv": return "📊";
            case "ppt":
            case "pptx": return "📙";
            case "png":
            case "jpg":
            case "jpeg":
            case "gif": return "🖼️";
            case "java": return "☕";
            case "sql": return "🗃️";
            case "py": return "🐍";
            case "js":
            case "html":
            case "css": return "🌐";
            case "zip":
            case "rar":
            case "7z": return "📦";
            default: return "📄";
        }
    }

    private void setupListeners() {
        // Selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listener != null) {
                TableItem item = getSelectedItem();
                boolean hasSelection = item != null;
                boolean isFolder = hasSelection && item.isFolder;
                boolean isFile = hasSelection && !item.isFolder;
                listener.onSelectionChanged(hasSelection ? 1 : 0, isFolder, isFile);
            }
        });

        // Mouse double click & right click context menu
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                }

                TableItem item = getSelectedItem();
                if (item == null) return;

                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    if (item.isFolder && listener != null) {
                        listener.onOpenFolder(item.id);
                    } else if (!item.isFolder && listener != null) {
                        listener.onOpenFilePreview(item.id);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                handleContextMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleContextMenu(e);
            }
        });
    }

    private void handleContextMenu(MouseEvent e) {
        if (!e.isPopupTrigger()) return;
        int row = table.rowAtPoint(e.getPoint());
        if (row >= 0) {
            table.setRowSelectionInterval(row, row);
        }
        TableItem item = getSelectedItem();
        if (item == null) return;

        JPopupMenu popup = new JPopupMenu();

        JMenuItem openItem = new JMenuItem(item.isFolder ? "Open Folder" : "Preview File");
        openItem.setFont(UITheme.FONT_REGULAR);
        openItem.addActionListener(ev -> {
            if (listener != null) {
                if (item.isFolder) listener.onOpenFolder(item.id);
                else listener.onOpenFilePreview(item.id);
            }
        });
        popup.add(openItem);

        if (!item.isFolder) {
            JMenuItem exportItem = new JMenuItem("Export to Disk...");
            exportItem.setFont(UITheme.FONT_REGULAR);
            exportItem.addActionListener(ev -> {
                if (listener != null) listener.onExportRequested(item.id, item.name);
            });
            popup.add(exportItem);
        }

        popup.addSeparator();

        JMenuItem renameItem = new JMenuItem("Rename...");
        renameItem.setFont(UITheme.FONT_REGULAR);
        renameItem.addActionListener(ev -> {
            if (listener != null) listener.onRenameRequested(item.isFolder, item.id, item.name);
        });
        popup.add(renameItem);

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.setFont(UITheme.FONT_REGULAR);
        deleteItem.setForeground(UITheme.DANGER);
        deleteItem.addActionListener(ev -> {
            if (listener != null) listener.onDeleteRequested(item.isFolder, item.id, item.name);
        });
        popup.add(deleteItem);

        popup.addSeparator();

        JMenuItem propItem = new JMenuItem("Properties");
        propItem.setFont(UITheme.FONT_REGULAR);
        propItem.addActionListener(ev -> {
            if (listener != null) listener.onPropertiesRequested(item.isFolder, item.id);
        });
        popup.add(propItem);

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    // --- Inner Table Item & Model ---

    public static class TableItem {
        public final boolean isFolder;
        public final int id;
        public final String name;
        public final String extension;
        public final String type;
        public final String sizeFormatted;
        public final long sizeBytes;
        public final String created;
        public final String modified;

        public TableItem(VirtualFolder folder) {
            this.isFolder = true;
            this.id = folder.getId();
            this.name = folder.getName();
            this.extension = "";
            this.type = "File Folder";
            this.sizeFormatted = "-";
            this.sizeBytes = 0;
            this.created = FileUtils.formatDate(folder.getCreatedAt());
            this.modified = FileUtils.formatDate(folder.getUpdatedAt());
        }

        public TableItem(VirtualFile file) {
            this.isFolder = false;
            this.id = file.getId();
            this.name = file.getName();
            this.extension = file.getExtension();
            this.type = file.getFileType();
            this.sizeFormatted = file.getFormattedSize();
            this.sizeBytes = file.getFileSize();
            this.created = FileUtils.formatDate(file.getCreatedAt());
            this.modified = FileUtils.formatDate(file.getUpdatedAt());
        }
    }

    private static class FileSystemTableModel extends AbstractTableModel {
        private final String[] columns = { "Name", "Type", "Size", "Date Created", "Date Modified" };
        private final List<TableItem> items = new ArrayList<>();

        public void setData(List<VirtualFolder> folders, List<VirtualFile> files) {
            items.clear();
            if (folders != null) {
                for (VirtualFolder f : folders) {
                    items.add(new TableItem(f));
                }
            }
            if (files != null) {
                for (VirtualFile f : files) {
                    items.add(new TableItem(f));
                }
            }
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return items.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            TableItem item = items.get(rowIndex);
            switch (columnIndex) {
                case 0: return item.name;
                case 1: return item.type;
                case 2: return item.sizeFormatted;
                case 3: return item.created;
                case 4: return item.modified;
                default: return "";
            }
        }
    }
}

