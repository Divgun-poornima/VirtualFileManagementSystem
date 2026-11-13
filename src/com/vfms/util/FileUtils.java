package com.vfms.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for file operations, extension handling, MIME types, and formatting.
 */
public class FileUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private static final Set<String> TEXT_EXTENSIONS = new HashSet<>(Arrays.asList(
        "txt", "md", "csv", "json", "xml", "html", "htm", "css", "js", 
        "java", "c", "cpp", "py", "sql", "log", "properties", "yaml", "yml", "ini"
    ));

    /**
     * Formats bytes into human-readable string (B, KB, MB, GB).
     */
    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        if (digitGroups >= units.length) digitGroups = units.length - 1;
        return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * Extracts extension from a file name (lowercase, without dot).
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1).toLowerCase().trim();
        }
        return "";
    }

    /**
     * Determines user-friendly file category based on extension.
     */
    public static String determineFileType(String extension) {
        if (extension == null || extension.isEmpty()) {
            return "Generic File";
        }
        String ext = extension.toLowerCase().trim();
        switch (ext) {
            case "txt": return "Text Document";
            case "md": return "Markdown Document";
            case "pdf": return "PDF Document";
            case "doc":
            case "docx": return "Word Document";
            case "xls":
            case "xlsx": return "Excel Spreadsheet";
            case "ppt":
            case "pptx": return "PowerPoint Presentation";
            case "csv": return "CSV Data File";
            case "json": return "JSON Data";
            case "xml": return "XML Document";
            case "html":
            case "htm": return "HTML Web Page";
            case "css": return "CSS Stylesheet";
            case "js": return "JavaScript File";
            case "java": return "Java Source Code";
            case "c":
            case "cpp": return "C/C++ Source Code";
            case "py": return "Python Script";
            case "sql": return "SQL Script";
            case "png": return "PNG Image";
            case "jpg":
            case "jpeg": return "JPEG Image";
            case "gif": return "GIF Image";
            case "svg": return "Vector Graphic (SVG)";
            case "zip":
            case "rar":
            case "7z":
            case "tar":
            case "gz": return "Compressed Archive";
            case "mp3":
            case "wav": return "Audio File";
            case "mp4":
            case "mkv": return "Video File";
            default: return ext.toUpperCase() + " File";
        }
    }

    /**
     * Formats a Timestamp object into a readable date string.
     */
    public static synchronized String formatDate(java.sql.Timestamp timestamp) {
        if (timestamp == null) return "-";
        return DATE_FORMAT.format(timestamp);
    }

    /**
     * Checks if a file is plain-text editable/viewable based on extension.
     */
    public static boolean isTextExtension(String extension) {
        if (extension == null) return false;
        return TEXT_EXTENSIONS.contains(extension.toLowerCase().trim());
    }
}

