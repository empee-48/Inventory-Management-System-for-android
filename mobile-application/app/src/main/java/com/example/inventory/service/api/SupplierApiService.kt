package com.example.inventory.service.api

import com.example.inventory.data.SupplierCreateDto
import com.example.inventory.data.SupplierResponseDto
import retrofit2.Response
import retrofit2.http.*

interface SuppliersApiService {

    @GET("inventory/api/suppliers")
    suspend fun getSuppliers(
        @Query("id") id: Long? = null
    ): Response<List<SupplierResponseDto>>

    // Get single supplier by ID
    @GET("inventory/api/suppliers")
    suspend fun getSupplierById(
        @Query("id") id: Long
    ): Response<SupplierResponseDto>

    // Create new supplier
    @POST("inventory/api/suppliers")
    suspend fun createSupplier(
        @Body supplierCreateDto: SupplierCreateDto
    ): Response<SupplierResponseDto>

    // Edit existing supplier
    @PUT("inventory/api/suppliers")
    suspend fun editSupplier(
        @Query("id") id: Long,
        @Body supplierCreateDto: SupplierCreateDto
    ): Response<SupplierResponseDto>

    // Delete single supplier
    @DELETE("inventory/api/suppliers")
    suspend fun deleteSupplier(
        @Query("id") id: Long
    ): Response<Void>

    // Delete all suppliers
    @DELETE("inventory/api/suppliers/all")
    suspend fun deleteAllSuppliers(): Response<Void>
}