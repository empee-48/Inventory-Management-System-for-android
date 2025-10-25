package com.brocode.service;

import com.brocode.entity.User;
import com.brocode.service.dto.UserCreateDto;
import com.brocode.service.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class UserMapper {
    private final PasswordEncoder passwordEncoder;

    public User createToUser(UserCreateDto dto){
        return User.builder()
                .username(dto.username())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .roles(new HashSet<>(Collections.singleton("ROLE_USER")))
                .build();
    }

    public UserResponseDto userToResponse(User user){
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getCreatedBy(),
                user.getLastModifiedAt(),
                user.getLastModifiedBy()
        );
    }

}
