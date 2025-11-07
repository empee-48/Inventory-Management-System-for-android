package com.example.inventory.service.api

import com.example.inventory.data.CategoryCreateDto
import com.example.inventory.data.CategoryResponseDto
import retrofit2.Response
import retrofit2.http.*

interface CategoryApiService {

    @GET("inventory/api/categories")
    suspend fun getCategories(
        @Query("id") id: Long? = null,
        @Query("categoryName") categoryName: String? = null
    ): Response<List<CategoryResponseDto>>

    @GET("inventory/api/categories")
    suspend fun getCategoryById(
        @Query("id") id: Long
    ): Response<CategoryResponseDto>

    @POST("inventory/api/categories")
    suspend fun createCategory(
        @Body categoryCreateDto: CategoryCreateDto
    ): Response<CategoryResponseDto>

    @PUT("inventory/api/categories")
    suspend fun editCategory(
        @Query("id") id: Long,
        @Body categoryCreateDto: CategoryCreateDto
    ): Response<CategoryResponseDto>

    @DELETE("inventory/api/categories")
    suspend fun deleteCategory(
        @Query("id") id: Long
    ): Response<Void>

    @DELETE("inventory/api/categories/all")
    suspend fun deleteAllCategories(): Response<Void>
}