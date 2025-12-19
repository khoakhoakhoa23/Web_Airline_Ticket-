package com.flightbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatLockDTO {
    private String id;
    private String flightNumber;
    private String segmentId;
    private String seatNumber;
    private String userId;
    private String sessionId;
    private LocalDateTime lockedAt;
    private LocalDateTime expiresAt;
    private String status;
    private String bookingId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean expired;
    private boolean active;
}

