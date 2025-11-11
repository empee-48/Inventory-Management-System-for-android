package com.example.inventory.screens.composable.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.inventory.data.ProductResponseDto

@Composable
fun ModernAlertsSection(products: List<ProductResponseDto>) {
    // Separate products into low stock and out of stock
    val lowStockProducts = products.filter { it.inStock > 0 && it.inStock <= it.warningStockLevel }
    val outOfStockProducts = products.filter { it.inStock <= 0 }

    val allAlerts = lowStockProducts + outOfStockProducts

    if (allAlerts.isEmpty()) return

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Dynamic alert icon based on severity
            val hasCriticalAlerts = outOfStockProducts.isNotEmpty()
            Surface(
                shape = CircleShape,
                color = if (hasCriticalAlerts)
                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                else
                    Color(0xFFFF9800).copy(alpha = 0.1f), // Orange for warnings
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = if (hasCriticalAlerts) Icons.Default.Error else Icons.Default.Warning,
                        contentDescription = "Alerts",
                        tint = if (hasCriticalAlerts)
                            MaterialTheme.colorScheme.error
                        else
                            Color(0xFFFF9800), // Orange for warnings
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column {
                Text(
                    text = if (hasCriticalAlerts) "Critical Stock Alerts" else "Low Stock Alerts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = buildAlertsSubtitle(lowStockProducts.size, outOfStockProducts.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            allAlerts.take(3).forEach { product ->
                ModernAlertItem(product = product)
            }
        }

        if (allAlerts.size > 3) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "+${allAlerts.size - 3} more alerts",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun ModernAlertItem(product: ProductResponseDto) {
    val isOutOfStock = product.inStock <= 0
    val alertColor = if (isOutOfStock) MaterialTheme.colorScheme.error else Color(0xFFFF9800) // Orange
    val alertIcon = if (isOutOfStock) Icons.Default.Error else Icons.Default.Warning
    val statusText = if (isOutOfStock) "Out of stock" else "Low stock"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            1.dp,
            alertColor.copy(alpha = 0.2f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // Status indicator with dynamic color
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(alertColor)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name ?: "Unnamed Product",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = alertColor.copy(alpha = 0.1f),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = alertColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Stock information
                    if (!isOutOfStock) {
                        Text(
                            text = "${product.inStock} left",
                            style = MaterialTheme.typography.bodyMedium,
                            color = alertColor,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Min: ${product.warningStockLevel}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "No stock available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = alertColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Urgency indicator with dynamic icon
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = alertColor.copy(alpha = 0.1f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = alertIcon,
                        contentDescription = if (isOutOfStock) "Critical" else "Warning",
                        tint = alertColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Helper function to build the alerts subtitle
private fun buildAlertsSubtitle(lowStockCount: Int, outOfStockCount: Int): String {
    return when {
        outOfStockCount > 0 && lowStockCount > 0 ->
            "$outOfStockCount out of stock, $lowStockCount low stock"
        outOfStockCount > 0 ->
            "$outOfStockCount products out of stock"
        lowStockCount > 0 ->
            "$lowStockCount products low on stock"
        else ->
            "No stock alerts"
    }
}