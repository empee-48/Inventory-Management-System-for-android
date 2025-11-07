package com.example.inventory.data

data class SupplierResponseDto(
    val name: String,
    val contact: String,
    val contactName: String,
    val address: String,
    val orders: List<OrderResponseDto>
): BaseResponseDto()

data class SupplierCreateDto(
    val name: String,
    val contact: String,
    val contactName: String,
    val address: String
)