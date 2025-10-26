package com.brocode.service.dto;

public record ErrorResponseDto(
        String message,
        int status
) {
}
