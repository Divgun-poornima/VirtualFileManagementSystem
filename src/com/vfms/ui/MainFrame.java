package com.vfms.ui;

import com.vfms.config.DatabaseConfig;
import com.vfms.model.StorageStats;
import com.vfms.model.VirtualFile;
import com.vfms.model.VirtualFolder;
import com.vfms.service.FileSystemService;
import com.vfms.ui.dialogs.CreateFileDialog;
import com.vfms.ui.dialogs.CreateFolderDialog;
import com.vfms.ui.dialogs.DatabaseConfigDialog;
import com.vfms.ui.dialogs.FilePreviewDialog;
import com.vfms.ui.dialogs.FilePropertiesDialog;
import com.vfms.ui.dialogs.RenameDialog;
import com.vfms.ui.theme.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Main application window for Virtual File Management System.
 * Connects the view components with FileSystemService.
 */
public class MainFrame extends JFrame implements 
        FileTablePanel.FileTableListener, 
        TopToolbarPanel.ToolbarListener, 
        SidebarPanel.SidebarListener {

    private final FileSystemService fileService;
    private final SidebarPanel sidebarPanel;
    private final TopToolbarPanel toolbarPanel;
    private final BreadcrumbPanel breadcrumbPanel;
    private final FileTablePanel tablePanel;
    private final StatusBarPanel statusBarPanel;

    private List<VirtualFolder> currentFolders;
    private List<VirtualFile> currentFiles;

    public MainFrame() {
        super("Virtual File Management System - Dashboard");
        this.fileService = new FileSystemService();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 600));
        setPreferredSize(new Dimension(1120, 720));
        setLocationRelativeTo(null);

        // Panels initialization
        sidebarPanel = new SidebarPanel();
        sidebarPanel.setListener(this);

        toolbarPanel = new TopToolbarPanel();
        toolbarPanel.setListener(this);

        breadcrumbPanel = new BreadcrumbPanel(this::navigateToFolder, this::navigateUp);

        tablePanel = new FileTablePanel();
        tablePanel.setListener(this);

        statusBarPanel = new StatusBarPanel();

        // Layout construction
        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.add(breadcrumbPanel, BorderLayout.NORTH);
        contentArea.add(tablePanel, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarPanel, contentArea);
        splitPane.setDividerLocation(240);
        splitPane.setDividerSize(1);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.add(toolbarPanel, BorderLayout.NORTH);
        rootPanel.add(splitPane, BorderLayout.CENTER);
        rootPanel.add(statusBarPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
        setupKeyBindings();

        // Initial Data Load
        refreshView();
    }

    private void setupKeyBindings() {
        // F5 Refresh
        getRootPane().getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "refresh"
        );
        getRootPane().getActionMap().put("refresh", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshView();
            }
        });

        // Delete key
        getRootPane().getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete"
        );
        getRootPane().getActionMap().put("delete", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDeleteSelected();
            }
        });
    }

    /**
     * Refreshes the active folder view, breadcrumbs, and metrics.
     */
    public void refreshView() {
        try {
            boolean dbOk = !fileService.isUsingMockStorage();
            sidebarPanel.updateDatabaseStatus(dbOk, fileService.isUsingMockStorage());

            currentFolders = fileService.getCurrentSubFolders();
            currentFiles = fileService.getCurrentFiles();

            tablePanel.setData(currentFolders, currentFiles);
            breadcrumbPanel.updatePath(fileService.getBreadcrumbs(), fileService.getCurrentFolderId());

            int folderCount = currentFolders != null ? currentFolders.size() : 0;
            int fileCount = currentFiles != null ? currentFiles.size() : 0;
            statusBarPanel.updateItemCount(folderCount, fileCount, 0);

            // Update global stats
            StorageStats stats = fileService.getStorageStats();
            sidebarPanel.updateStats(stats);

            toolbarPanel.updateSelectionState(false, false);
            statusBarPanel.setStatusMessage(fileService.isUsingMockStorage() 
                ? "Running in Demo Mode (In-Memory). Configure MySQL in Database Settings." 
                : "Connected to MySQL database.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading directory: " + e.getMessage(), 
                "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Navigation ---

    private void navigateToFolder(Integer folderId) {
        fileService.setCurrentFolderId(folderId);
        refreshView();
    }

    private void navigateUp() {
        try {
            fileService.navigateUp();
            refreshView();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not navigate up: " + e.getMessage());
        }
    }

    // --- FileTableListener implementation ---

    @Override
    public void onOpenFolder(int folderId) {
        navigateToFolder(folderId);
    }

    @Override
    public void onOpenFilePreview(int fileId) {
        try {
            VirtualFile file = fileService.getFileDetails(fileId, true);
            if (file != null) {
                new FilePreviewDialog(this, file).setVisible(true);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not load file preview: " + e.getMessage(), "Preview Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void onRenameRequested(boolean isFolder, int id, String currentName) {
        RenameDialog dialog = new RenameDialog(this, currentName, isFolder);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            String newName = dialog.getNewName();
            if (newName.equals(currentName)) return;

            try {
                if (isFolder) {
                    fileService.renameFolder(id, newName);
                    statusBarPanel.setStatusMessage("Renamed folder to: " + newName);
                } else {
                    fileService.renameFile(id, newName);
                    statusBarPanel.setStatusMessage("Renamed file to: " + newName);
                }
                refreshView();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Rename Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    @Override
    public void onDeleteRequested(boolean isFolder, int id, String name) {
        String itemType = isFolder ? "folder and all its contents" : "file";
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to permanently delete the " + itemType + " '" + name + "'?\nThis action cannot be undone.",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (isFolder) {
                    fileService.deleteFolder(id);
                    statusBarPanel.setStatusMessage("Deleted folder: " + name);
                } else {
                    fileService.deleteFile(id);
                    statusBarPanel.setStatusMessage("Deleted file: " + name);
                }
                refreshView();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Could not delete: " + e.getMessage(), "Delete Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onExportRequested(int fileId, String fileName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Virtual File to Computer");
        chooser.setSelectedFile(new File(fileName));

        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            if (target.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(
                    this, "File '" + target.getName() + "' already exists. Overwrite it?",
                    "Overwrite Confirmation", JOptionPane.YES_NO_OPTION
                );
                if (overwrite != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            try {
                fileService.exportFile(fileId, target);
                statusBarPanel.setStatusMessage("Exported file to: " + target.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "File successfully exported!", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onPropertiesRequested(boolean isFolder, int id) {
        try {
            String path = "Root";
            if (fileService.getCurrentFolderId() != null) {
                List<VirtualFolder> crumbs = fileService.getBreadcrumbs();
                StringBuilder sb = new StringBuilder("Root");
                for (VirtualFolder f : crumbs) {
                    sb.append(" / ").append(f.getName());
                }
                path = sb.toString();
            }

            if (isFolder) {
                for (VirtualFolder f : currentFolders) {
                    if (f.getId() == id) {
                        new FilePropertiesDialog(this, f, path).setVisible(true);
                        break;
                    }
                }
            } else {
                VirtualFile vf = fileService.getFileDetails(id, false);
                if (vf != null) {
                    new FilePropertiesDialog(this, vf, path).setVisible(true);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not read properties: " + e.getMessage());
        }
    }

    @Override
    public void onSelectionChanged(int selectedCount, boolean hasFolderSelected, boolean hasFileSelected) {
        toolbarPanel.updateSelectionState(selectedCount > 0, hasFileSelected);
        int folderCount = currentFolders != null ? currentFolders.size() : 0;
        int fileCount = currentFiles != null ? currentFiles.size() : 0;
        statusBarPanel.updateItemCount(folderCount, fileCount, selectedCount);
    }

    // --- ToolbarListener implementation ---

    @Override
    public void onNewFolder() {
        CreateFolderDialog dialog = new CreateFolderDialog(this);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            try {
                fileService.createFolder(dialog.getFolderName());
                statusBarPanel.setStatusMessage("Created folder: " + dialog.getFolderName());
                refreshView();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot Create Folder", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    @Override
    public void onNewFile() {
        CreateFileDialog dialog = new CreateFileDialog(this);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            try {
                fileService.createFile(dialog.getFullFileName(), dialog.getFileContent());
                statusBarPanel.setStatusMessage("Created file: " + dialog.getFullFileName());
                refreshView();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot Create File", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    @Override
    public void onImportFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a File to Import into Virtual File System");
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            try {
                fileService.importRealFile(selected);
                statusBarPanel.setStatusMessage("Imported: " + selected.getName());
                refreshView();
                JOptionPane.showMessageDialog(this, "File '" + selected.getName() + "' imported successfully!", "Import Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Import failed: " + e.getMessage(), "Import Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onExportFile() {
        FileTablePanel.TableItem item = tablePanel.getSelectedItem();
        if (item != null && !item.isFolder) {
            onExportRequested(item.id, item.name);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a file to export.", "No File Selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void onDeleteSelected() {
        FileTablePanel.TableItem item = tablePanel.getSelectedItem();
        if (item != null) {
            onDeleteRequested(item.isFolder, item.id, item.name);
        }
    }

    @Override
    public void onRefresh() {
        refreshView();
    }

    @Override
    public void onSearch(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                refreshView();
                return;
            }

            List<VirtualFile> searchResults = fileService.searchFiles(query);
            tablePanel.setData(null, searchResults);
            statusBarPanel.setStatusMessage("Search found " + searchResults.size() + " matching file(s).");
            statusBarPanel.updateItemCount(0, searchResults.size(), 0);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage());
        }
    }

    // --- SidebarListener implementation ---

    @Override
    public void onQuickFolderSelected(String folderName) {
        if (folderName == null) {
            navigateToFolder(null); // Root
            return;
        }

        try {
            // Find folder in root with that name
            fileService.setCurrentFolderId(null);
            List<VirtualFolder> rootFolders = fileService.getCurrentSubFolders();
            for (VirtualFolder f : rootFolders) {
                if (f.getName().equalsIgnoreCase(folderName)) {
                    navigateToFolder(f.getId());
                    return;
                }
            }
            // If doesn't exist, navigate to root and notify
            navigateToFolder(null);
            statusBarPanel.setStatusMessage("Folder '" + folderName + "' not found at root.");
        } catch (Exception e) {
            navigateToFolder(null);
        }
    }

    @Override
    public void onOpenDatabaseSettings() {
        DatabaseConfigDialog dialog = new DatabaseConfigDialog(this);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            boolean connected = fileService.checkDatabaseAvailability();
            if (connected) {
                JOptionPane.showMessageDialog(this, "Successfully connected to MySQL!", "Connected", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Settings saved, but MySQL connection failed:\n" + DatabaseConfig.getLastErrorMessage() +
                    "\n\nContinuing in Demo Mode.",
                    "Connection Warning", JOptionPane.WARNING_MESSAGE);
            }
            refreshView();
        }
    }

    @Override
    public void onStorageStatsClicked() {
        try {
            StorageStats stats = fileService.getStorageStats();
            StringBuilder msg = new StringBuilder();
            msg.append("Total Folders: ").append(stats.getTotalFolders()).append("\n");
            msg.append("Total Files: ").append(stats.getTotalFiles()).append("\n");
            msg.append("Total Space Used: ").append(stats.getFormattedTotalSize()).append("\n\n");
            msg.append("File Type Breakdown:\n");

            if (stats.getTypeCounts().isEmpty()) {
                msg.append("  (No files stored yet)\n");
            } else {
                for (Map.Entry<String, Integer> entry : stats.getTypeCounts().entrySet()) {
                    msg.append("  • .").append(entry.getKey()).append(": ").append(entry.getValue()).append(" file(s)\n");
                }
            }

            JOptionPane.showMessageDialog(this, msg.toString(), "Storage Overview Statistics", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not compute statistics: " + e.getMessage());
        }
    }
}

