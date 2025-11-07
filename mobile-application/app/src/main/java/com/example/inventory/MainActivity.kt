package com.example.inventory

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.inventory.service.AuthRepository
import com.example.inventory.screens.GetStartedScreen
import com.example.inventory.screens.LoginScreen
import com.example.inventory.screens.MainScreen
import com.example.inventory.service.RetrofitInstance
import com.example.inventory.ui.theme.InventoryTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            InventoryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val inventoryApp = context.applicationContext as InventoryApplication
    val tokenManager = inventoryApp.tokenManager
    val authRepository = AuthRepository(tokenManager)
    val retrofit = RetrofitInstance(tokenManager)
    val productApiService = retrofit.productsApiService
    val categoryApiService = retrofit.categoriesApiService
    val salesApiService = retrofit.salesApiService
    val ordersApiService = retrofit.ordersApiService
    val suppliersApiService = retrofit.suppliersApiService
    val batchApiService = retrofit.batchApiService


    var showGetStarted by remember { mutableStateOf(true) }
    var isLoggedIn by remember { mutableStateOf(authRepository.isLoggedIn()) }

    if (showGetStarted) {
        GetStartedScreen(
            onGetStarted = {
                showGetStarted = false
            }
        )
    } else {
        if (isLoggedIn) {
            MainScreen(
                tokenManager = tokenManager,
                userApiService = retrofit.userApiService,
                productApiService = productApiService,
                categoryApiService = categoryApiService,
                salesApiService = salesApiService,
                ordersApiService = ordersApiService,
                suppliersApiService = suppliersApiService,
                batchApiService = batchApiService,
                onLogout = {
                    authRepository.logout()
                    isLoggedIn = false
                }
            )
        } else {
            LoginScreen(
                onLoginSuccess = {
                    isLoggedIn = true
                }
            )
        }
    }
}