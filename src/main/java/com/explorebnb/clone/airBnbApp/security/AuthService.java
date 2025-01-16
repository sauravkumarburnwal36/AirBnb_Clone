package com.explorebnb.clone.airBnbApp.security;

import com.explorebnb.clone.airBnbApp.dto.LogInRequestDto;
import com.explorebnb.clone.airBnbApp.dto.SignUpRequestDto;
import com.explorebnb.clone.airBnbApp.dto.UserDto;
import com.explorebnb.clone.airBnbApp.entity.User;
import com.explorebnb.clone.airBnbApp.entity.enums.Role;
import com.explorebnb.clone.airBnbApp.exception.ResourceNotFoundException;
import com.explorebnb.clone.airBnbApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserDto signup(SignUpRequestDto signUpRequestDto) {
        User user = userRepository.findByEmail(signUpRequestDto.getEmail()).orElse(null);
        if (user!=null){
            throw new RuntimeException("User with email already present.");
        }
        User newUser=modelMapper.map(signUpRequestDto,User.class);
        newUser.setRoles(Set.of(Role.GUEST));
        newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
        newUser=userRepository.save(newUser);
        return modelMapper.map(newUser,UserDto.class);
    }

    public String[] login(LogInRequestDto logInRequestDto){
        Authentication authentication=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                logInRequestDto.getEmail(),logInRequestDto.getPassword())
        );
        User user=(User) authentication.getPrincipal();
        String[] arr=new String[2];
        arr[0]=jwtService.generateAccessToken(user);
        arr[1]=jwtService.generateRefreshToken(user);
        return arr;
    }

    public String refresh(String refreshToken){
        Long id= jwtService.getUserIdFromToken(refreshToken);
        User user=userRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("User not found with id:"+id));
        return jwtService.generateAccessToken(user);
    }
}