package com.brocode.service.dto;

public record ProductCreateDto(
        Long categoryId,
        String name,
        String description,
        Double price,
        Double inStock,
        Double warningStockLevel,
        String unit
){
}
