package com.brocode.service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record SaleResponseDto(
        Long id,
        String saleId,
        LocalDate saleDate,
        List<SaleItemResponseDto> items,
        Double totalAmount,
        LocalDateTime createdAt,
        LocalDateTime lastModifiedAt,
        String createdBy,
        String lastModifiedBy
) {
}
