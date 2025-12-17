# Script để chạy Backend - Flight Booking System
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Flight Booking Backend Startup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Set JAVA_HOME nếu chưa có
if (-not $env:JAVA_HOME) {
    $javaPath = "C:\Program Files\Java\jdk-17"
    if (Test-Path $javaPath) {
        $env:JAVA_HOME = $javaPath
        Write-Host "Set JAVA_HOME to: $javaPath" -ForegroundColor Yellow
    } else {
        Write-Host "ERROR: Java JDK not found at $javaPath" -ForegroundColor Red
        Write-Host "Please set JAVA_HOME manually or install JDK 17" -ForegroundColor Yellow
        exit 1
    }
}

# Kiểm tra Java
Write-Host "Checking Java..." -ForegroundColor Cyan
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "✓ Java: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Java not found!" -ForegroundColor Red
    exit 1
}

# Kiểm tra maven-wrapper.jar
Write-Host "Checking Maven Wrapper..." -ForegroundColor Cyan
$wrapperJar = ".\.mvn\wrapper\maven-wrapper.jar"
if (-not (Test-Path $wrapperJar)) {
    Write-Host "Downloading maven-wrapper.jar..." -ForegroundColor Yellow
    $wrapperDir = ".\.mvn\wrapper"
    if (-not (Test-Path $wrapperDir)) {
        New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null
    }
    try {
        Invoke-WebRequest -Uri "https://repo.maven.apache.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar" -OutFile $wrapperJar
        Write-Host "✓ Maven Wrapper downloaded" -ForegroundColor Green
    } catch {
        Write-Host "✗ Failed to download maven-wrapper.jar" -ForegroundColor Red
        Write-Host "Please check your internet connection" -ForegroundColor Yellow
        exit 1
    }
} else {
    Write-Host "✓ Maven Wrapper found" -ForegroundColor Green
}

# Kiểm tra PostgreSQL
Write-Host ""
Write-Host "IMPORTANT: Make sure PostgreSQL is running!" -ForegroundColor Yellow
Write-Host "Database: flight_booking" -ForegroundColor Yellow
Write-Host "Username: dbmaybay" -ForegroundColor Yellow
Write-Host "Password: 123456" -ForegroundColor Yellow
Write-Host ""

# Chạy backend
Write-Host "Starting Spring Boot application..." -ForegroundColor Cyan
Write-Host "Backend will be available at: http://localhost:8080" -ForegroundColor Green
Write-Host "API Base URL: http://localhost:8080/api" -ForegroundColor Green
Write-Host ""
Write-Host "Press Ctrl+C to stop the server" -ForegroundColor Yellow
Write-Host ""

.\mvnw.cmd spring-boot:run

