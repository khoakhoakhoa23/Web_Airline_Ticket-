# Script tự động setup database PostgreSQL
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Database Setup - Flight Booking" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Kiểm tra PostgreSQL service
Write-Host "Checking PostgreSQL service..." -ForegroundColor Cyan
$pgService = Get-Service -Name postgresql* -ErrorAction SilentlyContinue

if (-not $pgService) {
    Write-Host "✗ PostgreSQL service not found!" -ForegroundColor Red
    Write-Host "Please install PostgreSQL first:" -ForegroundColor Yellow
    Write-Host "  https://www.postgresql.org/download/windows/" -ForegroundColor Yellow
    exit 1
}

$runningService = $pgService | Where-Object { $_.Status -eq 'Running' }
if (-not $runningService) {
    Write-Host "⚠ PostgreSQL service is not running" -ForegroundColor Yellow
    Write-Host "Attempting to start PostgreSQL..." -ForegroundColor Yellow
    try {
        Start-Service -Name $pgService[0].Name
        Start-Sleep -Seconds 3
        Write-Host "✓ PostgreSQL service started" -ForegroundColor Green
    } catch {
        Write-Host "✗ Failed to start PostgreSQL service" -ForegroundColor Red
        Write-Host "Please start PostgreSQL manually" -ForegroundColor Yellow
        exit 1
    }
} else {
    Write-Host "✓ PostgreSQL service is running" -ForegroundColor Green
}

# Tìm psql
Write-Host ""
Write-Host "Looking for psql..." -ForegroundColor Cyan
$psqlPath = $null

# Các đường dẫn thường gặp
$possiblePaths = @(
    "C:\Program Files\PostgreSQL\*\bin\psql.exe",
    "C:\Program Files (x86)\PostgreSQL\*\bin\psql.exe"
)

foreach ($path in $possiblePaths) {
    $found = Get-ChildItem -Path $path -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($found) {
        $psqlPath = $found.FullName
        break
    }
}

# Kiểm tra trong PATH
if (-not $psqlPath) {
    try {
        $psqlCheck = Get-Command psql -ErrorAction SilentlyContinue
        if ($psqlCheck) {
            $psqlPath = $psqlCheck.Source
        }
    } catch {
        # psql not in PATH
    }
}

if (-not $psqlPath) {
    Write-Host "✗ psql not found!" -ForegroundColor Red
    Write-Host "Please add PostgreSQL bin to PATH or specify psql path manually" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Manual setup:" -ForegroundColor Yellow
    Write-Host "1. Open psql: psql -U postgres" -ForegroundColor White
    Write-Host "2. Run SQL commands from database\setup.sql" -ForegroundColor White
    exit 1
}

Write-Host "✓ Found psql at: $psqlPath" -ForegroundColor Green

# Thông tin database
Write-Host ""
Write-Host "Database Configuration:" -ForegroundColor Cyan
Write-Host "  Database: flight_booking" -ForegroundColor White
Write-Host "  Username: DBMaybay" -ForegroundColor White
Write-Host "  Password: 123456" -ForegroundColor White
Write-Host "  Host: localhost" -ForegroundColor White
Write-Host "  Port: 5432" -ForegroundColor White
Write-Host ""

# Yêu cầu nhập password postgres
Write-Host "You will be prompted for PostgreSQL 'postgres' user password" -ForegroundColor Yellow
Write-Host ""

# Tạo database
Write-Host "Creating database..." -ForegroundColor Cyan
$createDbSQL = @"
CREATE DATABASE flight_booking;
"@

try {
    $env:PGPASSWORD = Read-Host "Enter postgres user password" -AsSecureString
    $env:PGPASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($env:PGPASSWORD))
    
    # Thử tạo database
    $createResult = & $psqlPath -U postgres -c $createDbSQL 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Database created (or already exists)" -ForegroundColor Green
    } else {
        if ($createResult -match "already exists") {
            Write-Host "✓ Database already exists" -ForegroundColor Green
        } else {
            Write-Host "⚠ Warning: $createResult" -ForegroundColor Yellow
        }
    }
} catch {
    Write-Host "⚠ Could not create database automatically" -ForegroundColor Yellow
    Write-Host "Please create manually using psql" -ForegroundColor Yellow
}

# Hướng dẫn tiếp theo
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Next Steps" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Connect to PostgreSQL:" -ForegroundColor Yellow
Write-Host "   psql -U postgres" -ForegroundColor White
Write-Host ""
Write-Host "2. Run these SQL commands:" -ForegroundColor Yellow
Write-Host "   CREATE DATABASE flight_booking;" -ForegroundColor White
Write-Host "   \c flight_booking" -ForegroundColor White
Write-Host "   CREATE USER DBMaybay WITH PASSWORD '123456';" -ForegroundColor White
Write-Host "   GRANT ALL PRIVILEGES ON DATABASE flight_booking TO DBMaybay;" -ForegroundColor White
Write-Host ""
Write-Host "3. Or use the SQL script:" -ForegroundColor Yellow
Write-Host "   psql -U postgres -f database\setup.sql" -ForegroundColor White
Write-Host ""
Write-Host "4. Test connection:" -ForegroundColor Yellow
Write-Host "   psql -U DBMaybay -d flight_booking" -ForegroundColor White
Write-Host "   Password: 123456" -ForegroundColor White
Write-Host ""
Write-Host "5. Start backend - it will auto-create tables:" -ForegroundColor Yellow
Write-Host "   .\mvnw.cmd spring-boot:run" -ForegroundColor White
Write-Host ""

