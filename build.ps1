# ============================================================
# Virtual File Management System - Build Script (PowerShell)
# ============================================================

$ErrorActionPreference = "Stop"

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  Virtual File Management System - Compiling Project" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

# Locate javac
$javac = $null
if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\javac.exe")) {
    $javac = "$env:JAVA_HOME\bin\javac.exe"
} elseif (Get-Command javac -ErrorAction SilentlyContinue) {
    $javac = (Get-Command javac).Source
} elseif (Test-Path "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.2\jbr\bin\javac.exe") {
    $javac = "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.2\jbr\bin\javac.exe"
}

if (-not $javac) {
    Write-Host "[ERROR] Could not find javac.exe. Please install JDK or set JAVA_HOME." -ForegroundColor Red
    exit 1
}

Write-Host "[INFO] Using compiler: $javac" -ForegroundColor Green
& $javac -version

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" -Force | Out-Null
}

$javaFiles = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName

if ($javaFiles.Count -eq 0) {
    Write-Host "[ERROR] No Java source files found in src/" -ForegroundColor Red
    exit 1
}

Write-Host "[INFO] Compiling $($javaFiles.Count) Java source files..." -ForegroundColor Yellow

$sourcesFile = "sources.tmp"
[System.IO.File]::WriteAllLines((Join-Path (Get-Location) $sourcesFile), $javaFiles)

$classpath = "lib/mysql-connector-j-8.4.0.jar;lib/flatlaf-3.5.4.jar;lib/*"
& $javac -encoding UTF-8 -cp $classpath -d bin "@$sourcesFile"
if ($LASTEXITCODE -ne 0) {
    if (Test-Path $sourcesFile) { Remove-Item $sourcesFile -Force }
    Write-Host "[ERROR] Compilation failed with exit code $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}

if (Test-Path $sourcesFile) {
    Remove-Item $sourcesFile -Force
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "  [SUCCESS] Project successfully compiled into bin/" -ForegroundColor Green
Write-Host "  Execute 'run.bat' or './run.ps1' to launch the application." -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
