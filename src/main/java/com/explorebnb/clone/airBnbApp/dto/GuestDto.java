package com.explorebnb.clone.airBnbApp.dto;

import com.explorebnb.clone.airBnbApp.entity.User;
import com.explorebnb.clone.airBnbApp.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GuestDto {
    private Long id;
    private String name;
    private Gender gender;
    private LocalDate dateOfBirth;
}

