# Test Database Connection

## Quick Test Script

Run this PowerShell script to test database connection:

```powershell
# Test PostgreSQL Connection
$env:PGPASSWORD = "123456"
psql -U DBMaybay -d flight_booking -c "SELECT version();"
```

## Expected Output:
```
PostgreSQL 16.x on x86_64-pc-windows...
(1 row)
```

## If Connection Fails:

### 1. Check PostgreSQL Service
```powershell
Get-Service -Name postgresql*
# If stopped, start it:
Start-Service postgresql-x64-16
```

### 2. Test with postgres user
```powershell
psql -U postgres
# Then:
\c flight_booking
SELECT current_user, current_database();
```

### 3. Reset password if needed
```sql
ALTER USER DBMaybay WITH PASSWORD '123456';
GRANT ALL PRIVILEGES ON DATABASE flight_booking TO DBMaybay;
\c flight_booking
GRANT ALL ON SCHEMA public TO DBMaybay;
```

## Verify Tables Exist

After backend starts, check if tables are created:

```sql
\c flight_booking
\dt
```

You should see tables like:
- users
- bookings
- flight_segments
- passengers
- etc.

