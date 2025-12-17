# Restart Backend Script
# Stop old process on port 8080 and start fresh backend

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "🔄 RESTART BACKEND" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Cyan

# Step 1: Stop old process
Write-Host "1. Stopping old backend process..." -ForegroundColor Yellow
$process = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | 
           Select-Object -ExpandProperty OwningProcess

if ($process) {
    Stop-Process -Id $process -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
    Write-Host "   ✅ Stopped old process (PID: $process)" -ForegroundColor Green
} else {
    Write-Host "   ℹ️  No process found on port 8080" -ForegroundColor Gray
}

# Step 2: Start new backend
Write-Host "`n2. Starting new backend..." -ForegroundColor Yellow
Write-Host "   Command: mvn spring-boot:run" -ForegroundColor Gray
Write-Host "   Port: 8080" -ForegroundColor Gray
Write-Host "   Logs: Check console output below`n" -ForegroundColor Gray

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🚀 BACKEND STARTING..." -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

# Start backend (will show logs in console)
mvn spring-boot:run

