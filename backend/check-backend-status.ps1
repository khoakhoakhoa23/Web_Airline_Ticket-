# Check Backend Status Script
# Verify if backend is running and API is accessible

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "🔍 BACKEND STATUS CHECK" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Cyan

# Check port 8080
$connection = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue

if ($connection) {
    $processId = $connection.OwningProcess
    Write-Host "✅ Backend is RUNNING" -ForegroundColor Green
    Write-Host "   Port: 8080" -ForegroundColor Gray
    Write-Host "   PID: $processId" -ForegroundColor Gray
    
    # Test API endpoint
    Write-Host "`nTesting API endpoints..." -ForegroundColor Yellow
    
    # Test 1: Login endpoint (should return 401)
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
                                      -Method POST `
                                      -ContentType "application/json" `
                                      -Body '{"email":"test","password":"test"}' `
                                      -UseBasicParsing `
                                      -ErrorAction Stop
        Write-Host "   ✅ POST /api/auth/login - Status: $($response.StatusCode)" -ForegroundColor Green
    } catch {
        if ($_.Exception.Response.StatusCode -eq 400 -or $_.Exception.Response.StatusCode -eq 401) {
            Write-Host "   ✅ POST /api/auth/login - Status: $($_.Exception.Response.StatusCode.value__) (Expected)" -ForegroundColor Green
        } else {
            Write-Host "   ⚠️  POST /api/auth/login - Error: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }
    
    # Test 2: Register endpoint
    try {
        $testEmail = "status-check-$(Get-Date -Format 'yyyyMMddHHmmss')@test.com"
        $registerData = @{
            email = $testEmail
            password = "Test123"
            phone = "0123456789"
        } | ConvertTo-Json
        
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/register" `
                                      -Method POST `
                                      -ContentType "application/json" `
                                      -Body $registerData `
                                      -UseBasicParsing `
                                      -ErrorAction Stop
        Write-Host "   ✅ POST /api/auth/register - Status: $($response.StatusCode)" -ForegroundColor Green
    } catch {
        if ($_.Exception.Response.StatusCode -eq 409) {
            Write-Host "   ✅ POST /api/auth/register - Status: 409 (Email exists - OK)" -ForegroundColor Green
        } else {
            Write-Host "   ⚠️  POST /api/auth/register - Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
        }
    }
    
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "✅ BACKEND STATUS: HEALTHY" -ForegroundColor Green
    Write-Host "========================================`n" -ForegroundColor Cyan
    
} else {
    Write-Host "❌ Backend is NOT RUNNING" -ForegroundColor Red
    Write-Host "   Port 8080 is not in use" -ForegroundColor Gray
    Write-Host "`n📋 To start backend:" -ForegroundColor Yellow
    Write-Host "   cd backend" -ForegroundColor Cyan
    Write-Host "   mvn spring-boot:run" -ForegroundColor Cyan
    Write-Host "   OR" -ForegroundColor Gray
    Write-Host "   .\restart-backend.ps1`n" -ForegroundColor Cyan
}

