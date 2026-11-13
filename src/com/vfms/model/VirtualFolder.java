package com.vfms.model;

import java.sql.Timestamp;

/**
 * Model representing a virtual folder in the file management system.
 * Folders can be nested in a hierarchical tree via parentId.
 */
public class VirtualFolder {
    private int id;
    private String name;
    private Integer parentId; // null represents the root directory
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String path; // optional resolved display path, e.g., "/Documents/College"

    public VirtualFolder() {
    }

    public VirtualFolder(String name, Integer parentId) {
        this.name = name;
        this.parentId = parentId;
    }

    public VirtualFolder(int id, String name, Integer parentId, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return name != null ? name : "Folder #" + id;
    }
}

