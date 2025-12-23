# ==================================================
# FLIGHT BOOKING SYSTEM - COMPREHENSIVE TEST SUITE
# Gộp tất cả các test scripts thành 1 file duy nhất
# ==================================================

$ErrorActionPreference = "Continue"
$baseUrl = "http://localhost:8080/api"
$frontendUrl = "http://localhost:5173"

# Test Results
$script:testResults = @()
$script:testsPassed = 0
$script:testsFailed = 0

function Write-TestHeader {
    param([string]$message)
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host $message -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Cyan
}

function Write-TestResult {
    param(
        [string]$testName,
        [bool]$passed,
        [string]$message = ""
    )
    $icon = if ($passed) { "✅" } else { "❌" }
    $color = if ($passed) { "Green" } else { "Red" }
    Write-Host "$icon $testName" -ForegroundColor $color
    if ($message) {
        Write-Host "   $message" -ForegroundColor White
    }
    $script:testResults += @{
        Test = $testName
        Passed = $passed
        Message = $message
    }
    if ($passed) { $script:testsPassed++ } else { $script:testsFailed++ }
}

function Show-Menu {
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "FLIGHT BOOKING SYSTEM - TEST SUITE" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "1. Quick Test (Basic APIs)" -ForegroundColor White
    Write-Host "2. Complete System Test (All Features)" -ForegroundColor White
    Write-Host "3. Authentication & Security Test" -ForegroundColor White
    Write-Host "4. Flight Search Test" -ForegroundColor White
    Write-Host "5. Payment & Booking Test" -ForegroundColor White
    Write-Host "6. Admin Functionality Test" -ForegroundColor White
    Write-Host "7. Environment Check" -ForegroundColor White
    Write-Host "8. Run All Tests" -ForegroundColor White
    Write-Host "0. Exit" -ForegroundColor White
    Write-Host "========================================`n" -ForegroundColor Cyan
}

function Test-BackendStatus {
    Write-TestHeader "CHECKING BACKEND STATUS"
    try {
        $response = Invoke-WebRequest -Uri "$baseUrl/flights" -Method GET -TimeoutSec 5 -ErrorAction Stop
        Write-TestResult "Backend is running" $true "Backend responding on port 8080"
        return $true
    } catch {
        Write-TestResult "Backend is running" $false "Backend not responding. Please start backend first."
        Write-Host "`n❌ Backend is not running. Please start it first:" -ForegroundColor Red
        Write-Host "   cd backend" -ForegroundColor Yellow
        Write-Host "   .\mvnw.cmd spring-boot:run" -ForegroundColor Yellow
        return $false
    }
}

function Test-Quick {
    Write-TestHeader "QUICK TEST - BASIC APIs"
    
    if (-not (Test-BackendStatus)) { return }
    
    # Test 1: Get All Flights
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/flights" -Method GET -ErrorAction Stop
        Write-TestResult "Get All Flights" $true "Found $($response.Count) flights"
    } catch {
        Write-TestResult "Get All Flights" $false $_.Exception.Message
    }
    
    # Test 2: Flight Search
    try {
        $searchUrl = "$baseUrl/flights/search?origin=SGN&destination=HAN&departureDate=2025-12-24&passengers=1&page=0&size=10"
        $response = Invoke-RestMethod -Uri $searchUrl -Method GET -ErrorAction Stop
        Write-TestResult "Flight Search" $true "Found $($response.totalElements) flights"
    } catch {
        Write-TestResult "Flight Search" $false $_.Exception.Message
    }
    
    # Test 3: Register User
    $testEmail = "test$(Get-Random)@test.com"
    $testPassword = "Test123456!"
    try {
        $registerBody = @{
            email = $testEmail
            password = $testPassword
            phone = "0123456789"
            role = "USER"
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$baseUrl/auth/register" `
            -Method POST `
            -Body $registerBody `
            -ContentType "application/json" `
            -ErrorAction Stop
        
        Write-TestResult "User Registration" $true "User registered: $testEmail"
    } catch {
        Write-TestResult "User Registration" $false $_.Exception.Message
    }
    
    # Test 4: Login
    try {
        $loginBody = @{
            email = $testEmail
            password = $testPassword
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$baseUrl/auth/login" `
            -Method POST `
            -Body $loginBody `
            -ContentType "application/json" `
            -ErrorAction Stop
        
        if ($response.accessToken) {
            Write-TestResult "User Login" $true "JWT token received"
        } else {
            Write-TestResult "User Login" $false "No accessToken in response"
        }
    } catch {
        Write-TestResult "User Login" $false $_.Exception.Message
    }
}

function Test-Authentication {
    Write-TestHeader "AUTHENTICATION & SECURITY TEST"
    
    if (-not (Test-BackendStatus)) { return }
    
    $testEmail = "security-test-$(Get-Date -Format 'yyyyMMddHHmmss')@example.com"
    $testPassword = "SecurePass123!"
    
    # Test 1: Register (Password Hashing)
    try {
        $registerBody = @{
            email = $testEmail
            password = $testPassword
            phone = "0123456789"
            role = "USER"
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$baseUrl/auth/register" `
            -Method POST `
            -Body $registerBody `
            -ContentType "application/json" `
            -ErrorAction Stop
        
        if ($response.PSObject.Properties.Name -contains "password") {
            Write-TestResult "Password Security (Register)" $false "Password found in response!"
        } else {
            Write-TestResult "Password Security (Register)" $true "Password NOT in response"
        }
    } catch {
        Write-TestResult "User Registration" $false $_.Exception.Message
        return
    }
    
    # Test 2: Login (JWT)
    try {
        $loginBody = @{
            email = $testEmail
            password = $testPassword
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$baseUrl/auth/login" `
            -Method POST `
            -Body $loginBody `
            -ContentType "application/json" `
            -ErrorAction Stop
        
        $token = $response.accessToken
        if ($token) {
            Write-TestResult "JWT Token Generation" $true "Token received"
            
            # Test 3: Protected Endpoint
            $headers = @{ Authorization = "Bearer $token" }
            $userResponse = Invoke-RestMethod -Uri "$baseUrl/users/$($response.id)" `
                -Method GET `
                -Headers $headers `
                -ErrorAction Stop
            
            if ($userResponse.PSObject.Properties.Name -contains "password") {
                Write-TestResult "Password Security (User Endpoint)" $false "Password found in response!"
            } else {
                Write-TestResult "Password Security (User Endpoint)" $true "Password NOT in response"
            }
            
            Write-TestResult "Protected Endpoint Access" $true "Access granted with JWT"
        } else {
            Write-TestResult "JWT Token Generation" $false "No token received"
        }
    } catch {
        Write-TestResult "User Login" $false $_.Exception.Message
    }
    
    # Test 4: Wrong Password
    try {
        $wrongLoginBody = @{
            email = $testEmail
            password = "WrongPassword123"
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$baseUrl/auth/login" `
            -Method POST `
            -Body $wrongLoginBody `
            -ContentType "application/json" `
            -ErrorAction Stop
        
        Write-TestResult "Wrong Password Rejection" $false "Should return 401"
    } catch {
        if ($_.Exception.Response.StatusCode -eq 401) {
            Write-TestResult "Wrong Password Rejection" $true "Correctly returned 401"
        } else {
            Write-TestResult "Wrong Password Rejection" $false "Unexpected error"
        }
    }
}

function Test-FlightSearch {
    Write-TestHeader "FLIGHT SEARCH TEST"
    
    if (-not (Test-BackendStatus)) { return }
    
    $tomorrow = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")
    
    # Test 1: Basic Search
    try {
        $url = "$baseUrl/flights/search?origin=SGN&destination=HAN&departureDate=$tomorrow&page=0&size=5"
        $response = Invoke-RestMethod -Uri $url -Method GET -ErrorAction Stop
        Write-TestResult "Basic Flight Search" $true "Found $($response.totalElements) flights"
    } catch {
        Write-TestResult "Basic Flight Search" $false $_.Exception.Message
    }
    
    # Test 2: Price Filter
    try {
        $url = "$baseUrl/flights/search?origin=SGN&destination=HAN&departureDate=$tomorrow&minPrice=1000000&maxPrice=2000000"
        $response = Invoke-RestMethod -Uri $url -Method GET -ErrorAction Stop
        Write-TestResult "Price Filter" $true "Price filter working"
    } catch {
        Write-TestResult "Price Filter" $false $_.Exception.Message
    }
    
    # Test 3: Airline Filter
    try {
        $url = "$baseUrl/flights/search?origin=SGN&destination=HAN&departureDate=$tomorrow&airline=Vietnam"
        $response = Invoke-RestMethod -Uri $url -Method GET -ErrorAction Stop
        Write-TestResult "Airline Filter" $true "Found $($response.totalElements) flights"
    } catch {
        Write-TestResult "Airline Filter" $false $_.Exception.Message
    }
    
    # Test 4: Pagination
    try {
        $url1 = "$baseUrl/flights/search?origin=HAN&destination=SGN&departureDate=$tomorrow&page=0&size=3"
        $response1 = Invoke-RestMethod -Uri $url1 -Method GET -ErrorAction Stop
        
        $url2 = "$baseUrl/flights/search?origin=HAN&destination=SGN&departureDate=$tomorrow&page=1&size=3"
        $response2 = Invoke-RestMethod -Uri $url2 -Method GET -ErrorAction Stop
        
        Write-TestResult "Pagination" $true "Page 0: $($response1.content.Count), Page 1: $($response2.content.Count)"
    } catch {
        Write-TestResult "Pagination" $false $_.Exception.Message
    }
    
    # Test 5: Filter Expired Flights
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/flights" -Method GET -ErrorAction Stop
        $now = Get-Date
        $expiredCount = 0
        
        foreach ($flight in $response) {
            if ($flight.arriveTime) {
                $arriveTime = [DateTime]::Parse($flight.arriveTime)
                if ($arriveTime -lt $now) {
                    $expiredCount++
                }
            }
        }
        
        if ($expiredCount -eq 0) {
            Write-TestResult "Filter Expired Flights" $true "No expired flights in response"
        } else {
            Write-TestResult "Filter Expired Flights" $false "Found $expiredCount expired flights"
        }
    } catch {
        Write-TestResult "Filter Expired Flights" $false $_.Exception.Message
    }
}

function Test-PaymentBooking {
    Write-TestHeader "PAYMENT & BOOKING TEST"
    
    if (-not (Test-BackendStatus)) { return }
    
    # Register and Login
    $testEmail = "booking-test-$(Get-Random)@test.com"
    $testPassword = "Test123456!"
    $testToken = $null
    $bookingId = $null
    
    try {
        $registerBody = @{
            email = $testEmail
            password = $testPassword
            phone = "0123456789"
            role = "USER"
        } | ConvertTo-Json
        
        $registerResponse = Invoke-RestMethod -Uri "$baseUrl/auth/register" `
            -Method POST `
            -Body $registerBody `
            -ContentType "application/json" `
            -ErrorAction Stop
        
        $loginBody = @{
            email = $testEmail
            password = $testPassword
        } | ConvertTo-Json
        
        $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" `
            -Method POST `
            -Body $loginBody `
            -ContentType "application/json" `
            -ErrorAction Stop
        
        $testToken = $loginResponse.accessToken
        Write-TestResult "User Setup" $true "User registered and logged in"
    } catch {
        Write-TestResult "User Setup" $false $_.Exception.Message
        return
    }
    
    # Test 1: Create Booking
    if ($testToken) {
        try {
            $bookingBody = @{
                flightSegments = @(
                    @{
                        flightNumber = "TEST123"
                        airline = "Test Airline"
                        origin = "SGN"
                        destination = "HAN"
                        departTime = "2025-12-25T10:00:00"
                        arriveTime = "2025-12-25T12:00:00"
                        cabinClass = "ECONOMY"
                        baseFare = 2000000
                        taxes = 200000
                    }
                )
                passengers = @(
                    @{
                        fullName = "Test User"
                        dateOfBirth = "1990-01-01"
                        gender = "MALE"
                        nationality = "VN"
                        passportNumber = "TEST123456"
                        phone = "0123456789"
                        email = $testEmail
                    }
                )
                currency = "VND"
            } | ConvertTo-Json -Depth 10
            
            $headers = @{ Authorization = "Bearer $testToken" }
            
            $response = Invoke-RestMethod -Uri "$baseUrl/bookings" `
                -Method POST `
                -Body $bookingBody `
                -ContentType "application/json" `
                -Headers $headers `
                -ErrorAction Stop
            
            $bookingId = $response.id
            Write-TestResult "Create Booking" $true "Booking created: $($response.bookingCode)"
        } catch {
            Write-TestResult "Create Booking" $false $_.Exception.Message
        }
    }
    
    # Test 2: Create Payment (Manual Approval)
    if ($testToken -and $bookingId) {
        try {
            $paymentBody = @{
                bookingId = $bookingId
                paymentMethod = "STRIPE"
                successUrl = "$frontendUrl/booking/confirmation/$bookingId?payment=success"
                cancelUrl = "$frontendUrl/booking/payment?booking_id=$bookingId"
            } | ConvertTo-Json
            
            $headers = @{ Authorization = "Bearer $testToken" }
            
            $response = Invoke-RestMethod -Uri "$baseUrl/payments/create" `
                -Method POST `
                -Body $paymentBody `
                -ContentType "application/json" `
                -Headers $headers `
                -ErrorAction Stop
            
            if ($response.status -eq "PENDING") {
                Write-TestResult "Create Payment (Manual Approval)" $true "Payment created with PENDING status. Waiting for admin approval."
            } else {
                Write-TestResult "Create Payment (Manual Approval)" $false "Unexpected status: $($response.status)"
            }
        } catch {
            Write-TestResult "Create Payment" $false $_.Exception.Message
        }
    }
}

function Test-Admin {
    Write-TestHeader "ADMIN FUNCTIONALITY TEST"
    
    if (-not (Test-BackendStatus)) { return }
    
    # Test Admin Login
    $adminToken = $null
    try {
        $adminLoginBody = @{
            email = "admin@admin.com"
            password = "admin123"
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$baseUrl/auth/login" `
            -Method POST `
            -Body $adminLoginBody `
            -ContentType "application/json" `
            -ErrorAction Stop
        
        if ($response.accessToken) {
            $adminToken = $response.accessToken
            Write-TestResult "Admin Login" $true "Admin JWT token received"
        } else {
            Write-TestResult "Admin Login" $false "No accessToken in response"
            return
        }
    } catch {
        Write-TestResult "Admin Login" $false "Admin credentials may not exist. Skipping admin tests."
        return
    }
    
    # Test Admin: Get All Bookings
    if ($adminToken) {
        try {
            $headers = @{ Authorization = "Bearer $adminToken" }
            
            $response = Invoke-RestMethod -Uri "$baseUrl/admin/bookings?page=0&size=10" `
                -Method GET `
                -Headers $headers `
                -ErrorAction Stop
            
            Write-TestResult "Admin: Get All Bookings" $true "Retrieved $($response.totalElements) bookings"
        } catch {
            Write-TestResult "Admin: Get All Bookings" $false $_.Exception.Message
        }
    }
    
    # Test Admin: Get All Flights
    if ($adminToken) {
        try {
            $headers = @{ Authorization = "Bearer $adminToken" }
            
            $response = Invoke-RestMethod -Uri "$baseUrl/admin/flights?page=0&size=20" `
                -Method GET `
                -Headers $headers `
                -ErrorAction Stop
            
            $now = Get-Date
            $completedFlights = 0
            foreach ($flight in $response.content) {
                if ($flight.arriveTime) {
                    $arriveTime = [DateTime]::Parse($flight.arriveTime)
                    if ($arriveTime -lt $now) {
                        $completedFlights++
                    }
                }
            }
            
            Write-TestResult "Admin: Get All Flights" $true "Retrieved $($response.totalElements) flights ($completedFlights completed)"
        } catch {
            Write-TestResult "Admin: Get All Flights" $false $_.Exception.Message
        }
    }
}

function Test-Environment {
    Write-TestHeader "ENVIRONMENT CHECK"
    
    # Check Java
    try {
        $javaVersion = java -version 2>&1 | Select-String "version" | Select-Object -First 1
        Write-TestResult "Java installed" $true "$javaVersion"
    } catch {
        Write-TestResult "Java installed" $false "Java not found"
    }
    
    # Check Maven
    try {
        $mvnVersion = mvn -version 2>&1 | Select-String "Apache Maven" | Select-Object -First 1
        Write-TestResult "Maven installed" $true "$mvnVersion"
    } catch {
        Write-TestResult "Maven installed" $false "Maven not found"
    }
    
    # Check Node.js
    try {
        $nodeVersion = node --version
        Write-TestResult "Node.js installed" $true "Node $nodeVersion"
    } catch {
        Write-TestResult "Node.js installed" $false "Node.js not found"
    }
    
    # Check npm
    try {
        $npmVersion = npm --version
        Write-TestResult "npm installed" $true "npm $npmVersion"
    } catch {
        Write-TestResult "npm installed" $false "npm not found"
    }
    
    # Check Backend Files
    $backendFiles = @(
        "backend/pom.xml",
        "backend/src/main/resources/application.properties"
    )
    
    foreach ($file in $backendFiles) {
        $exists = Test-Path $file
        Write-TestResult "Backend: $file" $exists
    }
    
    # Check Frontend Files
    $frontendFiles = @(
        "frontend/package.json",
        "frontend/src/App.jsx"
    )
    
    foreach ($file in $frontendFiles) {
        $exists = Test-Path $file
        Write-TestResult "Frontend: $file" $exists
    }
}

function Test-Complete {
    Write-TestHeader "COMPLETE SYSTEM TEST"
    
    Test-BackendStatus
    Test-Quick
    Test-Authentication
    Test-FlightSearch
    Test-PaymentBooking
    Test-Admin
}

function Show-Summary {
    Write-TestHeader "TEST SUMMARY"
    
    $totalTests = $script:testsPassed + $script:testsFailed
    $passRate = if ($totalTests -gt 0) { [math]::Round(($script:testsPassed / $totalTests) * 100, 2) } else { 0 }
    
    Write-Host "Total Tests: $totalTests" -ForegroundColor Cyan
    Write-Host "Passed: $script:testsPassed" -ForegroundColor Green
    Write-Host "Failed: $script:testsFailed" -ForegroundColor $(if ($script:testsFailed -eq 0) { "Green" } else { "Red" })
    Write-Host "Pass Rate: $passRate%" -ForegroundColor $(if ($passRate -ge 80) { "Green" } else { "Yellow" })
    
    if ($script:testsFailed -gt 0) {
        Write-Host "`nFailed Tests:" -ForegroundColor Red
        $script:testResults | Where-Object { $_.Passed -eq $false } | ForEach-Object {
            Write-Host "  ❌ $($_.Test): $($_.Message)" -ForegroundColor Red
        }
    }
}

# Main Menu Loop
do {
    Show-Menu
    $choice = Read-Host "Chọn test (0-8)"
    
    # Reset counters
    $script:testResults = @()
    $script:testsPassed = 0
    $script:testsFailed = 0
    
    switch ($choice) {
        "1" { Test-Quick; Show-Summary }
        "2" { Test-Complete; Show-Summary }
        "3" { Test-Authentication; Show-Summary }
        "4" { Test-FlightSearch; Show-Summary }
        "5" { Test-PaymentBooking; Show-Summary }
        "6" { Test-Admin; Show-Summary }
        "7" { Test-Environment; Show-Summary }
        "8" { 
            Test-Environment
            Test-Complete
            Show-Summary
        }
        "0" { 
            Write-Host "`nThoát chương trình.`n" -ForegroundColor Yellow
            break
        }
        default {
            Write-Host "`nLựa chọn không hợp lệ. Vui lòng chọn lại.`n" -ForegroundColor Red
        }
    }
    
    if ($choice -ne "0") {
        Write-Host "`nNhấn Enter để tiếp tục..." -ForegroundColor Gray
        Read-Host
    }
} while ($choice -ne "0")

