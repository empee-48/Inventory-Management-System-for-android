package com.example.inventory.screens.composable.orders

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.inventory.data.*
import com.example.inventory.screens.composable.common.LoadingComponent
import com.example.inventory.service.api.OrdersApiService
import com.example.inventory.service.api.ProductApiService
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOrderScreen(
    order: OrderResponseDto,
    onBack: () -> Unit,
    onSave: (Long, OrderCreateDto) -> Unit,
    onDeleteOrder: () -> Unit,
    orderApiService: OrdersApiService,
    productApiService: ProductApiService
) {
    var products by remember { mutableStateOf<List<ProductResponseDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var submitError by remember { mutableStateOf<String?>(null) }

    var supplier by remember {
        mutableStateOf(order.supplierId.toString())
    }
    var orderDate by remember {
        mutableStateOf(order.orderDate.toString())
    }
    var orderItems by remember {
        mutableStateOf(
            order.items.map { item ->
                OrderItemWithId(
                    id = item.id,
                    productId = item.productId,
                    amount = item.amount.toInt(),
                    orderPrice = item.orderPrice
                )
            }
        )
    }

    var showDeleteOrderDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Calculate total amount
    val totalAmount = remember(orderItems) {
        orderItems.sumOf { it.amount * it.orderPrice }
    }

    // Form validation
    val isFormValid = supplier.isNotBlank() &&
            orderDate.isNotBlank() &&
            orderItems.isNotEmpty() &&
            orderItems.all { it.productId > 0 && it.amount > 0 && it.orderPrice >= 0 }

    // Load products and pre-select current items
    LaunchedEffect(Unit) {
        try {
            val response = productApiService.getProducts()
            if (response.isSuccessful) {
                products = response.body() ?: emptyList()
            } else {
                errorMessage = "Failed to load products: ${response.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Function to submit order update with current items and total amount
    val submitOrderUpdate = suspend {
        if (order.id != null) {
            // Only include new items (without IDs) to avoid duplicates
            val itemsToSave = orderItems.filter { it.id == null }.map { item ->
                OrderItemCreateDto(
                    productId = item.productId,
                    amount = item.amount,
                    orderPrice = item.orderPrice
                )
            }

            val orderCreateDto = OrderCreateDto(
                supplierId = supplier.toLong(),
                orderDate = LocalDate.parse(orderDate),
                items = itemsToSave,
                totalAmount = totalAmount
            )

            val response = orderApiService.editOrder(order.id, orderCreateDto)
            response.isSuccessful
        } else {
            false
        }
    }

    // Function to delete the entire order
    val deleteOrder = suspend {
        if (order.id != null) {
            val response = orderApiService.deleteOrder(order.id)
            response.isSuccessful
        } else {
            false
        }
    }

    // Handle updating an item (for new items only)
    val handleUpdateItem = { index: Int, updatedItem: OrderItemWithId ->
        orderItems = orderItems.toMutableList().apply {
            this[index] = updatedItem
        }
    }

    val handleDeleteItem = { itemId: Long?, index: Int ->
        coroutineScope.launch {
            try {
                var shouldUpdateOrder = false

                // First delete the item from database if it exists
                if (itemId != null) {
                    val deleteResponse = orderApiService.deleteOrderItem(itemId)
                    if (deleteResponse.isSuccessful) {
                        shouldUpdateOrder = true
                    } else {
                        submitError = "Failed to delete order item: ${deleteResponse.code()}"
                        return@launch
                    }
                }

                // Store the current total before removal for comparison
                val currentTotal = totalAmount

                // Remove from local state
                orderItems = orderItems.toMutableList().apply {
                    removeAt(index)
                }

                // Calculate new total after removal
                val newTotal = orderItems.sumOf { it.amount * it.orderPrice }

                // Only update order if total amount changed and there are still items
                if (shouldUpdateOrder && orderItems.isNotEmpty() && currentTotal != newTotal) {
                    val updateSuccess = submitOrderUpdate()
                    if (!updateSuccess) {
                        submitError = "Failed to update order total after deletion"
                    }
                }

                // Show delete order dialog if no items remain
                if (orderItems.isEmpty()) {
                    showDeleteOrderDialog = true
                }

            } catch (e: Exception) {
                submitError = "Network error: ${e.message}"
            }
        }
    }

    // Function to add a new item
    val handleAddItem = {
        orderItems = orderItems + OrderItemWithId(
            id = null,
            productId = 0,
            amount = 1,
            orderPrice = 0.0
        )
    }

    val handleSave = {
        if (isFormValid && !isSubmitting && order.id != null) {
            isSubmitting = true
            submitError = null

            coroutineScope.launch {
                try {
                    val updateSuccess = submitOrderUpdate()

                    if (updateSuccess) {
                        // Convert to OrderItemCreateDto for the callback (only new items)
                        val itemsToSave = orderItems.filter { it.id == null }.map { item ->
                            OrderItemCreateDto(
                                productId = item.productId,
                                amount = item.amount,
                                orderPrice = item.orderPrice
                            )
                        }

                        val orderCreateDto = OrderCreateDto(
                            supplierId = supplier.toLong(),
                            orderDate = LocalDate.parse(orderDate),
                            items = itemsToSave,
                            totalAmount = totalAmount
                        )

                        onSave(order.id, orderCreateDto)
                    } else {
                        submitError = "Failed to update order"
                    }
                } catch (e: Exception) {
                    submitError = "Network error: ${e.message}"
                } finally {
                    isSubmitting = false
                }
            }
        }
    }

    // Delete Order Confirmation Dialog
    if (showDeleteOrderDialog) {
        DeleteOrderConfirmationDialog(
            onConfirmDelete = {
                showDeleteOrderDialog = false
                coroutineScope.launch {
                    try {
                        val deleteSuccess = deleteOrder()
                        if (deleteSuccess) {
                            onDeleteOrder()
                        } else {
                            submitError = "Failed to delete order"
                        }
                    } catch (e: Exception) {
                        submitError = "Network error: ${e.message}"
                    }
                }
            },
            onDismiss = {
                showDeleteOrderDialog = false
                onBack()
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Edit Order",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        IconButton(
                            onClick = handleSave,
                            enabled = isFormValid
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
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
                            tint = MaterialTheme.colorScheme.error
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

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LoadingComponent(message = "Please Wait...")
                    }
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Failed to Load Products",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                coroutineScope.launch {
                                    try {
                                        val response = productApiService.getProducts()
                                        if (response.isSuccessful) {
                                            products = response.body() ?: emptyList()
                                        } else {
                                            errorMessage = "Failed to load products: ${response.code()}"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Network error: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                // Form Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Header Section
                    Text(
                        text = "Edit Order Information",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )

                    // Order ID Display (read-only)
                    OutlinedTextField(
                        value = order.id?.toString() ?: "N/A",
                        onValueChange = { },
                        label = { Text("Order ID") },
                        leadingIcon = {
                            Icon(Icons.Default.Numbers, "Order ID")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false,
                        shape = MaterialTheme.shapes.small
                    )

                    // Supplier ID
                    OutlinedTextField(
                        value = supplier,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.toLongOrNull() != null) {
                                supplier = newValue
                            }
                        },
                        label = { Text("Supplier ID *") },
                        placeholder = { Text("Enter supplier ID") },
                        leadingIcon = {
                            Icon(Icons.Default.Business, "Supplier")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        isError = supplier.isBlank() && supplier.isNotEmpty(),
                        shape = MaterialTheme.shapes.small
                    )

                    // Order Date
                    OutlinedTextField(
                        value = orderDate,
                        onValueChange = { orderDate = it },
                        label = { Text("Order Date *") },
                        placeholder = { Text("YYYY-MM-DD") },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarToday, "Order Date")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = orderDate.isBlank() && orderDate.isNotEmpty(),
                        shape = MaterialTheme.shapes.small
                    )

                    // Items Section
                    Text(
                        text = "Order Items",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )

                    FilledTonalButton(
                        onClick = handleAddItem,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF4A90D6).copy(alpha = 0.1f),
                            contentColor = Color(0xFF4A90D6)
                        )
                    ) {
                        Icon(Icons.Default.Add, "Add Item")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Item")
                    }

                    // Items List
                    if (orderItems.isEmpty()) {
                        Surface(
                            color = Color.White,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 2.dp,
                                    shape = MaterialTheme.shapes.medium
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = "No Items",
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "No items added",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF6B7280),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Add items to this order using the 'Add Item' button above",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF9CA3AF),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        orderItems.forEachIndexed { index, item ->
                            if (item.id != null) {
                                // Existing item (read-only)
                                EditOrderItemRow(
                                    item = item,
                                    index = index,
                                    products = products,
                                    onDelete = { handleDeleteItem(item.id, index) }
                                )
                            } else {
                                // New item (editable)
                                EditableOrderItemRow(
                                    item = item,
                                    index = index,
                                    products = products,
                                    onUpdate = { updatedItem -> handleUpdateItem(index, updatedItem) },
                                    onDelete = { handleDeleteItem(null, index) }
                                )
                            }
                            if (index < orderItems.size - 1) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }

                    // Current Total Amount Card
                    if (orderItems.isNotEmpty()) {
                        Surface(
                            color = Color.White,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 4.dp,
                                    shape = MaterialTheme.shapes.medium
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Current Total",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1A1A1A)
                                )
                                Text(
                                    text = "$${"%.2f".format(totalAmount)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4A90D6)
                                )
                            }
                        }
                    }

                    // Original Total (for reference)
                    Surface(
                        color = Color(0xFFF8FAFD),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 2.dp,
                                shape = MaterialTheme.shapes.medium
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Original Total:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$${"%.2f".format(order.totalAmount)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Validation Message
                    if (!isFormValid && (supplier.isNotBlank() || orderDate.isNotBlank() || orderItems.isNotEmpty())) {
                        Surface(
                            color = Color(0xFFFEF2F2),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Info",
                                    tint = Color(0xFFDC2626)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Please fill in all required fields and ensure all items are valid",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFDC2626),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Data class to hold order items with their database IDs
data class OrderItemWithId(
    val id: Long? = null,
    var productId: Long,
    var amount: Int,
    var orderPrice: Double
)

@Composable
fun EditOrderItemRow(
    item: OrderItemWithId,
    index: Int,
    products: List<ProductResponseDto>,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Get product name
    val productName = products.find { it.id == item.productId }?.name ?: "Unknown Product"

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        ModernDeleteConfirmationDialog(
            onConfirmDelete = {
                showDeleteDialog = false
                onDelete()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Surface(
        color = Color.White,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with remove button
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Item ${index + 1} (Existing)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Item",
                        tint = Color(0xFFDC2626)
                    )
                }
            }

            // Product Display (read-only)
            OutlinedTextField(
                value = productName,
                onValueChange = { },
                label = { Text("Product") },
                leadingIcon = {
                    Icon(Icons.Default.Inventory2, "Product")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false,
                shape = MaterialTheme.shapes.small
            )

            // Amount and Order Price Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Amount (read-only)
                OutlinedTextField(
                    value = item.amount.toString(),
                    onValueChange = { },
                    label = { Text("Amount") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    readOnly = true,
                    enabled = false,
                    shape = MaterialTheme.shapes.small
                )

                // Order Price (read-only)
                OutlinedTextField(
                    value = "%.2f".format(item.orderPrice),
                    onValueChange = { },
                    label = { Text("Order Price") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    readOnly = true,
                    enabled = false,
                    shape = MaterialTheme.shapes.small
                )
            }

            // Subtotal
            val subtotal = item.amount * item.orderPrice
            Surface(
                color = Color(0xFFF3F4F6),
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Subtotal:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$${"%.2f".format(subtotal)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4A90D6),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableOrderItemRow(
    item: OrderItemWithId,
    index: Int,
    products: List<ProductResponseDto>,
    onUpdate: (OrderItemWithId) -> Unit,
    onDelete: () -> Unit
) {
    var selectedProduct by remember { mutableStateOf(item.productId) }
    var amount by remember { mutableStateOf(item.amount.toString()) }
    var price by remember { mutableStateOf(item.orderPrice.toString()) }
    var productExpanded by remember { mutableStateOf(false) }

    // Update parent when values change
    LaunchedEffect(selectedProduct, amount, price) {
        val updatedItem = OrderItemWithId(
            id = item.id,
            productId = selectedProduct,
            amount = amount.toIntOrNull() ?: 0,
            orderPrice = price.toDoubleOrNull() ?: 0.0
        )
        onUpdate(updatedItem)
    }

    Surface(
        color = Color.White,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with remove button
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Item ${index + 1} (New)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Item",
                        tint = Color(0xFFDC2626)
                    )
                }
            }

            // Product Selection (editable)
            ExposedDropdownMenuBox(
                expanded = productExpanded,
                onExpandedChange = { productExpanded = !productExpanded }
            ) {
                OutlinedTextField(
                    value = products.find { it.id == selectedProduct }?.name ?: "Select Product",
                    onValueChange = { },
                    label = { Text("Product *") },
                    leadingIcon = {
                        Icon(Icons.Default.Inventory2, "Product")
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly = true,
                    isError = selectedProduct == 0L,
                    shape = MaterialTheme.shapes.small
                )

                ExposedDropdownMenu(
                    expanded = productExpanded,
                    onDismissRequest = { productExpanded = false }
                ) {
                    products.forEach { product ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(product.name ?: "Unnamed Product")
                                    Text(
                                        "Price: $${"%.2f".format(product.price)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            },
                            onClick = {
                                selectedProduct = product.id
                                // Auto-fill price when product is selected
                                if (price == "0.0" || price == "0") {
                                    price = "%.2f".format(product.price)
                                }
                                productExpanded = false
                            }
                        )
                    }
                }
            }

            // Amount and Order Price Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Amount (editable)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.toIntOrNull() != null) {
                            amount = newValue
                        }
                    },
                    label = { Text("Amount *") },
                    placeholder = { Text("1") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    isError = amount.isBlank() && amount.isNotEmpty(),
                    shape = MaterialTheme.shapes.small
                )

                // Order Price (editable)
                OutlinedTextField(
                    value = price,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                            price = newValue
                        }
                    },
                    label = { Text("Order Price *") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    isError = price.isBlank() && price.isNotEmpty(),
                    shape = MaterialTheme.shapes.small
                )
            }

            // Subtotal
            val subtotal = (amount.toIntOrNull() ?: 0) * (price.toDoubleOrNull() ?: 0.0)
            Surface(
                color = Color(0xFFF3F4F6),
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Subtotal:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$${"%.2f".format(subtotal)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4A90D6),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ModernDeleteConfirmationDialog(
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 380.dp, max = 450.dp)
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning icon
                Surface(
                    color = Color(0xFFFFFFFF),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color(0xFFDC2626)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Delete Item",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFFFFFFF),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Confirmation text
                Text(
                    text = "Are you sure you want to delete this item? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFFFFFF),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Cancel button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFFFFFF)
                        )
                    ) {
                        Text("Cancel")
                    }

                    // Delete button
                    Button(
                        onClick = onConfirmDelete,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626).copy(alpha = 0.8f),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteOrderConfirmationDialog(
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 400.dp, max = 500.dp)
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning icon
                Surface(
                    color = Color(0xFFFFFFFF),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color(0xFFDC2626)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Empty Order",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFFFFFFF),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Confirmation text
                Text(
                    text = "This order has no items left. Would you like to delete the entire order?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFFFFFF),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Keep Order button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFFFFFF)
                        )
                    ) {
                        Text("Keep")
                    }

                    // Delete Order button
                    Button(
                        onClick = onConfirmDelete,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626).copy(alpha = 0.8f),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}