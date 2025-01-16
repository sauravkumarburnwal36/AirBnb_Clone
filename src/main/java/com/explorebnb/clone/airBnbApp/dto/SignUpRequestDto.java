package com.explorebnb.clone.airBnbApp.dto;

import lombok.*;

@Data
public class SignUpRequestDto {
    private String email;
    private String password;
    private String name;
}
