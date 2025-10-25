package com.brocode.service.dto;

import java.util.Set;

public record UserEditDto(
        String email,
        Set<String> roles,
        boolean isEnabled
) {
}
