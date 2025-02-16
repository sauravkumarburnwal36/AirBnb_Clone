package com.explorebnb.clone.airBnbApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelReportDto {
    private Long bookingCounts;
    private BigDecimal totalRevenue;
    private BigDecimal avgRevenue;
}
