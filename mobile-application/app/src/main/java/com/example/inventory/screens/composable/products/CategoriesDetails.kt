package com.example.inventory.screens.composable.products

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.inventory.data.CategoryCreateDto
import com.example.inventory.data.CategoryResponseDto
import com.example.inventory.data.ProductResponseDto
import com.example.inventory.service.api.CategoryApiService
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailsScreen(
    category: CategoryResponseDto,
    onBack: () -> Unit,
    categoryApiService: CategoryApiService,
    onEditCategory: (CategoryResponseDto) -> Unit = {},
    onProductClick: (ProductResponseDto) -> Unit = {},
    onDeleteCategory: () -> Unit = {}
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var updatedCategory by remember { mutableStateOf(category) }
    val coroutineScope = rememberCoroutineScope()

    if (showEditDialog) {
        EditCategoryDialog(
            category = category,
            onDismiss = { showEditDialog = false },
            onEditCategory = { newName ->
                coroutineScope.launch {
                    try {
                        val categoryId = category.id ?: throw IllegalArgumentException("Category ID is null")
                        val createDto = CategoryCreateDto(name = newName)
                        val response = categoryApiService.editCategory(categoryId, createDto)
                        if (response.isSuccessful) {
                            val updatedCategoryResponse = response.body()
                            if (updatedCategoryResponse != null) {
                                updatedCategory = updatedCategoryResponse
                                onEditCategory(updatedCategoryResponse)
                            }
                            showEditDialog = false
                        }
                    } catch (e: Exception) {
                        // Handle error
                    }
                }
            }
        )
    }

    if (showDeleteDialog) {
        DeleteCategoryDialog(
            category = category,
            onDismiss = { showDeleteDialog = false },
            onDeleteCategory = {
                coroutineScope.launch {
                    try {
                        val categoryId = category.id ?: throw IllegalArgumentException("Category ID is null")
                        val response = categoryApiService.deleteCategory(categoryId)
                        if (response.isSuccessful) {
                            onDeleteCategory()
                        }
                    } catch (e: Exception) {
                        // Handle error
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Category Details",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Edit Button
                    Surface(
                        onClick = { showEditDialog = true },
                        shape = MaterialTheme.shapes.small,
                        color = Color.Transparent,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF4A90D6),
                                        Color(0xFF357ABD)
                                    )
                                ),
                                shape = MaterialTheme.shapes.small
                            )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Category",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Delete Button
                    Surface(
                        onClick = { showDeleteDialog = true },
                        shape = MaterialTheme.shapes.small,
                        color = Color.Transparent,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFF6B6B),
                                        Color(0xFFEE5A52)
                                    )
                                ),
                                shape = MaterialTheme.shapes.small
                            )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Category",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category Information Section
            item {
                CategoryInfoSection(category = updatedCategory)
            }

            // Products Section
            item {
                Text(
                    text = "Products (${updatedCategory.products.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            if (updatedCategory.products.isEmpty()) {
                item {
                    EmptyCategoryProductsState()
                }
            } else {
                items(updatedCategory.products, key = { it.id ?: 0L }) { product ->
                    CategoryProductItem(
                        product = product,
                        onClick = { onProductClick(product) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteCategoryDialog(
    category: CategoryResponseDto,
    onDismiss: () -> Unit,
    onDeleteCategory: () -> Unit
) {
    var isDeleting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            if (!isDeleting) {
                onDismiss()
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("❌", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "Delete Category",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Are you sure you want to delete \"${category.name}\"?",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (category.products.isNotEmpty()) {
                    Text(
                        text = "⚠️ This category contains ${category.products.size} product(s). Deleting it may affect these products.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD97706)
                    )
                }

                Text(
                    text = "This action cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isDeleting
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        isDeleting = true
                        onDeleteCategory()
                    },
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626)
                    )
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Deleting...")
                    } else {
                        Text("Delete")
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryDialog(
    category: CategoryResponseDto,
    onDismiss: () -> Unit,
    onEditCategory: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf(category.name) }
    var isEditing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            if (!isEditing) {
                onDismiss()
            }
        },
        title = {
            Text(
                text = "Edit Category",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Update the category name",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666)
                )

                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name") },
                    placeholder = { Text("Enter category name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isEditing,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFF4A90D6),
                        focusedLabelColor = Color(0xFF4A90D6)
                    )
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isEditing
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        if (categoryName.isNotBlank() && categoryName != category.name) {
                            isEditing = true
                            onEditCategory(categoryName)
                        } else {
                            onDismiss()
                        }
                    },
                    enabled = categoryName.isNotBlank() && categoryName != category.name && !isEditing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A90D6)
                    )
                ) {
                    if (isEditing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Updating...")
                    } else {
                        Text("Update")
                    }
                }
            }
        }
    )
}

@Composable
private fun CategoryInfoSection(category: CategoryResponseDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Category Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    color = Color(0xFF4A90D6).copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Category",
                            tint = Color(0xFF4A90D6),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = "Category ID: #${category.id}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF666666)
                    )
                }
            }

            // Statistics Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatItem(
                    title = "Total Products",
                    value = category.products.size.toString(),
                    emoji = "📦",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFF8F9FA),
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A90D6)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CategoryProductItem(
    product: ProductResponseDto,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Product Icon
            Surface(
                color = Color(0xFF4A90D6).copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = "Product",
                        tint = Color(0xFF4A90D6),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Product Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name ?: "Unnamed Product",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    product.price?.let { price ->
                        Text(
                            text = "$${"%.2f".format(price)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4A90D6),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    product.inStock?.let { stock ->
                        Text(
                            text = "${stock.toInt()} in stock",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (stock < 10) Color(0xFFDC2626) else Color(0xFF666666)
                        )
                    }
                }
            }

            // Chevron
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = "View Details",
                tint = Color(0xFF666666),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun EmptyCategoryProductsState() {
    Surface(
        color = Color.White,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color(0xFF4A90D6).copy(alpha = 0.1f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = "No Products",
                        tint = Color(0xFF4A90D6),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Text(
                text = "No Products in Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Products added to this category will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )
        }
    }
}