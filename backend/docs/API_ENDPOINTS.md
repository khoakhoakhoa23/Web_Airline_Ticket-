# API Endpoints Documentation

Base URL: `http://localhost:8080/api`

## User APIs

### Register User
- **POST** `/users/register`
- **Body:**
  ```json
  {
    "email": "user@example.com",
    "password": "password123",
    "phone": "0123456789",
    "role": "USER"
  }
  ```

### Login
- **POST** `/users/login`
- **Body:**
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```

### Get User by ID
- **GET** `/users/{id}`

### Get User by Email
- **GET** `/users/email/{email}`

### Get All Users
- **GET** `/users`

### Update User
- **PUT** `/users/{id}`
- **Body:**
  ```json
  {
    "phone": "0987654321",
    "status": "ACTIVE",
    "role": "USER"
  }
  ```

## Booking APIs

### Create Booking
- **POST** `/bookings`
- **Body:**
  ```json
  {
    "userId": "user-id",
    "currency": "VND",
    "flightSegments": [
      {
        "airline": "Vietnam Airlines",
        "flightNumber": "VN123",
        "origin": "HAN",
        "destination": "SGN",
        "departTime": "2024-01-15T10:00:00",
        "arriveTime": "2024-01-15T12:00:00",
        "cabinClass": "ECONOMY",
        "baseFare": 2000000,
        "taxes": 500000
      }
    ],
    "passengers": [
      {
        "fullName": "Nguyen Van A",
        "dateOfBirth": "1990-01-01",
        "gender": "MALE",
        "documentType": "PASSPORT",
        "documentNumber": "A12345678"
      }
    ]
  }
  ```

### Get Booking by ID
- **GET** `/bookings/{id}`

### Get Booking by Code
- **GET** `/bookings/code/{bookingCode}`

### Get Bookings by User ID
- **GET** `/bookings/user/{userId}`

### Update Booking Status
- **PUT** `/bookings/{id}/status`
- **Body:** `"CONFIRMED"` (string)

## Payment APIs

### Create Payment
- **POST** `/payments`
- **Body:**
  ```json
  {
    "bookingId": "booking-id",
    "provider": "VNPAY",
    "amount": 2500000,
    "transactionId": "TXN123456"
  }
  ```

### Get Payment by ID
- **GET** `/payments/{id}`

### Get Payments by Booking ID
- **GET** `/payments/booking/{bookingId}`

### Update Payment Status
- **PUT** `/payments/{id}/status`
- **Body:** `"COMPLETED"` (string)
- **Status values:** PENDING, COMPLETED, FAILED, REFUNDED

## Flight Segment APIs

### Get Segments by Booking ID
- **GET** `/flight-segments/booking/{bookingId}`

### Search Flights
- **GET** `/flight-segments/search?origin=HAN&destination=SGN`

## Seat Selection APIs

### Create Seat Selection
- **POST** `/seat-selections`
- **Body:**
  ```json
  {
    "passengerId": "passenger-id",
    "segmentId": "segment-id",
    "seatNumber": "12A",
    "seatType": "WINDOW",
    "price": 200000
  }
  ```

### Get Seat Selections by Passenger ID
- **GET** `/seat-selections/passenger/{passengerId}`

### Get Seat Selections by Segment ID
- **GET** `/seat-selections/segment/{segmentId}`

## Baggage Service APIs

### Create Baggage Service
- **POST** `/baggage-services?passengerId={id}&segmentId={id}&weightKg=20&price=500000`

### Get Baggage Services by Passenger ID
- **GET** `/baggage-services/passenger/{passengerId}`

### Get Baggage Services by Segment ID
- **GET** `/baggage-services/segment/{segmentId}`

## Response Format

### Success Response
```json
{
  "id": "...",
  "field1": "value1",
  "field2": "value2"
}
```

### Error Response
```json
{
  "message": "Error message",
  "status": "ERROR"
}
```

### Validation Error Response
```json
{
  "fieldName": "Error message",
  "status": "VALIDATION_ERROR"
}
```

