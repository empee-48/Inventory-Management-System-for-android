package com.example.inventory.service

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.inventory.data.UserResponseDto
import com.google.gson.Gson
import androidx.core.content.edit

class TokenManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val gson = Gson()

    fun saveToken(token: String) {
        sharedPreferences.edit { putString(KEY_TOKEN, token) }
    }

    fun saveUser(userResponse: UserResponseDto) {
        sharedPreferences.edit { putString(KEY_USER_DATA, gson.toJson(userResponse)) }
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    fun getUser(): UserResponseDto? {
        val userJson = sharedPreferences.getString(KEY_USER_DATA, null)
        return try {
            gson.fromJson(userJson, UserResponseDto::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getUserRole(): String {
        val user = getUser()
        return if (user?.roles != null) {
            when {
                user.roles.contains("ROLE_ADMIN") -> "admin"  // ✅ Now this works with List<String>
                user.roles.contains("ROLE_USER") -> "user"
                else -> "user"
            }
        } else {
            "user"
        }
    }

    fun isAdmin(): Boolean {
        return getUserRole() == "admin"
    }

    fun clearToken() {
        sharedPreferences.edit { remove(KEY_TOKEN) }
    }

    fun clearUser() {
        sharedPreferences.edit { remove(KEY_USER_DATA) }
    }

    fun clearAll() {
        sharedPreferences.edit { clear() }
    }

    fun debugStorage() {
        val allEntries = sharedPreferences.all
        println("=== DEBUG TokenManager ===")
        println("All keys: ${allEntries.keys}")
        println("Token exists: ${sharedPreferences.contains(KEY_TOKEN)}")
        println("User data exists: ${sharedPreferences.contains(KEY_USER_DATA)}")
        println("Token value length: ${getToken()?.length ?: "null"}")
        println("User data: ${getUser() != null}")
        println("==========================")
    }

    fun testStorage() {
        val testToken = "test_token_${System.currentTimeMillis()}"
        saveToken(testToken)

        val retrieved = getToken()
        println("TEST - Saved: '$testToken', Retrieved: '$retrieved'")
        println("TEST - Match: ${testToken == retrieved}")
    }

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_DATA = "user_data"
    }
}