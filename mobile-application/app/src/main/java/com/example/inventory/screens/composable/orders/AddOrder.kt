package com.example.inventory.screens.composable.orders

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.inventory.data.*
import com.example.inventory.service.api.OrdersApiService
import com.example.inventory.service.api.ProductApiService
import com.example.inventory.service.api.SuppliersApiService
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrderScreen(
    onBack: () -> Unit,
    onSave: (OrderCreateDto) -> Unit,
    orderApiService: OrdersApiService,
    suppliersApiService: SuppliersApiService,
    productApiService: ProductApiService
) {
    var products by remember { mutableStateOf<List<ProductResponseDto>>(emptyList()) }
    var suppliers by remember { mutableStateOf<List<SupplierResponseDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var submitError by remember { mutableStateOf<String?>(null) }

    // Form state
    var selectedSupplier by remember { mutableStateOf<Long?>(null) }
    var orderDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var orderItems by remember { mutableStateOf<List<OrderItemCreateDto>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()

    // Calculate total amount
    val totalAmount = remember(orderItems) {
        orderItems.sumOf { it.amount * it.orderPrice }
    }

    // Form validation
    val isFormValid = selectedSupplier != null &&
            orderDate.isNotBlank() &&
            orderItems.isNotEmpty() &&
            orderItems.all { it.productId > 0 && it.amount > 0 && it.orderPrice >= 0 }

    // Load products and suppliers on startup
    LaunchedEffect(Unit) {
        try {
            // Load products
            val productsResponse = productApiService.getProducts()
            if (productsResponse.isSuccessful) {
                products = productsResponse.body() ?: emptyList()
            } else {
                errorMessage = "Failed to load products: ${productsResponse.code()}"
            }

            // Load suppliers
            val suppliersResponse = suppliersApiService.getSuppliers()
            if (suppliersResponse.isSuccessful) {
                suppliers = suppliersResponse.body() ?: emptyList()
                // Select first supplier by default if available
                if (suppliers.isNotEmpty() && selectedSupplier == null) {
                    selectedSupplier = suppliers.first().id
                }
            } else {
                errorMessage = "Failed to load suppliers: ${suppliersResponse.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Handle order creation
    val handleSave = {
        if (isFormValid && !isSubmitting && selectedSupplier != null) {
            isSubmitting = true
            submitError = null

            coroutineScope.launch {
                try {
                    val orderCreateDto = OrderCreateDto(
                        supplierId = selectedSupplier!!,
                        orderDate = LocalDate.parse(orderDate),
                        items = orderItems,
                        totalAmount = totalAmount
                    )

                    val response = orderApiService.createOrder(orderCreateDto)

                    if (response.isSuccessful) {
                        onSave(orderCreateDto)
                    } else {
                        submitError = "Failed to create order: ${response.code()} - ${response.message()}"
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
                        text = "Add New Order",
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
                        CircularProgressIndicator(
                            color = Color(0xFF4A90D6)
                        )
                        Text(
                            text = "Loading products and suppliers...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                            text = "Failed to Load Data",
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
                                        // Reload both products and suppliers
                                        val productsResponse = productApiService.getProducts()
                                        val suppliersResponse = suppliersApiService.getSuppliers()

                                        if (productsResponse.isSuccessful) {
                                            products = productsResponse.body() ?: emptyList()
                                        }
                                        if (suppliersResponse.isSuccessful) {
                                            suppliers = suppliersResponse.body() ?: emptyList()
                                        }

                                        if (!productsResponse.isSuccessful() || !suppliersResponse.isSuccessful()) {
                                            errorMessage = "Failed to load some data"
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
                        text = "Order Information",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )

                    // Supplier Selection
                    ModernSupplierDropdown(
                        selectedSupplier = selectedSupplier,
                        suppliers = suppliers,
                        onSupplierSelected = { supplierId ->
                            selectedSupplier = supplierId
                        }
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

                    // Available Products Info
                    if (products.isEmpty()) {
                        Surface(
                            color = Color(0xFFFFFBEB),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = Color(0xFFD97706)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "No products available. Please add products first.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    } else {
                        // Add Item Button
                        FilledTonalButton(
                            onClick = {
                                orderItems = orderItems + OrderItemCreateDto(
                                    productId = 0,
                                    amount = 0,
                                    orderPrice = 0.0
                                )
                            },
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
                                    text = "Add items to create an order",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF9CA3AF),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        orderItems.forEachIndexed { index, item ->
                            ModernOrderItemRow(
                                item = item,
                                index = index,
                                products = products,
                                onItemChange = { updatedItem ->
                                    orderItems = orderItems.toMutableList().apply {
                                        this[index] = updatedItem
                                    }
                                },
                                onRemove = {
                                    orderItems = orderItems.toMutableList().apply {
                                        removeAt(index)
                                    }
                                }
                            )
                            if (index < orderItems.size - 1) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }

                    // Total Amount Card
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
                                    text = "Total Amount",
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

                    // Validation Message
                    if (!isFormValid && (orderDate.isNotBlank() || orderItems.isNotEmpty())) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSupplierDropdown(
    selectedSupplier: Long?,
    suppliers: List<SupplierResponseDto>,
    onSupplierSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Filter suppliers based on search query
    val filteredSuppliers = remember(suppliers, searchQuery) {
        if (searchQuery.isBlank()) {
            suppliers
        } else {
            suppliers.filter { supplier ->
                supplier.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = searchQuery.ifBlank {
                suppliers.find { it.id == selectedSupplier }?.name ?: ""
            },
            onValueChange = {
                searchQuery = it
                if (it.isNotBlank()) {
                    expanded = true
                }
            },
            label = { Text("Supplier *") },
            placeholder = { Text("Type to search suppliers...") },
            leadingIcon = {
                Icon(Icons.Default.Business, "Supplier")
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            isError = selectedSupplier == null,
            shape = MaterialTheme.shapes.small
        )

        if (expanded) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (filteredSuppliers.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No suppliers found") },
                        onClick = { expanded = false }
                    )
                } else {
                    filteredSuppliers.forEach { supplier ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = supplier.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Contact: ${supplier.contact}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            },
                            onClick = {
                                onSupplierSelected(supplier.id)
                                searchQuery = ""
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernOrderItemRow(
    item: OrderItemCreateDto,
    index: Int,
    products: List<ProductResponseDto>,
    onItemChange: (OrderItemCreateDto) -> Unit,
    onRemove: () -> Unit
) {
    var selectedProduct by remember { mutableStateOf<Long?>(item.productId) }
    var amount by remember { mutableStateOf(if (item.amount == 0) "" else item.amount.toString()) }
    var orderPrice by remember { mutableStateOf(if (item.orderPrice == 0.0) "" else item.orderPrice.toString()) }
    var searchQuery by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    // Filter products based on search query
    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) {
            products
        } else {
            products.filter { product ->
                product.name?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    // Update parent when values change
    LaunchedEffect(selectedProduct, amount, orderPrice) {
        val updatedItem = OrderItemCreateDto(
            productId = selectedProduct ?: 0,
            amount = amount.toIntOrNull() ?: 0,
            orderPrice = orderPrice.toDoubleOrNull() ?: 0.0
        )
        onItemChange(updatedItem)
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
                    text = "Item ${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Item",
                        tint = Color(0xFFDC2626)
                    )
                }
            }

            // Searchable Product Selection
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = searchQuery.ifBlank {
                        products.find { it.id == selectedProduct }?.name ?: ""
                    },
                    onValueChange = {
                        searchQuery = it
                        if (it.isNotBlank()) {
                            expanded = true
                        }
                    },
                    label = { Text("Search Product *") },
                    placeholder = { Text("Type to search products...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, "Search")
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    isError = selectedProduct == null || selectedProduct == 0L,
                    shape = MaterialTheme.shapes.small
                )

                if (expanded) {
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        if (filteredProducts.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No products found") },
                                onClick = { expanded = false }
                            )
                        } else {
                            filteredProducts.forEach { product ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = product.name ?: "Unnamed Product",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "In stock: ${product.inStock.toInt()} • Price: $${"%.2f".format(product.price)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF6B7280)
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedProduct = product.id
                                        searchQuery = ""
                                        expanded = false
                                        // Auto-fill order price when product is selected
                                        if (orderPrice.isEmpty()) {
                                            orderPrice = "%.2f".format(product.price)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Amount and Order Price Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Amount (Integer input)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.toIntOrNull() != null) {
                            amount = newValue
                        }
                    },
                    label = { Text("Amount *") },
                    placeholder = { Text("0") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    isError = amount.isBlank() && amount.isNotEmpty(),
                    shape = MaterialTheme.shapes.small
                )

                // Order Price
                OutlinedTextField(
                    value = orderPrice,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                            orderPrice = newValue
                        }
                    },
                    label = { Text("Order Price *") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    isError = orderPrice.isBlank() && orderPrice.isNotEmpty(),
                    shape = MaterialTheme.shapes.small
                )
            }

            // Subtotal
            val subtotal = (amount.toIntOrNull() ?: 0) * (orderPrice.toDoubleOrNull() ?: 0.0)
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