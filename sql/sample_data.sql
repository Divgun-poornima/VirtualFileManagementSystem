-- ============================================================
-- Virtual File Management System - Starter / Demo Data
-- ============================================================

USE virtual_file_system;

-- Clear previous starter data if desired (optional)
-- DELETE FROM files;
-- DELETE FROM folders;

-- 1. Root Level Folders
INSERT INTO folders (id, name, parent_id) VALUES 
(1, 'Documents', NULL),
(2, 'College Projects', NULL),
(3, 'Pictures', NULL),
(4, 'Archives', NULL)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. Subfolders inside "College Projects"
INSERT INTO folders (id, name, parent_id) VALUES 
(5, 'NSP Minor Project', 2),
(6, 'Database Lab', 2)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 3. Subfolder inside "Documents"
INSERT INTO folders (id, name, parent_id) VALUES 
(7, 'Semester Notes', 1),
(8, 'Resume & Certificates', 1)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 4. Sample Files
INSERT INTO files (name, extension, file_type, file_size, folder_id, content) VALUES
('README.txt', 'txt', 'Text Document', 342, 5, 
 CONVERT('Virtual File Management System\nCollege NSP Minor Project\nTechnology Stack: Java, Swing, MySQL, JDBC\nDeveloped for academic submission.', USING utf8mb4)),

('Project_Synopsis.pdf', 'pdf', 'PDF Document', 1048576, 5, 
 CONVERT('Virtual File Management System Synopsis content placeholder.', USING utf8mb4)),

('Database_ER_Diagram.png', 'png', 'PNG Image', 524288, 5, 
 CONVERT('Virtual File Management System ER diagram preview bytes.', USING utf8mb4)),

('Lab_Experiment_01.sql', 'sql', 'SQL Script', 2048, 6, 
 CONVERT('CREATE TABLE students (\n  id INT PRIMARY KEY,\n  name VARCHAR(100)\n);', USING utf8mb4)),

('OS_Unit1_Notes.txt', 'txt', 'Text Document', 4512, 7, 
 CONVERT('Operating Systems - Unit 1: Process Management & Scheduling Algorithms.', USING utf8mb4)),

('divgun_resume.pdf', 'pdf', 'PDF Document', 1572864, 8, 
 CONVERT('Resume placeholder document.', USING utf8mb4)),

('welcome.txt', 'txt', 'Text Document', 186, NULL, 
 CONVERT('Welcome to Virtual File Management System!\nDouble-click a folder to enter.\nUse the top toolbar to create folders, files, or upload real files.', USING utf8mb4))
ON DUPLICATE KEY UPDATE name=VALUES(name);

