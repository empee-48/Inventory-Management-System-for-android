package com.brocode.service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDto(
        Long id,
        Long supplierId,
        String orderId,
        LocalDate orderDate,
        List<OrderItemResponseDto> items,
        Double totalAmount,
        LocalDateTime createdAt,
        LocalDateTime lastModifiedAt,
        String createdBy,
        String lastModifiedBy
) {
}
