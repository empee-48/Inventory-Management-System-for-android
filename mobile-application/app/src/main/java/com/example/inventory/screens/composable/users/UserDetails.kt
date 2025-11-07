package com.example.inventory.screens.composable.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventory.data.UserResponseDto
import com.example.inventory.service.api.UserApiService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsScreen(
    user: UserResponseDto,
    onBack: () -> Unit,
    onEdit: (UserResponseDto) -> Unit,
    onDelete: (UserResponseDto) -> Unit,
    userApiService: UserApiService
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Handle delete confirmation
    val handleDelete: () -> Unit = {
        isDeleting = true
        deleteError = null

        coroutineScope.launch {
            try {
                val response = userApiService.deleteUser(user.id!!)
                if (response.isSuccessful) {
                    onDelete(user)
                    showDeleteDialog = false
                } else {
                    deleteError = "Failed to delete user: ${response.code()}"
                }
            } catch (e: Exception) {
                deleteError = "Network error: ${e.message}"
            } finally {
                isDeleting = false
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) {
                    showDeleteDialog = false
                }
            },
            title = {
                Text("Delete User")
            },
            text = {
                Column {
                    Text("Are you sure you want to delete \"${user.username}\"?")
                    if (deleteError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = deleteError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = handleDelete,
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    enabled = !isDeleting
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "User Details",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Change Password Button
                    IconButton(onClick = { onEdit(user) }) {
                        Icon(Icons.Default.Lock, contentDescription = "Change Password")
                    }

                    // Delete Button
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section
            UserHeaderSection(user = user)

            Spacer(modifier = Modifier.height(24.dp))

            // User Information Section
            UserInformationSection(user = user)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun UserHeaderSection(user: UserResponseDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Username and ID
        Text(
            text = user.username ?: "Unknown User",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "User ID: ${user.id}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Role Badge
        val isAdmin = user.roles?.any { it.contains("ADMIN", ignoreCase = true) } == true
        Surface(
            color = if (isAdmin) Color(0xFFF0FDF4) else Color(0xFFF3F4F6),
            shape = MaterialTheme.shapes.small
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isAdmin) "👑" else "👤",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isAdmin) "Administrator" else "Standard User",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isAdmin) Color(0xFF059669) else Color(0xFF6B7280),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun UserInformationSection(user: UserResponseDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Section Title
        Text(
            text = "User Information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Username
        InfoItemWithBorder(
            icon = "👤",
            title = "Username",
            content = user.username ?: "Unknown"
        )

        // User ID
        user.id?.let { id ->
            InfoItemWithBorder(
                icon = "🆔",
                title = "User ID",
                content = id.toString()
            )
        }

        // Roles
        InfoItemWithBorder(
            icon = "🔐",
            title = "Roles",
            content = user.roles?.joinToString { it.removePrefix("ROLE_") } ?: "No roles assigned",
            showBorder = false
        )
    }
}

@Composable
private fun InfoItemWithBorder(
    icon: String,
    title: String,
    content: String,
    showBorder: Boolean = true
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.size(32.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1A1A1A),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF666666),
                    lineHeight = 24.sp
                )
            }
        }

        // Bottom border
        if (showBorder) {
            Divider(
                color = Color(0xFFF0F0F0),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}