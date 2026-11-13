package com.vfms.model;

import com.vfms.util.FileUtils;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;

/**
 * Model representing a virtual file stored in the management system.
 * Contains file metadata and optional virtual content.
 */
public class VirtualFile {
    private int id;
    private String name;
    private String extension;
    private String fileType;
    private long fileSize; // size in bytes
    private Integer folderId; // null represents placed in root directory
    private byte[] content;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public VirtualFile() {
    }

    public VirtualFile(String name, String extension, String fileType, long fileSize, Integer folderId, byte[] content) {
        this.name = name;
        this.extension = extension;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.folderId = folderId;
        this.content = content;
    }

    public VirtualFile(int id, String name, String extension, String fileType, long fileSize, Integer folderId, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.name = name;
        this.extension = extension;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.folderId = folderId;
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

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getFolderId() {
        return folderId;
    }

    public void setFolderId(Integer folderId) {
        this.folderId = folderId;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
        if (content != null) {
            this.fileSize = content.length;
        }
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

    public String getFormattedSize() {
        return FileUtils.formatFileSize(fileSize);
    }

    public String getContentAsString() {
        if (content == null) {
            return "";
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    public boolean isTextBased() {
        return FileUtils.isTextExtension(extension);
    }

    @Override
    public String toString() {
        return name != null ? name : "File #" + id;
    }
}

