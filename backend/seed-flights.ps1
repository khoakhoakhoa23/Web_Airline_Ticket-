# Seed Flights Data
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "SEEDING FLIGHTS DATA" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Cyan

$env:PGPASSWORD = "123456"
$psqlPath = "C:\Program Files\PostgreSQL\16\bin\psql.exe"

# Check if psql exists
if (Test-Path $psqlPath) {
    Write-Host "✓ Found PostgreSQL at: $psqlPath" -ForegroundColor Green
    & $psqlPath -U dbmaybay -d flight_booking -f "database\seed-flights.sql"
} else {
    Write-Host "❌ PostgreSQL not found at default location" -ForegroundColor Red
    Write-Host "Trying alternative methods..." -ForegroundColor Yellow
    
    # Try default psql in PATH
    try {
        psql -U dbmaybay -d flight_booking -f "database\seed-flights.sql"
    } catch {
        Write-Host "`n⚠️  Cannot run psql command automatically." -ForegroundColor Yellow
        Write-Host "`nPlease run manually:" -ForegroundColor White
        Write-Host "1. Open pgAdmin 4 or PostgreSQL command line" -ForegroundColor Cyan
        Write-Host "2. Connect to database: flight_booking" -ForegroundColor Cyan
        Write-Host "3. Run the SQL file: D:\TMDT\WebMayBay\backend\database\seed-flights.sql" -ForegroundColor Cyan
        Write-Host "`nOr use this command in your terminal:" -ForegroundColor White
        Write-Host 'psql -U dbmaybay -d flight_booking -f "database\seed-flights.sql"' -ForegroundColor Cyan
    }
}

Write-Host "`n✅ Database seeding completed!" -ForegroundColor Green
Write-Host "You can verify by running:" -ForegroundColor White
Write-Host "SELECT COUNT(*) FROM flights;" -ForegroundColor Cyan

