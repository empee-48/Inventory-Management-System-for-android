package com.example.inventory.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.inventory.screens.composable.mainscreen.ModernBottomNavigation
import com.example.inventory.screens.composable.mainscreen.ModernTopAppBar
import com.example.inventory.service.TokenManager
import com.example.inventory.service.api.BatchApiService
import com.example.inventory.service.api.CategoryApiService
import com.example.inventory.service.api.OrdersApiService
import com.example.inventory.service.api.ProductApiService
import com.example.inventory.service.api.SalesApiService
import com.example.inventory.service.api.SuppliersApiService
import com.example.inventory.service.api.UserApiService

enum class MainScreenTab {
    DASHBOARD, PRODUCTS, ORDERS, SALES, PROFILE, SETTINGS
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    tokenManager: TokenManager,
    userApiService: UserApiService,
    productApiService: ProductApiService,
    categoryApiService: CategoryApiService,
    salesApiService: SalesApiService,
    ordersApiService: OrdersApiService,
    suppliersApiService: SuppliersApiService,
    batchApiService: BatchApiService,
    onLogout: () -> Unit
) {
    var currentTab by remember { mutableStateOf(MainScreenTab.DASHBOARD) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = ModernLightColorScheme()
    ) {
        Scaffold(
            topBar = {
                ModernTopAppBar(
                    onSettingsClick = { currentTab = MainScreenTab.SETTINGS },
                    onLogoutClick = { showLogoutDialog = true }
                )
            },
            bottomBar = {
                ModernBottomNavigation(
                    currentTab = currentTab,
                    onTabSelected = { tab -> currentTab = tab }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentTab) {
                    MainScreenTab.DASHBOARD -> DashboardScreen(
                        tokenManager = tokenManager,
                        userApiService = userApiService,
                        productApiService = productApiService,
                        categoryApiService = categoryApiService
                    )
                    MainScreenTab.PRODUCTS -> ProductsScreen(
                        productApiService = productApiService,
                        categoryApiService = categoryApiService
                    )
                    MainScreenTab.ORDERS -> OrdersScreen(
                        productApiService = productApiService,
                        orderApiService = ordersApiService,
                        suppliersApiService = suppliersApiService
                    )
                    MainScreenTab.SALES -> SalesScreen(
                        productApiService = productApiService,
                        salesApiService = salesApiService,
                        batchApiService = batchApiService
                    )
                    MainScreenTab.PROFILE -> ProfileScreen()
                    MainScreenTab.SETTINGS -> UsersScreen(
                        userApiService = userApiService
                    )
                }
            }
        }
    }

    if (showLogoutDialog) {
        ModernLogoutConfirmationDialog(
            onConfirmLogout = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = {
                showLogoutDialog = false
            }
        )
    }
}

@Composable
fun ModernLogoutConfirmationDialog(
    onConfirmLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 380.dp, max = 450.dp)
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning icon
                Surface(
                    color = Color(0xFFFFFFFF),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color(0xFFDC2626)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFFFFFFF),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Confirmation text
                Text(
                    text = "Are you sure you want to logout?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFFFFFF), // White text for body
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Cancel button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "Cancel",
                            color = Color(0xFFFFFFFF) // White text
                        )
                    }

                    // Logout button with reduced opacity
                    Button(
                        onClick = onConfirmLogout,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFBF092F).copy(alpha = 0.8f), // Reduced opacity red
                            contentColor = Color.White // White text
                        )
                    ) {
                        Text("Logout")
                    }
                }
            }
        }
    }
}

private fun ModernLightColorScheme() = lightColorScheme(
    primary = Color(0xFF0066CC),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001C3A),

    secondary = Color(0xFF535E70),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E3F7),
    onSecondaryContainer = Color(0xFF101C2B),

    tertiary = Color(0xFF6B5778),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF2DAFF),
    onTertiaryContainer = Color(0xFF251431),

    surface = Color(0xFFFBFCFE),
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFE0E2EC),
    onSurfaceVariant = Color(0xFF43474E),

    background = Color(0xFFF8FAFD),
    onBackground = Color(0xFF191C1E),

    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0)
)