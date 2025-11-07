package com.example.inventory.screens.composable.sales

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.inventory.data.*
import com.example.inventory.screens.composable.orders.DeleteSaleConfirmationDialog
import com.example.inventory.screens.composable.orders.ModernDeleteItemConfirmationDialog
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
    productApiService: ProductApiService
) {
    var products by remember { mutableStateOf<List<ProductResponseDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var submitError by remember { mutableStateOf<String?>(null) }

    var saleDate by remember {
        mutableStateOf(sale.saleDate.toString())
    }

    var saleItems by remember {
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

    var showDeleteSaleDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Calculate total amount
    val totalAmount = remember(saleItems) {
        saleItems.sumOf { it.amount * it.price }
    }

    // Form validation - only check if we have items and valid date
    val isFormValid = saleDate.isNotBlank() && saleItems.isNotEmpty()

    // Load products
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

    // Function to submit sale update with current items
    val submitSaleUpdate = suspend {
        if (sale.id != null) {
            // Send all remaining items to the backend
            val itemsToSave = saleItems.map { item ->
                SaleItemCreateDto(
                    productId = item.productId,
                    batchId = item.batchId,
                    amount = item.amount,
                    price = item.price
                )
            }

            val saleCreateDto = SaleCreateDto(
                saleDate = LocalDate.parse(saleDate),
                items = itemsToSave,
                totalAmount = totalAmount
            )

            val response = salesApiService.editSale(sale.id, saleCreateDto)
            response.isSuccessful
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

    val handleDeleteItem = { itemId: Long?, index: Int ->
        coroutineScope.launch {
            try {
                var shouldUpdateSale = false

                // First delete the item from database if it exists
                if (itemId != null) {
                    val deleteResponse = salesApiService.deleteSaleItem(itemId)
                    if (deleteResponse.isSuccessful) {
                        shouldUpdateSale = true
                    } else {
                        submitError = "Failed to delete sale item: ${deleteResponse.code()}"
                        return@launch
                    }
                }

                // Remove from local state
                saleItems = saleItems.toMutableList().apply {
                    removeAt(index)
                }

                // If we deleted from database and there are still items, update the sale total
                if (shouldUpdateSale && saleItems.isNotEmpty()) {
                    val updateSuccess = submitSaleUpdate()
                    if (!updateSuccess) {
                        submitError = "Failed to update sale total after deletion"
                    }
                }

                // Show delete sale dialog if no items remain
                if (saleItems.isEmpty()) {
                    showDeleteSaleDialog = true
                }

            } catch (e: Exception) {
                submitError = "Network error: ${e.message}"
            }
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
                        // Convert to SaleItemCreateDto for the callback
                        val itemsToSave = saleItems.map { item ->
                            SaleItemCreateDto(
                                productId = item.productId,
                                batchId = item.batchId,
                                amount = item.amount,
                                price = item.price
                            )
                        }

                        val saleCreateDto = SaleCreateDto(
                            saleDate = LocalDate.parse(saleDate),
                            items = itemsToSave,
                            totalAmount = totalAmount
                        )

                        onSave(sale.id, saleCreateDto)
                    } else {
                        submitError = "Failed to update sale"
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
                            // Sale successfully deleted, navigate back
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
                // User chose to keep the empty sale, just navigate back
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
                                    text = "All items have been removed",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF9CA3AF),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        saleItems.forEachIndexed { index, item ->
                            EditSaleItemRow(
                                item = item,
                                index = index,
                                products = products,
                                onDelete = { handleDeleteItem(item.id, index) }
                            )
                            if (index < saleItems.size - 1) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }

                    // Current Total Amount Card
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