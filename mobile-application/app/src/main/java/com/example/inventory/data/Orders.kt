package com.example.inventory.data

import java.time.LocalDate

data class OrderCreateDto(
    val supplierId: Long,
    val orderDate: LocalDate,
    val items: List<OrderItemCreateDto>,
    val totalAmount: Double
)

data class OrderItemCreateDto(
    val productId: Long,
    val amount: Int,
    val orderPrice: Double
)

data class OrderResponseDto(
    val supplierId: Long,
    val orderId: String,
    val orderDate: LocalDate,
    val items: List<OrderItemResponseDto>,
    val totalAmount: Double
): BaseResponseDto()

data class OrderItemResponseDto(
    val productId: Long,
    val orderId: Long,
    val productName: String,
    val amount: Double,
    val orderPrice: Double
): BaseResponseDto()