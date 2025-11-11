package com.example.inventory.data

import java.time.LocalDate

data class BatchResponseDto(
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val sales: List<SaleItemResponseDto>,
    val stockLeft: Double,
    val orderDate: LocalDate,
    val orderPrice: Double
): BaseResponseDto()