package com.brocode.service.dto;

import java.time.LocalDateTime;

public record ProductResponseDto(
        Long id,
        Long categoryId,
        String categoryName,
        String productKey,
        String name,
        String description,
        Double price,
        Double inStock,
        Double warningStockLevel,
        boolean stockLevelLow,
        String unit,
        LocalDateTime createdAt,
        LocalDateTime lastModifiedAt,
        String createdBy,
        String lastModifiedBy
) {
}
