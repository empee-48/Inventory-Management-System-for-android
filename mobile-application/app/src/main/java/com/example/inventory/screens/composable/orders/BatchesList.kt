package com.example.inventory.screens.composable.orders

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventory.data.BatchResponseDto
import com.example.inventory.data.SaleItemResponseDto
import com.example.inventory.service.api.BatchApiService
import com.example.inventory.screens.composable.common.LoadingComponent
import com.example.inventory.screens.composable.common.LoadingComponentSmall
import com.example.inventory.screens.composable.sales.formatDateTime
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BatchesList(
    batchesApiService: BatchApiService,
    onBack: () -> Unit
) {
    var batches by remember { mutableStateOf<List<BatchResponseDto>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var isInitialLoad by remember { mutableStateOf(true) }

    val fetchBatches: () -> Unit = {
        coroutineScope.launch {
            isRefreshing = true
            try {
                val response = batchesApiService.getBatches()
                if (response.isSuccessful) {
                    batches = response.body() ?: emptyList()
                    errorMessage = null
                } else {
                    errorMessage = "Failed to load batches: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.message}"
            } finally {
                isRefreshing = false
                isInitialLoad = false
            }
        }
    }

    // Load initial data
    LaunchedEffect(Unit) {
        if (batches == null && errorMessage == null) {
            fetchBatches()
        }
    }

    // Show loading state
    if ((isInitialLoad && batches == null) || (isRefreshing && batches == null)) {
        LoadingState()
        return
    }

    // Show error state
    if (errorMessage != null && batches == null) {
        BatchErrorState(
            errorMessage = errorMessage!!,
            onRetry = {
                errorMessage = null
                isInitialLoad = true
                fetchBatches()
            }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        val batchList = batches ?: emptyList()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ButtonPanel(
                    onRefresh = {
                        isRefreshing = true
                        fetchBatches()
                    },
                    onFilter = { showFilterDialog = true },
                    isRefreshing = isRefreshing,
                    hasActiveFilters = false // You can add filter logic later
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (batchList.isEmpty()) {
                item {
                    EmptyBatchesState()
                }
            } else {
                items(batchList, key = { it.id }) { batch ->
                    BatchCard(
                        batch = batch
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LoadingComponent(
            message = "Loading batches...",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun ButtonPanel(
    onRefresh: () -> Unit,
    onFilter: () -> Unit,
    isRefreshing: Boolean = false,
    hasActiveFilters: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            // Filter Button
            BlueAccentButton(
                onClick = onFilter,
                icon = Icons.Default.FilterList,
                label = "Filter",
                isActive = hasActiveFilters,
                modifier = Modifier.weight(1f)
            )

            // Minimal divider
            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            )

            // Refresh Button
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isRefreshing) {
                    LoadingComponentSmall(
                        message = "",
                        size = 16.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BlueAccentButton(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) {
            Color(0xFF4A90D6).copy(alpha = 0.12f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 200)
    )

    val contentColor by animateColorAsState(
        targetValue = if (isActive) {
            Color(0xFF4A90D6)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        },
        animationSpec = tween(durationMillis = 200)
    )

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor,
        tonalElevation = 0.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = contentColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BatchCard(
    batch: BatchResponseDto
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        onClick = { isExpanded = !isExpanded },
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Batch icon with your brand color
                Surface(
                    color = Color(0xFF4A90D6).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = "Batch",
                            tint = Color(0xFF4A90D6),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Batch info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Header with product name and stock
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = batch.productName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF1A1A1A),
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${batch.stockLeft} left",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF4A90D6)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Order info
                    Text(
                        text = "Order #${batch.orderId} • Product #${batch.productId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF666666)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Order date
                    Text(
                        text = "Ordered: ${batch.orderDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                }

                // Expand/collapse icon
                Icon(
                    imageVector = if (isExpanded)
                        Icons.Default.ArrowBackIos
                    else
                        Icons.Default.ArrowForwardIos,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Expanded content
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))

                // Sales information
                if (batch.sales.isNotEmpty()) {
                    Text(
                        text = "Sales History",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    batch.sales.forEach { sale ->
                        SaleItemRow(sale = sale)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Metadata
                Text(
                    text = "Details",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    batch.createdAt?.let {
                        InfoRow(label = "Created:", value = formatDateTime(batch.createdAt))
                    }
                    batch.lastModifiedAt?.let {
                        InfoRow(label = "Last Modified:", value = formatDateTime(batch.lastModifiedAt))
                    }
                    batch.createdBy?.let {
                        InfoRow(label = "Created By:", value = it)
                    }
                    batch.lastModifiedBy?.let {
                        InfoRow(label = "Last Modified By:", value = it)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(120.dp),
            color = Color(0xFF666666)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF666666)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SaleItemRow(sale: SaleItemResponseDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3F4F6)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1.5f)
            ) {
                // Sale Item ID and Sale ID
                Text(
                    text = "Item #${sale.id}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = "Sale #${sale.saleId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4A90D6)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Quantity
                Text(
                    text = "Qty: ${sale.amount}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A)
                )
            }

            Column(
                modifier = Modifier.weight(1.5f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$${"%.2f".format(sale.price)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4A90D6)
                )

                // Date
                sale.createdAt?.let {
                    Text(
                        text = it.format(DateTimeFormatter.ofPattern("MMM dd")),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyBatchesState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF4A90D6).copy(alpha = 0.1f),
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Inventory,
                contentDescription = "No Batches",
                tint = Color(0xFF4A90D6),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Batches Found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Batches will appear here when orders are processed",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun BatchErrorState(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Failed to Load Batches",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        FilledTonalButton(
            onClick = onRetry,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = Color(0xFF4A90D6),
                contentColor = Color.White
            )
        ) {
            Text("Try Again", style = MaterialTheme.typography.labelLarge)
        }
    }
}