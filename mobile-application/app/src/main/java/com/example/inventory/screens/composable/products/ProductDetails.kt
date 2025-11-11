package com.example.inventory.screens.composable.products

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.inventory.data.ProductResponseDto
import com.example.inventory.service.api.ProductApiService
import com.example.inventory.service.api.SalesApiService
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

data class StatusInfo(
val backgroundColor: Color,
val textColor: Color,
val text: String,
val emoji: String
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    product: ProductResponseDto,
    onBack: () -> Unit,
    onEdit: (ProductResponseDto) -> Unit,
    onDelete: (ProductResponseDto) -> Unit,
    productApiService: ProductApiService,
    salesApiService: SalesApiService
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCannotDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var isCheckingSales by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val handleDeleteCheck: () -> Unit = {
        isCheckingSales = true
        coroutineScope.launch {
            try {
                // Check if there are any sale items for this product
                val response = salesApiService.getSaleItems(productId = product.id)
                if (response.isSuccessful) {
                    val saleItems = response.body()
                    if (saleItems.isNullOrEmpty()) {
                        showDeleteDialog = true
                    } else {
                        showCannotDeleteDialog = true
                    }
                } else {
                    showDeleteDialog = true
                }
            } catch (e: Exception) {
                showDeleteDialog = true
            } finally {
                isCheckingSales = false
            }
        }
    }

    val handleDelete: () -> Unit = {
        isDeleting = true
        coroutineScope.launch {
            try {
                val response = productApiService.deleteProduct(product.id!!)
                if (response.isSuccessful) {
                    onDelete(product)
                    showDeleteDialog = false
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isDeleting = false
            }
        }
    }

    if (showDeleteDialog) {
        DeleteProductDialog(
            product = product,
            onDismiss = { showDeleteDialog = false },
            onDeleteProduct = handleDelete,
            isDeleting = isDeleting
        )
    }

    if (showCannotDeleteDialog) {
        CannotDeleteProductDialog(
            product = product,
            onDismiss = { showCannotDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Product Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Edit Button - Dark Gray
                    IconButton(
                        onClick = { onEdit(product) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF666666)
                        )
                    }

                    // Delete Button - Dark Gray
                    IconButton(
                        onClick = handleDeleteCheck,
                        enabled = !isCheckingSales
                    ) {
                        if (isCheckingSales) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF666666)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFF666666)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            ProductHeader(product = product)

            Spacer(modifier = Modifier.height(24.dp))

            MetricsCards(product = product)

            Spacer(modifier = Modifier.height(32.dp))

            // Product Information Section
            ProductInformation(product = product)

            Spacer(modifier = Modifier.height(32.dp))

            // Metadata Section
            ProductMetadataSection(product = product)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CannotDeleteProductDialog(
    product: ProductResponseDto,
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
                    color = Color(0xFFFFFBEB),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color(0xFFD97706)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Cannot Delete Product",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFFFFFFF),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Explanation text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "This product cannot be deleted because it has been sold.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFFFFFF),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "\"${product.name}\" has sales history associated with it. To maintain data integrity, products with sales records cannot be deleted.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCCCCCC),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "You can archive the product or update its status instead.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFB800),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A90D6),
                        contentColor = Color.White
                    )
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ProductMetadataSection(product: ProductResponseDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Section Title
        Text(
            text = "Metadata",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Created Information
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Created By",
                        tint = Color(0xFF666666),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Created by:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666),
                        modifier = Modifier.width(100.dp)
                    )
                    Text(
                        text = product.createdBy ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Created At",
                        tint = Color(0xFF666666),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Created at:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666),
                        modifier = Modifier.width(100.dp)
                    )
                    Text(
                        text = product.createdAt?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")) ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Last Modified Information
                if (product.lastModifiedBy != null || product.lastModifiedAt != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Last Modified By",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Last modified by:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF666666),
                            modifier = Modifier.width(110.dp)
                        )
                        Text(
                            text = product.lastModifiedBy ?: "Unknown",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditCalendar,
                            contentDescription = "Last Modified At",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Last modified at:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF666666),
                            modifier = Modifier.width(110.dp)
                        )
                        Text(
                            text = product.lastModifiedAt?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")) ?: "Unknown",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductHeader(product: ProductResponseDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Product Name and SKU
        Text(
            text = product.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "SKU: ${product.id.toString()}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Status Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusBadge(product = product)
        }
    }
}

@Composable
fun DeleteProductDialog(
    product: ProductResponseDto,
    onDismiss: () -> Unit,
    onDeleteProduct: () -> Unit,
    isDeleting: Boolean
) {
    Dialog(
        onDismissRequest = {
            if (!isDeleting) {
                onDismiss()
            }
        },
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
                    text = "Delete Product",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFFFFFFF),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Confirmation text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Are you sure you want to delete \"${product.name}\"?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFFFFFF),
                        textAlign = TextAlign.Center
                    )

                    if (product.inStock > 0) {
                        Text(
                            text = "⚠️ This product has ${product.inStock.toInt()} units in stock that will be removed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFB800),
                            textAlign = TextAlign.Center
                        )
                    }

                    Text(
                        text = "This action cannot be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFDC2626),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Keep Product button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFFFFFF)
                        ),
                        enabled = !isDeleting
                    ) {
                        Text("Keep")
                    }

                    // Delete Product button
                    Button(
                        onClick = onDeleteProduct,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626).copy(alpha = 0.8f),
                            contentColor = Color.White
                        ),
                        enabled = !isDeleting
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
        }
    }
}

@Composable
private fun StatusBadge(product: ProductResponseDto) {
    val statusInfo = when {
        product.inStock <= 0 -> StatusInfo(
            backgroundColor = Color(0xFFFEF2F2),
            textColor = Color(0xFFDC2626),
            text = "Out of Stock",
            emoji = "❌"
        )
        product.stockLevelLow -> StatusInfo(
            backgroundColor = Color(0xFFFFFBEB),
            textColor = Color(0xFFD97706),
            text = "Low Stock",
            emoji = "⚠️"
        )
        else -> StatusInfo(
            backgroundColor = Color(0xFFF0FDF4),
            textColor = Color(0xFF059669),
            text = "In Stock",
            emoji = "✅"
        )
    }

    Surface(
        color = statusInfo.backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = statusInfo.emoji,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = statusInfo.text,
                style = MaterialTheme.typography.labelMedium,
                color = statusInfo.textColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MetricsCards(product: ProductResponseDto) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Price Card
        MetricCard(
            title = "Price",
            value = "$${"%.2f".format(product.price)}",
            accentColor = Color(0xFF4A90D6),
            emoji = "💰",
            modifier = Modifier.weight(1f)
        )

        // Stock Card
        MetricCard(
            title = "In Stock",
            value = "${product.inStock.toInt()}",
            accentColor = Color(0xFF1A1A1A),
            emoji = "📦",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    accentColor: Color,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
        shadowElevation = 4.dp,
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Emoji at the top
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Value
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProductInformation(product: ProductResponseDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Section Title
        Text(
            text = "Product Information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Category
        InfoItemWithBorder(
            icon = "📁",
            title = "Category",
            content = product.categoryName ?: "Uncategorized"
        )

        // Description
        if (!product.description.isNullOrBlank()) {
            InfoItemWithBorder(
                icon = "📝",
                title = "Description",
                content = product.description
            )
        }

        // Stock Analysis
        InfoItemWithBorder(
            icon = "📊",
            title = "Stock Analysis",
            content = getStockAnalysis(product)
        )

        // Warning Level
        InfoItemWithBorder(
            icon = "⚠️",
            title = "Warning Level",
            content = "${product.warningStockLevel} ${product.unit}",
            showBorder = false
        )
    }
}

@Composable
private fun InfoItemWithBorder(
    icon: String,
    title: String,
    content: String,
    showBorder: Boolean = true
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.size(32.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1A1A1A),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF666666),
                    lineHeight = 24.sp
                )
            }
        }

        if (showBorder) {
            Divider(
                color = Color(0xFFF0F0F0),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun getStockAnalysis(product: ProductResponseDto): String {
    return when {
        product.inStock <= 0 -> "This product is currently out of stock and requires immediate restocking."
        product.inStock <= product.warningStockLevel -> "Stock levels are low. Consider placing a reorder soon."
        product.inStock <= product.warningStockLevel * 2 -> "Stock levels are adequate for current demand."
        else -> "Stock levels are healthy and well above the warning threshold."
    }
}