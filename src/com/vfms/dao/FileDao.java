package com.vfms.dao;

import com.vfms.config.DatabaseConfig;
import com.vfms.model.StorageStats;
import com.vfms.model.VirtualFile;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for VirtualFile operations in MySQL.
 */
public class FileDao {

    /**
     * Inserts a new virtual file into the database.
     */
    public int createFile(VirtualFile file) throws SQLException {
        String sql = "INSERT INTO files (name, extension, file_type, file_size, folder_id, content) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, file.getName());
            stmt.setString(2, file.getExtension());
            stmt.setString(3, file.getFileType());
            stmt.setLong(4, file.getFileSize());

            if (file.getFolderId() == null) {
                stmt.setNull(5, Types.INTEGER);
            } else {
                stmt.setInt(5, file.getFolderId());
            }

            if (file.getContent() != null && file.getContent().length > 0) {
                stmt.setBinaryStream(6, new ByteArrayInputStream(file.getContent()), file.getContent().length);
            } else {
                stmt.setNull(6, Types.BLOB);
            }

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating file failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    file.setId(id);
                    return id;
                } else {
                    throw new SQLException("Creating file failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Retrieves file metadata (and optionally binary/text content) by ID.
     */
    public VirtualFile getFileById(int id, boolean loadContent) throws SQLException {
        String sql = loadContent 
            ? "SELECT id, name, extension, file_type, file_size, folder_id, content, created_at, updated_at FROM files WHERE id = ?"
            : "SELECT id, name, extension, file_type, file_size, folder_id, created_at, updated_at FROM files WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToFile(rs, loadContent);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all files inside a folder (or root if folderId is null).
     */
    public List<VirtualFile> getFilesByFolder(Integer folderId) throws SQLException {
        List<VirtualFile> list = new ArrayList<>();
        String sql;
        if (folderId == null) {
            sql = "SELECT id, name, extension, file_type, file_size, folder_id, created_at, updated_at FROM files WHERE folder_id IS NULL ORDER BY name ASC";
        } else {
            sql = "SELECT id, name, extension, file_type, file_size, folder_id, created_at, updated_at FROM files WHERE folder_id = ? ORDER BY name ASC";
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (folderId != null) {
                stmt.setInt(1, folderId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToFile(rs, false));
                }
            }
        }
        return list;
    }

    /**
     * Searches for files by keyword in file name across the entire system or in a specific folder.
     */
    public List<VirtualFile> searchFiles(String keyword, Integer folderId) throws SQLException {
        List<VirtualFile> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT id, name, extension, file_type, file_size, folder_id, created_at, updated_at " +
            "FROM files WHERE LOWER(name) LIKE ? "
        );

        if (folderId != null) {
            sql.append("AND folder_id = ? ");
        }
        sql.append("ORDER BY name ASC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            String searchPattern = "%" + keyword.toLowerCase().trim() + "%";
            stmt.setString(1, searchPattern);

            if (folderId != null) {
                stmt.setInt(2, folderId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToFile(rs, false));
                }
            }
        }
        return list;
    }

    /**
     * Renames a file, updating its name, extension, and file type.
     */
    public boolean renameFile(int id, String newName, String newExtension, String newFileType) throws SQLException {
        String sql = "UPDATE files SET name = ?, extension = ?, file_type = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setString(2, newExtension);
            stmt.setString(3, newFileType);
            stmt.setInt(4, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a file by primary key.
     */
    public boolean deleteFile(int id) throws SQLException {
        String sql = "DELETE FROM files WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Checks if a file with the given name already exists in the destination folder.
     */
    public boolean fileExists(Integer folderId, String name, Integer excludeId) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM files WHERE name = ? ");
        if (folderId == null) {
            sql.append("AND folder_id IS NULL ");
        } else {
            sql.append("AND folder_id = ? ");
        }
        if (excludeId != null) {
            sql.append("AND id != ? ");
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            stmt.setString(paramIndex++, name);
            if (folderId != null) {
                stmt.setInt(paramIndex++, folderId);
            }
            if (excludeId != null) {
                stmt.setInt(paramIndex++, excludeId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Aggregates system-wide storage metrics.
     */
    public StorageStats getStorageStats() throws SQLException {
        StorageStats stats = new StorageStats();

        // Count folders
        String folderSql = "SELECT COUNT(*) FROM folders";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(folderSql)) {
            if (rs.next()) {
                stats.setTotalFolders(rs.getInt(1));
            }
        }

        // Count files & total bytes
        String fileSql = "SELECT COUNT(*), COALESCE(SUM(file_size), 0) FROM files";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(fileSql)) {
            if (rs.next()) {
                stats.setTotalFiles(rs.getInt(1));
                stats.setTotalBytes(rs.getLong(2));
            }
        }

        // Breakdown by extension
        String typeSql = "SELECT extension, COUNT(*) FROM files GROUP BY extension ORDER BY COUNT(*) DESC LIMIT 5";
        Map<String, Integer> typeCounts = new HashMap<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(typeSql)) {
            while (rs.next()) {
                String ext = rs.getString(1);
                typeCounts.put(ext == null || ext.isEmpty() ? "other" : ext.toLowerCase(), rs.getInt(2));
            }
        }
        stats.setTypeCounts(typeCounts);

        return stats;
    }

    private VirtualFile mapRowToFile(ResultSet rs, boolean loadContent) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String extension = rs.getString("extension");
        String fileType = rs.getString("file_type");
        long fileSize = rs.getLong("file_size");
        int folderIdInt = rs.getInt("folder_id");
        Integer folderId = rs.wasNull() ? null : folderIdInt;

        VirtualFile file = new VirtualFile(
            id, name, extension, fileType, fileSize, folderId,
            rs.getTimestamp("created_at"),
            rs.getTimestamp("updated_at")
        );

        if (loadContent) {
            byte[] bytes = rs.getBytes("content");
            file.setContent(bytes);
        }
        return file;
    }
}

