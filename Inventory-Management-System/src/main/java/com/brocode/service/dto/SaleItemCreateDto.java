package com.brocode.service.dto;

public record SaleItemCreateDto(
        Long productId,
        Double amount,
        Double price
) {
}
