package com.example.inventory.screens.composable.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventory.data.ProductResponseDto

@Composable
fun ProductCard(
    product: ProductResponseDto,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header with product name and stock indicator
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Product Name and Key
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = product.name ?: "Unnamed Product",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = product.productKey ?: "No SKU",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Stock indicator
                StockIndicator(
                    inStock = product.inStock,
                    isLow = product.stockLevelLow
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category badge
            Surface(
                color = Color(0xFF4A90D6).copy(alpha = 0.1f), // Your theme color
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = product.categoryName ?: "Uncategorized",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4A90D6), // Your theme color
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            val description = product.description
            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Footer with metrics
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Price
                MetricItem(
                    label = "Price",
                    value = "$${"%.2f".format(product.price)}",
                    icon = Icons.Outlined.Inventory2,
                    iconColor = Color(0xFF4A90D6), // Your theme color
                    modifier = Modifier.weight(1f)
                )

                // Vertical divider
                Divider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                )

                // Stock
                MetricItem(
                    label = "In Stock",
                    value = "${product.inStock.toInt()}",
                    icon = Icons.Filled.Circle,
                    iconColor = when {
                        product.inStock <= 0 -> Color(0xFFDC2626)
                        product.stockLevelLow -> Color(0xFFD97706)
                        else -> Color(0xFF059669)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun StockIndicator(
    inStock: Double,
    isLow: Boolean
) {
    data class IndicatorData(
        val backgroundColor: Color,
        val textColor: Color,
        val text: String
    )

    val indicatorInfo = when {
        inStock <= 0 -> IndicatorData(
            backgroundColor = Color(0xFFFFF5F5),
            textColor = Color(0xFFDC2626),
            text = "Out of Stock"
        )
        isLow -> IndicatorData(
            backgroundColor = Color(0xFFFFFBEB),
            textColor = Color(0xFFD97706),
            text = "Low Stock"
        )
        else -> IndicatorData(
            backgroundColor = Color(0xFFF0FDF4),
            textColor = Color(0xFF059669),
            text = "In Stock"
        )
    }

    Surface(
        color = indicatorInfo.backgroundColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
    ) {
        Text(
            text = indicatorInfo.text,
            style = MaterialTheme.typography.labelSmall,
            color = indicatorInfo.textColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}