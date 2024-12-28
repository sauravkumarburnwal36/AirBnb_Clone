package com.explorebnb.clone.airBnbApp.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HotelSearchRequestDto {
    private String city;
    private LocalDate startDate;
    private LocalDate endDate;

    private Integer roomCounts;

    private Integer pageNumber=0;
    private Integer size=10;
}
