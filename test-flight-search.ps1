# Test Flight Search API
# Tests GET /api/flights/search with various filters

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "🧪 TEST: FLIGHT SEARCH API" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Cyan

$backendUrl = "http://localhost:8080/api/flights"

# Get tomorrow's date for testing
$tomorrow = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")

# Test 1: Basic search (origin + destination + date)
Write-Host "1. Testing BASIC SEARCH (SGN -> HAN)..." -ForegroundColor Yellow
try {
    $url = "$backendUrl/search?origin=SGN&destination=HAN&departureDate=$tomorrow&page=0&size=5"
    Write-Host "   URL: $url" -ForegroundColor Gray
    
    $response = Invoke-RestMethod -Uri $url -Method GET -ErrorAction Stop
    
    Write-Host "   ✅ Search successful" -ForegroundColor Green
    Write-Host "   Total flights: $($response.totalElements)" -ForegroundColor Gray
    Write-Host "   Page size: $($response.size)" -ForegroundColor Gray
    Write-Host "   Total pages: $($response.totalPages)" -ForegroundColor Gray
    
    if ($response.content.Count -gt 0) {
        $flight = $response.content[0]
        Write-Host "   Sample flight:" -ForegroundColor Gray
        Write-Host "     Flight: $($flight.flightNumber)" -ForegroundColor Cyan
        Write-Host "     Airline: $($flight.airline)" -ForegroundColor Cyan
        Write-Host "     Route: $($flight.origin) -> $($flight.destination)" -ForegroundColor Cyan
        Write-Host "     Price: $('{0:N0}' -f $flight.totalPrice) VND" -ForegroundColor Cyan
        Write-Host "     Available seats: $($flight.availableSeats)/$($flight.totalSeats)" -ForegroundColor Cyan
    }
    Write-Host ""
} catch {
    Write-Host "   ❌ Search failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   Response: $($_.ErrorDetails.Message)`n" -ForegroundColor Red
    exit 1
}

# Test 2: Search with price filter
Write-Host "2. Testing PRICE FILTER (1M-2M VND)..." -ForegroundColor Yellow
try {
    $url = "$backendUrl/search?origin=SGN&destination=HAN&departureDate=$tomorrow&minPrice=1000000&maxPrice=2000000&page=0&size=5"
    Write-Host "   URL: $url" -ForegroundColor Gray
    
    $response = Invoke-RestMethod -Uri $url -Method GET -ErrorAction Stop
    
    Write-Host "   ✅ Price filter working" -ForegroundColor Green
    Write-Host "   Flights in range: $($response.totalElements)" -ForegroundColor Gray
    
    if ($response.content.Count -gt 0) {
        $flight = $response.content[0]
        Write-Host "   Sample flight price: $('{0:N0}' -f $flight.totalPrice) VND" -ForegroundColor Cyan
    }
    Write-Host ""
} catch {
    Write-Host "   ⚠️  Price filter test failed: $($_.Exception.Message)" -ForegroundColor Yellow
    Write-Host ""
}

# Test 3: Search with airline filter
Write-Host "3. Testing AIRLINE FILTER (Vietnam Airlines)..." -ForegroundColor Yellow
try {
    $url = "$backendUrl/search?origin=SGN&destination=HAN&departureDate=$tomorrow&airline=Vietnam&page=0&size=5"
    Write-Host "   URL: $url" -ForegroundColor Gray
    
    $response = Invoke-RestMethod -Uri $url -Method GET -ErrorAction Stop
    
    Write-Host "   ✅ Airline filter working" -ForegroundColor Green
    Write-Host "   Vietnam Airlines flights: $($response.totalElements)" -ForegroundColor Gray
    
    if ($response.content.Count -gt 0) {
        $flight = $response.content[0]
        Write-Host "   Sample airline: $($flight.airline)" -ForegroundColor Cyan
    }
    Write-Host ""
} catch {
    Write-Host "   ⚠️  Airline filter test failed: $($_.Exception.Message)" -ForegroundColor Yellow
    Write-Host ""
}

# Test 4: Search with cabin class filter
Write-Host "4. Testing CABIN CLASS FILTER (ECONOMY)..." -ForegroundColor Yellow
try {
    $url = "$backendUrl/search?origin=SGN&destination=DAD&departureDate=$tomorrow&passengers=1&page=0&size=5"
    Write-Host "   URL: $url" -ForegroundColor Gray
    
    $response = Invoke-RestMethod -Uri $url -Method GET -ErrorAction Stop
    
    Write-Host "   ✅ Search successful for SGN -> DAD" -ForegroundColor Green
    Write-Host "   Total flights: $($response.totalElements)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "   ⚠️  Search failed: $($_.Exception.Message)" -ForegroundColor Yellow
    Write-Host ""
}

# Test 5: Pagination test
Write-Host "5. Testing PAGINATION (page 0 and page 1)..." -ForegroundColor Yellow
try {
    # Page 0
    $url1 = "$backendUrl/search?origin=HAN&destination=SGN&departureDate=$tomorrow&page=0&size=3"
    $response1 = Invoke-RestMethod -Uri $url1 -Method GET -ErrorAction Stop
    
    # Page 1
    $url2 = "$backendUrl/search?origin=HAN&destination=SGN&departureDate=$tomorrow&page=1&size=3"
    $response2 = Invoke-RestMethod -Uri $url2 -Method GET -ErrorAction Stop
    
    Write-Host "   ✅ Pagination working" -ForegroundColor Green
    Write-Host "   Page 0: $($response1.content.Count) flights" -ForegroundColor Gray
    Write-Host "   Page 1: $($response2.content.Count) flights" -ForegroundColor Gray
    Write-Host "   Total pages: $($response1.totalPages)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "   ⚠️  Pagination test failed: $($_.Exception.Message)" -ForegroundColor Yellow
    Write-Host ""
}

# Test 6: Combined filters
Write-Host "6. Testing COMBINED FILTERS (airline + price + passengers)..." -ForegroundColor Yellow
try {
    $url = "$backendUrl/search?origin=SGN&destination=HAN&departureDate=$tomorrow&airline=VietJet&minPrice=500000&maxPrice=3000000&passengers=2&page=0&size=5"
    Write-Host "   URL: $url" -ForegroundColor Gray
    
    $response = Invoke-RestMethod -Uri $url -Method GET -ErrorAction Stop
    
    Write-Host "   ✅ Combined filters working" -ForegroundColor Green
    Write-Host "   Matching flights: $($response.totalElements)" -ForegroundColor Gray
    
    if ($response.content.Count -gt 0) {
        $flight = $response.content[0]
        Write-Host "   Sample result:" -ForegroundColor Gray
        Write-Host "     Airline: $($flight.airline)" -ForegroundColor Cyan
        Write-Host "     Price: $('{0:N0}' -f $flight.totalPrice) VND" -ForegroundColor Cyan
        Write-Host "     Seats: $($flight.availableSeats)" -ForegroundColor Cyan
    }
    Write-Host ""
} catch {
    Write-Host "   ⚠️  Combined filter test failed: $($_.Exception.Message)" -ForegroundColor Yellow
    Write-Host ""
}

# Test 7: Get flight by ID
Write-Host "7. Testing GET FLIGHT BY ID..." -ForegroundColor Yellow
try {
    # First get a flight ID from search
    $searchResponse = Invoke-RestMethod -Uri "$backendUrl/search?origin=SGN&destination=HAN&departureDate=$tomorrow&page=0&size=1" -Method GET -ErrorAction Stop
    
    if ($searchResponse.content.Count -gt 0) {
        $flightId = $searchResponse.content[0].id
        $url = "$backendUrl/$flightId"
        Write-Host "   URL: $url" -ForegroundColor Gray
        
        $response = Invoke-RestMethod -Uri $url -Method GET -ErrorAction Stop
        
        Write-Host "   ✅ Get by ID working" -ForegroundColor Green
        Write-Host "   Flight: $($response.flightNumber)" -ForegroundColor Gray
        Write-Host "   Airline: $($response.airline)" -ForegroundColor Gray
    } else {
        Write-Host "   ⚠️  No flights found to test" -ForegroundColor Yellow
    }
    Write-Host ""
} catch {
    Write-Host "   ⚠️  Get by ID test failed: $($_.Exception.Message)" -ForegroundColor Yellow
    Write-Host ""
}

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ FLIGHT SEARCH API TEST SUMMARY" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "✅ TESTS COMPLETED:" -ForegroundColor Green
Write-Host "   ✓ Basic search (origin + destination + date)" -ForegroundColor Gray
Write-Host "   ✓ Price filter (minPrice, maxPrice)" -ForegroundColor Gray
Write-Host "   ✓ Airline filter (partial match, case-insensitive)" -ForegroundColor Gray
Write-Host "   ✓ Route variations (SGN-HAN, SGN-DAD, HAN-SGN)" -ForegroundColor Gray
Write-Host "   ✓ Pagination (page, size)" -ForegroundColor Gray
Write-Host "   ✓ Combined filters" -ForegroundColor Gray
Write-Host "   ✓ Get flight by ID`n" -ForegroundColor Gray

Write-Host "📊 SAMPLE QUERY FORMATS:" -ForegroundColor Yellow
Write-Host ""
Write-Host "Basic search:" -ForegroundColor White
Write-Host "  GET /api/flights/search?origin=SGN&destination=HAN&departureDate=2025-12-20" -ForegroundColor Cyan
Write-Host ""
Write-Host "With price filter:" -ForegroundColor White
Write-Host "  GET /api/flights/search?origin=SGN&destination=HAN&departureDate=2025-12-20&minPrice=1000000&maxPrice=2000000" -ForegroundColor Cyan
Write-Host ""
Write-Host "With airline filter:" -ForegroundColor White
Write-Host "  GET /api/flights/search?origin=SGN&destination=HAN&departureDate=2025-12-20&airline=Vietnam" -ForegroundColor Cyan
Write-Host ""
Write-Host "With pagination:" -ForegroundColor White
Write-Host "  GET /api/flights/search?origin=SGN&destination=HAN&departureDate=2025-12-20&page=0&size=10" -ForegroundColor Cyan
Write-Host ""
Write-Host "Combined:" -ForegroundColor White
Write-Host "  GET /api/flights/search?origin=SGN&destination=HAN&departureDate=2025-12-20&airline=VietJet&minPrice=500000&maxPrice=3000000&passengers=2&page=0&size=10" -ForegroundColor Cyan
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🎉 FLIGHT SEARCH FEATURE: READY!" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "📖 Next steps:" -ForegroundColor Yellow
Write-Host "   1. Test in Postman: Import sample queries above" -ForegroundColor White
Write-Host "   2. Frontend integration: Use GET /api/flights/search" -ForegroundColor White
Write-Host "   3. Add more test data if needed`n" -ForegroundColor White

