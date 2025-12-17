# Script reset password cho user DBMaybay
# Chạy script này với quyền Admin (nếu cần)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Reset Password cho User DBMaybay" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Kiểm tra psql có sẵn không
$psqlPath = Get-Command psql -ErrorAction SilentlyContinue
if (-not $psqlPath) {
    Write-Host "ERROR: psql không tìm thấy!" -ForegroundColor Red
    Write-Host "Vui lòng thêm PostgreSQL bin vào PATH hoặc dùng đường dẫn đầy đủ." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Ví dụ:" -ForegroundColor Yellow
    Write-Host '  $env:Path += ";C:\Program Files\PostgreSQL\16\bin"' -ForegroundColor Gray
    exit 1
}

Write-Host "Bước 1: Kết nối PostgreSQL với user postgres..." -ForegroundColor Yellow
Write-Host "Bạn sẽ được yêu cầu nhập password của user 'postgres'" -ForegroundColor Yellow
Write-Host ""

# Tạo SQL script tạm thời
$tempSql = @"
-- Reset password cho user DBMaybay
DO `$`$
BEGIN
    IF EXISTS (SELECT FROM pg_user WHERE usename = 'DBMaybay') THEN
        ALTER USER DBMaybay WITH PASSWORD '123456';
        RAISE NOTICE 'Password đã được reset thành công!';
    ELSE
        CREATE USER DBMaybay WITH PASSWORD '123456';
        RAISE NOTICE 'User DBMaybay đã được tạo với password 123456';
    END IF;
END
`$`$;

-- Cấp quyền
GRANT ALL PRIVILEGES ON DATABASE flight_booking TO DBMaybay;
ALTER DATABASE flight_booking OWNER TO DBMaybay;

-- Kết nối vào database và cấp quyền schema
\c flight_booking
GRANT ALL ON SCHEMA public TO DBMaybay;
ALTER SCHEMA public OWNER TO DBMaybay;

\echo ''
\echo '========================================'
\echo 'Reset password thành công!'
\echo 'User: DBMaybay'
\echo 'Password: 123456'
\echo '========================================'
"@

$tempFile = Join-Path $env:TEMP "reset_password_$(Get-Date -Format 'yyyyMMddHHmmss').sql"
$tempSql | Out-File -FilePath $tempFile -Encoding UTF8

try {
    Write-Host "Đang reset password..." -ForegroundColor Green
    psql -U postgres -f $tempFile
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "Reset password thành công!" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "User: DBMaybay" -ForegroundColor White
        Write-Host "Password: 123456" -ForegroundColor White
        Write-Host ""
        Write-Host "Bây giờ bạn có thể chạy backend:" -ForegroundColor Yellow
        Write-Host "  .\mvnw.cmd spring-boot:run" -ForegroundColor Gray
    } else {
        Write-Host ""
        Write-Host "ERROR: Reset password thất bại!" -ForegroundColor Red
        Write-Host "Vui lòng kiểm tra:" -ForegroundColor Yellow
        Write-Host "  1. PostgreSQL service đang chạy" -ForegroundColor Yellow
        Write-Host "  2. Password của user 'postgres' đúng" -ForegroundColor Yellow
        Write-Host "  3. Database 'flight_booking' đã tồn tại" -ForegroundColor Yellow
    }
} catch {
    Write-Host ""
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
} finally {
    # Xóa file tạm
    if (Test-Path $tempFile) {
        Remove-Item $tempFile -Force
    }
}

