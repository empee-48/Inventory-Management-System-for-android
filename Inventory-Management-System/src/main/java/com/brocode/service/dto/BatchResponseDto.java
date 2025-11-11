package com.brocode.service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BatchResponseDto(
        Long id,
        Long orderId,
        Long productId,
        String productName,
        List<SaleItemResponseDto> sales,
        Double stockLeft,
        LocalDate orderDate,
        Double orderPrice,
        String createdBy,
        String lastModifiedBy,
        LocalDateTime createdAt,
        LocalDateTime lastModifiedAt
) {
}
