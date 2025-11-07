package com.example.inventory.screens.composable.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventory.data.OrderResponseDto
import com.example.inventory.service.api.OrdersApiService
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun OrdersList(
    orderApiService: OrdersApiService,
    onOrderClick: (OrderResponseDto) -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean = false,
    modifier: Modifier = Modifier
) {
    var orders by remember { mutableStateOf<List<OrderResponseDto>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var internalRefreshing by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var selectedOrder by remember { mutableStateOf<OrderResponseDto?>(null) }
    var categoryIdFilter by remember { mutableStateOf<Long?>(null) }
    var orderDate by remember { mutableStateOf("") }
    var minAmount by remember { mutableStateOf("") }
    var maxAmount by remember { mutableStateOf("") }

    val fetchOrders: (Map<String, Any?>) -> Unit = { filters ->
        coroutineScope.launch {
            internalRefreshing = true
            try {
                val id = filters["id"] as? Long
                val categoryId = filters["categoryId"] as? Long
                val orderDateFilter = (filters["orderDate"] as? String)?.let {
                    try {
                        LocalDate.parse(it)
                    } catch (e: Exception) {
                        null
                    }
                }
                val lowerBoundaryAmount = filters["minAmount"] as? Double
                val higherBoundaryAmount = filters["maxAmount"] as? Double

                val response = orderApiService.getOrders(
                    id = id,
                    categoryId = categoryId,
                    orderDate = orderDateFilter,
                    lowerBoundaryAmount = lowerBoundaryAmount,
                    higherBoundaryAmount = higherBoundaryAmount
                )
                if (response.isSuccessful) {
                    orders = response.body() ?: emptyList()
                    errorMessage = null
                } else {
                    errorMessage = "Failed to load orders: ${response.code()}"
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
        if (orders == null && errorMessage == null) {
            fetchOrders(emptyMap())
        }
    }

    // Filter Dialog
    if (showFilterDialog) {
        FilterDialog(
            orders = orders ?: emptyList(),
            selectedOrder = selectedOrder,
            categoryIdFilter = categoryIdFilter,
            orderDate = orderDate,
            minAmount = minAmount,
            maxAmount = maxAmount,
            onOrderSelected = { selectedOrder = it },
            onCategoryIdFilterChange = { categoryIdFilter = it },
            onOrderDateChange = { orderDate = it },
            onMinAmountChange = { minAmount = it },
            onMaxAmountChange = { maxAmount = it },
            onApplyFilters = {
                val filters = buildMap<String, Any?> {
                    selectedOrder?.id?.let { put("id", it) }
                    categoryIdFilter?.let { put("categoryId", it) }
                    if (orderDate.isNotBlank()) put("orderDate", orderDate)
                    minAmount.toDoubleOrNull()?.let { put("minAmount", it) }
                    maxAmount.toDoubleOrNull()?.let { put("maxAmount", it) }
                }
                fetchOrders(filters)
                showFilterDialog = false
            },
            onClearFilters = {
                selectedOrder = null
                categoryIdFilter = null
                orderDate = ""
                minAmount = ""
                maxAmount = ""
                fetchOrders(emptyMap())
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }

    // Show loading state
    if (orders == null && errorMessage == null && !isRefreshing && !internalRefreshing) {
        LoadingState()
        return
    }

    // Show error state
    if (errorMessage != null && orders == null) {
        ErrorState(
            errorMessage = errorMessage!!,
            onRetry = {
                errorMessage = null
                internalRefreshing = true
                fetchOrders(emptyMap())
            }
        )
        return
    }

    // Show orders list
    val orderList = orders ?: emptyList()
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
                    fetchOrders(emptyMap())
                },
                onFilter = { showFilterDialog = true },
                isRefreshing = internalRefreshing || isRefreshing,
                hasActiveFilters = selectedOrder != null ||
                        categoryIdFilter != null ||
                        orderDate.isNotBlank() ||
                        minAmount.isNotBlank() ||
                        maxAmount.isNotBlank()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (orderList.isEmpty()) {
            item {
                EmptyOrdersState()
            }
        } else {
            items(orderList, key = { it.id }) { order ->
                OrderCard(
                    order = order,
                    onClick = { onOrderClick(order) }
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
                text = "Loading orders...",
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
    isRefreshing: Boolean = false,
    hasActiveFilters: Boolean = false
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDialog(
    orders: List<OrderResponseDto>,
    selectedOrder: OrderResponseDto?,
    categoryIdFilter: Long?,
    orderDate: String,
    minAmount: String,
    maxAmount: String,
    onOrderSelected: (OrderResponseDto?) -> Unit,
    onCategoryIdFilterChange: (Long?) -> Unit,
    onOrderDateChange: (String) -> Unit,
    onMinAmountChange: (String) -> Unit,
    onMaxAmountChange: (String) -> Unit,
    onApplyFilters: () -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Orders") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                var orderDropdownExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = orderDropdownExpanded,
                    onExpandedChange = { orderDropdownExpanded = !orderDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedOrder?.let { "Order #${it.id}" } ?: "",
                        onValueChange = {},
                        label = { Text("Select Order") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = orderDropdownExpanded) },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = orderDropdownExpanded,
                        onDismissRequest = { orderDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Orders") },
                            onClick = {
                                onOrderSelected(null)
                                orderDropdownExpanded = false
                            }
                        )
                        orders.forEach { order ->
                            DropdownMenuItem(
                                text = { Text("Order #${order.id}") },
                                onClick = {
                                    onOrderSelected(order)
                                    orderDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Category ID Filter
                OutlinedTextField(
                    value = categoryIdFilter?.toString() ?: "",
                    onValueChange = { newValue ->
                        if (newValue.isEmpty()) {
                            onCategoryIdFilterChange(null)
                        } else {
                            newValue.toLongOrNull()?.let { onCategoryIdFilterChange(it) }
                        }
                    },
                    label = { Text("Category ID") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Order Date
                OutlinedTextField(
                    value = orderDate,
                    onValueChange = onOrderDateChange,
                    label = { Text("Order Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Amount Range
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = minAmount,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                                onMinAmountChange(newValue)
                            }
                        },
                        label = { Text("Min Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxAmount,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                                onMaxAmountChange(newValue)
                            }
                        },
                        label = { Text("Max Amount") },
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
fun OrderCard(
    order: OrderResponseDto,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Order icon with your brand color
            Surface(
                color = Color(0xFF4A90D6).copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Order",
                        tint = Color(0xFF4A90D6),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Order info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Header with order number and total amount
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Order #${order.id}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = "$${"%.2f".format(order.totalAmount)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF4A90D6)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Supplier info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = "Supplier",
                        tint = Color(0xFF666666),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Supplier #${order.supplierId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF666666)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Details row - Date and Items
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Order date
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Order Date",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = formatOrderDate(order.orderDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666)
                        )
                    }

                    // Items count with visual indicator
                    Surface(
                        color = Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory,
                                contentDescription = "Items",
                                tint = Color(0xFF666666),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${order.items.size} ${if (order.items.size == 1) "item" else "items"}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }
            }

            // Chevron for navigation
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = "View Order Details",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun EmptyOrdersState() {
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
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "No Orders",
                tint = Color(0xFF4A90D6),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Orders Found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Get started by creating your first order",
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
            text = "Failed to Load Orders",
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

private fun formatOrderDate(localDate: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    return localDate.format(formatter)
}