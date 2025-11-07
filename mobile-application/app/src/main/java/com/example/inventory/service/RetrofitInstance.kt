package com.example.inventory.service

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.inventory.data.adapters.LocalDateAdapter
import com.example.inventory.data.adapters.LocalDateTimeAdapter
import com.example.inventory.service.api.BatchApiService
import com.example.inventory.service.api.CategoryApiService
import com.example.inventory.service.api.OrdersApiService
import com.example.inventory.service.api.ProductApiService
import com.example.inventory.service.api.SalesApiService
import com.example.inventory.service.api.SuppliersApiService
import com.example.inventory.service.api.UserApiService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.O)
class RetrofitInstance(
    private val tokenManager: TokenManager,
    private val username: String? = null,
    private val password: String? = null
) {

    private val BASE_URL = "http://92.112.181.128:8082"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = AuthInterceptor(tokenManager,username,password)

    @RequiresApi(Build.VERSION_CODES.O)
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .create()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @RequiresApi(Build.VERSION_CODES.O)
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val userApiService: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }

    val productsApiService: ProductApiService by lazy {
        retrofit.create(ProductApiService::class.java)
    }

    val categoriesApiService: CategoryApiService by lazy {
        retrofit.create(CategoryApiService::class.java)
    }

    val salesApiService: SalesApiService by lazy {
        retrofit.create(SalesApiService::class.java)
    }

    val ordersApiService: OrdersApiService by lazy {
        retrofit.create(OrdersApiService::class.java)
    }

    val suppliersApiService: SuppliersApiService by lazy {
        retrofit.create(SuppliersApiService::class.java)
    }

    val batchApiService: BatchApiService by lazy {
        retrofit.create(BatchApiService::class.java)
    }
}