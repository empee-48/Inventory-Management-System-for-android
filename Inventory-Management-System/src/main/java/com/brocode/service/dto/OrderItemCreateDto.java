package com.brocode.service.dto;

public record OrderItemCreateDto(
        Long productId,
        Double amount,
        Double orderPrice
) {
}