package com.example.inventory.screens.composable.dashboard

import android.annotation.SuppressLint
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inventory.data.OrderResponseDto
import com.example.inventory.data.SalesResponseDto
import com.example.inventory.viewModels.DashboardData

@Composable
fun RecentActivitySection(data: DashboardData) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "Recent Activity",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Combine last 5 activities from both lists (already sorted)
        val recentActivities = buildList {
            // Add last 5 sales (assuming already sorted with newest last)
            addAll(data.recentSales.takeLast(5).map { it to "sale" })
            // Add last 5 orders (assuming already sorted with newest last)
            addAll(data.recentOrders.takeLast(5).map { it to "order" })
        }.takeLast(5) // Take last 5 from combined list to get newest overall

        if (recentActivities.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                recentActivities.forEach { (activity, type) ->
                    when (type) {
                        "sale" -> {
                            val sale = activity as SalesResponseDto
                            ModernActivityItem(
                                title = sale.id,
                                subtitle = "${sale.items.size} items • ${sale.saleDate}",
                                amount = sale.totalAmount,
                                type = "sale",
                                icon = Icons.Default.PointOfSale
                            )
                        }
                        "order" -> {
                            val order = activity as OrderResponseDto
                            ModernActivityItem(
                                title = order.id,
                                subtitle = "${order.items.size} items • ${order.orderDate}",
                                amount = order.totalAmount,
                                type = "order",
                                icon = Icons.Default.ShoppingCart
                            )
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun ModernActivityItem(
    title: Long,
    subtitle: String,
    amount: Double,
    type: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // Icon background - NO SHADOW
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (type) {
                    "sale" -> Color(0xFF10B981).copy(alpha = 0.1f)
                    else -> Color(0xFF8B5CF6).copy(alpha = 0.1f)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title.toString(),
                        tint = when (type) {
                            "sale" -> Color(0xFF10B981)
                            else -> Color(0xFF8B5CF6)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${if (type == "sale") "Sale" else "Order"} #$title",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "$${String.format("%.2f", amount)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = when (type) {
                    "sale" -> Color(0xFF10B981)
                    else -> Color(0xFF8B5CF6)
                }
            )
        }
    }
}