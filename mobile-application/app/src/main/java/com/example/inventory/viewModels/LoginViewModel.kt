package com.example.inventory.viewModels

import com.example.inventory.service.AuthRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

// Sealed class for specific error types
sealed class LoginError {
    object NetworkError : LoginError()
    object InvalidCredentials : LoginError()
    object UnknownError : LoginError()

    fun getMessage(): String {
        return when (this) {
            is NetworkError -> "Could not connect to network. Please check your connection."
            is InvalidCredentials -> "Invalid credentials. Please check your username and password."
            is UnknownError -> "Could not connect to network. Please check your connection."
        }
    }
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: LoginError? = null, // Changed from String to LoginError
    val isLoggedIn: Boolean = false
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username,
            error = null
        )
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            error = null
        )
    }

    fun login() {
        val currentState = _uiState.value

        // Validation
        if (currentState.username.isBlank()) {
            _uiState.value = currentState.copy(error = LoginError.InvalidCredentials)
            return
        }
        if (currentState.password.isBlank()) {
            _uiState.value = currentState.copy(error = LoginError.InvalidCredentials)
            return
        }

        // Start loading
        _uiState.value = currentState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val result = authRepository.login(currentState.username, currentState.password)

                result.onSuccess { token ->
                    // Success - navigate to main screen
                    _uiState.value = LoginUiState(isLoggedIn = true)
                }.onFailure { exception ->
                    // Handle specific error types
                    val error = when {
                        exception is UnknownHostException ||
                                exception is ConnectException ||
                                exception is SocketTimeoutException -> {
                            LoginError.NetworkError
                        }
                        exception.message?.contains("401") == true ||
                                exception.message?.contains("unauthorized", ignoreCase = true) == true ||
                                exception.message?.contains("invalid", ignoreCase = true) == true -> {
                            LoginError.InvalidCredentials
                        }
                        else -> {
                            LoginError.UnknownError
                        }
                    }

                    _uiState.value = LoginUiState(
                        username = currentState.username,
                        password = "", // Clear password for security
                        error = error
                    )
                }
            } catch (e: Exception) {
                // Handle coroutine or other unexpected errors
                val error = when {
                    e is UnknownHostException ||
                            e is ConnectException ||
                            e is SocketTimeoutException -> {
                        LoginError.NetworkError
                    }
                    else -> {
                        LoginError.UnknownError
                    }
                }

                _uiState.value = LoginUiState(
                    username = currentState.username,
                    password = "",
                    error = error
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}