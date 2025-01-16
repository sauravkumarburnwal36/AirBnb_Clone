package com.explorebnb.clone.airBnbApp.controller;

import com.explorebnb.clone.airBnbApp.dto.LogInRequestDto;
import com.explorebnb.clone.airBnbApp.dto.LogInResponseDto;
import com.explorebnb.clone.airBnbApp.dto.SignUpRequestDto;
import com.explorebnb.clone.airBnbApp.dto.UserDto;
import com.explorebnb.clone.airBnbApp.security.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody SignUpRequestDto signUpRequestDto){
        return new ResponseEntity<>(authService.signup(signUpRequestDto),HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LogInResponseDto> login(@RequestBody LogInRequestDto logInRequestDto, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        String token[]=authService.login(logInRequestDto);
        Cookie cookie=new Cookie("refreshToken",token[1]);
        cookie.setHttpOnly(true);
        httpServletResponse.addCookie(cookie);
        return ResponseEntity.ok(new LogInResponseDto(token[0]));
    }
    @PostMapping("/refresh")
    public ResponseEntity<LogInResponseDto> refresh(HttpServletRequest request){
        String refreshToken= Arrays.stream(request.getCookies())
                .filter(cookie->"refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(cookie->cookie.getValue())
                .orElseThrow(()->new AuthenticationServiceException("Refresh token doesn't exist"));
        String accessToken=authService.refresh(refreshToken);
        return ResponseEntity.ok(new LogInResponseDto(accessToken));
    }
}
