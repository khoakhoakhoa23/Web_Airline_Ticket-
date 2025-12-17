# Script để dừng Backend
# Usage: .\stop-backend.ps1

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Stopping Backend" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$pidFile = "backend.pid"
$stopped = $false

# Stop bằng PID file
if (Test-Path $pidFile) {
    $pid = Get-Content $pidFile
    Write-Host "Found PID file: $pid" -ForegroundColor Yellow
    
    try {
        $process = Get-Process -Id $pid -ErrorAction SilentlyContinue
        if ($process) {
            Write-Host "Stopping process (PID: $pid)..." -ForegroundColor Yellow
            Stop-Process -Id $pid -Force
            Write-Host "✓ Process stopped" -ForegroundColor Green
            $stopped = $true
        } else {
            Write-Host "Process not found (may have already exited)" -ForegroundColor Gray
        }
    } catch {
        Write-Host "Could not stop process: $_" -ForegroundColor Red
    }
    
    Remove-Item $pidFile -ErrorAction SilentlyContinue
}

# Stop bằng tên process (mvnw.cmd hoặc java)
Write-Host "Checking for running backend processes..." -ForegroundColor Yellow

$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object {
    $_.CommandLine -like "*FlightBookingApplication*" -or
    $_.CommandLine -like "*spring-boot*"
}

if ($javaProcesses) {
    foreach ($proc in $javaProcesses) {
        Write-Host "Stopping Java process (PID: $($proc.Id))..." -ForegroundColor Yellow
        Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
        $stopped = $true
    }
}

# Stop scheduled task nếu có
$taskName = "FlightBookingBackend"
$task = Get-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue
if ($task) {
    Write-Host "Stopping scheduled task: $taskName..." -ForegroundColor Yellow
    Stop-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue
    $stopped = $true
}

if ($stopped) {
    Write-Host ""
    Write-Host "✓ Backend stopped successfully" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "No running backend process found" -ForegroundColor Gray
}

Write-Host ""

