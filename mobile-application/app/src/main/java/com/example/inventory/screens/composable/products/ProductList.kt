package com.example.inventory.screens.composable.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventory.data.CategoryResponseDto
import com.example.inventory.data.ProductResponseDto
import com.example.inventory.service.api.CategoryApiService
import com.example.inventory.service.api.ProductApiService
import kotlinx.coroutines.launch

@Composable
fun ProductsList(
    productApiService: ProductApiService,
    categoryApiService: CategoryApiService,
    onProductClick: (ProductResponseDto) -> Unit,
    onNavigateToCategories: () -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean = false
) {
    var products by remember { mutableStateOf<List<ProductResponseDto>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var internalRefreshing by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var selectedProduct by remember { mutableStateOf<ProductResponseDto?>(null) }
    var productNameFilter by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CategoryResponseDto?>(null) }
    var lowStockFilter by remember { mutableStateOf(false) }
    var minPrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf<List<CategoryResponseDto>>(emptyList()) }

    val fetchProducts: (Map<String, Any?>) -> Unit = { filters ->
        coroutineScope.launch {
            internalRefreshing = true
            try {
                val response = productApiService.getProducts(
                    id = filters["id"] as? Long,
                    productName = filters["productName"] as? String,
                    categoryId = filters["categoryId"] as? Long,
                    lowStock = filters["lowStock"] as? Boolean,
                    lowerBoundaryPrice = filters["minPrice"] as? Double,
                    higherBoundaryPrice = filters["maxPrice"] as? Double
                )
                if (response.isSuccessful) {
                    products = response.body() ?: emptyList()
                    errorMessage = null
                } else {
                    errorMessage = "Failed to load products: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.message}"
            } finally {
                internalRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        if (products == null && errorMessage == null) {
            fetchProducts(emptyMap())

            try {
                val categoriesResponse = categoryApiService.getCategories()
                if (categoriesResponse.isSuccessful) {
                    categories = categoriesResponse.body() ?: emptyList()
                }
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    if (showFilterDialog) {
        FilterDialog(
            products = products ?: emptyList(),
            categories = categories,
            selectedProduct = selectedProduct,
            productNameFilter = productNameFilter,
            selectedCategory = selectedCategory,
            lowStockFilter = lowStockFilter,
            minPrice = minPrice,
            maxPrice = maxPrice,
            onProductSelected = { selectedProduct = it },
            onProductNameFilterChange = { productNameFilter = it },
            onCategorySelected = { selectedCategory = it },
            onLowStockFilterChange = { lowStockFilter = it },
            onMinPriceChange = { minPrice = it },
            onMaxPriceChange = { maxPrice = it },
            onApplyFilters = {
                val filters = buildMap<String, Any?> {
                    selectedProduct?.id?.let { put("id", it) }
                    if (productNameFilter.isNotBlank()) put("productName", productNameFilter)
                    selectedCategory?.id?.let { put("categoryId", it) }
                    if (lowStockFilter) put("lowStock", true)
                    minPrice.toDoubleOrNull()?.let { put("minPrice", it) }
                    maxPrice.toDoubleOrNull()?.let { put("maxPrice", it) }
                }
                fetchProducts(filters)
                showFilterDialog = false
            },
            onClearFilters = {
                selectedProduct = null
                productNameFilter = ""
                selectedCategory = null
                lowStockFilter = false
                minPrice = ""
                maxPrice = ""
                fetchProducts(emptyMap())
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }

    // Show loading state
    if (products == null && errorMessage == null && !isRefreshing && !internalRefreshing) {
        LoadingState()
        return
    }

    // Show error state
    if (errorMessage != null && products == null) {
        ErrorState(
            errorMessage = errorMessage!!,
            onRetry = {
                errorMessage = null
                internalRefreshing = true
                fetchProducts(emptyMap())
            }
        )
        return
    }

    // Show products list
    val productList = products ?: emptyList()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ButtonPanel(
                onRefresh = {
                    onRefresh()
                    internalRefreshing = true
                    fetchProducts(emptyMap())
                },
                onFilter = { showFilterDialog = true },
                onNavigateToCategories = onNavigateToCategories,
                isRefreshing = internalRefreshing || isRefreshing,
                hasActiveFilters = selectedProduct != null ||
                        productNameFilter.isNotBlank() ||
                        selectedCategory != null ||
                        lowStockFilter ||
                        minPrice.isNotBlank() ||
                        maxPrice.isNotBlank()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (productList.isEmpty()) {
            item {
                EmptyProductsState()
            }
        } else {
            items(productList, key = { it.id ?: 0L }) { product ->
                ProductCard(
                    product = product,
                    onClick = { onProductClick(product) }
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
            CircularProgressIndicator(
                color = Color(0xFF4A90D6)
            )
            Text(
                text = "Loading products...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ButtonPanel(
    onRefresh: () -> Unit,
    onFilter: () -> Unit,
    onNavigateToCategories: () -> Unit,
    isRefreshing: Boolean = false,
    hasActiveFilters: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        FilledTonalButton(
            onClick = onFilter,
            modifier = Modifier.weight(1f),
            colors = if (hasActiveFilters) {
                ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF4A90D6).copy(alpha = 0.1f),
                    contentColor = Color(0xFF4A90D6)
                )
            } else {
                ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Filter")
        }

        FilledTonalButton(
            onClick = onNavigateToCategories,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = "Categories",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Categories")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDialog(
    products: List<ProductResponseDto>,
    categories: List<CategoryResponseDto>,
    selectedProduct: ProductResponseDto?,
    productNameFilter: String,
    selectedCategory: CategoryResponseDto?,
    lowStockFilter: Boolean,
    minPrice: String,
    maxPrice: String,
    onProductSelected: (ProductResponseDto?) -> Unit,
    onProductNameFilterChange: (String) -> Unit,
    onCategorySelected: (CategoryResponseDto?) -> Unit,
    onLowStockFilterChange: (Boolean) -> Unit,
    onMinPriceChange: (String) -> Unit,
    onMaxPriceChange: (String) -> Unit,
    onApplyFilters: () -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Products") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                var productDropdownExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = productDropdownExpanded,
                    onExpandedChange = { productDropdownExpanded = !productDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedProduct?.name ?: "",
                        onValueChange = {},
                        label = { Text("Select Product") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = productDropdownExpanded,
                        onDismissRequest = { productDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Products") },
                            onClick = {
                                onProductSelected(null)
                                productDropdownExpanded = false
                            }
                        )
                        products.forEach { product ->
                            DropdownMenuItem(
                                text = { Text(product.name) },
                                onClick = {
                                    onProductSelected(product)
                                    productDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = productNameFilter,
                    onValueChange = onProductNameFilterChange,
                    label = { Text("Product Name Contains") },
                    modifier = Modifier.fillMaxWidth()
                )

                var categoryDropdownExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "",
                        onValueChange = {},
                        label = { Text("Select Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories") },
                            onClick = {
                                onCategorySelected(null)
                                categoryDropdownExpanded = false
                            }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    onCategorySelected(category)
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = lowStockFilter,
                        onCheckedChange = onLowStockFilterChange
                    )
                    Text(
                        text = "Show Only Low Stock Items",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = minPrice,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                                onMinPriceChange(newValue)
                            }
                        },
                        label = { Text("Min Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxPrice,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                                onMaxPriceChange(newValue)
                            }
                        },
                        label = { Text("Max Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onClearFilters) {
                    Text("Clear")
                }
                Button(
                    onClick = onApplyFilters,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A90D6)
                    )
                ) {
                    Text("Apply Filters")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EmptyProductsState() {
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
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "No Products",
                tint = Color(0xFF4A90D6),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Products Found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Get started by adding your first product to the inventory",
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
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Failed to Load Products",
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