# Test Transaction Fix
# This script verifies the transaction fix is working

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST: Transaction Fix Verification" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "📋 CHECKLIST:" -ForegroundColor Yellow
Write-Host ""

# Check 1: application.properties
Write-Host "1. Checking application.properties..." -ForegroundColor White
$appProps = Get-Content "src\main\resources\application.properties" -Raw
if ($appProps -match "spring\.datasource\.hikari\.auto-commit\s*=\s*false") {
    Write-Host "   ✅ hikari.auto-commit=false found" -ForegroundColor Green
} else {
    Write-Host "   ❌ hikari.auto-commit=false NOT found" -ForegroundColor Red
    Write-Host "   Please add: spring.datasource.hikari.auto-commit=false" -ForegroundColor Yellow
}

# Check 2: Scheduler
Write-Host "`n2. Checking HoldBookingScheduler.java..." -ForegroundColor White
$scheduler = Get-Content "src\main\java\com\flightbooking\scheduler\HoldBookingScheduler.java" -Raw
if ($scheduler -match "@Transactional" -and $scheduler -match "@Scheduled") {
    Write-Host "   ❌ Scheduler still has @Transactional on scheduled method" -ForegroundColor Red
    Write-Host "   Fix: Remove @Transactional from scheduler, move to service" -ForegroundColor Yellow
} else {
    Write-Host "   ✅ Scheduler does NOT have @Transactional" -ForegroundColor Green
}

if ($scheduler -match "BookingExpirationService") {
    Write-Host "   ✅ Scheduler delegates to service" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  Scheduler does not use BookingExpirationService" -ForegroundColor Yellow
}

# Check 3: Service exists
Write-Host "`n3. Checking BookingExpirationService.java..." -ForegroundColor White
if (Test-Path "src\main\java\com\flightbooking\service\BookingExpirationService.java") {
    Write-Host "   ✅ BookingExpirationService.java exists" -ForegroundColor Green
    
    $service = Get-Content "src\main\java\com\flightbooking\service\BookingExpirationService.java" -Raw
    if ($service -match "@Transactional") {
        Write-Host "   ✅ Service has @Transactional" -ForegroundColor Green
    } else {
        Write-Host "   ❌ Service missing @Transactional" -ForegroundColor Red
    }
} else {
    Write-Host "   ❌ BookingExpirationService.java NOT found" -ForegroundColor Red
}

# Check 4: Repository
Write-Host "`n4. Checking BookingRepository.java..." -ForegroundColor White
$repo = Get-Content "src\main\java\com\flightbooking\repository\BookingRepository.java" -Raw
if ($repo -match "findByStatusInAndHoldExpiresAtBefore") {
    Write-Host "   ✅ Custom query method exists" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  Custom query method not found" -ForegroundColor Yellow
    Write-Host "   Add: List<Booking> findByStatusInAndHoldExpiresAtBefore(...)" -ForegroundColor Yellow
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "📖 NEXT STEPS" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "1. Rebuild project:" -ForegroundColor White
Write-Host "   mvn clean install`n" -ForegroundColor Cyan

Write-Host "2. Start backend:" -ForegroundColor White
Write-Host "   mvn spring-boot:run`n" -ForegroundColor Cyan

Write-Host "3. Monitor logs for:" -ForegroundColor White
Write-Host "   - NO PSQLException" -ForegroundColor Gray
Write-Host "   - 'Starting scheduled task: expire hold bookings'" -ForegroundColor Gray
Write-Host "   - 'Successfully expired X bookings'`n" -ForegroundColor Gray

Write-Host "4. Create test data (optional):" -ForegroundColor White
Write-Host "   See: backend/database/test-booking-expiration.sql`n" -ForegroundColor Cyan

Write-Host "📚 Documentation:" -ForegroundColor Yellow
Write-Host "   - Detailed guide: backend/TRANSACTION_FIX_GUIDE.md" -ForegroundColor Cyan
Write-Host "   - Quick reference: backend/QUICK_FIX_TRANSACTION_ERROR.md`n" -ForegroundColor Cyan

Write-Host "✅ Verification script completed!`n" -ForegroundColor Green

