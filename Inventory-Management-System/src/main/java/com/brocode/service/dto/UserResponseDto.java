package com.brocode.service.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponseDto(
        Long id,
        String username,
        String email,
        Set<String> roles,
        boolean isEnabled,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime lastModifiedAt,
        String lastModifiedBy
) {
}
