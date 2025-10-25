package com.brocode.service.dto;

import java.util.Set;

public record UserCreateDto(
        String username,
        String email,
        String password,
        Set<String> roles
) {
}
