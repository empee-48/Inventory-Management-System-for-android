package com.brocode.service.dto;

public record SaleItemCreateDto(
            Long productId,
            Long batchId,
            Double amount,
            Double price
) {
}
