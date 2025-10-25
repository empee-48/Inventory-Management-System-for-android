package com.brocode.service.dto;

import java.time.LocalDateTime;

public record OrderItemResponseDto(
        Long id,
        Long productId,
        Long orderId,
        String productName,
        Double amount,
        Double orderPrice,
        LocalDateTime createdAt,
        LocalDateTime lastModifiedAt,
        String createdBy,
        String lastModifiedBy
) {
}