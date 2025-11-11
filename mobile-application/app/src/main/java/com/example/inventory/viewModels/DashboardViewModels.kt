package com.example.inventory.viewModels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.*
import com.example.inventory.service.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.LocalDate

// Move DashboardUiState to the top
sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val data: DashboardData) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel(
    private val productApiService: ProductApiService,
    private val categoryApiService: CategoryApiService,
    private val salesApiService: SalesApiService,
    private val ordersApiService: OrdersApiService,
    private val suppliersApiService: SuppliersApiService,
    private val batchApiService: BatchApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun refreshData() {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                // Fetch all data in parallel using async
                val productsDeferred = async { productApiService.getProducts() }
                val categoriesDeferred = async { categoryApiService.getCategories() }
                val salesDeferred = async { salesApiService.getSales() }
                val ordersDeferred = async { ordersApiService.getOrders() }
                val suppliersDeferred = async { suppliersApiService.getSuppliers() }
                val batchesDeferred = async { batchApiService.getBatches() }

                // Await all results
                val products = productsDeferred.await()
                val categories = categoriesDeferred.await()
                val sales = salesDeferred.await()
                val orders = ordersDeferred.await()
                val suppliers = suppliersDeferred.await()
                val batches = batchesDeferred.await()

                if (products.isSuccessful && categories.isSuccessful && sales.isSuccessful &&
                    orders.isSuccessful && suppliers.isSuccessful && batches.isSuccessful
                ) {
                    val dashboardData = DashboardData(
                        products = products.body() ?: emptyList(),
                        categories = categories.body() ?: emptyList(),
                        sales = sales.body() ?: emptyList(),
                        orders = orders.body() ?: emptyList(),
                        suppliers = suppliers.body() ?: emptyList(),
                        batches = batches.body() ?: emptyList()
                    )
                    _uiState.value = DashboardUiState.Success(dashboardData)
                } else {
                    _uiState.value = DashboardUiState.Error("Failed to load dashboard data")
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error("Network error: ${e.message}")
            }
        }
    }
}

data class DashboardData(
    val products: List<ProductResponseDto>,
    val categories: List<CategoryResponseDto>,
    val sales: List<SalesResponseDto>,
    val orders: List<OrderResponseDto>,
    val suppliers: List<SupplierResponseDto>,
    val batches: List<BatchResponseDto>
) {
    val totalProducts: Int get() = products.size
    val totalCategories: Int get() = categories.size
    val totalSuppliers: Int get() = suppliers.size
    val totalOrders: Int get() = orders.size
    val totalSales: Int get() = sales.size

    val lowStockProducts: List<ProductResponseDto>
        get() = products.filter { it.stockLevelLow }

    val totalRevenue: Double
        get() = sales.sumOf { it.totalAmount }

    val todayRevenue: Double
        @RequiresApi(Build.VERSION_CODES.O)
        get() = sales.filter { it.saleDate == LocalDate.now() }
            .sumOf { it.totalAmount }

    val recentSales: List<SalesResponseDto>
        get() = sales.sortedByDescending { it.saleDate }.take(5)

    val recentOrders: List<OrderResponseDto>
        get() = orders.sortedByDescending { it.orderDate }.take(5)

    val totalInventoryValue: Double
        get() = products.sumOf { it.price * it.inStock }
}