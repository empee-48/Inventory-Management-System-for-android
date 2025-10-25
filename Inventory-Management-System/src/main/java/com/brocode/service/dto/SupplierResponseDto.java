package com.brocode.service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SupplierResponseDto(
    Long id,
    String name,
    String contact,
    String address,
    String contactPerson,
    List<OrderResponseDto> orders,
    LocalDateTime createdAt,
    LocalDateTime lastModifiedAt,
    String createdBy,
    String lastModifiedBy
) {
}
