package com.vfms.model;

import com.vfms.util.FileUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates global storage statistics across the virtual file system.
 */
public class StorageStats {
    private int totalFiles;
    private int totalFolders;
    private long totalBytes;
    private Map<String, Integer> typeCounts;

    public StorageStats() {
        this.typeCounts = new HashMap<>();
    }

    public StorageStats(int totalFiles, int totalFolders, long totalBytes, Map<String, Integer> typeCounts) {
        this.totalFiles = totalFiles;
        this.totalFolders = totalFolders;
        this.totalBytes = totalBytes;
        this.typeCounts = typeCounts != null ? typeCounts : new HashMap<>();
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }

    public int getTotalFolders() {
        return totalFolders;
    }

    public void setTotalFolders(int totalFolders) {
        this.totalFolders = totalFolders;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public String getFormattedTotalSize() {
        return FileUtils.formatFileSize(totalBytes);
    }

    public Map<String, Integer> getTypeCounts() {
        return typeCounts;
    }

    public void setTypeCounts(Map<String, Integer> typeCounts) {
        this.typeCounts = typeCounts;
    }
}

