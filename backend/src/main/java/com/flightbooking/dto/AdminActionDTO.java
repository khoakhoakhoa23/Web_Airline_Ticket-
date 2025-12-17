package com.flightbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminActionDTO {
    private String id;
    private String bookingId;
    private String adminId;
    private String action;
    private String note;
    private LocalDateTime createdAt;
}

