package com.flightbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaggageServiceDTO {
    private String id;
    private String passengerId;
    private String segmentId;
    private Integer weightKg;
    private BigDecimal price;
}

