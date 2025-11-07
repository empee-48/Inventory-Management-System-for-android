package com.example.inventory.screens.composable.products

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventory.data.ProductResponseDto
import com.example.inventory.service.api.ProductApiService
import kotlinx.coroutines.launch

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
    productApiService: ProductApiService
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("❌", style = MaterialTheme.typography.headlineMedium)
                    Text("Delete Product")
                }
            },
            text = { Text("Are you sure you want to delete \"${product.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = handleDelete,
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    }
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    enabled = !isDeleting
                ) {
                    Text("Cancel")
                }
            }
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
                    // Edit Button
                    Surface(
                        onClick = { onEdit(product) },
                        shape = MaterialTheme.shapes.small,
                        color = Color.Transparent,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(horizontal = 4.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF4A90D6),
                                        Color(0xFF357ABD),
                                        Color(0xFF1E5FA4)
                                    )
                                ),
                                shape = MaterialTheme.shapes.small
                            )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Delete Button
                    Surface(
                        onClick = { showDeleteDialog = true },
                        shape = MaterialTheme.shapes.small,
                        color = Color.Transparent,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(horizontal = 4.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFF6B6B),
                                        Color(0xFFEE5A52),
                                        Color(0xFFDC2626)
                                    )
                                ),
                                shape = MaterialTheme.shapes.small
                            )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
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
            // Header Section
            ProductHeader(product = product)

            Spacer(modifier = Modifier.height(24.dp))

            // Metrics Cards Section
            MetricsCards(product = product)

            Spacer(modifier = Modifier.height(32.dp))

            // Product Information Section
            ProductInformation(product = product)

            Spacer(modifier = Modifier.height(32.dp))
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
            text = "SKU: ${product.productKey ?: "Not specified"}",
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