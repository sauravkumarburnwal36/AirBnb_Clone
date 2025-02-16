package com.explorebnb.clone.airBnbApp.service;

import com.explorebnb.clone.airBnbApp.dto.ProfileUpdateRequestDto;
import com.explorebnb.clone.airBnbApp.dto.UserDto;
import com.explorebnb.clone.airBnbApp.entity.User;

public interface UserService {
    User getUserById(Long userId);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);
    UserDto getMyProfile();

}
