package com.example.inventory.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class UiState(
    val isLoading: Boolean = false
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun startLoading() {
        viewModelScope.launch {
            _uiState.value = UiState(isLoading = true)

            delay(3000)

            _uiState.value = UiState(isLoading = false)
        }
    }
}