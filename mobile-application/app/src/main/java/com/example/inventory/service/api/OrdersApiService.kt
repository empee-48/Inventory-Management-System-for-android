package com.example.inventory.service.api

import com.example.inventory.data.OrderCreateDto
import com.example.inventory.data.OrderResponseDto
import retrofit2.Response
import retrofit2.http.*
import java.time.LocalDate

interface OrdersApiService {

    @GET("inventory/api/orders")
    suspend fun getOrders(
        @Query("id") id: Long? = null,
        @Query("categoryId") categoryId: Long? = null,
        @Query("orderDate") orderDate: LocalDate? = null,
        @Query("lowerBoundaryAmount") lowerBoundaryAmount: Double? = null,
        @Query("higherBoundaryAmount") higherBoundaryAmount: Double? = null
    ): Response<List<OrderResponseDto>>

    @POST("inventory/api/orders")
    suspend fun createOrder(
        @Body orderCreateDto: OrderCreateDto
    ): Response<OrderResponseDto>

    @PUT("inventory/api/orders")
    suspend fun editOrder(
        @Query("id") id: Long,
        @Body orderCreateDto: OrderCreateDto
    ): Response<OrderResponseDto>

    @DELETE("inventory/api/orders")
    suspend fun deleteOrder(
        @Query("id") id: Long
    ): Response<Void>

    @DELETE("inventory/api/items")
    suspend fun deleteOrderItem(
        @Query("id") id: Long
    ): Response<Void>

    @DELETE("inventory/api/orders/all")
    suspend fun deleteAllOrders(): Response<Void>
}