@echo off
echo Starting Flight Booking Backend...
echo.
echo Make sure PostgreSQL is running and database 'flight_booking' exists!
echo.
pause

REM Try to find Maven
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Using system Maven...
    mvn spring-boot:run
) else (
    echo Maven not found in PATH.
    echo Please either:
    echo 1. Install Maven and add it to PATH, OR
    echo 2. Run from your IDE (IntelliJ IDEA / Eclipse)
    echo.
    pause
)

