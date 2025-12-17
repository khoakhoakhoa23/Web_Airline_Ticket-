# ✅ QUICK TEST CHECKLIST - PRODUCTION READINESS

## 🚀 **PHASE 1: BACKEND TEST** (5 minutes)

### **Step 1: Start Backend**
```bash
cd backend
mvn spring-boot:run
```

**Expected**:
- ✅ Application starts without errors
- ✅ See: "Started FlightBookingApplication in X seconds"
- ✅ No red ERROR logs

### **Step 2: Test API Endpoints**

Open browser or Postman:

**Test 1: Flight Search (Public)**
```
GET http://localhost:8080/api/flights/search?origin=SGN&destination=HAN&departureDate=2025-01-20
```
✅ Expected: 200 OK, JSON with flights

**Test 2: Register (Public)**
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "test123@test.com",
  "password": "Test123456",
  "phone": "0123456789"
}
```
✅ Expected: 200 OK or 400 (if email exists)

**Test 3: Health Check**
```
GET http://localhost:8080/api/flights/search?origin=SGN&destination=HAN&departureDate=2025-01-20
```
✅ Expected: Returns flights data

---

## 🎨 **PHASE 2: FRONTEND TEST** (3 minutes)

### **Step 1: Start Frontend**

**New terminal**:
```bash
cd frontend
npm run dev
```

**Expected**:
- ✅ Vite starts
- ✅ Local: http://localhost:5173
- ✅ No errors in terminal

### **Step 2: Test UI**

**Open**: http://localhost:5173

**Test 1: Home Page**
- ✅ Page loads
- ✅ Search form visible
- ✅ No console errors (F12)

**Test 2: Search Flights**
- From: `SGN`
- To: `HAN`  
- Date: `2025-01-20`
- Click "Search Flights"

✅ Expected:
- Redirects to `/flights?origin=...`
- Shows loading spinner
- Displays flight results
- No console errors

**Test 3: Select Flight**
- Click on a flight card
- Click "Select Flight" button

✅ Expected:
- Flight highlighted
- Navigates to traveller info page
- No errors

---

## 🔑 **PHASE 3: CORE FEATURES** (5 minutes)

### **Test: Full Booking Flow**

**Step 1: Register/Login**
- Go to `/register`
- Create account
- Login

**Step 2: Search Flight**
- Search SGN → HAN
- See results

**Step 3: Select Flight** 
- Click on flight
- Click "Select Flight"

**Step 4: Passenger Info**
- Fill passenger details
- Submit (should create booking)

**Step 5: Payment**
- Should redirect to payment page
- See booking summary
- Payment button visible

✅ **If you reach here: SYSTEM WORKING!**

---

## 📊 **QUICK HEALTH CHECK**

### **Backend Health**
```bash
curl http://localhost:8080/api/flights/search?origin=SGN&destination=HAN&departureDate=2025-01-20
```
✅ Should return JSON

### **Frontend Health**
- Open: http://localhost:5173
- Check console (F12)
- ✅ No red errors

### **Database Health**
- Backend logs show: "Seeded X flights"
- ✅ Flights appear in search results

---

## 🐛 **COMMON ISSUES & FIXES**

### **Backend won't start**
```bash
# Check port 8080
netstat -ano | findstr :8080

# Kill process if needed
taskkill /PID <PID> /F

# Restart
mvn spring-boot:run
```

### **Frontend won't start**
```bash
# Reinstall dependencies
rm -rf node_modules
npm install
npm run dev
```

### **No flights in search**
- Check backend logs for "Seeded X flights"
- If 0, database may not be seeded
- Check application.properties database config

### **CORS errors**
- Check SecurityConfig.java has CORS config
- Check API base URL in frontend/src/services/api.js

---

## ✅ **PRODUCTION READINESS CHECKLIST**

### **Must-Have (Core)**
- [ ] Backend starts without errors
- [ ] Flight search API works
- [ ] User registration works
- [ ] User login works (JWT)
- [ ] Frontend loads without errors
- [ ] Can search flights in UI
- [ ] Can select flight
- [ ] Database has seed data

### **Important (Flow)**
- [ ] Full booking flow works
- [ ] Payment page loads
- [ ] Email service configured (optional for test)
- [ ] Error handling works (try invalid data)
- [ ] Toast notifications appear
- [ ] Loading states show

### **Nice-to-Have (Polish)**
- [ ] Responsive on mobile
- [ ] No console warnings
- [ ] Fast page load
- [ ] Smooth transitions

---

## 🎯 **MINIMAL PASSING CRITERIA**

Your system is **READY FOR NEXT PHASE** if:

1. ✅ Backend starts and serves API
2. ✅ Frontend loads and displays UI
3. ✅ Flight search returns results
4. ✅ Can navigate through pages
5. ✅ No critical errors in console

**If 5/5: PROCEED! 🚀**

---

## 📝 **QUICK TEST REPORT**

Fill this out:

```
Date: ___________
Tester: ___________

Backend Status: [ ] WORKING [ ] ISSUES
Frontend Status: [ ] WORKING [ ] ISSUES
Flight Search: [ ] WORKING [ ] ISSUES
User Auth: [ ] WORKING [ ] ISSUES
Booking Flow: [ ] WORKING [ ] ISSUES

Critical Issues:
___________________________________________
___________________________________________

Overall: [ ] READY [ ] NOT READY

Notes:
___________________________________________
___________________________________________
```

---

## 🚀 **NEXT STEPS AFTER TESTING**

### **If All Tests Pass**:
1. Apply contexts to remaining pages
2. Add advanced filters (optional)
3. Deploy to production

### **If Some Tests Fail**:
1. Check COMMON ISSUES section
2. Review error messages
3. Fix and retest

### **If Critical Failures**:
1. Check environment setup
2. Verify database connection
3. Review application.properties
4. Check port conflicts

---

**Remember**: You don't need 100% to proceed. 80%+ is good enough for next phase!

