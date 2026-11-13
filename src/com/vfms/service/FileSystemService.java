package com.vfms.service;

import com.vfms.config.DatabaseConfig;
import com.vfms.dao.FileDao;
import com.vfms.dao.FolderDao;
import com.vfms.model.StorageStats;
import com.vfms.model.VirtualFile;
import com.vfms.model.VirtualFolder;
import com.vfms.util.FileUtils;
import com.vfms.util.ValidationUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service orchestrating virtual file and folder operations,
 * input validation, navigation hierarchy, physical file imports/exports,
 * and resilient database-to-mock failover.
 */
public class FileSystemService {

    private final FolderDao folderDao;
    private final FileDao fileDao;

    private Integer currentFolderId = null; // null represents Root
    private boolean useMockStorage = false;

    // In-memory mock storage structures (for offline demo mode when MySQL isn't reachable)
    private final List<VirtualFolder> mockFolders = new ArrayList<>();
    private final List<VirtualFile> mockFiles = new ArrayList<>();
    private final AtomicInteger mockFolderIdGen = new AtomicInteger(100);
    private final AtomicInteger mockFileIdGen = new AtomicInteger(100);

    public FileSystemService() {
        this.folderDao = new FolderDao();
        this.fileDao = new FileDao();
        checkDatabaseAvailability();
    }

    /**
     * Checks if MySQL database is reachable. If not, prepares in-memory demo data.
     */
    public boolean checkDatabaseAvailability() {
        try {
            folderDao.getSubFolders(null);
            useMockStorage = false;
            return true;
        } catch (Exception e) {
            useMockStorage = true;
            if (mockFolders.isEmpty()) {
                initializeMockData();
            }
            return false;
        }
    }

    public boolean isUsingMockStorage() {
        return useMockStorage;
    }

    public void setUseMockStorage(boolean mock) {
        this.useMockStorage = mock;
    }

    public Integer getCurrentFolderId() {
        return currentFolderId;
    }

    public void setCurrentFolderId(Integer folderId) {
        this.currentFolderId = folderId;
    }

    /**
     * Navigates to the parent directory.
     */
    public void navigateUp() throws Exception {
        if (currentFolderId == null) {
            return; // Already at root
        }
        if (useMockStorage) {
            VirtualFolder current = findMockFolderById(currentFolderId);
            currentFolderId = current != null ? current.getParentId() : null;
        } else {
            VirtualFolder current = folderDao.getFolderById(currentFolderId);
            currentFolderId = current != null ? current.getParentId() : null;
        }
    }

    /**
     * Returns list of subfolders in current directory.
     */
    public List<VirtualFolder> getCurrentSubFolders() throws Exception {
        if (useMockStorage) {
            List<VirtualFolder> result = new ArrayList<>();
            for (VirtualFolder f : mockFolders) {
                if (currentFolderId == null) {
                    if (f.getParentId() == null) result.add(f);
                } else {
                    if (currentFolderId.equals(f.getParentId())) result.add(f);
                }
            }
            return result;
        }
        return folderDao.getSubFolders(currentFolderId);
    }

    /**
     * Returns list of files in current directory.
     */
    public List<VirtualFile> getCurrentFiles() throws Exception {
        if (useMockStorage) {
            List<VirtualFile> result = new ArrayList<>();
            for (VirtualFile f : mockFiles) {
                if (currentFolderId == null) {
                    if (f.getFolderId() == null) result.add(f);
                } else {
                    if (currentFolderId.equals(f.getFolderId())) result.add(f);
                }
            }
            return result;
        }
        return fileDao.getFilesByFolder(currentFolderId);
    }

    /**
     * Creates a new folder inside the current directory.
     */
    public void createFolder(String name) throws Exception {
        String validationError = ValidationUtils.validateName(name);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        String trimmedName = name.trim();
        if (useMockStorage) {
            for (VirtualFolder f : mockFolders) {
                boolean sameParent = (currentFolderId == null && f.getParentId() == null) ||
                                     (currentFolderId != null && currentFolderId.equals(f.getParentId()));
                if (sameParent && f.getName().equalsIgnoreCase(trimmedName)) {
                    throw new IllegalArgumentException("A folder named '" + trimmedName + "' already exists here.");
                }
            }
            VirtualFolder newFolder = new VirtualFolder(
                mockFolderIdGen.incrementAndGet(),
                trimmedName,
                currentFolderId,
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis())
            );
            mockFolders.add(newFolder);
            return;
        }

        if (folderDao.folderExists(currentFolderId, trimmedName, null)) {
            throw new IllegalArgumentException("A folder named '" + trimmedName + "' already exists here.");
        }

        VirtualFolder folder = new VirtualFolder(trimmedName, currentFolderId);
        folderDao.createFolder(folder);
    }

    /**
     * Creates a new virtual file with initial text content.
     */
    public void createFile(String name, String contentStr) throws Exception {
        String validationError = ValidationUtils.validateName(name);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        String trimmedName = name.trim();
        String extension = FileUtils.getFileExtension(trimmedName);
        String fileType = FileUtils.determineFileType(extension);
        byte[] contentBytes = (contentStr != null) ? contentStr.getBytes(StandardCharsets.UTF_8) : new byte[0];

        if (useMockStorage) {
            for (VirtualFile f : mockFiles) {
                boolean sameFolder = (currentFolderId == null && f.getFolderId() == null) ||
                                     (currentFolderId != null && currentFolderId.equals(f.getFolderId()));
                if (sameFolder && f.getName().equalsIgnoreCase(trimmedName)) {
                    throw new IllegalArgumentException("A file named '" + trimmedName + "' already exists here.");
                }
            }
            VirtualFile newFile = new VirtualFile(
                mockFileIdGen.incrementAndGet(),
                trimmedName, extension, fileType, contentBytes.length, currentFolderId,
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis())
            );
            newFile.setContent(contentBytes);
            mockFiles.add(newFile);
            return;
        }

        if (fileDao.fileExists(currentFolderId, trimmedName, null)) {
            throw new IllegalArgumentException("A file named '" + trimmedName + "' already exists here.");
        }

        VirtualFile file = new VirtualFile(
            trimmedName, extension, fileType, contentBytes.length, currentFolderId, contentBytes
        );
        fileDao.createFile(file);
    }

    /**
     * Imports an existing file from the local physical drive into the virtual system.
     */
    public void importRealFile(File physicalFile) throws Exception {
        if (physicalFile == null || !physicalFile.exists() || !physicalFile.isFile()) {
            throw new IllegalArgumentException("Invalid source file selected.");
        }

        String fileName = physicalFile.getName();
        String extension = FileUtils.getFileExtension(fileName);
        String fileType = FileUtils.determineFileType(extension);
        long fileSize = physicalFile.length();

        // Safety limit: 50MB for in-memory / database blob storage
        if (fileSize > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("File exceeds maximum allowed size (50 MB).");
        }

        byte[] fileBytes = new byte[(int) fileSize];
        try (FileInputStream fis = new FileInputStream(physicalFile)) {
            int read = fis.read(fileBytes);
            if (read != fileSize) {
                throw new IOException("Could not completely read physical file data.");
            }
        }

        if (useMockStorage) {
            for (VirtualFile f : mockFiles) {
                boolean sameFolder = (currentFolderId == null && f.getFolderId() == null) ||
                                     (currentFolderId != null && currentFolderId.equals(f.getFolderId()));
                if (sameFolder && f.getName().equalsIgnoreCase(fileName)) {
                    throw new IllegalArgumentException("A file named '" + fileName + "' already exists here.");
                }
            }
            VirtualFile vf = new VirtualFile(
                mockFileIdGen.incrementAndGet(),
                fileName, extension, fileType, fileSize, currentFolderId,
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis())
            );
            vf.setContent(fileBytes);
            mockFiles.add(vf);
            return;
        }

        if (fileDao.fileExists(currentFolderId, fileName, null)) {
            throw new IllegalArgumentException("A file named '" + fileName + "' already exists here.");
        }

        VirtualFile vf = new VirtualFile(fileName, extension, fileType, fileSize, currentFolderId, fileBytes);
        fileDao.createFile(vf);
    }

    /**
     * Exports a virtual file to a target location on the physical computer.
     */
    public void exportFile(int fileId, File destinationFile) throws Exception {
        VirtualFile file = getFileDetails(fileId, true);
        if (file == null) {
            throw new IllegalArgumentException("File not found.");
        }

        byte[] content = file.getContent();
        if (content == null) {
            content = new byte[0];
        }

        try (FileOutputStream fos = new FileOutputStream(destinationFile)) {
            fos.write(content);
        }
    }

    /**
     * Renames a folder.
     */
    public void renameFolder(int folderId, String newName) throws Exception {
        String validationError = ValidationUtils.validateName(newName);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        String trimmed = newName.trim();
        if (useMockStorage) {
            VirtualFolder target = findMockFolderById(folderId);
            if (target == null) throw new IllegalArgumentException("Folder not found.");
            for (VirtualFolder f : mockFolders) {
                if (f.getId() != folderId) {
                    boolean sameParent = (target.getParentId() == null && f.getParentId() == null) ||
                                         (target.getParentId() != null && target.getParentId().equals(f.getParentId()));
                    if (sameParent && f.getName().equalsIgnoreCase(trimmed)) {
                        throw new IllegalArgumentException("Another folder with name '" + trimmed + "' already exists here.");
                    }
                }
            }
            target.setName(trimmed);
            target.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            return;
        }

        VirtualFolder folder = folderDao.getFolderById(folderId);
        if (folder == null) throw new IllegalArgumentException("Folder not found.");

        if (folderDao.folderExists(folder.getParentId(), trimmed, folderId)) {
            throw new IllegalArgumentException("Another folder with name '" + trimmed + "' already exists here.");
        }

        folderDao.renameFolder(folderId, trimmed);
    }

    /**
     * Renames a file.
     */
    public void renameFile(int fileId, String newName) throws Exception {
        String validationError = ValidationUtils.validateName(newName);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        String trimmed = newName.trim();
        String newExtension = FileUtils.getFileExtension(trimmed);
        String newFileType = FileUtils.determineFileType(newExtension);

        if (useMockStorage) {
            VirtualFile target = findMockFileById(fileId);
            if (target == null) throw new IllegalArgumentException("File not found.");
            for (VirtualFile f : mockFiles) {
                if (f.getId() != fileId) {
                    boolean sameFolder = (target.getFolderId() == null && f.getFolderId() == null) ||
                                         (target.getFolderId() != null && target.getFolderId().equals(f.getFolderId()));
                    if (sameFolder && f.getName().equalsIgnoreCase(trimmed)) {
                        throw new IllegalArgumentException("Another file with name '" + trimmed + "' already exists here.");
                    }
                }
            }
            target.setName(trimmed);
            target.setExtension(newExtension);
            target.setFileType(newFileType);
            target.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            return;
        }

        VirtualFile file = fileDao.getFileById(fileId, false);
        if (file == null) throw new IllegalArgumentException("File not found.");

        if (fileDao.fileExists(file.getFolderId(), trimmed, fileId)) {
            throw new IllegalArgumentException("Another file with name '" + trimmed + "' already exists here.");
        }

        fileDao.renameFile(fileId, trimmed, newExtension, newFileType);
    }

    /**
     * Deletes a folder and all its contents.
     */
    public void deleteFolder(int folderId) throws Exception {
        if (useMockStorage) {
            deleteMockFolderRecursively(folderId);
            return;
        }
        folderDao.deleteFolder(folderId);
    }

    /**
     * Deletes a single file.
     */
    public void deleteFile(int fileId) throws Exception {
        if (useMockStorage) {
            mockFiles.removeIf(f -> f.getId() == fileId);
            return;
        }
        fileDao.deleteFile(fileId);
    }

    /**
     * Retrieves file metadata and optional content.
     */
    public VirtualFile getFileDetails(int fileId, boolean loadContent) throws Exception {
        if (useMockStorage) {
            return findMockFileById(fileId);
        }
        return fileDao.getFileById(fileId, loadContent);
    }

    /**
     * Searches for files by keyword.
     */
    public List<VirtualFile> searchFiles(String keyword) throws Exception {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getCurrentFiles();
        }
        String term = keyword.toLowerCase().trim();

        if (useMockStorage) {
            List<VirtualFile> results = new ArrayList<>();
            for (VirtualFile f : mockFiles) {
                if (f.getName().toLowerCase().contains(term)) {
                    results.add(f);
                }
            }
            return results;
        }
        return fileDao.searchFiles(term, null);
    }

    /**
     * Computes breadcrumb hierarchy path.
     */
    public List<VirtualFolder> getBreadcrumbs() throws Exception {
        List<VirtualFolder> breadcrumbs = new ArrayList<>();
        if (currentFolderId == null) {
            return breadcrumbs;
        }

        Integer id = currentFolderId;
        while (id != null) {
            VirtualFolder f;
            if (useMockStorage) {
                f = findMockFolderById(id);
            } else {
                f = folderDao.getFolderById(id);
            }
            if (f == null) break;
            breadcrumbs.add(f);
            id = f.getParentId();
        }

        Collections.reverse(breadcrumbs);
        return breadcrumbs;
    }

    /**
     * Calculates storage statistics across all files and folders.
     */
    public StorageStats getStorageStats() throws Exception {
        if (useMockStorage) {
            StorageStats stats = new StorageStats();
            stats.setTotalFolders(mockFolders.size());
            stats.setTotalFiles(mockFiles.size());
            long totalBytes = 0;
            Map<String, Integer> counts = new HashMap<>();
            for (VirtualFile f : mockFiles) {
                totalBytes += f.getFileSize();
                String ext = f.getExtension().toLowerCase();
                counts.put(ext.isEmpty() ? "other" : ext, counts.getOrDefault(ext, 0) + 1);
            }
            stats.setTotalBytes(totalBytes);
            stats.setTypeCounts(counts);
            return stats;
        }
        return fileDao.getStorageStats();
    }

    // --- Internal Mock Helpers ---

    private VirtualFolder findMockFolderById(int id) {
        for (VirtualFolder f : mockFolders) {
            if (f.getId() == id) return f;
        }
        return null;
    }

    private VirtualFile findMockFileById(int id) {
        for (VirtualFile f : mockFiles) {
            if (f.getId() == id) return f;
        }
        return null;
    }

    private void deleteMockFolderRecursively(int folderId) {
        List<Integer> childFolderIds = new ArrayList<>();
        for (VirtualFolder f : mockFolders) {
            if (Integer.valueOf(folderId).equals(f.getParentId())) {
                childFolderIds.add(f.getId());
            }
        }
        for (int childId : childFolderIds) {
            deleteMockFolderRecursively(childId);
        }
        mockFiles.removeIf(file -> Integer.valueOf(folderId).equals(file.getFolderId()));
        mockFolders.removeIf(folder -> folder.getId() == folderId);
    }

    private void initializeMockData() {
        mockFolders.clear();
        mockFiles.clear();

        Timestamp now = new Timestamp(System.currentTimeMillis());

        VirtualFolder docs = new VirtualFolder(1, "Documents", null, now, now);
        VirtualFolder college = new VirtualFolder(2, "College Projects", null, now, now);
        VirtualFolder pics = new VirtualFolder(3, "Pictures", null, now, now);

        VirtualFolder nsp = new VirtualFolder(4, "NSP Minor Project", 2, now, now);
        VirtualFolder dblab = new VirtualFolder(5, "Database Lab", 2, now, now);

        mockFolders.add(docs);
        mockFolders.add(college);
        mockFolders.add(pics);
        mockFolders.add(nsp);
        mockFolders.add(dblab);

        byte[] welcomeBytes = ("Welcome to Virtual File Management System!\n"
            + "This project demonstrates hierarchical file management with Java Swing & MySQL.\n"
            + "Notice: Running in Demo Mode because MySQL database is not yet connected.\n"
            + "You can test all folder/file actions here, or click 'Database Settings' to connect MySQL.")
            .getBytes(StandardCharsets.UTF_8);

        VirtualFile welcome = new VirtualFile(
            1, "README.txt", "txt", "Text Document", welcomeBytes.length, null, now, now
        );
        welcome.setContent(welcomeBytes);
        mockFiles.add(welcome);

        byte[] codeBytes = "public class HelloWorld {\n    public static void main(String[] args) {\n        System.out.println(\"Hello VFMS!\");\n    }\n}"
            .getBytes(StandardCharsets.UTF_8);
        VirtualFile code = new VirtualFile(
            2, "Demo.java", "java", "Java Source Code", codeBytes.length, 4, now, now
        );
        code.setContent(codeBytes);
        mockFiles.add(code);
    }
}

