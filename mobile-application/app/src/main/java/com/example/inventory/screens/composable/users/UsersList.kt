package com.example.inventory.screens.composable.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventory.data.UserResponseDto
import com.example.inventory.screens.composable.common.LoadingComponent
import com.example.inventory.service.api.UserApiService
import kotlinx.coroutines.launch

@Composable
fun UsersList(
    userApiService: UserApiService,
    onUserClick: (UserResponseDto) -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean = false,
    modifier: Modifier = Modifier
) {
    var users by remember { mutableStateOf<List<UserResponseDto>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var internalRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val fetchUsers = {
        coroutineScope.launch {
            internalRefreshing = true
            try {
                val response = userApiService.getUsers()
                if (response.isSuccessful) {
                    users = response.body() ?: emptyList()
                    errorMessage = null
                } else {
                    errorMessage = "Failed to load users: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.message}"
            } finally {
                internalRefreshing = false
            }
        }
    }

    // Load initial data
    LaunchedEffect(Unit) {
        if (users == null && errorMessage == null) {
            fetchUsers()
        }
    }

    // Show loading state
    if (users == null && errorMessage == null && !isRefreshing && !internalRefreshing) {
        LoadingState()
        return
    }

    // Show error state
    if (errorMessage != null && users == null) {
        ErrorState(
            errorMessage = errorMessage!!,
            onRetry = {
                errorMessage = null
                internalRefreshing = true
                fetchUsers()
            }
        )
        return
    }

    // Show users list
    val userList = users ?: emptyList()
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ButtonPanel(
                onRefresh = {
                    onRefresh()
                    internalRefreshing = true
                    fetchUsers()
                },
                isRefreshing = internalRefreshing || isRefreshing
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Users Count
            Text(
                text = "Users (${userList.size})",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (userList.isEmpty()) {
            item {
                EmptyUsersState()
            }
        } else {
            items(userList, key = { it.id ?: 0L }) { user ->
                UserCard(
                    user = user,
                    onClick = { onUserClick(user) }
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            LoadingComponent(message = "Loading Users...")
        }
    }
}

@Composable
private fun ButtonPanel(
    onRefresh: () -> Unit,
    isRefreshing: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        FilledTonalButton(
            onClick = onRefresh,
            enabled = !isRefreshing,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Refresh")
        }
    }
}

@Composable
fun UserCard(
    user: UserResponseDto,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = MaterialTheme.shapes.medium
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User avatar with role-based color
            val isAdmin = user.roles?.any { it.contains("ADMIN", ignoreCase = true) } == true
            Surface(
                color = if (isAdmin) Color(0xFF4A90D6).copy(alpha = 0.1f) else Color(0xFF6B7280).copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        tint = if (isAdmin) Color(0xFF4A90D6) else Color(0xFF6B7280),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = user.username ?: "Unknown User",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF1F2937)
                    )

                    // Role badge
                    Surface(
                        color = if (isAdmin) Color(0xFF4A90D6) else Color(0xFF6B7280),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = if (isAdmin) "ADMIN" else "USER",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // User ID
                user.id?.let { id ->
                    Text(
                        text = "ID: $id",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Roles
                user.roles?.let { roles ->
                    if (roles.isNotEmpty()) {
                        Text(
                            text = "Roles: ${roles.joinToString { it.removePrefix("ROLE_") }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }

            // Chevron
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = "View Details",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun EmptyUsersState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF4A90D6).copy(alpha = 0.1f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "No Users",
                    tint = Color(0xFF4A90D6),
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Users Found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Get started by adding your first user to the system",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun ErrorState(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Failed to Load Users",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        FilledTonalButton(
            onClick = onRetry,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = Color(0xFF4A90D6),
                contentColor = Color.White
            )
        ) {
            Text("Try Again", style = MaterialTheme.typography.labelLarge)
        }
    }
}