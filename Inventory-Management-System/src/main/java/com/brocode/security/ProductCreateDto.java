package com.brocode.security;

import com.brocode.entity.Category;

public record ProductCreateDto(
        String name,
        String description,
        Category category,
        Double price,
        Double warningStockLevel,
        Double inStock,
        String unit
) {
}