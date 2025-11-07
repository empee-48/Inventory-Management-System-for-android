package com.example.inventory.screens.composable.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.inventory.data.CategoryResponseDto
import com.example.inventory.data.ProductCreateDto
import com.example.inventory.data.ProductResponseDto
import com.example.inventory.service.api.CategoryApiService
import com.example.inventory.service.api.ProductApiService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    product: ProductResponseDto,
    onBack: () -> Unit,
    onSave: (Long, ProductCreateDto) -> Unit,
    categoryApiService: CategoryApiService,
    productApiService: ProductApiService
) {
    var categories by remember { mutableStateOf<List<CategoryResponseDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var submitError by remember { mutableStateOf<String?>(null) }

    // Form state - pre-filled with current product data
    var selectedCategory by remember {
        mutableStateOf<CategoryResponseDto?>(null)
    }
    var productName by remember {
        mutableStateOf(product.name ?: "")
    }
    var description by remember {
        mutableStateOf(product.description ?: "")
    }
    var price by remember {
        mutableStateOf(product.price.toString())
    }
    var inStock by remember {
        mutableStateOf(product.inStock.toString())
    }
    var warningStockLevel by remember {
        mutableStateOf(product.warningStockLevel.toString())
    }
    var unit by remember {
        mutableStateOf(product.unit ?: "")
    }

    val coroutineScope = rememberCoroutineScope()

    // Form validation
    val isFormValid = selectedCategory != null &&
            productName.isNotBlank() &&
            price.isNotBlank() &&
            inStock.isNotBlank() &&
            warningStockLevel.isNotBlank() &&
            unit.isNotBlank()

    // Load categories and pre-select the current category
    LaunchedEffect(Unit) {
        try {
            val response = categoryApiService.getCategories()
            if (response.isSuccessful) {
                val loadedCategories = response.body() ?: emptyList()
                categories = loadedCategories

                // Pre-select the current product's category
                selectedCategory = loadedCategories.find { it.id == product.categoryId }
            } else {
                errorMessage = "Failed to load categories: ${response.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Handle product update
    val handleSave = {
        if (isFormValid && !isSubmitting && product.id != null) {
            isSubmitting = true
            submitError = null

            coroutineScope.launch {
                try {
                    val productCreateDto = ProductCreateDto(
                        categoryId = selectedCategory!!.id!!,
                        name = productName,
                        description = description,
                        price = price.toDouble(),
                        inStock = inStock.toDouble(),
                        warningStockLevel = warningStockLevel.toDouble(),
                        unit = unit
                    )

                    val response = productApiService.editProduct(product.id, productCreateDto)

                    if (response.isSuccessful) {
                        onSave(product.id, productCreateDto)
                    } else {
                        submitError = "Failed to update product: ${response.code()} - ${response.message()}"
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
                        text = "Edit Product",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp),
                        enabled = !isSubmitting
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(8.dp)
                        )
                    } else {
                        IconButton(
                            onClick = handleSave,
                            enabled = isFormValid && !isSubmitting
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            )
        }
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
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
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
                        CircularProgressIndicator()
                        Text(
                            text = "Loading categories...",
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
                            text = "Failed to Load Categories",
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
                                        val response = categoryApiService.getCategories()
                                        if (response.isSuccessful) {
                                            val loadedCategories = response.body() ?: emptyList()
                                            categories = loadedCategories
                                            selectedCategory = loadedCategories.find { it.id == product.categoryId }
                                        } else {
                                            errorMessage = "Failed to load categories: ${response.code()}"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Network error: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            enabled = !isSubmitting
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
                    Text(
                        text = "Edit Product Information",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Product ID Display (read-only)
                    OutlinedTextField(
                        value = product.id?.toString() ?: "N/A",
                        onValueChange = { },
                        label = { Text("Product ID") },
                        leadingIcon = {
                            Icon(Icons.Default.Inventory2, "Product ID")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false
                    )

                    // Category Selection
                    CategoryDropdown(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSubmitting
                    )

                    // Product Name
                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = { Text("Product Name *") },
                        placeholder = { Text("Enter product name") },
                        leadingIcon = {
                            Icon(Icons.Default.Inventory2, "Product Name")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = productName.isBlank() && productName.isNotEmpty(),
                        enabled = !isSubmitting
                    )

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        placeholder = { Text("Enter product description (optional)") },
                        leadingIcon = {
                            Icon(Icons.Default.Description, "Description")
                        },
                        singleLine = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 4,
                        enabled = !isSubmitting
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
                        leadingIcon = {
                            Icon(Icons.Default.Money, "Price")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        isError = price.isBlank() && price.isNotEmpty(),
                        enabled = !isSubmitting
                    )

                    // Current Stock
                    OutlinedTextField(
                        value = inStock,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                                inStock = newValue
                            }
                        },
                        label = { Text("Current Stock *") },
                        placeholder = { Text("0") },
                        leadingIcon = {
                            Icon(Icons.Default.Inventory2, "Current Stock")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        isError = inStock.isBlank() && inStock.isNotEmpty(),
                        enabled = !isSubmitting
                    )

                    // Warning Stock Level
                    OutlinedTextField(
                        value = warningStockLevel,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                                warningStockLevel = newValue
                            }
                        },
                        label = { Text("Warning Stock Level *") },
                        placeholder = { Text("0") },
                        leadingIcon = {
                            Icon(Icons.Default.Warning, "Warning Level")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        isError = warningStockLevel.isBlank() && warningStockLevel.isNotEmpty(),
                        enabled = !isSubmitting
                    )

                    // Unit
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit *") },
                        placeholder = { Text("pcs, kg, units, etc.") },
                        leadingIcon = {
                            Icon(Icons.Default.Inventory2, "Unit")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = unit.isBlank() && unit.isNotEmpty(),
                        enabled = !isSubmitting
                    )

                    // Validation Message
                    if (!isFormValid && (
                                productName.isNotBlank() ||
                                        price.isNotBlank() ||
                                        inStock.isNotBlank() ||
                                        warningStockLevel.isNotBlank() ||
                                        unit.isNotBlank()
                                )) {
                        Text(
                            text = "Please fill in all required fields (*)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    // Form Helper Text
                    Text(
                        text = "* Required fields",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
            }
        }
    }
}