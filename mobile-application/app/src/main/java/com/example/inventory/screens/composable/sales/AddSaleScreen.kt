package com.example.inventory.screens.composable.sales

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
import com.example.inventory.service.api.BatchApiService
import com.example.inventory.service.api.ProductApiService
import com.example.inventory.service.api.SalesApiService
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaleScreen(
    onBack: () -> Unit,
    onSave: (SaleCreateDto) -> Unit,
    salesApiService: SalesApiService,
    productApiService: ProductApiService,
    batchApiService: BatchApiService
) {
    var products by remember { mutableStateOf<List<ProductResponseDto>>(emptyList()) }
    var batches by remember { mutableStateOf<List<BatchResponseDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var submitError by remember { mutableStateOf<String?>(null) }

    // Form state
    var saleDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var saleItems by remember { mutableStateOf<List<SaleItemCreateDto>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()

    // Filter products to only those with inStock > 0
    val availableProducts = remember(products) {
        products.filter { it.inStock > 0 }
    }

    // Calculate total amount
    val totalAmount = remember(saleItems) {
        saleItems.sumOf { it.amount * it.price }
    }

    // Form validation
    val isFormValid = saleDate.isNotBlank() &&
            saleItems.isNotEmpty() &&
            saleItems.all { it.productId > 0 && it.amount > 0 && it.price >= 0 && it.batchId > 0 }

    // Load products and batches on startup
    LaunchedEffect(Unit) {
        try {
            // Load products
            val productsResponse = productApiService.getProducts()
            if (productsResponse.isSuccessful) {
                products = productsResponse.body() ?: emptyList()
                println("🛍️ Loaded ${products.size} products, ${availableProducts.size} available")
            } else {
                errorMessage = "Failed to load products: ${productsResponse.code()}"
            }

            // Load batches
            val batchesResponse = batchApiService.getBatches()
            if (batchesResponse.isSuccessful) {
                batches = batchesResponse.body() ?: emptyList()
                println("📦 Loaded ${batches.size} batches")
            } else {
                errorMessage = "Failed to load batches: ${batchesResponse.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Handle sale creation
    val handleSave = {
        if (isFormValid && !isSubmitting) {
            isSubmitting = true
            submitError = null

            coroutineScope.launch {
                try {
                    val saleCreateDto = SaleCreateDto(
                        saleDate = LocalDate.parse(saleDate),
                        items = saleItems,
                        totalAmount = totalAmount
                    )

                    val response = salesApiService.createSale(saleCreateDto)

                    if (response.isSuccessful) {
                        onSave(saleCreateDto)
                    } else {
                        submitError = "Failed to create sale: ${response.code()} - ${response.message()}"
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
                        text = "Add New Sale",
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
                            text = "Loading products and batches...",
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
                                        // Reload both products and batches
                                        val productsResponse = productApiService.getProducts()
                                        val batchesResponse = batchApiService.getBatches()

                                        if (productsResponse.isSuccessful) {
                                            products = productsResponse.body() ?: emptyList()
                                        }
                                        if (batchesResponse.isSuccessful) {
                                            batches = batchesResponse.body() ?: emptyList()
                                        }

                                        if (!productsResponse.isSuccessful() || !batchesResponse.isSuccessful()) {
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
                        text = "Sale Information",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )

                    // Sale Date
                    OutlinedTextField(
                        value = saleDate,
                        onValueChange = { saleDate = it },
                        label = { Text("Sale Date *") },
                        placeholder = { Text("YYYY-MM-DD") },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarToday, "Sale Date")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = saleDate.isBlank() && saleDate.isNotEmpty(),
                        shape = MaterialTheme.shapes.small
                    )

                    // Items Section
                    Text(
                        text = "Sale Items",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )

                    // Available Products Info
                    if (availableProducts.isEmpty()) {
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
                                    text = "No products available for sale. All products are out of stock.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    } else {
                        // Add Item Button
                        FilledTonalButton(
                            onClick = {
                                saleItems = saleItems + SaleItemCreateDto(
                                    productId = 0,
                                    batchId = 0,
                                    amount = 0.0,
                                    price = 0.0
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
                    if (saleItems.isEmpty()) {
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
                                    imageVector = Icons.Default.ShoppingCart,
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
                                    text = "Add items to create a sale",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF9CA3AF),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        saleItems.forEachIndexed { index, item ->
                            ModernSaleItemRow(
                                item = item,
                                index = index,
                                availableProducts = availableProducts,
                                batches = batches,
                                onItemChange = { updatedItem ->
                                    saleItems = saleItems.toMutableList().apply {
                                        this[index] = updatedItem
                                    }
                                },
                                onRemove = {
                                    saleItems = saleItems.toMutableList().apply {
                                        removeAt(index)
                                    }
                                }
                            )
                            if (index < saleItems.size - 1) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }

                    // Total Amount Card
                    if (saleItems.isNotEmpty()) {
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
                    if (!isFormValid && (saleDate.isNotBlank() || saleItems.isNotEmpty())) {
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
fun ModernSaleItemRow(
    item: SaleItemCreateDto,
    index: Int,
    availableProducts: List<ProductResponseDto>,
    batches: List<BatchResponseDto>,
    onItemChange: (SaleItemCreateDto) -> Unit,
    onRemove: () -> Unit
) {
    var selectedProduct by remember { mutableStateOf<Long?>(item.productId) }
    var selectedBatch by remember { mutableStateOf<Long?>(item.batchId) }
    var amount by remember { mutableStateOf(if (item.amount == 0.0) "" else item.amount.toString()) }
    var price by remember { mutableStateOf(if (item.price == 0.0) "" else item.price.toString()) }
    var productSearchQuery by remember { mutableStateOf("") }
    var batchSearchQuery by remember { mutableStateOf("") }
    var productExpanded by remember { mutableStateOf(false) }
    var batchExpanded by remember { mutableStateOf(false) }

    // Filter products based on search query
    val filteredProducts = remember(availableProducts, productSearchQuery) {
        if (productSearchQuery.isBlank()) {
            availableProducts
        } else {
            availableProducts.filter { product ->
                product.name?.contains(productSearchQuery, ignoreCase = true) == true
            }
        }
    }

    // Filter batches based on selected product and stock
    val availableBatches = remember(batches, selectedProduct) {
        batches.filter { batch ->
            batch.productId == selectedProduct && batch.stockLeft > 0
        }
    }

    // Filter batches based on search query (searching by ID since we're using id as batchNumber)
    val filteredBatches = remember(availableBatches, batchSearchQuery) {
        if (batchSearchQuery.isBlank()) {
            availableBatches
        } else {
            availableBatches.filter { batch ->
                batch.id.toString().contains(batchSearchQuery, ignoreCase = true)
            }
        }
    }

    // Auto-select first batch when product changes
    LaunchedEffect(selectedProduct) {
        if (selectedProduct != null && selectedProduct != 0L) {
            val firstBatch = availableBatches.firstOrNull()
            if (firstBatch != null && selectedBatch != firstBatch.id) {
                selectedBatch = firstBatch.id
            }
        }
    }

    // Update parent when values change
    LaunchedEffect(selectedProduct, selectedBatch, amount, price) {
        val updatedItem = SaleItemCreateDto(
            productId = selectedProduct ?: 0,
            batchId = selectedBatch ?: 0,
            amount = amount.toDoubleOrNull() ?: 0.0,
            price = price.toDoubleOrNull() ?: 0.0
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
                expanded = productExpanded,
                onExpandedChange = { productExpanded = !productExpanded }
            ) {
                OutlinedTextField(
                    value = productSearchQuery.ifBlank {
                        availableProducts.find { it.id == selectedProduct }?.name ?: ""
                    },
                    onValueChange = {
                        productSearchQuery = it
                        if (it.isNotBlank()) {
                            productExpanded = true
                        }
                    },
                    label = { Text("Search Product *") },
                    placeholder = { Text("Type to search products...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, "Search")
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    isError = selectedProduct == null || selectedProduct == 0L,
                    shape = MaterialTheme.shapes.small
                )

                if (productExpanded) {
                    ExposedDropdownMenu(
                        expanded = productExpanded,
                        onDismissRequest = { productExpanded = false }
                    ) {
                        if (filteredProducts.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No products found") },
                                onClick = { productExpanded = false }
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
                                        productSearchQuery = ""
                                        productExpanded = false
                                        // Auto-fill price when product is selected
                                        if (price.isEmpty()) {
                                            price = "%.2f".format(product.price)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Searchable Batch Selection (only shown when product is selected)
            if (selectedProduct != null && selectedProduct != 0L) {
                ExposedDropdownMenuBox(
                    expanded = batchExpanded,
                    onExpandedChange = { batchExpanded = !batchExpanded }
                ) {
                    OutlinedTextField(
                        value = batchSearchQuery.ifBlank {
                            availableBatches.find { it.id == selectedBatch }?.id?.toString() ?: ""
                        },
                        onValueChange = {
                            batchSearchQuery = it
                            if (it.isNotBlank()) {
                                batchExpanded = true
                            }
                        },
                        label = { Text("Select Batch *") },
                        placeholder = { Text("Type to search batch ID...") },
                        leadingIcon = {
                            Icon(Icons.Default.Numbers, "Batch")
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = batchExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        isError = selectedBatch == null || selectedBatch == 0L,
                        shape = MaterialTheme.shapes.small
                    )

                    if (batchExpanded) {
                        ExposedDropdownMenu(
                            expanded = batchExpanded,
                            onDismissRequest = { batchExpanded = false }
                        ) {
                            if (filteredBatches.isEmpty()) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "No batches available for this product",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF6B7280)
                                        )
                                    },
                                    onClick = { batchExpanded = false }
                                )
                            } else {
                                filteredBatches.forEach { batch ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = "Batch ID: ${batch.id}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = "Stock left: ${batch.stockLeft.toInt()} • Order Date: ${batch.orderDate}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF6B7280)
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedBatch = batch.id
                                            batchSearchQuery = ""
                                            batchExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Amount and Price Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                            amount = newValue
                        }
                    },
                    label = { Text("Amount *") },
                    placeholder = { Text("0") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    isError = amount.isBlank() && amount.isNotEmpty(),
                    shape = MaterialTheme.shapes.small
                )

                // Price
                OutlinedTextField(
                    value = price,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                            price = newValue
                        }
                    },
                    label = { Text("Price *") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    isError = price.isBlank() && price.isNotEmpty(),
                    shape = MaterialTheme.shapes.small
                )
            }

            // Subtotal
            val subtotal = (amount.toDoubleOrNull() ?: 0.0) * (price.toDoubleOrNull() ?: 0.0)
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