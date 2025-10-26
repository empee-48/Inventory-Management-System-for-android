package com.example.inventory.data

data class LoginResponse(
    val token: String,
)

data class UserResponseDto(
    val username: String,
    val roles: String? = null,
    val enabled: Boolean? = null,
): BaseResponseDto()

data class UserCreateDto(
    val username: String,
    val password: String,
    val role: String? = null
)

data class UserEditDto(
    val username: String? = null,
    val email: String? = null,
    val role: String? = null
)

data class PasswordChangeDto(
    val currentPassword: String,
    val newPassword: String
)

data class LoginRequest(
    val username: String,
    val password: String
)