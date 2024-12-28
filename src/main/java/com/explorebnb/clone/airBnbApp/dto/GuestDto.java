package com.explorebnb.clone.airBnbApp.dto;

import com.explorebnb.clone.airBnbApp.entity.User;
import com.explorebnb.clone.airBnbApp.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class GuestDto {
    private Long id;
    private User user;
    private String name;
    private String email;
    private Gender gender;
    private Integer age;
}

