package com.flightbooking.config;

import com.flightbooking.entity.Flight;
import com.flightbooking.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Seeder
 * 
 * Seeds sample flight data on application startup
 * - Only seeds if database is empty
 * - Creates flights for multiple routes
 * - Various airlines, prices, times
 * - Covers next 7 days
 */
@Configuration
public class DataSeeder {
    
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    
    @Bean
    public CommandLineRunner seedFlightData(FlightRepository flightRepository) {
        return args -> {
            // Check if flights already exist
            if (flightRepository.count() > 0) {
                logger.info("Flights already seeded. Skipping data seeding.");
                return;
            }
            
            logger.info("Seeding flight data...");
            
            List<Flight> flights = new ArrayList<>();
            
            // Routes
            String[][] routes = {
                    {"SGN", "HAN"}, // Saigon -> Hanoi
                    {"HAN", "SGN"}, // Hanoi -> Saigon
                    {"SGN", "DAD"}, // Saigon -> Da Nang
                    {"DAD", "SGN"}, // Da Nang -> Saigon
                    {"HAN", "DAD"}, // Hanoi -> Da Nang
                    {"DAD", "HAN"}  // Da Nang -> Hanoi
            };
            
            // Airlines
            String[] airlines = {
                    "Vietnam Airlines",
                    "VietJet Air",
                    "Bamboo Airways"
            };
            
            // Cabin classes with base multipliers
            Object[][] cabinClasses = {
                    {"ECONOMY", 1.0},
                    {"BUSINESS", 2.5},
                    {"FIRST", 4.0}
            };
            
            // Generate flights for next 7 days
            LocalDateTime now = LocalDateTime.now();
            for (int day = 0; day < 7; day++) {
                LocalDateTime flightDate = now.plusDays(day);
                
                // For each route
                for (String[] route : routes) {
                    String origin = route[0];
                    String destination = route[1];
                    
                    // Calculate base fare based on route
                    BigDecimal baseFare = calculateBaseFare(origin, destination);
                    
                    // Calculate duration based on route
                    int duration = calculateDuration(origin, destination);
                    
                    // For each airline
                    for (String airline : airlines) {
                        // For each cabin class
                        for (Object[] cabinData : cabinClasses) {
                            String cabinClass = (String) cabinData[0];
                            double multiplier = (double) cabinData[1];
                            
                            // Morning flight (6:00-8:00)
                            flights.add(createFlight(
                                    airline,
                                    origin,
                                    destination,
                                    flightDate.withHour(6 + (int)(Math.random() * 2)).withMinute((int)(Math.random() * 60)),
                                    duration,
                                    baseFare.multiply(BigDecimal.valueOf(multiplier)),
                                    cabinClass
                            ));
                            
                            // Afternoon flight (12:00-14:00)
                            flights.add(createFlight(
                                    airline,
                                    origin,
                                    destination,
                                    flightDate.withHour(12 + (int)(Math.random() * 2)).withMinute((int)(Math.random() * 60)),
                                    duration,
                                    baseFare.multiply(BigDecimal.valueOf(multiplier * 1.1)), // Slightly higher
                                    cabinClass
                            ));
                            
                            // Evening flight (18:00-20:00)
                            flights.add(createFlight(
                                    airline,
                                    origin,
                                    destination,
                                    flightDate.withHour(18 + (int)(Math.random() * 2)).withMinute((int)(Math.random() * 60)),
                                    duration,
                                    baseFare.multiply(BigDecimal.valueOf(multiplier * 1.2)), // Higher for evening
                                    cabinClass
                            ));
                        }
                    }
                }
            }
            
            // Save all flights
            flightRepository.saveAll(flights);
            logger.info("Seeded {} flights successfully", flights.size());
        };
    }
    
    private Flight createFlight(String airline, String origin, String destination, 
                                LocalDateTime departTime, int durationMinutes, 
                                BigDecimal baseFare, String cabinClass) {
        Flight flight = new Flight();
        
        flight.setId(UUID.randomUUID().toString());
        flight.setFlightNumber(generateFlightNumber(airline));
        flight.setAirline(airline);
        flight.setOrigin(origin);
        flight.setDestination(destination);
        flight.setDepartTime(departTime);
        flight.setArriveTime(departTime.plusMinutes(durationMinutes));
        flight.setCabinClass(cabinClass);
        
        // Set prices
        flight.setBaseFare(baseFare);
        flight.setTaxes(baseFare.multiply(BigDecimal.valueOf(0.2))); // 20% tax
        
        // Set seats based on cabin class
        int totalSeats = switch (cabinClass) {
            case "ECONOMY" -> 180;
            case "BUSINESS" -> 40;
            case "FIRST" -> 12;
            default -> 150;
        };
        
        flight.setTotalSeats(totalSeats);
        flight.setAvailableSeats((int)(totalSeats * (0.5 + Math.random() * 0.5))); // 50-100% available
        
        flight.setStatus("SCHEDULED");
        flight.setAircraftType(getAircraftType(airline));
        flight.setDurationMinutes(durationMinutes);
        
        return flight;
    }
    
    private String generateFlightNumber(String airline) {
        String code = switch (airline) {
            case "Vietnam Airlines" -> "VN";
            case "VietJet Air" -> "VJ";
            case "Bamboo Airways" -> "QH";
            default -> "XX";
        };
        return code + (100 + (int)(Math.random() * 900));
    }
    
    private BigDecimal calculateBaseFare(String origin, String destination) {
        // Base fares in VND
        String route = origin + "-" + destination;
        return switch (route) {
            case "SGN-HAN", "HAN-SGN" -> new BigDecimal("1500000"); // 1.5M VND
            case "SGN-DAD", "DAD-SGN" -> new BigDecimal("800000");  // 800K VND
            case "HAN-DAD", "DAD-HAN" -> new BigDecimal("900000");  // 900K VND
            default -> new BigDecimal("1000000"); // 1M VND default
        };
    }
    
    private int calculateDuration(String origin, String destination) {
        // Flight duration in minutes
        String route = origin + "-" + destination;
        return switch (route) {
            case "SGN-HAN", "HAN-SGN" -> 120; // 2 hours
            case "SGN-DAD", "DAD-SGN" -> 80;  // 1h 20min
            case "HAN-DAD", "DAD-HAN" -> 70;  // 1h 10min
            default -> 90; // 1.5 hours default
        };
    }
    
    private String getAircraftType(String airline) {
        return switch (airline) {
            case "Vietnam Airlines" -> "Boeing 787";
            case "VietJet Air" -> "Airbus A320";
            case "Bamboo Airways" -> "Airbus A321";
            default -> "Boeing 737";
        };
    }
}

