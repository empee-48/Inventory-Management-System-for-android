package com.brocode.service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CategoryResponseDto(
        Long id,
        String name,
        List<ProductResponseDto> products,
        LocalDateTime createdAt,
        LocalDateTime lastModifiedAt,
        String createdBy,
        String lastModifiedBy
){
}
