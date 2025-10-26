package com.example.inventory.viewModels

import com.example.inventory.service.AuthRepository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
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
            _uiState.value = currentState.copy(error = "Please enter username")
            return
        }
        if (currentState.password.isBlank()) {
            _uiState.value = currentState.copy(error = "Please enter password")
            return
        }

        // Start loading
        _uiState.value = currentState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = authRepository.login(currentState.username, currentState.password)

            result.onSuccess { token ->
                // Success - navigate to main screen
                _uiState.value = LoginUiState(isLoggedIn = true)
            }.onFailure { exception ->
                // Failure - show error but keep username
                _uiState.value = LoginUiState(
                    username = currentState.username,
                    password = "",
                    error = exception.message ?: "Login failed. Please try again."
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
