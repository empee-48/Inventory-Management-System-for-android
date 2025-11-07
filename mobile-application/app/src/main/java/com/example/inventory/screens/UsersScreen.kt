package com.example.inventory.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.inventory.data.UserResponseDto
import com.example.inventory.screens.composable.users.AddUserScreen
import com.example.inventory.screens.composable.users.ChangePasswordScreen
import com.example.inventory.screens.composable.users.UserDetailsScreen
import com.example.inventory.screens.composable.users.UsersList
import com.example.inventory.service.api.UserApiService

sealed class UsersScreenState {
    object List : UsersScreenState()
    data class Details(val user: UserResponseDto) : UsersScreenState()
    object Add : UsersScreenState()
    data class ChangePassword(val user: UserResponseDto) : UsersScreenState()
}

@Composable
fun UsersScreen(
    userApiService: UserApiService
) {
    var screenState by remember { mutableStateOf<UsersScreenState>(UsersScreenState.List) }
    var isRefreshing by remember { mutableStateOf(false) }

    when (val currentState = screenState) {
        is UsersScreenState.List -> {
            UsersListScreen(
                userApiService = userApiService,
                onUserClick = { user ->
                    screenState = UsersScreenState.Details(user)
                },
                onAddUser = {
                    screenState = UsersScreenState.Add
                },
                isRefreshing = isRefreshing,
                onRefresh = { isRefreshing = true }
            )
        }
        is UsersScreenState.Details -> {
            UserDetailsScreen(
                user = currentState.user,
                onBack = { screenState = UsersScreenState.List },
                onEdit = { user ->
                    screenState = UsersScreenState.ChangePassword(user)
                },
                onDelete = { user ->
                    screenState = UsersScreenState.List
                },
                userApiService = userApiService
            )
        }
        is UsersScreenState.Add -> {
            AddUserScreen(
                onBack = { screenState = UsersScreenState.List },
                onSave = {
                    screenState = UsersScreenState.List
                },
                userApiService = userApiService
            )
        }
        is UsersScreenState.ChangePassword -> {
            ChangePasswordScreen(
                user = currentState.user,
                onBack = { screenState = UsersScreenState.Details(currentState.user) },
                onSave = { screenState = UsersScreenState.Details(currentState.user) },
                userApiService = userApiService
            )
        }
    }
}

@Composable
fun UsersListScreen(
    userApiService: UserApiService,
    onUserClick: (UserResponseDto) -> Unit,
    onAddUser: () -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        UsersList(
            userApiService = userApiService,
            onUserClick = onUserClick,
            onRefresh = onRefresh,
            isRefreshing = isRefreshing
        )

        FloatingActionButton(
            onClick = onAddUser,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFF4A90D6),
            contentColor = Color.White,
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add User",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}