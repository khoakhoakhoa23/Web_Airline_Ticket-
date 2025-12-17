# Script PowerShell để chạy Backend
Write-Host "Starting Flight Booking Backend..." -ForegroundColor Green
Write-Host ""

# Kiểm tra Java
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "Java found: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Java not found! Please install JDK 17 or higher." -ForegroundColor Red
    Write-Host "Download from: https://adoptium.net/" -ForegroundColor Yellow
    exit 1
}

# Kiểm tra Maven
$mavenInstalled = $false
try {
    $mvnVersion = mvn --version 2>&1 | Select-Object -First 1
    if ($mvnVersion -match "Apache Maven") {
        Write-Host "Maven found: $mvnVersion" -ForegroundColor Green
        $mavenInstalled = $true
    }
} catch {
    Write-Host "Maven not found in PATH" -ForegroundColor Yellow
}

# Chọn cách chạy
if ($mavenInstalled) {
    Write-Host ""
    Write-Host "Using system Maven..." -ForegroundColor Cyan
    Write-Host "Make sure PostgreSQL is running and database 'flight_booking' exists!" -ForegroundColor Yellow
    Write-Host ""
    mvn spring-boot:run
} else {
    Write-Host ""
    Write-Host "Maven Wrapper will be used..." -ForegroundColor Cyan
    
    # Kiểm tra file maven-wrapper.jar
    $wrapperJar = ".\.mvn\wrapper\maven-wrapper.jar"
    if (-not (Test-Path $wrapperJar)) {
        Write-Host "Downloading Maven Wrapper..." -ForegroundColor Yellow
        $wrapperUrl = "https://repo.maven.apache.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar"
        $wrapperDir = ".\.mvn\wrapper"
        if (-not (Test-Path $wrapperDir)) {
            New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null
        }
        Invoke-WebRequest -Uri $wrapperUrl -OutFile $wrapperJar
        Write-Host "Maven Wrapper downloaded!" -ForegroundColor Green
    }
    
    Write-Host "Make sure PostgreSQL is running and database 'flight_booking' exists!" -ForegroundColor Yellow
    Write-Host ""
    .\mvnw.cmd spring-boot:run
}

