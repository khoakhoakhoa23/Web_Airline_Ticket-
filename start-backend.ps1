# Script chạy Backend - Flight Booking System
# Chạy script này từ thư mục gốc: D:\TMDT\WebMayBay

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Starting Backend Server" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Vào thư mục backend
Set-Location -Path "$PSScriptRoot\backend"

# Set JAVA_HOME
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

# Kiểm tra Java
Write-Host "Checking Java..." -ForegroundColor Cyan
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "✓ Java: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Java not found!" -ForegroundColor Red
    Write-Host "Please set JAVA_HOME manually" -ForegroundColor Yellow
    exit 1
}

# Kiểm tra maven-wrapper.jar
if (-not (Test-Path ".\.mvn\wrapper\maven-wrapper.jar")) {
    Write-Host "Downloading maven-wrapper.jar..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path ".\.mvn\wrapper" -Force | Out-Null
    Invoke-WebRequest -Uri "https://repo.maven.apache.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar" -OutFile ".\.mvn\wrapper\maven-wrapper.jar"
}

Write-Host ""
Write-Host "Starting Spring Boot application..." -ForegroundColor Cyan
Write-Host "Backend will be available at: http://localhost:8080" -ForegroundColor Green
Write-Host "API Base URL: http://localhost:8080/api" -ForegroundColor Green
Write-Host ""
Write-Host "Press Ctrl+C to stop the server" -ForegroundColor Yellow
Write-Host ""

# Chạy backend
.\mvnw.cmd spring-boot:run

