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
import com.example.inventory.screens.composable.common.LoadingComponent
import com.example.inventory.screens.composable.orders.DeleteSaleConfirmationDialog
import com.example.inventory.screens.composable.orders.ModernDeleteItemConfirmationDialog
import com.example.inventory.service.api.BatchApiService
import com.example.inventory.service.api.ProductApiService
import com.example.inventory.service.api.SalesApiService
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSaleScreen(
    sale: SalesResponseDto,
    onBack: () -> Unit,
    onSave: (Long, SaleCreateDto) -> Unit,
    onDeleteSale: () -> Unit,
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

    var saleDate by remember {
        mutableStateOf(sale.saleDate.toString())
    }

    // Track existing items (read-only) and new items (to be submitted)
    var existingItems by remember {
        mutableStateOf(
            sale.items.map { item ->
                SaleItemWithId(
                    id = item.id,
                    productId = item.productId,
                    batchId = item.batchId,
                    amount = item.amount.toDouble(),
                    price = item.price
                )
            }
        )
    }

    var newItems by remember {
        mutableStateOf<List<SaleItemWithId>>(emptyList())
    }

    var showDeleteSaleDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Combine all items for display and total calculation
    val allItems = remember(existingItems, newItems) {
        existingItems + newItems
    }

    // Calculate total amount (existing + new items)
    val totalAmount = remember(allItems) {
        allItems.sumOf { it.amount * it.price }
    }

    // Form validation - only validate new items since existing items are read-only
    val isFormValid = saleDate.isNotBlank() &&
            (existingItems.isNotEmpty() || newItems.isNotEmpty()) && // At least some items must exist
            newItems.all { it.productId > 0 && it.amount > 0 && it.price >= 0 && it.batchId > 0 } // Only validate new items

    // Load products and batches
    LaunchedEffect(Unit) {
        try {
            // Load products
            val productsResponse = productApiService.getProducts()
            if (productsResponse.isSuccessful) {
                products = productsResponse.body() ?: emptyList()
            } else {
                errorMessage = "Failed to load products: ${productsResponse.code()}"
            }

            // Load batches
            val batchesResponse = batchApiService.getBatches()
            if (batchesResponse.isSuccessful) {
                batches = batchesResponse.body() ?: emptyList()
            } else {
                errorMessage = "Failed to load batches: ${batchesResponse.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Filter products to only those with inStock > 0
    val availableProducts = remember(products) {
        products.filter { it.inStock > 0 }
    }

    // Function to submit sale update with ONLY NEW items
    val submitSaleUpdate = suspend {
        if (sale.id != null) {
            // Send ONLY NEW items to the backend (existing items are preserved)
            val itemsToSave = newItems.map { item ->
                SaleItemCreateDto(
                    productId = item.productId,
                    batchId = item.batchId,
                    amount = item.amount,
                    price = item.price
                )
            }

            val saleCreateDto = SaleCreateDto(
                saleDate = LocalDate.parse(saleDate),
                items = itemsToSave, // Only new items
                totalAmount = totalAmount
            )

            val response = salesApiService.editSale(sale.id, saleCreateDto)
            if (response.isSuccessful) {
                true
            } else {
                // Log the error for debugging
                println("Edit sale failed: ${response.code()} - ${response.errorBody()?.string()}")
                false
            }
        } else {
            false
        }
    }

    // Function to delete the entire sale
    val deleteSale = suspend {
        if (sale.id != null) {
            val response = salesApiService.deleteSale(sale.id)
            response.isSuccessful
        } else {
            false
        }
    }

    // Handle adding new item
    val handleAddItem = {
        newItems = newItems + SaleItemWithId(
            id = null, // New items don't have IDs
            productId = 0,
            batchId = 0,
            amount = 0.0,
            price = 0.0
        )
    }

    // Handle updating a new item
    val handleUpdateNewItem = { index: Int, updatedItem: SaleItemWithId ->
        newItems = newItems.toMutableList().apply {
            this[index] = updatedItem
        }
    }

    // Handle deleting an existing item
    val handleDeleteExistingItem = { itemId: Long?, index: Int ->
        coroutineScope.launch {
            try {
                // Delete the existing item from database
                if (itemId != null) {
                    val deleteResponse = salesApiService.deleteSaleItem(itemId)
                    if (deleteResponse.isSuccessful) {
                        // Remove from local state
                        existingItems = existingItems.toMutableList().apply {
                            removeAt(index)
                        }

                        // If no items remain, show delete sale dialog
                        if (allItems.isEmpty()) {
                            showDeleteSaleDialog = true
                        }
                    } else {
                        submitError = "Failed to delete sale item: ${deleteResponse.code()}"
                    }
                }
            } catch (e: Exception) {
                submitError = "Network error: ${e.message}"
            }
        }
    }

    // Handle deleting a new item
    val handleDeleteNewItem = { index: Int ->
        newItems = newItems.toMutableList().apply {
            removeAt(index)
        }

        // If no items remain, show delete sale dialog
        if (allItems.isEmpty()) {
            showDeleteSaleDialog = true
        }
    }

    val handleSave = {
        if (isFormValid && !isSubmitting && sale.id != null) {
            isSubmitting = true
            submitError = null

            coroutineScope.launch {
                try {
                    val updateSuccess = submitSaleUpdate()

                    if (updateSuccess) {
                        // Convert ONLY NEW items to SaleItemCreateDto for the callback
                        val itemsToSave = newItems.map { item ->
                            SaleItemCreateDto(
                                productId = item.productId,
                                batchId = item.batchId,
                                amount = item.amount,
                                price = item.price
                            )
                        }

                        val saleCreateDto = SaleCreateDto(
                            saleDate = LocalDate.parse(saleDate),
                            items = itemsToSave, // Only new items
                            totalAmount = totalAmount
                        )

                        onSave(sale.id, saleCreateDto)
                    } else {
                        submitError = "Failed to update sale. Please check if all new items are valid."
                    }
                } catch (e: Exception) {
                    submitError = "Network error: ${e.message}"
                } finally {
                    isSubmitting = false
                }
            }
        }
    }

    // Delete Sale Confirmation Dialog
    if (showDeleteSaleDialog) {
        DeleteSaleConfirmationDialog(
            onConfirmDelete = {
                showDeleteSaleDialog = false
                coroutineScope.launch {
                    try {
                        val deleteSuccess = deleteSale()
                        if (deleteSuccess) {
                            onDeleteSale()
                        } else {
                            submitError = "Failed to delete sale"
                        }
                    } catch (e: Exception) {
                        submitError = "Network error: ${e.message}"
                    }
                }
            },
            onDismiss = {
                showDeleteSaleDialog = false
                onBack()
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Edit Sale",
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
                        text = "Edit Sale Information",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )

                    // Sale ID Display (read-only)
                    OutlinedTextField(
                        value = sale.id?.toString() ?: "N/A",
                        onValueChange = { },
                        label = { Text("Sale ID") },
                        leadingIcon = {
                            Icon(Icons.Default.Numbers, "Sale ID")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false,
                        shape = MaterialTheme.shapes.small
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

                    // Info banner explaining the behavior
                    if (existingItems.isNotEmpty()) {
                        Surface(
                            color = Color(0xFFEFF6FF),
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
                                    tint = Color(0xFF4A90D6)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Existing items are preserved. You can only add new items or delete existing ones.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF1E40AF),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

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
                            Text("Add New Item")
                        }
                    }

                    // Items List
                    if (allItems.isEmpty()) {
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
                                    text = "Add items to update the sale",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF9CA3AF),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Display existing items first (read-only)
                        if (existingItems.isNotEmpty()) {
                            Text(
                                text = "Existing Items",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1A1A1A)
                            )
                            existingItems.forEachIndexed { index, item ->
                                EditSaleItemRow(
                                    item = item,
                                    index = index,
                                    products = products,
                                    onDelete = { handleDeleteExistingItem(item.id, index) }
                                )
                                if (index < existingItems.size - 1 || newItems.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }

                        // Display new items (editable)
                        if (newItems.isNotEmpty()) {
                            if (existingItems.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                            Text(
                                text = "New Items",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1A1A1A)
                            )
                            newItems.forEachIndexed { index, item ->
                                ModernSaleItemRow(
                                    item = item,
                                    index = index,
                                    availableProducts = availableProducts,
                                    batches = batches,
                                    onItemChange = { updatedItem ->
                                        handleUpdateNewItem(index, updatedItem)
                                    },
                                    onRemove = { handleDeleteNewItem(index) }
                                )
                                if (index < newItems.size - 1) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }

                    // Current Total Amount Card
                    if (allItems.isNotEmpty()) {
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
                                text = "$${"%.2f".format(sale.totalAmount)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Validation Message
                    if (!isFormValid && (saleDate.isNotBlank() || allItems.isNotEmpty())) {
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
                                    text = "Please fill in all required fields and ensure all new items are valid",
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

// Data class to hold sale items with their database IDs
data class SaleItemWithId(
    val id: Long? = null,
    val productId: Long,
    val batchId: Long,
    val amount: Double,
    val price: Double
)

@Composable
fun EditSaleItemRow(
    item: SaleItemWithId,
    index: Int,
    products: List<ProductResponseDto>,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Get product name
    val productName = products.find { it.id == item.productId }?.name ?: "Unknown Product"

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        ModernDeleteItemConfirmationDialog(
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
                    text = "Item ${index + 1}",
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

            // Batch ID Display (read-only)
            OutlinedTextField(
                value = item.batchId.toString(),
                onValueChange = { },
                label = { Text("Batch ID") },
                leadingIcon = {
                    Icon(Icons.Default.Numbers, "Batch ID")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false,
                shape = MaterialTheme.shapes.small
            )

            // Amount and Price Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Amount (read-only)
                OutlinedTextField(
                    value = "%.2f".format(item.amount),
                    onValueChange = { },
                    label = { Text("Amount") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    readOnly = true,
                    enabled = false,
                    shape = MaterialTheme.shapes.small
                )

                // Price (read-only)
                OutlinedTextField(
                    value = "%.2f".format(item.price),
                    onValueChange = { },
                    label = { Text("Price") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    readOnly = true,
                    enabled = false,
                    shape = MaterialTheme.shapes.small
                )
            }

            // Subtotal
            val subtotal = item.amount * item.price
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
fun ModernSaleItemRow(
    item: SaleItemWithId,
    index: Int,
    availableProducts: List<ProductResponseDto>,
    batches: List<BatchResponseDto>,
    onItemChange: (SaleItemWithId) -> Unit,
    onRemove: () -> Unit
) {
    var selectedProduct by remember { mutableStateOf(item.productId) }
    var selectedBatch by remember { mutableStateOf(item.batchId) }
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

    // Filter batches based on search query
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
        if (selectedProduct != 0L) {
            val firstBatch = availableBatches.firstOrNull()
            if (firstBatch != null && selectedBatch != firstBatch.id) {
                selectedBatch = firstBatch.id
            }
        }
    }

    // Update parent when values change
    LaunchedEffect(selectedProduct, selectedBatch, amount, price) {
        val updatedItem = SaleItemWithId(
            id = item.id, // Preserve the ID
            productId = selectedProduct,
            batchId = selectedBatch,
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
                    text = "New Item ${index + 1}",
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
                    isError = selectedProduct == 0L,
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
            if (selectedProduct != 0L) {
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
                        isError = selectedBatch == 0L,
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