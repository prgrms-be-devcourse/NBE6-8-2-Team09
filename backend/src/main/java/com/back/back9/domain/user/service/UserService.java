package com.back.back9.domain.user.service;

import com.back.back9.domain.user.dto.UserDto;
import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean register(UserDto dto) {
        if (userRepository.existsByUsername(dto.getUsername()) ||
                userRepository.existsByUserLoginId(dto.getUserLoginId())) return false;
        if (!dto.getPassword().equals(dto.getConfirmPassword())) return false;

        User user = User.builder()
                .userLoginId(dto.getUserLoginId())
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role("MEMBER")
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String userLoginId) throws UsernameNotFoundException {
        User user = userRepository.findByUserLoginId(userLoginId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userLoginId));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUserLoginId())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole())
                .build();
    }
}
