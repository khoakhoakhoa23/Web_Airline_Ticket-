-- Seed data for flights table
-- Run this after creating the flights table

-- Create flights table if not exists
CREATE TABLE IF NOT EXISTS flights (
    id VARCHAR(255) PRIMARY KEY,
    flight_number VARCHAR(255) NOT NULL,
    airline VARCHAR(255) NOT NULL,
    origin VARCHAR(10) NOT NULL,
    destination VARCHAR(10) NOT NULL,
    depart_time TIMESTAMP NOT NULL,
    arrive_time TIMESTAMP NOT NULL,
    cabin_class VARCHAR(255) NOT NULL,
    base_fare DECIMAL(10, 2),
    taxes DECIMAL(10, 2),
    available_seats INTEGER,
    total_seats INTEGER,
    status VARCHAR(255),
    aircraft_type VARCHAR(255),
    duration_minutes INTEGER,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Clear existing data
TRUNCATE TABLE flights CASCADE;

-- Insert sample flights
-- HAN (Hanoi) -> SGN (Ho Chi Minh City)
INSERT INTO flights (id, flight_number, airline, origin, destination, depart_time, arrive_time, cabin_class, base_fare, taxes, available_seats, total_seats, status, aircraft_type, duration_minutes, created_at, updated_at)
VALUES
('flight-001', 'VN123', 'Vietnam Airlines', 'HAN', 'SGN', '2025-01-20 06:00:00', '2025-01-20 08:15:00', 'ECONOMY', 2000000, 500000, 150, 180, 'SCHEDULED', 'A321', 135, NOW(), NOW()),
('flight-002', 'VJ456', 'VietJet Air', 'HAN', 'SGN', '2025-01-20 08:30:00', '2025-01-20 10:45:00', 'ECONOMY', 1500000, 400000, 120, 180, 'SCHEDULED', 'A320', 135, NOW(), NOW()),
('flight-003', 'BB789', 'Bamboo Airways', 'HAN', 'SGN', '2025-01-20 14:00:00', '2025-01-20 16:15:00', 'ECONOMY', 1800000, 450000, 100, 150, 'SCHEDULED', 'A321neo', 135, NOW(), NOW()),
('flight-004', 'VN125', 'Vietnam Airlines', 'HAN', 'SGN', '2025-01-20 18:00:00', '2025-01-20 20:15:00', 'ECONOMY', 2200000, 550000, 130, 180, 'SCHEDULED', 'A321', 135, NOW(), NOW()),

-- SGN (Ho Chi Minh City) -> HAN (Hanoi)
('flight-005', 'VN124', 'Vietnam Airlines', 'SGN', 'HAN', '2025-01-20 09:00:00', '2025-01-20 11:15:00', 'ECONOMY', 2000000, 500000, 140, 180, 'SCHEDULED', 'A321', 135, NOW(), NOW()),
('flight-006', 'VJ457', 'VietJet Air', 'SGN', 'HAN', '2025-01-20 11:30:00', '2025-01-20 13:45:00', 'ECONOMY', 1500000, 400000, 110, 180, 'SCHEDULED', 'A320', 135, NOW(), NOW()),
('flight-007', 'BB790', 'Bamboo Airways', 'SGN', 'HAN', '2025-01-20 16:00:00', '2025-01-20 18:15:00', 'ECONOMY', 1800000, 450000, 95, 150, 'SCHEDULED', 'A321neo', 135, NOW(), NOW()),

-- HAN (Hanoi) -> DAD (Da Nang)
('flight-008', 'VN234', 'Vietnam Airlines', 'HAN', 'DAD', '2025-01-20 07:00:00', '2025-01-20 08:30:00', 'ECONOMY', 1200000, 300000, 130, 150, 'SCHEDULED', 'A320', 90, NOW(), NOW()),
('flight-009', 'VJ567', 'VietJet Air', 'HAN', 'DAD', '2025-01-20 15:00:00', '2025-01-20 16:30:00', 'ECONOMY', 1000000, 250000, 100, 150, 'SCHEDULED', 'A320', 90, NOW(), NOW()),
('flight-010', 'BB891', 'Bamboo Airways', 'HAN', 'DAD', '2025-01-20 19:00:00', '2025-01-20 20:30:00', 'ECONOMY', 1100000, 280000, 85, 150, 'SCHEDULED', 'A321', 90, NOW(), NOW()),

-- DAD (Da Nang) -> HAN (Hanoi)
('flight-011', 'VN235', 'Vietnam Airlines', 'DAD', 'HAN', '2025-01-20 09:00:00', '2025-01-20 10:30:00', 'ECONOMY', 1200000, 300000, 125, 150, 'SCHEDULED', 'A320', 90, NOW(), NOW()),
('flight-012', 'VJ568', 'VietJet Air', 'DAD', 'HAN', '2025-01-20 17:00:00', '2025-01-20 18:30:00', 'ECONOMY', 1000000, 250000, 95, 150, 'SCHEDULED', 'A320', 90, NOW(), NOW()),

-- SGN (Ho Chi Minh City) -> DAD (Da Nang)
('flight-013', 'VN345', 'Vietnam Airlines', 'SGN', 'DAD', '2025-01-20 08:00:00', '2025-01-20 09:20:00', 'ECONOMY', 1100000, 280000, 140, 160, 'SCHEDULED', 'A320', 80, NOW(), NOW()),
('flight-014', 'VJ678', 'VietJet Air', 'SGN', 'DAD', '2025-01-20 14:00:00', '2025-01-20 15:20:00', 'ECONOMY', 900000, 230000, 120, 160, 'SCHEDULED', 'A320', 80, NOW(), NOW()),

-- DAD (Da Nang) -> SGN (Ho Chi Minh City)
('flight-015', 'VN346', 'Vietnam Airlines', 'DAD', 'SGN', '2025-01-20 10:00:00', '2025-01-20 11:20:00', 'ECONOMY', 1100000, 280000, 135, 160, 'SCHEDULED', 'A320', 80, NOW(), NOW()),
('flight-016', 'VJ679', 'VietJet Air', 'DAD', 'SGN', '2025-01-20 16:00:00', '2025-01-20 17:20:00', 'ECONOMY', 900000, 230000, 115, 160, 'SCHEDULED', 'A320', 80, NOW(), NOW()),

-- HAN (Hanoi) -> HPH (Hai Phong)
('flight-017', 'VN890', 'Vietnam Airlines', 'HAN', 'HPH', '2025-01-20 10:00:00', '2025-01-20 10:40:00', 'ECONOMY', 500000, 150000, 60, 70, 'SCHEDULED', 'ATR72', 40, NOW(), NOW()),
('flight-018', 'BB992', 'Bamboo Airways', 'HAN', 'HPH', '2025-01-20 17:00:00', '2025-01-20 17:40:00', 'ECONOMY', 480000, 140000, 55, 70, 'SCHEDULED', 'ATR72', 40, NOW(), NOW()),

-- Business Class flights
('flight-019', 'VN901', 'Vietnam Airlines', 'HAN', 'SGN', '2025-01-20 12:00:00', '2025-01-20 14:15:00', 'BUSINESS', 5000000, 800000, 24, 30, 'SCHEDULED', 'B787', 135, NOW(), NOW()),
('flight-020', 'VN902', 'Vietnam Airlines', 'SGN', 'HAN', '2025-01-20 15:00:00', '2025-01-20 17:15:00', 'BUSINESS', 5000000, 800000, 20, 30, 'SCHEDULED', 'B787', 135, NOW(), NOW());

-- Add flights for tomorrow (2025-01-21)
INSERT INTO flights (id, flight_number, airline, origin, destination, depart_time, arrive_time, cabin_class, base_fare, taxes, available_seats, total_seats, status, aircraft_type, duration_minutes, created_at, updated_at)
VALUES
('flight-021', 'VN123', 'Vietnam Airlines', 'HAN', 'SGN', '2025-01-21 06:00:00', '2025-01-21 08:15:00', 'ECONOMY', 2000000, 500000, 150, 180, 'SCHEDULED', 'A321', 135, NOW(), NOW()),
('flight-022', 'VJ456', 'VietJet Air', 'HAN', 'SGN', '2025-01-21 08:30:00', '2025-01-21 10:45:00', 'ECONOMY', 1500000, 400000, 120, 180, 'SCHEDULED', 'A320', 135, NOW(), NOW()),
('flight-023', 'VN124', 'Vietnam Airlines', 'SGN', 'HAN', '2025-01-21 09:00:00', '2025-01-21 11:15:00', 'ECONOMY', 2000000, 500000, 140, 180, 'SCHEDULED', 'A321', 135, NOW(), NOW()),
('flight-024', 'VJ457', 'VietJet Air', 'SGN', 'HAN', '2025-01-21 11:30:00', '2025-01-21 13:45:00', 'ECONOMY', 1500000, 400000, 110, 180, 'SCHEDULED', 'A320', 135, NOW(), NOW());

-- Verify data
SELECT COUNT(*) as total_flights FROM flights;
SELECT origin, destination, COUNT(*) as flight_count 
FROM flights 
GROUP BY origin, destination 
ORDER BY flight_count DESC;

