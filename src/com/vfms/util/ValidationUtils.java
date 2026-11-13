package com.vfms.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validates file and folder names according to operating system and filesystem safety standards.
 */
public class ValidationUtils {

    // Characters forbidden in standard file systems: \ / : * ? " < > |
    private static final String ILLEGAL_CHARACTERS = "\\/:*?\"<>|";

    // Windows reserved filenames
    private static final Set<String> RESERVED_NAMES = new HashSet<>(Arrays.asList(
        "CON", "PRN", "AUX", "NUL",
        "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
        "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
    ));

    /**
     * Validates a folder or file name.
     * Returns null if valid, or a descriptive error message if invalid.
     */
    public static String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Name cannot be empty or contain only whitespace.";
        }
        
        String trimmed = name.trim();
        if (trimmed.length() > 255) {
            return "Name cannot exceed 255 characters.";
        }

        for (char c : ILLEGAL_CHARACTERS.toCharArray()) {
            if (trimmed.indexOf(c) >= 0) {
                return "Name cannot contain special characters: \\ / : * ? \" < > |";
            }
        }

        if (trimmed.equals(".") || trimmed.equals("..")) {
            return "Name cannot be '.' or '..'.";
        }

        // Check reserved names (case-insensitive)
        String baseName = trimmed;
        int dot = baseName.indexOf('.');
        if (dot > 0) {
            baseName = baseName.substring(0, dot);
        }
        if (RESERVED_NAMES.contains(baseName.toUpperCase())) {
            return "'" + baseName + "' is a system reserved keyword and cannot be used.";
        }

        return null; // Valid
    }

    /**
     * Replaces illegal characters with underscores.
     */
    public static String sanitizeName(String name) {
        if (name == null) return "untitled";
        String clean = name.trim();
        for (char c : ILLEGAL_CHARACTERS.toCharArray()) {
            clean = clean.replace(c, '_');
        }
        return clean.isEmpty() ? "untitled" : clean;
    }
}

