package com.example.inventory.data

data class CategoryResponseDto(
    val name: String,
    val products: List<ProductResponseDto>
): BaseResponseDto()


data class CategoryCreateDto(
    val name: String
)