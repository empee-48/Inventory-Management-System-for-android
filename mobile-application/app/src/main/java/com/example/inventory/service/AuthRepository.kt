package com.example.inventory.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val tokenManager: TokenManager
) {

    suspend fun login(username: String, password: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val retrofit = RetrofitInstance(tokenManager, username, password)
                val authApiService = retrofit.userApiService

                val response = authApiService.loginWithBasicAuth()

                if (response.isSuccessful) {
                    val loginResponse = response.body()!!

                    tokenManager.saveToken(loginResponse.token)
                    Result.success(loginResponse.token)
                } else {
                    Result.failure(Exception("Login failed: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Network error: ${e.message}"))
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.getToken() != null
    }

    fun logout() {
        tokenManager.clearToken()
    }
}