package com.brocode.service.dto;

import java.time.LocalDateTime;

public record SaleItemResponseDto(
        Long id,
        Long productId,
        Long batchId,
        Long saleId,
        String productName,
        Double amount,
        Double price,
        LocalDateTime createdAt,
        LocalDateTime lastModifiedAt,
        String createdBy,
        String lastModifiedBy
) {
}
