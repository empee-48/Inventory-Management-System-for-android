package com.brocode.service.dto;

import java.time.LocalDate;
import java.util.List;

public record OrderCreateDto(
        Long supplierId,
        LocalDate orderDate,
        List<OrderItemCreateDto> items,
        Double totalAmount
) {
    
}
