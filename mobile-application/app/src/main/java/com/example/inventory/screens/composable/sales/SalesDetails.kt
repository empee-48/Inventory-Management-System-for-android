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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    fun setShowDeleteDialogFalse(){
        showDeleteDialog = false
    }

    if (showDeleteDialog) {
        DeleteSaleDialog(
            sale = sale,
            onDismiss = { setShowDeleteDialogFalse() },
            onDeleteSale = handleDelete,
            isDeleting = isDeleting
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
                    // Edit Button - Dark Gray
                    IconButton(onClick = { onEdit(sale) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF666666)
                        )
                    }

                    // Delete Button - Dark Gray
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFF666666)
                        )
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

            // Metadata Section
            SaleMetadataSection(sale = sale)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DeleteSaleDialog(
    sale: SalesResponseDto,
    onDismiss: () -> Unit,
    onDeleteSale: () -> Unit,
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
                        text = "Are you sure you want to delete sale \"#${sale.id}\"?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFFFFFF),
                        textAlign = TextAlign.Center
                    )

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

                    Button(
                        onClick = onDeleteSale,
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


@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SaleMetadataSection(sale: SalesResponseDto) {
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
                        text = sale.createdBy ?: "Unknown",
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
                        text = sale.createdAt?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")) ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Last Modified Information
                if (sale.lastModifiedBy != null || sale.lastModifiedAt != null) {

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
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            text = sale.lastModifiedBy ?: "Unknown",
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
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            text = sale.lastModifiedAt?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")) ?: "Unknown",
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
        // Section Title with Count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sale Items",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = "Count: ${sale.items.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )
        }

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
fun formatDateTime(localDateTime: java.time.LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")
    return localDateTime.format(formatter)
}