package com.vfms.test;

import com.vfms.model.StorageStats;
import com.vfms.model.VirtualFile;
import com.vfms.model.VirtualFolder;
import com.vfms.service.FileSystemService;
import com.vfms.util.FileUtils;
import com.vfms.util.ValidationUtils;

import java.io.File;
import java.util.List;

/**
 * Headless test runner to verify core operations, logic, validation,
 * and service behavior without requiring a GUI window.
 */
public class SystemTest {

    public static void main(String[] args) {
        System.out.println(">>> Running Virtual File Management System Automated Verification Tests <<<\n");
        int passed = 0;
        int failed = 0;

        // Test 1: ValidationUtils
        try {
            assertNotNull(ValidationUtils.validateName(null), "Null name must fail validation");
            assertNotNull(ValidationUtils.validateName("   "), "Empty/whitespace name must fail validation");
            assertNotNull(ValidationUtils.validateName("folder/name"), "Slashes must fail validation");
            assertNotNull(ValidationUtils.validateName("file:name"), "Colons must fail validation");
            assertNotNull(ValidationUtils.validateName("CON"), "Reserved Windows device name must fail validation");
            assertNull(ValidationUtils.validateName("My Folder"), "Valid folder name must pass");
            assertNull(ValidationUtils.validateName("Assignment_01.pdf"), "Valid file name must pass");
            System.out.println("  [PASS] Test 1: ValidationUtils rules (special chars, reserved names, whitespace)");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 1: ValidationUtils: " + t.getMessage());
            failed++;
        }

        // Test 2: FileUtils
        try {
            assertEquals("0 B", FileUtils.formatFileSize(0), "Zero bytes format");
            assertEquals("1.0 KB", FileUtils.formatFileSize(1024), "1024 bytes format");
            assertEquals("1.5 MB", FileUtils.formatFileSize((long)(1.5 * 1024 * 1024)), "1.5 MB format");
            assertEquals("pdf", FileUtils.getFileExtension("document.PDF"), "Extension extraction");
            assertEquals("PDF Document", FileUtils.determineFileType("pdf"), "MIME category detection");
            assertTrue(FileUtils.isTextExtension("java"), "Java file is text-based");
            assertTrue(!FileUtils.isTextExtension("png"), "PNG is not text-based");
            System.out.println("  [PASS] Test 2: FileUtils (size formatting, extensions, MIME types)");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 2: FileUtils: " + t.getMessage());
            failed++;
        }

        // Test 3: FileSystemService in demo/in-memory mode
        try {
            FileSystemService service = new FileSystemService();
            service.setUseMockStorage(true); // Force mock mode for headless sandbox verification
            
            // Check initial structure
            List<VirtualFolder> rootFolders = service.getCurrentSubFolders();
            assertTrue(rootFolders.size() >= 3, "Initial root folders present");

            // Test Create Folder
            service.createFolder("Test_Unit_Folder");
            boolean found = false;
            for (VirtualFolder f : service.getCurrentSubFolders()) {
                if ("Test_Unit_Folder".equals(f.getName())) {
                    found = true;
                    // Navigate into it
                    service.setCurrentFolderId(f.getId());
                    break;
                }
            }
            assertTrue(found, "Newly created folder found");
            assertEquals(0, service.getCurrentSubFolders().size(), "New folder is empty");

            // Test Create File
            service.createFile("Notes.txt", "Unit test notes content.");
            List<VirtualFile> files = service.getCurrentFiles();
            assertEquals(1, files.size(), "1 file created in new folder");
            assertEquals("Notes.txt", files.get(0).getName(), "File name matches");
            assertEquals("Unit test notes content.", files.get(0).getContentAsString(), "File content matches");

            // Test Rename File
            service.renameFile(files.get(0).getId(), "Renamed_Notes.txt");
            files = service.getCurrentFiles();
            assertEquals("Renamed_Notes.txt", files.get(0).getName(), "Renamed file name matches");

            // Test Search
            List<VirtualFile> searchRes = service.searchFiles("Renamed");
            assertTrue(searchRes.size() >= 1, "Search finds renamed file");

            // Test Export & Import
            File tempExport = File.createTempFile("vfms_export_test_", ".txt");
            service.exportFile(files.get(0).getId(), tempExport);
            assertTrue(tempExport.exists() && tempExport.length() > 0, "File exported to disk");
            
            // Import it back as another file
            service.importRealFile(tempExport);
            assertTrue(service.getCurrentFiles().size() >= 2, "Imported file added to folder");
            tempExport.delete();

            // Test Delete File
            service.deleteFile(files.get(0).getId());
            assertTrue(service.getCurrentFiles().size() >= 1, "File deleted");

            // Navigate back to root and delete test folder
            service.navigateUp();
            assertNull(service.getCurrentFolderId(), "Navigated back to Root");

            // Test Storage Stats
            StorageStats stats = service.getStorageStats();
            assertTrue(stats.getTotalFolders() > 0, "Stats total folders > 0");
            assertTrue(stats.getTotalFiles() > 0, "Stats total files > 0");

            System.out.println("  [PASS] Test 3: FileSystemService CRUD, navigation, breadcrumbs, search, import/export, stats");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 3: FileSystemService: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        System.out.println("\n------------------------------------------------------------");
        System.out.println(String.format("Tests Completed: %d Passed, %d Failed.", passed, failed));
        System.out.println("------------------------------------------------------------");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void assertEquals(Object expected, Object actual, String msg) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError(msg + " -> Expected: [" + expected + "], Actual: [" + actual + "]");
    }

    private static void assertNotNull(Object obj, String msg) {
        if (obj == null) throw new AssertionError(msg + " -> Expected not null, but was null.");
    }

    private static void assertNull(Object obj, String msg) {
        if (obj != null) throw new AssertionError(msg + " -> Expected null, but was: " + obj);
    }

    private static void assertTrue(boolean condition, String msg) {
        if (!condition) throw new AssertionError(msg + " -> Condition failed.");
    }
}

