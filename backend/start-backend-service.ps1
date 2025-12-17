# Script để chạy Backend như một Windows Service (Background)
# Usage: .\start-backend-service.ps1

$ErrorActionPreference = "Continue"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Backend Service (Background Mode)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Kiểm tra quyền admin
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host "⚠ Running without admin privileges" -ForegroundColor Yellow
    Write-Host "Some features may not work properly" -ForegroundColor Yellow
    Write-Host ""
}

# Đường dẫn script keep-alive
$keepAliveScript = Join-Path $PSScriptRoot "keep-backend-running.ps1"

if (-not (Test-Path $keepAliveScript)) {
    Write-Host "ERROR: keep-backend-running.ps1 not found!" -ForegroundColor Red
    exit 1
}

# Tạo scheduled task để chạy backend tự động
$taskName = "FlightBookingBackend"
$taskDescription = "Flight Booking Backend - Auto Start on Boot"

Write-Host "Creating Windows Scheduled Task..." -ForegroundColor Cyan

# Xóa task cũ nếu có
$existingTask = Get-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue
if ($existingTask) {
    Write-Host "Removing existing task..." -ForegroundColor Yellow
    Unregister-ScheduledTask -TaskName $taskName -Confirm:$false -ErrorAction SilentlyContinue
}

# Tạo action
$action = New-ScheduledTaskAction -Execute "powershell.exe" `
    -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$keepAliveScript`""

# Tạo trigger: Start on boot + on logon
$trigger1 = New-ScheduledTaskTrigger -AtStartup
$trigger2 = New-ScheduledTaskTrigger -AtLogOn

# Settings: Run as current user, allow start on demand, restart on failure
$principal = New-ScheduledTaskPrincipal -UserId "$env:USERDOMAIN\$env:USERNAME" -LogonType Interactive -RunLevel Highest
$settings = New-ScheduledTaskSettingsSet `
    -AllowStartIfOnBatteries `
    -DontStopIfGoingOnBatteries `
    -StartWhenAvailable `
    -RestartCount 3 `
    -RestartInterval (New-TimeSpan -Minutes 1)

try {
    Register-ScheduledTask `
        -TaskName $taskName `
        -Action $action `
        -Trigger @($trigger1, $trigger2) `
        -Principal $principal `
        -Settings $settings `
        -Description $taskDescription | Out-Null
    
    Write-Host "✓ Scheduled Task created successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Task Name: $taskName" -ForegroundColor Cyan
    Write-Host "Description: $taskDescription" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Options:" -ForegroundColor Yellow
    Write-Host "1. Start now: Start-ScheduledTask -TaskName '$taskName'" -ForegroundColor White
    Write-Host "2. Stop: Stop-ScheduledTask -TaskName '$taskName'" -ForegroundColor White
    Write-Host "3. Remove: Unregister-ScheduledTask -TaskName '$taskName' -Confirm:`$false" -ForegroundColor White
    Write-Host "4. View status: Get-ScheduledTask -TaskName '$taskName'" -ForegroundColor White
    Write-Host ""
    
    # Hỏi có muốn start ngay không
    $response = Read-Host "Start backend service now? (Y/N)"
    if ($response -eq "Y" -or $response -eq "y") {
        Write-Host "Starting backend service..." -ForegroundColor Cyan
        Start-ScheduledTask -TaskName $taskName
        Write-Host "✓ Backend service started!" -ForegroundColor Green
        Write-Host "Check logs: backend-run.log" -ForegroundColor Gray
    }
    
} catch {
    Write-Host "✗ Failed to create scheduled task: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Alternative: Run keep-backend-running.ps1 manually" -ForegroundColor Yellow
    exit 1
}

