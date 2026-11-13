-- ============================================================
-- Virtual File Management System (VFMS) Database Schema
-- Compatible with MySQL 5.7+ and MySQL 8.x
-- ============================================================

CREATE DATABASE IF NOT EXISTS virtual_file_system
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE virtual_file_system;

-- ------------------------------------------------------------
-- 1. Folders Table
-- Represents virtual directory hierarchy (self-referencing parent_id)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS folders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key to self for nested subfolder hierarchy
    CONSTRAINT fk_folder_parent 
        FOREIGN KEY (parent_id) 
        REFERENCES folders(id) 
        ON DELETE CASCADE,
        
    -- Prevent duplicate folder names under the same parent directory
    CONSTRAINT uq_folder_parent_name 
        UNIQUE (parent_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Index for fast retrieval of subfolders
CREATE INDEX idx_folders_parent_id ON folders(parent_id);

-- ------------------------------------------------------------
-- 2. Files Table
-- Represents virtual files contained inside folders (or root if folder_id is NULL)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS files (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    extension VARCHAR(50) NOT NULL,
    file_type VARCHAR(100) DEFAULT 'Generic File',
    file_size BIGINT DEFAULT 0,
    folder_id INT NULL,
    content LONGBLOB NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key to folders table. If folder is deleted, its files are cascade-deleted.
    CONSTRAINT fk_file_folder 
        FOREIGN KEY (folder_id) 
        REFERENCES folders(id) 
        ON DELETE CASCADE,
        
    -- Prevent duplicate file names inside the same folder
    CONSTRAINT uq_file_folder_name 
        UNIQUE (folder_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indexes for fast folder lookups and searching
CREATE INDEX idx_files_folder_id ON files(folder_id);
CREATE INDEX idx_files_name ON files(name);
CREATE INDEX idx_files_extension ON files(extension);

