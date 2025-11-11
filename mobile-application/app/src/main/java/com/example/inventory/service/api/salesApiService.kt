package com.example.inventory.service.api

import com.example.inventory.data.SaleCreateDto
import com.example.inventory.data.SaleItemResponseDto
import com.example.inventory.data.SalesResponseDto
import retrofit2.Response
import retrofit2.http.*
import java.time.LocalDate

interface SalesApiService {

    @GET("inventory/api/sales")
    suspend fun getSales(
        @Query("id") id: Long? = null,
        @Query("categoryId") categoryId: Long? = null,
        @Query("saleDateStart") saleDateStart: LocalDate? = null,
        @Query("saleDateEnd") saleDateEnd: LocalDate? = null,
        @Query("lowerSalesBoundary") lowerSalesBoundary: Double? = null,
        @Query("higherSalesBoundary") higherSalesBoundary: Double? = null
    ): Response<List<SalesResponseDto>>

    @GET("inventory/api/sales/items")
    suspend fun getSaleItems(
        @Query("id") id: Long? = null,
        @Query("productId") productId: Long? = null,
        @Query("batchId") batchId: Long? = null,
    ): Response<List<SaleItemResponseDto>>

    @POST("inventory/api/sales")
    suspend fun createSale(
        @Body saleCreateDto: SaleCreateDto
    ): Response<SalesResponseDto>

    @PUT("inventory/api/sales")
    suspend fun editSale(
        @Query("id") id: Long,
        @Body saleCreateDto: SaleCreateDto
    ): Response<SalesResponseDto>

    @DELETE("inventory/api/sales")
    suspend fun deleteSale(
        @Query("id") id: Long
    ): Response<Void>

    @DELETE("inventory/api/sales/items")
    suspend fun deleteSaleItem(
        @Query("id") id: Long
    ): Response<Void>

    @DELETE("inventory/api/sales/all")
    suspend fun deleteAllSales(): Response<Void>
}