package com.vfms.dao;

import com.vfms.config.DatabaseConfig;
import com.vfms.model.VirtualFolder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data Access Object for VirtualFolder operations in MySQL.
 */
public class FolderDao {

    /**
     * Inserts a new folder into the database.
     * Returns generated primary key id.
     */
    public int createFolder(VirtualFolder folder) throws SQLException {
        String sql = "INSERT INTO folders (name, parent_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, folder.getName());
            if (folder.getParentId() == null) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setInt(2, folder.getParentId());
            }

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating folder failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    folder.setId(id);
                    return id;
                } else {
                    throw new SQLException("Creating folder failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Retrieves a folder by its primary key.
     */
    public VirtualFolder getFolderById(int id) throws SQLException {
        String sql = "SELECT id, name, parent_id, created_at, updated_at FROM folders WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToFolder(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves immediate subfolders for a given parent folder.
     * If parentId is null, returns top-level root folders.
     */
    public List<VirtualFolder> getSubFolders(Integer parentId) throws SQLException {
        List<VirtualFolder> list = new ArrayList<>();
        String sql;
        if (parentId == null) {
            sql = "SELECT id, name, parent_id, created_at, updated_at FROM folders WHERE parent_id IS NULL ORDER BY name ASC";
        } else {
            sql = "SELECT id, name, parent_id, created_at, updated_at FROM folders WHERE parent_id = ? ORDER BY name ASC";
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (parentId != null) {
                stmt.setInt(1, parentId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToFolder(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all folders in the database.
     */
    public List<VirtualFolder> getAllFolders() throws SQLException {
        List<VirtualFolder> list = new ArrayList<>();
        String sql = "SELECT id, name, parent_id, created_at, updated_at FROM folders ORDER BY name ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToFolder(rs));
            }
        }
        return list;
    }

    /**
     * Renames a folder.
     */
    public boolean renameFolder(int id, String newName) throws SQLException {
        String sql = "UPDATE folders SET name = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a folder by ID. Thanks to ON DELETE CASCADE, child folders and files are also removed.
     */
    public boolean deleteFolder(int id) throws SQLException {
        String sql = "DELETE FROM folders WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Checks if a folder with the given name already exists under the parent folder.
     */
    public boolean folderExists(Integer parentId, String name, Integer excludeId) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM folders WHERE name = ? ");
        if (parentId == null) {
            sql.append("AND parent_id IS NULL ");
        } else {
            sql.append("AND parent_id = ? ");
        }
        if (excludeId != null) {
            sql.append("AND id != ? ");
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            stmt.setString(paramIndex++, name);
            if (parentId != null) {
                stmt.setInt(paramIndex++, parentId);
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
     * Computes the display path of a folder, e.g. "Root / Documents / Projects".
     */
    public String getFolderPath(int folderId) throws SQLException {
        List<String> pathSegments = new ArrayList<>();
        Integer currentId = folderId;

        while (currentId != null) {
            VirtualFolder f = getFolderById(currentId);
            if (f == null) break;
            pathSegments.add(f.getName());
            currentId = f.getParentId();
        }

        Collections.reverse(pathSegments);
        if (pathSegments.isEmpty()) {
            return "Root";
        }
        return "Root / " + String.join(" / ", pathSegments);
    }

    private VirtualFolder mapRowToFolder(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        int parentIdInt = rs.getInt("parent_id");
        Integer parentId = rs.wasNull() ? null : parentIdInt;
        VirtualFolder folder = new VirtualFolder(
            id,
            name,
            parentId,
            rs.getTimestamp("created_at"),
            rs.getTimestamp("updated_at")
        );
        return folder;
    }
}

