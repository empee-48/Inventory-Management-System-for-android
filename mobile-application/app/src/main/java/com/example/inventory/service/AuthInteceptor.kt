package com.example.inventory.service

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager,
    private val username: String? = null,
    private val password: String? = null
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val isLoginRequest = originalRequest.url.encodedPath.contains("auth/token")

        val newRequest = when {
            isLoginRequest && username != null && password != null -> {
                val credential = okhttp3.Credentials.basic(username, password)
                originalRequest.newBuilder()
                    .header("Authorization", credential)
                    .build()
            }
            else -> {
                val token = tokenManager.getToken()
                if (token != null) {
                    originalRequest.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    originalRequest
                }
            }
        }

        return chain.proceed(newRequest)
    }
}