# ============================================================
# Virtual File Management System - Launcher Script (PowerShell)
# ============================================================

$ErrorActionPreference = "Stop"

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  Virtual File Management System - Launching Application" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

# Locate java
$java = $null
if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $java = "$env:JAVA_HOME\bin\java.exe"
} elseif (Test-Path "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.2\jbr\bin\java.exe") {
    $java = "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.2\jbr\bin\java.exe"
} elseif (Get-Command java -ErrorAction SilentlyContinue) {
    $java = (Get-Command java).Source
}

if (-not $java) {
    Write-Host "[ERROR] Could not find java.exe. Please install Java runtime." -ForegroundColor Red
    exit 1
}

if (-not (Test-Path "bin\com\vfms\Main.class")) {
    Write-Host "[WARN] Project is not compiled yet. Running build.ps1 first..." -ForegroundColor Yellow
    & ".\build.ps1"
}

Write-Host "[INFO] Starting Java Swing GUI..." -ForegroundColor Green
$classpath = "bin;lib/*;resources"
& $java -cp $classpath com.vfms.Main

