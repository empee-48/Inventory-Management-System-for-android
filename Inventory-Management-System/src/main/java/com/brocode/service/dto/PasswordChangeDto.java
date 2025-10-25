package com.brocode.service.dto;

public record PasswordChangeDto(
        String currentPassword,
        String newPassword
) {
}
