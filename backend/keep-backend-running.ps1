# Script để đảm bảo Backend luôn chạy - Auto Restart khi crash
# Usage: .\keep-backend-running.ps1

$ErrorActionPreference = "Continue"
$script:restartCount = 0
$script:maxRestarts = 100  # Giới hạn số lần restart (tránh loop vô hạn)
$script:restartDelay = 5   # Đợi 5 giây trước khi restart
$script:logFile = "backend-run.log"
$script:pidFile = "backend.pid"

# Function để log
function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] [$Level] $Message"
    Write-Host $logMessage
    Add-Content -Path $script:logFile -Value $logMessage
}

# Function để kiểm tra backend có đang chạy không
function Test-BackendRunning {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/users" -Method Get -TimeoutSec 2 -ErrorAction SilentlyContinue
        return $response.StatusCode -eq 200
    } catch {
        return $false
    }
}

# Function để kill process cũ nếu có
function Stop-OldBackend {
    if (Test-Path $script:pidFile) {
        $oldPid = Get-Content $script:pidFile
        try {
            $process = Get-Process -Id $oldPid -ErrorAction SilentlyContinue
            if ($process) {
                Write-Log "Stopping old backend process (PID: $oldPid)" "WARN"
                Stop-Process -Id $oldPid -Force -ErrorAction SilentlyContinue
            }
        } catch {
            # Process không tồn tại, bỏ qua
        }
        Remove-Item $script:pidFile -ErrorAction SilentlyContinue
    }
}

# Function để kiểm tra dependencies
function Test-Dependencies {
    Write-Log "Checking dependencies..." "INFO"
    
    # Kiểm tra Java
    try {
        $javaVersion = java -version 2>&1 | Select-Object -First 1
        Write-Log "✓ Java found: $javaVersion" "INFO"
    } catch {
        Write-Log "✗ Java not found! Please install JDK 17 or higher." "ERROR"
        return $false
    }
    
    # Kiểm tra PostgreSQL
    try {
        $pgService = Get-Service -Name "postgresql*" -ErrorAction SilentlyContinue | Where-Object { $_.Status -eq "Running" }
        if ($pgService) {
            Write-Log "✓ PostgreSQL is running" "INFO"
        } else {
            Write-Log "⚠ PostgreSQL service not found or not running" "WARN"
            Write-Log "Please ensure PostgreSQL is running and database 'flight_booking' exists" "WARN"
        }
    } catch {
        Write-Log "⚠ Could not check PostgreSQL status" "WARN"
    }
    
    # Kiểm tra maven-wrapper.jar
    $wrapperJar = ".\.mvn\wrapper\maven-wrapper.jar"
    if (-not (Test-Path $wrapperJar)) {
        Write-Log "Downloading maven-wrapper.jar..." "INFO"
        $wrapperDir = ".\.mvn\wrapper"
        if (-not (Test-Path $wrapperDir)) {
            New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null
        }
        try {
            Invoke-WebRequest -Uri "https://repo.maven.apache.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar" -OutFile $wrapperJar
            Write-Log "✓ Maven Wrapper downloaded" "INFO"
        } catch {
            Write-Log "✗ Failed to download maven-wrapper.jar" "ERROR"
            return $false
        }
    }
    
    return $true
}

# Function để start backend
function Start-Backend {
    Write-Log "========================================" "INFO"
    Write-Log "Starting Flight Booking Backend..." "INFO"
    Write-Log "Restart count: $script:restartCount" "INFO"
    Write-Log "========================================" "INFO"
    
    # Set JAVA_HOME nếu chưa có
    if (-not $env:JAVA_HOME) {
        $javaPaths = @(
            "C:\Program Files\Java\jdk-17",
            "C:\Program Files\Java\jdk-21",
            "C:\Program Files\Eclipse Adoptium\jdk-17*",
            "$env:ProgramFiles\Java\jdk-17"
        )
        
        foreach ($path in $javaPaths) {
            $resolvedPath = Resolve-Path $path -ErrorAction SilentlyContinue
            if ($resolvedPath -and (Test-Path $resolvedPath)) {
                $env:JAVA_HOME = $resolvedPath
                Write-Log "Set JAVA_HOME to: $env:JAVA_HOME" "INFO"
                break
            }
        }
    }
    
    # Start backend process
    $processInfo = New-Object System.Diagnostics.ProcessStartInfo
    $processInfo.FileName = ".\mvnw.cmd"
    $processInfo.Arguments = "spring-boot:run"
    $processInfo.WorkingDirectory = $PSScriptRoot
    $processInfo.UseShellExecute = $false
    $processInfo.RedirectStandardOutput = $true
    $processInfo.RedirectStandardError = $true
    $processInfo.CreateNoWindow = $false
    
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $processInfo
    
    # Event handlers để log output
    $outputBuilder = New-Object System.Text.StringBuilder
    $errorBuilder = New-Object System.Text.StringBuilder
    
    $process.add_OutputDataReceived({
        param($sender, $e)
        if ($e.Data) {
            $null = $outputBuilder.AppendLine($e.Data)
            Write-Log $e.Data "OUTPUT"
        }
    })
    
    $process.add_ErrorDataReceived({
        param($sender, $e)
        if ($e.Data) {
            $null = $errorBuilder.AppendLine($e.Data)
            Write-Log $e.Data "ERROR"
        }
    })
    
    try {
        $process.Start() | Out-Null
        $process.BeginOutputReadLine()
        $process.BeginErrorReadLine()
        
        # Lưu PID
        $process.Id | Out-File $script:pidFile
        Write-Log "Backend started with PID: $($process.Id)" "INFO"
        Write-Log "Backend URL: http://localhost:8080" "INFO"
        Write-Log "API Base URL: http://localhost:8080/api" "INFO"
        
        return $process
    } catch {
        Write-Log "Failed to start backend: $_" "ERROR"
        return $null
    }
}

# Function để đợi backend sẵn sàng
function Wait-BackendReady {
    param([int]$MaxWaitSeconds = 60)
    
    Write-Log "Waiting for backend to be ready..." "INFO"
    $elapsed = 0
    $checkInterval = 2
    
    while ($elapsed -lt $MaxWaitSeconds) {
        Start-Sleep -Seconds $checkInterval
        $elapsed += $checkInterval
        
        if (Test-BackendRunning) {
            Write-Log "✓ Backend is ready and responding!" "INFO"
            return $true
        }
        
        if ($elapsed % 10 -eq 0) {
            Write-Log "Still waiting... ($elapsed/$MaxWaitSeconds seconds)" "INFO"
        }
    }
    
    Write-Log "⚠ Backend did not become ready within $MaxWaitSeconds seconds" "WARN"
    return $false
}

# Main execution
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Backend Keep-Alive Service" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "This script will keep your backend running" -ForegroundColor Yellow
Write-Host "and automatically restart it if it crashes." -ForegroundColor Yellow
Write-Host ""
Write-Host "Press Ctrl+C to stop" -ForegroundColor Yellow
Write-Host "Log file: $script:logFile" -ForegroundColor Gray
Write-Host ""

# Cleanup old processes
Stop-OldBackend

# Check dependencies
if (-not (Test-Dependencies)) {
    Write-Log "Dependency check failed. Exiting." "ERROR"
    exit 1
}

# Main loop
while ($true) {
    try {
        # Check restart limit
        if ($script:restartCount -ge $script:maxRestarts) {
            Write-Log "Maximum restart limit reached ($script:maxRestarts). Exiting." "ERROR"
            exit 1
        }
        
        # Start backend
        $backendProcess = Start-Backend
        
        if (-not $backendProcess) {
            Write-Log "Failed to start backend. Retrying in $script:restartDelay seconds..." "ERROR"
            Start-Sleep -Seconds $script:restartDelay
            $script:restartCount++
            continue
        }
        
        # Wait for backend to be ready
        $ready = Wait-BackendReady -MaxWaitSeconds 90
        
        if ($ready) {
            $script:restartCount = 0  # Reset counter on successful start
        }
        
        # Monitor process
        Write-Log "Monitoring backend process..." "INFO"
        
        while (-not $backendProcess.HasExited) {
            Start-Sleep -Seconds 5
            
            # Health check every 30 seconds
            if ((Get-Date).Second % 30 -eq 0) {
                if (-not (Test-BackendRunning)) {
                    Write-Log "⚠ Health check failed - backend not responding" "WARN"
                }
            }
        }
        
        # Process exited
        $exitCode = $backendProcess.ExitCode
        Write-Log "Backend process exited with code: $exitCode" "WARN"
        
        # Cleanup
        if (Test-Path $script:pidFile) {
            Remove-Item $script:pidFile -ErrorAction SilentlyContinue
        }
        
        # Restart
        $script:restartCount++
        Write-Log "Restarting backend in $script:restartDelay seconds... (Restart #$script:restartCount)" "INFO"
        Start-Sleep -Seconds $script:restartDelay
        
    } catch {
        Write-Log "Unexpected error: $_" "ERROR"
        Write-Log "Stack trace: $($_.ScriptStackTrace)" "ERROR"
        
        # Cleanup
        Stop-OldBackend
        
        $script:restartCount++
        Write-Log "Restarting in $script:restartDelay seconds... (Restart #$script:restartCount)" "INFO"
        Start-Sleep -Seconds $script:restartDelay
    }
}

