package com.example.inventory.service.api

import com.example.inventory.data.BatchResponseDto
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Query

interface BatchApiService {

    @GET("inventory/api/batch")
    suspend fun getBatches(): Response<List<BatchResponseDto>>

    @DELETE("inventory/api/batch")
    suspend fun deleteBatch(@Query("id") id: Long): Response<Void>

    @DELETE("inventory/api/batch/all")
    suspend fun deleteAllBatches(): Response<Void>
}