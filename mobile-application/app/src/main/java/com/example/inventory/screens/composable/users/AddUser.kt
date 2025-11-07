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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.inventory.data.UserCreateDto
import com.example.inventory.service.api.UserApiService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    onBack: () -> Unit,
    onSave: (UserCreateDto) -> Unit,
    userApiService: UserApiService
) {
    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }

    // Form state
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("USER") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Convert selected role to roles list
    val roles = remember(selectedRole) {
        when (selectedRole) {
            "ADMIN" -> listOf("ROLE_ADMIN", "ROLE_USER")
            else -> listOf("ROLE_USER")
        }
    }

    // Form validation
    val isFormValid = username.isNotBlank() &&
            password.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            password == confirmPassword &&
            password.length >= 6

    // Password validation messages
    val passwordError = when {
        password.isBlank() -> null
        password.length < 6 -> "Password must be at least 6 characters"
        else -> null
    }

    val confirmPasswordError = when {
        confirmPassword.isBlank() -> null
        password != confirmPassword -> "Passwords do not match"
        else -> null
    }

    // Handle user creation
    val handleSave = {
        if (isFormValid && !isSubmitting) {
            isSubmitting = true
            submitError = null

            coroutineScope.launch {
                try {
                    val userCreateDto = UserCreateDto(
                        username = username,
                        password = password,
                        roles = roles
                    )

                    val response = userApiService.createUser(userCreateDto)

                    if (response.isSuccessful) {
                        onSave(userCreateDto)
                    } else {
                        submitError = "Failed to create user: ${response.code()} - ${response.message()}"
                    }
                } catch (e: Exception) {
                    submitError = "Network error: ${e.message}"
                } finally {
                    isSubmitting = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Add New User",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp),
                        enabled = !isSubmitting
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(8.dp)
                        )
                    } else {
                        IconButton(
                            onClick = handleSave,
                            enabled = isFormValid && !isSubmitting
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Submit Error Banner
            submitError?.let { error ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Form Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "User Information",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Username
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username *") },
                    placeholder = { Text("Enter username") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, "Username")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = username.isBlank() && username.isNotEmpty(),
                    enabled = !isSubmitting
                )

                // Role Dropdown
                RoleDropdown(
                    selectedRole = selectedRole,
                    onRoleSelected = { role ->
                        selectedRole = role
                    },
                    enabled = !isSubmitting
                )

                // Role Description
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Role Permissions:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        roles.forEach { role ->
                            Text(
                                text = "• ${role.removePrefix("ROLE_")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password *") },
                    placeholder = { Text("Enter password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, "Password")
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible }
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = passwordError != null,
                    supportingText = {
                        passwordError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    enabled = !isSubmitting
                )

                // Confirm Password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password *") },
                    placeholder = { Text("Confirm your password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, "Confirm Password")
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { confirmPasswordVisible = !confirmPasswordVisible }
                        ) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmPasswordError != null,
                    supportingText = {
                        confirmPasswordError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    enabled = !isSubmitting
                )

                // Validation Message
                if (!isFormValid && (
                            username.isNotBlank() || password.isNotBlank() || confirmPassword.isNotBlank()
                            )) {
                    Text(
                        text = "Please fill in all required fields and ensure passwords match",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        textAlign = TextAlign.Center
                    )
                }

                // Form Helper Text
                Text(
                    text = "* Required fields\n• Password must be at least 6 characters\n• Passwords must match",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleDropdown(
    selectedRole: String,
    onRoleSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("USER", "ADMIN")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedRole,
            onValueChange = { },
            label = { Text("Role *") },
            placeholder = { Text("Select user role") },
            leadingIcon = {
                Icon(Icons.Default.Security, "Role")
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            enabled = enabled
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            roles.forEach { role ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = role,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onRoleSelected(role)
                        expanded = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}