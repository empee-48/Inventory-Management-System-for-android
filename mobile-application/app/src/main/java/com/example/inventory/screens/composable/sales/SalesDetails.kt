package com.example.inventory.screens.composable.sales

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventory.data.SalesResponseDto
import com.example.inventory.service.api.SalesApiService
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleDetailsScreen(
    sale: SalesResponseDto,
    onBack: () -> Unit,
    onEdit: (SalesResponseDto) -> Unit,
    onDelete: (SalesResponseDto) -> Unit,
    salesApiService: SalesApiService
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Handle delete confirmation
    val handleDelete: () -> Unit = {
        isDeleting = true
        deleteError = null

        coroutineScope.launch {
            try {
                val response = salesApiService.deleteSale(sale.id!!)
                if (response.isSuccessful) {
                    onDelete(sale)
                    showDeleteDialog = false
                } else {
                    deleteError = "Failed to delete sale: ${response.code()}"
                }
            } catch (e: Exception) {
                deleteError = "Network error: ${e.message}"
            } finally {
                isDeleting = false
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) {
                    showDeleteDialog = false
                }
            },
            title = {
                Text("Delete Sale")
            },
            text = {
                Column {
                    Text("Are you sure you want to delete \"Sale #${sale.id}\"?")
                    if (deleteError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = deleteError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = handleDelete,
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
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
                title = {
                    Text(
                        text = "Sale Details",
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
                    IconButton(onClick = { onEdit(sale) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }

                    // Delete Button
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
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
            // Header Section
            SaleHeaderSection(sale = sale)

            Spacer(modifier = Modifier.height(24.dp))

            // Metrics Cards Section
            MetricsCards(sale = sale)

            Spacer(modifier = Modifier.height(32.dp))

            // Sale Items Section
            SaleItemsSection(sale = sale)

            Spacer(modifier = Modifier.height(32.dp))

            // Sale Information Section
            SaleInformationSection(sale = sale)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SaleHeaderSection(sale: SalesResponseDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Sale Number and ID
        Text(
            text = "Sale #${sale.id}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "ID: ${sale.saleId}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Status Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusBadge(sale = sale)
        }
    }
}

@Composable
private fun StatusBadge(sale: SalesResponseDto) {
    Surface(
        color = Color(0xFFF0FDF4),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "✅",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Completed",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF059669),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MetricsCards(sale: SalesResponseDto) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Total Amount Card
        MetricCard(
            title = "Total Amount",
            value = "$${"%.2f".format(sale.totalAmount)}",
            accentColor = Color(0xFF4A90D6),
            emoji = "💰",
            modifier = Modifier.weight(1f)
        )

        // Items Card
        MetricCard(
            title = "Total Items",
            value = "${sale.items.size}",
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
private fun SaleItemsSection(sale: SalesResponseDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Section Title
        Text(
            text = "Sale Items",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        if (sale.items.isEmpty()) {
            Text(
                text = "No items in this sale",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666)
            )
        } else {
            sale.items.forEachIndexed { index, item ->
                SaleItemRow(
                    item = item,
                    index = index + 1
                )
                if (index < sale.items.size - 1) {
                    Divider(
                        color = Color(0xFFF0F0F0),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SaleItemRow(
    item: com.example.inventory.data.SaleItemResponseDto,
    index: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Item number
        Surface(
            color = Color(0xFFF3F4F6),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = index.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Item details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.productName ?: "Unknown Product",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Qty: ${item.amount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666)
                )
                Text(
                    text = "Price: $${"%.2f".format(item.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666)
                )
                Text(
                    text = "Subtotal: $${"%.2f".format(item.amount * item.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4A90D6),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SaleInformationSection(sale: SalesResponseDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Section Title
        Text(
            text = "Sale Information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Sale Date
        InfoItemWithBorder(
            icon = "📅",
            title = "Sale Date",
            content = sale.saleDate.toString()
        )

        // Created Date
        sale.createdAt?.let { createdAt ->
            InfoItemWithBorder(
                icon = "⏰",
                title = "Created",
                content = formatDateTime(createdAt)
            )
        }

        // Last Modified
        sale.lastModifiedAt?.let { modifiedAt ->
            InfoItemWithBorder(
                icon = "✏️",
                title = "Last Modified",
                content = formatDateTime(modifiedAt),
                showBorder = false
            )
        }
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

        // Bottom border
        if (showBorder) {
            Divider(
                color = Color(0xFFF0F0F0),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDateTime(localDateTime: java.time.LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")
    return localDateTime.format(formatter)
}