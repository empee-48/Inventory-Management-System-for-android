package com.example.inventory.service.api

import com.example.inventory.data.LoginResponse
import com.example.inventory.data.PasswordChangeDto
import com.example.inventory.data.UserCreateDto
import com.example.inventory.data.UserEditDto
import com.example.inventory.data.UserResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

const val BASE_URL = "/inventory/api"
interface UserApiService {


    @POST("auth/token")
    suspend fun loginWithBasicAuth(): Response<LoginResponse>

    // Get users with optional filters
    @GET("$BASE_URL/users")
    suspend fun getUsers(
        @Query("id") id: Long? = null,
        @Query("username") username: String? = null
    ): Response<List<UserResponseDto>>

    // Create new user
    @POST("$BASE_URL/users")
    suspend fun createUser(
        @Body userCreateDto: UserCreateDto
    ): Response<UserResponseDto>  // Directly returns UserResponseDto

    // Edit user - returns 202 Accepted with no body
    @PUT("$BASE_URL/users")
    suspend fun editUser(
        @Body userEditDto: UserEditDto,
        @Query("username") username: String
    ): Response<Void>  // No response body, just status code

    // Delete user - returns 204 No Content
    @DELETE("$BASE_URL/users")
    suspend fun deleteUser(
        @Query("id") id: Long
    ): Response<Void>  // No response body

    // Get current user
    @GET("$BASE_URL/users/current")
    suspend fun getCurrentUser(): Response<UserResponseDto>

    // Change password - returns 202 Accepted
    @PUT("$BASE_URL/users/change-password")
    suspend fun changePassword(
        @Body passwordChangeDto: PasswordChangeDto,
        @Query("username") username: String
    ): Response<Void>

    // Reset password (admin only)
    @PATCH("$BASE_URL/users/reset-password")
    suspend fun resetPassword(
        @Query("username") username: String
    ): Response<Void>

    // Enable user
    @PUT("$BASE_URL/users/enable")
    suspend fun enableUser(
        @Query("id") id: Long? = null
    ): Response<Void>
}