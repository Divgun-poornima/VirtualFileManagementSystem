# Virtual File Management System (VFMS)

> **College NSP Minor Project**  
> **Tech Stack:** Java, Java Swing, MySQL, JDBC, VS Code

---

## 📌 Project Overview
The **Virtual File Management System (VFMS)** is a desktop application designed to simulate and manage an operating system's hierarchical file system virtually. Files and directories are organized in a nested tree structure with metadata and content stored securely in a **MySQL** relational database using **Java Database Connectivity (JDBC)**.

### Key Highlights
- **Clean, Modern UI:** Sleek dashboard with a sidebar, toolbar, interactive breadcrumbs, and a detailed file/folder table.
- **Hierarchical Storage:** Infinite nested folders via self-referencing foreign key schema (`parent_id -> folders.id`).
- **Complete CRUD Operations:** Create, View, Rename, Search, and Delete files and folders.
- **Physical Import / Export:** Upload existing files from your computer into the virtual filesystem and export virtual files back to disk.
- **Metadata Management:** Automatic tracking of name, extension, MIME type, file size (formatted to B/KB/MB), creation timestamp, modification timestamp, and directory path.
- **Validation & Safety:** Input sanitization (rejection of illegal characters `\ / : * ? " < > |` and Windows reserved names like `CON, PRN, AUX, NUL`), name collision detection within the same directory, and confirmation dialogs before permanent deletion.
- **Dual-Mode Resiliency:** If MySQL is not running or credentials are not yet configured, the system automatically runs in **Offline Demo Mode (In-Memory)** with sample files and folders. You can connect to MySQL anytime via the in-app **Database Settings** dialog without restarting!

---

## 📁 Project Directory Structure

```
VirtualFileManagementSystem/
├── .vscode/
│   ├── settings.json              # VS Code Java classpath & JDK configuration
│   └── launch.json                # F5 Debug & Run configuration
├── bin/                           # Compiled .class files (generated)
├── lib/                           # Pre-packaged JAR dependencies
│   ├── mysql-connector-j-8.4.0.jar# Official MySQL JDBC Driver
│   └── flatlaf-3.5.4.jar          # Modern Flat Look and Feel for Swing
├── resources/
│   ├── db.properties              # Active database credentials
│   └── db.properties.example      # Example configuration template
├── sql/
│   ├── schema.sql                 # MySQL DDL (Database, Tables, Indexes, Foreign Keys)
│   └── sample_data.sql            # Sample starter folders and files
├── src/
│   └── com/
│       └── vfms/
│           ├── Main.java          # Application entry point
│           ├── config/
│           │   └── DatabaseConfig.java       # Thread-safe JDBC manager (no hardcoded passwords)
│           ├── dao/
│           │   ├── FolderDao.java            # Folder CRUD operations with PreparedStatement
│           │   └── FileDao.java              # File CRUD, search, and storage statistics
│           ├── model/
│           │   ├── VirtualFolder.java        # Folder model entity
│           │   ├── VirtualFile.java          # File entity with content & metadata
│           │   └── StorageStats.java         # Global storage metrics
│           ├── service/
│           │   └── FileSystemService.java    # Business logic, validation, failover
│           ├── test/
│           │   ├── SystemTest.java           # Automated backend test suite
│           │   └── GuiSmokeTest.java         # Automated GUI smoke test
│           ├── ui/
│           │   ├── MainFrame.java            # Main application window & controller
│           │   ├── SidebarPanel.java         # Left navigation & storage usage card
│           │   ├── TopToolbarPanel.java      # Toolbar buttons & live search
│           │   ├── BreadcrumbPanel.java      # Clickable path hierarchy
│           │   ├── FileTablePanel.java       # Custom JTable with type icons & context menu
│           │   ├── StatusBarPanel.java       # Item counts & status updates
│           │   ├── dialogs/
│           │   │   ├── CreateFolderDialog.java
│           │   │   ├── CreateFileDialog.java
│           │   │   ├── FilePreviewDialog.java
│           │   │   ├── FilePropertiesDialog.java
│           │   │   ├── RenameDialog.java
│           │   │   └── DatabaseConfigDialog.java
│           │   └── theme/
│           │       └── UITheme.java          # Modern color palette and typography
│           └── util/
│               ├── FileUtils.java            # Size formatting, MIME types, extensions
│               └── ValidationUtils.java      # Filesystem naming validation rules
├── build.bat                      # 1-Click compiler script for Windows
├── run.bat                        # 1-Click launcher script for Windows
├── build.ps1                      # PowerShell build script
├── run.ps1                        # PowerShell launcher script
└── README.md                      # Comprehensive documentation
```

---

## ⚙️ Prerequisites & Software Requirements

1. **Java Development Kit (JDK):** JDK 8 or higher (JDK 17, 21, or 25 recommended).
   - *Note:* The build scripts automatically detect standard JDKs as well as the installed JetBrains OpenJDK 25.
2. **MySQL Server:** Version 8.0+ or MySQL 5.7 (or MySQL Workbench / XAMPP MySQL).
3. **VS Code:** With the **"Extension Pack for Java"** installed.

---

## 🗄️ MySQL Database Setup Instructions

### Step 1: Open MySQL Workbench or MySQL CLI
Log in as your MySQL root user:
```bash
mysql -u root -p
```

### Step 2: Execute Schema & Sample Data
You can either open and execute the files in **MySQL Workbench**, or run them via terminal:

```bash
# 1. Create database and tables
mysql -u root -p < sql/schema.sql

# 2. (Optional) Load starter files and folders
mysql -u root -p < sql/sample_data.sql
```

### Step 3: Configure Database Credentials
Open `resources/db.properties` and enter your MySQL password:
```properties
db.host=localhost
db.port=3306
db.name=virtual_file_system
db.user=root
db.password=YOUR_MYSQL_PASSWORD_HERE
```
> 🔒 **Security Notice:** Passwords are never hardcoded in Java source files. They are stored in `resources/db.properties` (which is excluded via `.gitignore`). You can also configure them directly inside the GUI!

---

## 🚀 How to Run the Application

### Option A: Running from VS Code
1. Open this folder `VirtualFileManagementSystem` in VS Code.
2. Ensure you have the **Extension Pack for Java** installed.
3. Open `src/com/vfms/Main.java`.
4. Click **Run** or press **F5**.
   *(The `.vscode/launch.json` and `.vscode/settings.json` are already preconfigured!)*

### Option B: Running with 1-Click Batch Scripts (Terminal or File Explorer)
1. Double-click `run.bat` (or in terminal: `.\run.bat`).
2. If the project isn't compiled, it will automatically build it first and launch the GUI!

### Option C: Running with PowerShell
```powershell
# Compile
.\build.ps1

# Launch
.\run.ps1
```

---

## 🧪 Testing and Verification
The project includes automated verification tests that validate all components:
```powershell
# Run the core logic verification test:
java -cp "bin;lib/*;resources" com.vfms.test.SystemTest

# Run the GUI layout smoke test:
java -cp "bin;lib/*;resources" com.vfms.test.GuiSmokeTest
```
Both test suites verify:
- ✅ Filesystem naming rules and illegal character checks
- ✅ Human-readable file size formatting and MIME type detection
- ✅ Folder hierarchy creation, navigation, and breadcrumbs
- ✅ File creation, content retrieval, renaming, and deletion
- ✅ Real physical file export and import round-trip
- ✅ GUI theme initialization and frame packaging

---

## 💡 Key Technical Features for College Presentation

1. **MVC Architecture:**
   - **Model:** `VirtualFile`, `VirtualFolder`, `StorageStats`
   - **View:** `MainFrame`, `FileTablePanel`, `SidebarPanel`, `BreadcrumbPanel`, `TopToolbarPanel`, `StatusBarPanel`, and Dialogs
   - **Controller / Service:** `FileSystemService`, `FolderDao`, `FileDao`
2. **Relational Database Design:**
   - `folders.parent_id` references `folders.id` with `ON DELETE CASCADE`.
   - `files.folder_id` references `folders.id` with `ON DELETE CASCADE`.
   - Unique constraints prevent duplicate file/folder names inside the same directory level.
3. **Prepared Statements & SQL Injection Prevention:**
   - All SQL queries in `FileDao` and `FolderDao` use parameterized `PreparedStatement` to ensure security.
4. **BLOB / Text Content Storage:**
   - Virtual files store their simulated or imported content using MySQL `LONGBLOB`, allowing both plain text files and binary files (PDFs, images) to be stored and exported.
5. **Interactive UI Features:**
   - Double-click to navigate folders or preview text documents.
   - Right-click context menu for quick actions (Preview, Export, Rename, Delete, Properties).
   - Real-time search filter in toolbar.
   - Live storage usage meter in sidebar.

