package com.example.inventory.data

import java.time.LocalDateTime

data class ErrorResponseDto(
    val status: String,
    val message: String
)

open class BaseResponseDto(
    open val id: Long = 0,
    open val createdAt: LocalDateTime? = null,
    open val lastModifiedAt: LocalDateTime? = null,
    open val createdBy: String? = null,
    open val lastModifiedBy: String? = null
)

data class ActivityResponseDto (
    val activity: String,
    val description: String,
): BaseResponseDto()
