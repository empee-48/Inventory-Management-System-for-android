package com.example.inventory.screens.composable.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
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
import com.example.inventory.viewModels.DashboardData

data class StatItem(
    val title: String,
    val value: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val trend: String?
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun QuickStatsGrid(data: DashboardData) {
    val stats = listOf(
        StatItem(
            title = "Total Products",
            value = data.totalProducts.toString(),
            subtitle = "${data.lowStockProducts.size} low stock",
            icon = Icons.Default.Inventory2,
            color = MaterialTheme.colorScheme.primary,
            trend = null
        ),
        StatItem(
            title = "Today's Revenue",
            value = "$${String.format("%.2f", data.todayRevenue)}",
            subtitle = "Total: $${String.format("%.2f", data.totalRevenue)}",
            icon = Icons.Default.TrendingUp,
            color = Color(0xFF10B981),
            trend = "up"
        ),
        StatItem(
            title = "Recent Sales",
            value = data.recentSales.size.toString(),
            subtitle = "${data.totalSales} total",
            icon = Icons.Default.PointOfSale,
            color = Color(0xFFF59E0B),
            trend = null
        ),
        StatItem(
            title = "Active Orders",
            value = data.recentOrders.size.toString(),
            subtitle = "${data.totalOrders} total",
            icon = Icons.Default.ShoppingCart,
            color = Color(0xFF8B5CF6),
            trend = null
        )
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        stats.chunked(2).forEach { rowStats ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowStats.forEach { stat ->
                    ModernStatCard(stat = stat, modifier = Modifier.weight(1f))
                }
                // Fill empty space if odd number
                if (rowStats.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ModernStatCard(stat: StatItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // Only card has shadow
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Icon with background - NO SHADOW
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = stat.color.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = stat.icon,
                        contentDescription = stat.title,
                        tint = stat.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Value and title
            Text(
                text = stat.value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stat.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle with trend
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stat.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                stat.trend?.let {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = when (it) {
                            "up" -> Icons.Default.ArrowUpward
                            "down" -> Icons.Default.ArrowDownward
                            else -> Icons.Default.Remove
                        },
                        contentDescription = "Trend",
                        modifier = Modifier.size(12.dp),
                        tint = when (it) {
                            "up" -> Color(0xFF10B981)
                            "down" -> Color(0xFFEF4444)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}
