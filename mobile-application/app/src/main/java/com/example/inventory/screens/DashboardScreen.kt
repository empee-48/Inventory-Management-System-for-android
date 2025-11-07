package com.example.inventory.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inventory.data.UserResponseDto
import com.example.inventory.screens.composable.dashboard.QuickActionsSection
import com.example.inventory.screens.composable.dashboard.RecentActivitySection
import com.example.inventory.service.TokenManager
import com.example.inventory.service.api.UserApiService
import com.example.inventory.screens.composable.dashboard.StatsOverviewSection
import com.example.inventory.screens.composable.dashboard.UserProfileSection
import com.example.inventory.service.api.CategoryApiService
import com.example.inventory.service.api.ProductApiService

@Composable
fun DashboardScreen(
    tokenManager: TokenManager,
    userApiService: UserApiService,
    productApiService: ProductApiService,
    categoryApiService: CategoryApiService,
    onActionClick: (String) -> Unit = {},
    onError: (String) -> Unit = {}
) {
    var user by remember { mutableStateOf(tokenManager.getUser()) }
    val isAdmin = tokenManager.isAdmin()
    var isLoading by remember { mutableStateOf(false) }
    rememberCoroutineScope()

    LaunchedEffect(key1 = userApiService) {
        if (user == null) {
            isLoading = true
            try {
                val response = userApiService.getCurrentUser()
                if (response.isSuccessful) {
                    response.body()?.let { userData ->
                        tokenManager.saveUser(userData)
                        user = userData
                    }
                } else {
                    onError("Failed to fetch user data: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("Network error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(24.dp)
    ) {
        // Header Section
        item {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Loading user data...",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = buildWelcomeMessage(user),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            StatsOverviewSection(
            )
        }

        item {
            QuickActionsSection(
                isAdmin = isAdmin,
                onActionClick = onActionClick
            )
        }

        item {
            RecentActivitySection()
        }
    }
}

private fun buildWelcomeMessage(user: UserResponseDto?): String {
    return when {
        user?.username != null -> "Welcome back, ${user.username}!"
        else -> "Welcome to your dashboard!"
    }
}

fun formatRoles(roles: List<String>?): String {
    return roles?.joinToString(", ") { role ->
        role.removePrefix("ROLE_").replace("_", " ").lowercase()
            .replaceFirstChar { it.uppercase() }
    } ?: "User"
}


