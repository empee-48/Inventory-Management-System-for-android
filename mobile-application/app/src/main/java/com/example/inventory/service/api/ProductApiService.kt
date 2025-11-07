package com.example.inventory.service.api

import com.example.inventory.data.ProductCreateDto
import com.example.inventory.data.ProductResponseDto
import retrofit2.Response
import retrofit2.http.*

interface ProductApiService {

    @GET("inventory/api/products")
    suspend fun getProducts(
        @Query("id") id: Long? = null,
        @Query("productName") productName: String? = null,
        @Query("categoryId") categoryId: Long? = null,
        @Query("lowStock") lowStock: Boolean? = null,
        @Query("lowerBoundaryPrice") lowerBoundaryPrice: Double? = null,
        @Query("higherBoundaryPrice") higherBoundaryPrice: Double? = null
    ): Response<List<ProductResponseDto>>

    // Get single product by ID
    @GET("inventory/api/products")
    suspend fun getProductById(
        @Query("id") id: Long
    ): Response<ProductResponseDto>

    // Create new product
    @POST("inventory/api/products")
    suspend fun createProduct(
        @Body productCreateDto: ProductCreateDto
    ): Response<ProductResponseDto>

    // Edit existing product
    @PUT("inventory/api/products")
    suspend fun editProduct(
        @Query("id") id: Long,
        @Body productCreateDto: ProductCreateDto
    ): Response<ProductResponseDto>

    // Delete single product
    @DELETE("inventory/api/products")
    suspend fun deleteProduct(
        @Query("id") id: Long
    ): Response<Void>

    // Delete all products
    @DELETE("inventory/api/products/all")
    suspend fun deleteAllProducts(): Response<Void>
}