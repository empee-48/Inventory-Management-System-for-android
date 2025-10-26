package com.example.inventory

import android.app.Application
import com.example.inventory.service.TokenManager

class InventoryApplication : Application() {

    val tokenManager: TokenManager by lazy {
        TokenManager(this)
    }
}