package com.example.inventory.data

data class ProductResponseDto(
    val categoryId: Long,
    val categoryName: String,
    val productKey: String,
    val name: String,
    val description: String,
    val price: Double,
    val inStock: Double,
    val warningStockLevel: Double,
    val stockLevelLow: Boolean,
    val unit: String,
): BaseResponseDto()

data class ProductCreateDto(
    val categoryId: Long,
    val name: String,
    val description: String,
    val price: Double,
    val inStock: Double,
    val warningStockLevel: Double,
    val unit: String,
)