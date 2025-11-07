package com.example.inventory.data

import java.time.LocalDate

data class SalesResponseDto(
    val saleId: String,
    val saleDate: LocalDate,
    val items: List<SaleItemResponseDto>,
    val totalAmount: Double,

): BaseResponseDto()

data class SaleItemResponseDto(
    val productId: Long,
    val saleId: Long,
    val batchId: Long,
    val productName: String,
    val amount: Double,
    val price: Double
): BaseResponseDto()

data class SaleItemCreateDto(
    val productId: Long,
    val batchId: Long,
    val amount: Double,
    val price: Double
)

data class SaleCreateDto(
    val saleDate: LocalDate,
    val items: List<SaleItemCreateDto>,
    val totalAmount: Double
)
