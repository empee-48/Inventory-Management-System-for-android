package com.brocode.service.dto;

import com.brocode.utils.Activity;

import java.time.LocalDateTime;

public record ActivityCreateDto(
        String description,
        String username,
        LocalDateTime timeStamp,
        Activity activity
) {
}
