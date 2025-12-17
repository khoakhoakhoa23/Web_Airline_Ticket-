# 🧪 FLIGHT SEARCH - QUICK TEST GUIDE

## ✅ **READY TO TEST**

---

## 🚀 **Prerequisites**

### **Backend**: ✅ Running
- URL: http://localhost:8080
- Status: ONLINE
- Data: 2,646+ flights seeded

### **Frontend**: ✅ Running  
- URL: http://localhost:5173
- Status: ONLINE
- Dev Server: Vite

---

## 🧪 **TEST FLOW**

### **Step 1: Home Page**

**URL**: http://localhost:5173

**Actions**:
1. Open browser
2. Navigate to http://localhost:5173
3. See hero image and search form

**Expected**:
- Home page loads successfully
- Search form visible with fields:
  - From (origin)
  - To (destination)
  - Depart date
  - Passengers
  - Class
- "Search Flights" button enabled

---

### **Step 2: Fill Search Form**

**Test Case 1**: Basic Search (SGN → HAN)

**Input**:
- From: `SGN`
- To: `HAN`
- Depart: `2025-01-20`
- Passengers: `1`
- Class: `ECONOMY`

**Actions**:
1. Type "SGN" in From field
2. Type "HAN" in To field
3. Select date: 2025-01-20
4. Keep passengers: 1
5. Keep class: ECONOMY
6. Click "Search Flights" button

**Expected**:
- No validation errors
- Redirect to: `/flights?origin=SGN&destination=HAN&departureDate=2025-01-20&passengers=1&cabinClass=ECONOMY`

---

### **Step 3: FlightSelection Page - Loading**

**URL**: `/flights?origin=SGN&destination=HAN&departureDate=2025-01-20&passengers=1&cabinClass=ECONOMY`

**Expected**:
1. Loading spinner appears
2. Message: "Searching for flights..."
3. Progress bar shows "Step 1: Flight Selection" highlighted
4. API call to:
   ```
   GET http://localhost:8080/api/flights/search?origin=SGN&destination=HAN&departureDate=2025-01-20&passengers=1&cabinClass=ECONOMY&page=0&size=10
   ```

**Check Browser Console**:
- No errors
- Network tab shows successful API call (status 200)

---

### **Step 4: FlightSelection Page - Results**

**Expected UI**:

1. **Search Summary** (top section):
   ```
   Route: SGN → HAN | Date: Mon, Jan 20, 2025 | Passengers: 1 | Class: ECONOMY
   Found 4 flights
   ```

2. **Flight Cards** (list):

   **Card 1: VN124 - Vietnam Airlines**
   ```
   Vietnam Airlines
   VN124 | Boeing 787
   
   09:00          2h 15m           11:15
   SGN    ──────────✈───────────   HAN
   
   Class: ECONOMY | Seats: 140/180 | Status: SCHEDULED
   
   Total Price
   2,500,000 VND
   (Base: 2,083,333 + Tax: 416,667)
   
   [Select Flight →]
   ```

   **Card 2: VJ457 - VietJet Air**
   ```
   VietJet Air
   VJ457 | Airbus A320
   
   11:30          2h 15m           13:45
   SGN    ──────────✈───────────   HAN
   
   Class: ECONOMY | Seats: 110/180 | Status: SCHEDULED
   
   Total Price
   1,900,000 VND
   (Base: 1,583,333 + Tax: 316,667)
   
   [Select Flight →]
   ```

   (... more cards ...)

3. **Pagination Info** (bottom):
   ```
   Showing 4 of 4 flights (Page 1 of 1)
   ```

**Expected Behavior**:
- All flight details displayed correctly
- Prices formatted in VND (e.g., 2,500,000)
- Times formatted in 24-hour (e.g., 09:00, 11:15)
- Duration calculated (e.g., 2h 15m)
- "Select Flight" button visible on each card

---

### **Step 5: Hover & Select Flight**

**Actions**:
1. Hover over a flight card
2. Click on the card (anywhere)
3. Card should highlight
4. Click "Select Flight →" button

**Expected**:
1. On hover:
   - Card border changes to blue
   - Subtle shadow appears
   
2. On click:
   - Card background becomes light blue (#f3f4ff)
   - Border becomes solid blue
   
3. On "Select Flight" click:
   - Flight data saved to localStorage
   - Navigate to: `/booking/traveller-info`

---

### **Step 6: Test Error Cases**

#### **Test Case 2**: No Flights Found

**Actions**:
1. Go back to home (click browser back or navigate to /)
2. Search for future date with no data:
   - From: `SGN`
   - To: `HAN`
   - Depart: `2025-12-31`

**Expected**:
```
✈️
No flights found

We couldn't find any flights matching your search criteria.
Try adjusting your search parameters.

[Back to Search]
```

**Click "Back to Search"**:
- Should navigate to home page (/)

---

#### **Test Case 3**: Validation Error

**Actions**:
1. On home page
2. Leave "From" field empty
3. Fill "To" and "Date"
4. Click "Search Flights"

**Expected**:
- Error message appears: "Please fill in all required fields"
- Does NOT navigate away from home page
- Form fields retain values

---

#### **Test Case 4**: Backend Error (Optional - for testing only)

**Actions**:
1. Stop backend (Ctrl+C in backend terminal)
2. On home, search for flights
3. FlightSelection page should load

**Expected**:
```
⚠️ Failed to search flights. Please try again.

(Error banner at top of page)
```

**Restart backend and refresh page**:
- Should load flights normally

---

### **Step 7: Test Different Routes**

**Test Case 5**: HAN → SGN

**Input**:
- From: `HAN`
- To: `SGN`
- Depart: `2025-01-20`

**Expected**:
- Different flights (reverse route)
- Similar UI and behavior

---

**Test Case 6**: SGN → DAD

**Input**:
- From: `SGN`
- To: `DAD`
- Depart: `2025-01-20`

**Expected**:
- Da Nang route flights
- Different prices and times

---

### **Step 8: Test Pagination (if applicable)**

**Note**: If more than 10 flights exist for a route

**Expected**:
1. First 10 flights displayed
2. "Load More" button at bottom:
   ```
   [Load More (15 remaining)]
   ```
3. Click "Load More"
4. Button changes to "Loading..."
5. Next 10 flights appear below
6. Pagination info updates:
   ```
   Showing 20 of 25 flights (Page 2 of 3)
   ```

---

### **Step 9: Test Responsive Design (Mobile)**

**Actions**:
1. Open browser DevTools (F12)
2. Toggle device toolbar (Ctrl+Shift+M)
3. Select mobile device (e.g., iPhone 12)

**Expected**:
- Flight cards stack vertically
- All text readable
- Buttons touch-friendly
- Progress bar adapts
- No horizontal scroll

---

## ✅ **CHECKLIST**

- [ ] Home page loads
- [ ] Search form validates input
- [ ] Redirects with query params
- [ ] FlightSelection shows loading spinner
- [ ] API call succeeds (check Network tab)
- [ ] Flight results display correctly
- [ ] Prices formatted in VND
- [ ] Times and durations correct
- [ ] Hover effect works
- [ ] Flight selection works
- [ ] Navigate to traveller-info page
- [ ] "No flights found" state works
- [ ] Validation errors display
- [ ] Different routes work
- [ ] Responsive on mobile
- [ ] No console errors

---

## 🐛 **TROUBLESHOOTING**

### **Issue 1**: "Network Error" or CORS error

**Cause**: Backend not running or CORS not configured

**Solution**:
```bash
cd backend
mvn spring-boot:run
```

Verify backend at: http://localhost:8080/api/flights

---

### **Issue 2**: Flights not showing

**Cause**: No flights seeded for that date

**Solution**:
- Use date: `2025-01-20` (known to have data)
- Or check backend logs for seeded dates

---

### **Issue 3**: "Cannot read property..." JavaScript error

**Cause**: API response format mismatch

**Solution**:
- Check browser console for exact error
- Verify API response structure matches FlightDTO
- Check Network tab for API response

---

### **Issue 4**: Page stuck on loading

**Cause**: API call failed silently

**Solution**:
- Open browser console
- Check Network tab for failed requests
- Verify backend is running
- Check for CORS errors

---

## 📊 **EXPECTED API RESPONSE**

When you search for SGN → HAN on 2025-01-20, the API should return:

```json
{
  "content": [
    {
      "id": "uuid-123",
      "flightNumber": "VN124",
      "airline": "Vietnam Airlines",
      "origin": "SGN",
      "destination": "HAN",
      "departTime": "2025-01-20T09:00:00",
      "arriveTime": "2025-01-20T11:15:00",
      "cabinClass": "ECONOMY",
      "baseFare": 2083333.33,
      "taxes": 416666.67,
      "totalPrice": 2500000.00,
      "availableSeats": 140,
      "totalSeats": 180,
      "status": "SCHEDULED",
      "aircraftType": "Boeing 787",
      "durationMinutes": 135
    }
  ],
  "totalElements": 4,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

---

## 🎯 **SUCCESS CRITERIA**

✅ **Test Passes If**:
1. Home → Search → Redirect works
2. Loading spinner appears briefly
3. Flight results display with correct data
4. Prices formatted correctly (VND)
5. Times formatted correctly (24-hour)
6. Duration calculated correctly
7. Flight selection navigates to next page
8. No console errors
9. No network errors
10. Responsive on mobile

---

## ✅ **PRODUCTION READY**

Your flight search is **PRODUCTION-READY** when:
- ✅ All test cases pass
- ✅ No console errors
- ✅ Error handling works
- ✅ Loading states work
- ✅ Responsive design works
- ✅ API integration successful

---

**Next**: Complete booking flow (Traveller Info → Payment → Confirmation)! 🚀

---

**Last Updated**: 2025-12-17  
**Status**: ✅ READY TO TEST

