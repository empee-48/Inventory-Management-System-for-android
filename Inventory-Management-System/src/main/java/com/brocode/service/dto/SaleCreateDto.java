package com.brocode.service.dto;

import java.time.LocalDate;
import java.util.List;

public record SaleCreateDto(
        LocalDate saleDate,
        List<SaleItemCreateDto> items,
        Double totalAmount
) {
}
