package com.example.inventory.screens.composable.orders

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.inventory.data.BatchResponseDto
import com.example.inventory.data.OrderItemResponseDto
import com.example.inventory.data.OrderResponseDto
import com.example.inventory.service.api.BatchApiService
import com.example.inventory.service.api.OrdersApiService
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    order: OrderResponseDto,
    onBack: () -> Unit,
    onEdit: (OrderResponseDto) -> Unit,
    onDelete: (OrderResponseDto) -> Unit,
    orderApiService: OrdersApiService,
    batchApiService: BatchApiService
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCannotDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var isCheckingBatches by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    var batches by remember { mutableStateOf<List<BatchResponseDto>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()

    val handleDeleteCheck: () -> Unit = {
        isCheckingBatches = true
        coroutineScope.launch {
            try {
                val response = batchApiService.getBatches()
                if (response.isSuccessful) {
                    val allBatches = response.body() ?: emptyList()
                    batches = allBatches

                    val orderBatches = allBatches.filter { it.orderId == order.id }
                    val hasSales = orderBatches.any { batch ->
                        batch.sales.isNotEmpty()
                    }

                    if (hasSales) {
                        showCannotDeleteDialog = true
                    } else {
                        showDeleteDialog = true
                    }
                } else {
                    showDeleteDialog = true
                }
            } catch (e: Exception) {
                showDeleteDialog = true
            } finally {
                isCheckingBatches = false
            }
        }
    }

    // Handle delete confirmation
    val handleDelete: () -> Unit = {
        isDeleting = true
        deleteError = null

        coroutineScope.launch {
            try {
                val response = orderApiService.deleteOrder(order.id!!)
                if (response.isSuccessful) {
                    onDelete(order)
                    showDeleteDialog = false
                } else {
                    deleteError = "Failed to delete order: ${response.code()}"
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
        ConfirmDeleteDialog(
            onDismiss = { showDeleteDialog = false },
            isDeleting = isDeleting,
            order = order,
            deleteError = deleteError,
            onDelete = handleDelete
        )
    }

    // Cannot Delete Dialog
    if (showCannotDeleteDialog) {
        CannotDeleteOrderDialog(
            order = order,
            onDismiss = { showCannotDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Order Details",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Edit Button - Dark Gray
                    IconButton(onClick = { onEdit(order) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF666666)
                        )
                    }

                    // Delete Button - Dark Gray
                    IconButton(
                        onClick = handleDeleteCheck,
                        enabled = !isCheckingBatches
                    ) {
                        if (isCheckingBatches) {
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
            OrderHeaderSection(order = order)

            Spacer(modifier = Modifier.height(24.dp))

            // Metrics Cards Section
            MetricsCards(order = order)

            Spacer(modifier = Modifier.height(32.dp))

            // Order Items Section
            OrderItemsSection(order = order)

            Spacer(modifier = Modifier.height(32.dp))

            // Order Information Section
            OrderInformationSection(order = order)

            Spacer(modifier = Modifier.height(32.dp))

            // Metadata Section
            OrderMetadataSection(order = order)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    onDismiss: () -> Unit,
    isDeleting: Boolean,
    order: OrderResponseDto,
    deleteError: String?,
    onDelete: () -> Unit
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
                    text = "Delete Order",
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
                        text = "Are you sure you want to delete \"Order #${order.id}\"?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFFFFFF),
                        textAlign = TextAlign.Center
                    )

                    if (order.items.isNotEmpty()) {
                        Text(
                            text = "⚠️ This order contains ${order.items.size} items that will be removed.",
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

                    // Show delete error if any
                    deleteError?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFEF4444),
                            textAlign = TextAlign.Center
                        )
                    }
                }

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
                        ),
                        enabled = !isDeleting
                    ) {
                        Text("Cancel")
                    }

                    // Delete button
                    Button(
                        onClick = onDelete,
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
fun CannotDeleteOrderDialog(
    order: OrderResponseDto,
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
                    text = "Cannot Delete Order",
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
                        text = "This order cannot be deleted because it has batches that have been sold from.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFFFFFF),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "\"Order #${order.id}\" has sales history associated with it. To maintain data integrity, orders with sales records cannot be deleted.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCCCCCC),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Consider archiving the order instead of deleting it.",
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
                    Text("Understand")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun OrderMetadataSection(order: OrderResponseDto) {
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
                        text = order.createdBy ?: "Unknown",
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
                        text = order.createdAt?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")) ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Last Modified Information
                if (order.lastModifiedBy != null || order.lastModifiedAt != null) {

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
                            modifier = Modifier.width(100.dp)
                        )
                        Text(
                            text = order.lastModifiedBy ?: "Unknown",
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
                            modifier = Modifier.width(100.dp)
                        )
                        Text(
                            text = order.lastModifiedAt?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")) ?: "Unknown",
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
private fun OrderHeaderSection(order: OrderResponseDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Order Number and ID
        Text(
            text = "Order #${order.id}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "ID: ${order.id}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Status Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusBadge(order = order)
        }
    }
}

@Composable
private fun StatusBadge(order: OrderResponseDto) {
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
private fun MetricsCards(order: OrderResponseDto) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Total Amount Card
        MetricCard(
            title = "Total Amount",
            value = "$${"%.2f".format(order.totalAmount)}",
            accentColor = Color(0xFF4A90D6),
            emoji = "💰",
            modifier = Modifier.weight(1f)
        )

        // Items Card
        MetricCard(
            title = "Total Items",
            value = "${order.items.size}",
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
private fun OrderItemsSection(order: OrderResponseDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Section Title with Count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Order Items",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = "Count: ${order.items.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )
        }

        if (order.items.isEmpty()) {
            Text(
                text = "No items in this order",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666)
            )
        } else {
            order.items.forEachIndexed { index, item ->
                OrderItemRow(
                    item = item,
                    index = index + 1
                )
                if (index < order.items.size - 1) {
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
private fun OrderItemRow(
    item: OrderItemResponseDto,
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
                    text = "Price: $${"%.2f".format(item.orderPrice)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666)
                )
                Text(
                    text = "Subtotal: $${"%.2f".format(item.amount * item.orderPrice)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4A90D6),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Product ID: ${item.productId}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF999999)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun OrderInformationSection(order: OrderResponseDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Section Title
        Text(
            text = "Order Information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Supplier
        InfoItemWithBorder(
            icon = "🏢",
            title = "Supplier",
            content = "Supplier #${order.supplierId}"
        )

        // Order Date
        InfoItemWithBorder(
            icon = "📅",
            title = "Order Date",
            content = order.orderDate.toString()
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
private fun formatDateTime(localDateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")
    return localDateTime.format(formatter)
}