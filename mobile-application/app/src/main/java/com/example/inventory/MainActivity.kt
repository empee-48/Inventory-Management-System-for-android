package com.example.inventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.inventory.InventoryApplication
import com.example.inventory.service.AuthRepository
import com.example.inventory.screens.LoginScreen
import com.example.inventory.screens.MainScreen
import com.example.inventory.ui.theme.InventoryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            InventoryTheme {
                // A surface container using the 'background' color from the theme
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

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val inventoryApp = context.applicationContext as InventoryApplication
    val authRepository = AuthRepository(inventoryApp.tokenManager)

    var isLoggedIn by remember { mutableStateOf(authRepository.isLoggedIn()) }

    if (isLoggedIn) {
        MainScreen(
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

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun MainActivityPreview() {
//    InventoryTheme {
//        Surface(modifier = Modifier.fillMaxSize()) {
//            LoginScreen(onLoginSuccess = {})
//        }
//    }
//}
//
//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun MainActivityLoggedInPreview() {
//    InventoryTheme {
//        Surface(modifier = Modifier.fillMaxSize()) {
//            MainScreen(onLogout = {})
//        }
//    }
//}