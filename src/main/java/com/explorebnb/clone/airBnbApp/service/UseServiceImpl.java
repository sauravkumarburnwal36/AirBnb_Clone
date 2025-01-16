package com.explorebnb.clone.airBnbApp.service;

import com.explorebnb.clone.airBnbApp.entity.User;
import com.explorebnb.clone.airBnbApp.exception.ResourceNotFoundException;
import com.explorebnb.clone.airBnbApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UseServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(()->new ResourceNotFoundException("User not found with id:"+userId));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElse(null);
    }
}
