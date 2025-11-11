package com.example.inventory.screens.composable.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventory.data.SalesResponseDto
import com.example.inventory.viewModels.DashboardData

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SalesGraphSection(data: DashboardData) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Modern Header with gradient accent
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Gradient background for icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Sales Chart",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Weekly Sales Trend",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Last 7 days performance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Enhanced Line Chart
            ModernWeeklySalesLineChart(sales = data.recentSales)

            Spacer(modifier = Modifier.height(24.dp))

            // Modern Summary Stats with improved spacing
            val weeklyTotal = data.recentSales.sumOf { it.totalAmount }
            val todaySales = data.todayRevenue
            val avgDaily = weeklyTotal / 7

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ModernSummaryStat(
                    title = "Weekly Total",
                    value = "$${String.format("%.2f", weeklyTotal)}",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                ModernSummaryStat(
                    title = "Today",
                    value = "$${String.format("%.2f", todaySales)}",
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )

                ModernSummaryStat(
                    title = "Avg Daily",
                    value = "$${String.format("%.2f", avgDaily)}",
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ModernWeeklySalesLineChart(sales: List<SalesResponseDto>) {
    val last7Days = getLast7Days()
    val dailySales = last7Days.map { day ->
        val daySales = sales.filter { it.saleDate == day }
        day to daySales.sumOf { it.totalAmount }
    }

    val maxSales = dailySales.maxOfOrNull { it.second } ?: 1.0
    val chartHeight = 160.dp
    val primaryColor = MaterialTheme.colorScheme.primary
    val gradientColors = listOf(
        primaryColor.copy(alpha = 0.3f),
        primaryColor.copy(alpha = 0.1f),
        Color.Transparent
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(chartHeight + 48.dp)
    ) {
        // Gradient area under the line
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .align(Alignment.TopCenter)
        ) {
            val points = dailySales.mapIndexed { index, (_, amount) ->
                val x = (size.width / 6) * index
                val y = size.height - (amount / maxSales * size.height).toFloat()
                androidx.compose.ui.geometry.Offset(x, y)
            }

            // Create gradient area path
            val areaPath = androidx.compose.ui.graphics.Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points.first().x, size.height)
                    points.forEach { point ->
                        lineTo(point.x, point.y)
                    }
                    lineTo(points.last().x, size.height)
                    close()
                }
            }

            // Draw gradient area
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = size.height,
                    endY = 0f
                )
            )

            // Draw the main line
            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    points.forEachIndexed { index, point ->
                        if (index == 0) {
                            moveTo(point.x, point.y)
                        } else {
                            lineTo(point.x, point.y)
                        }
                    }
                },
                color = primaryColor,
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )

            // Draw data points with glow effect
            points.forEach { point ->
                // Outer glow
                drawCircle(
                    color = primaryColor.copy(alpha = 0.2f),
                    radius = 12.dp.toPx(),
                    center = point
                )
                // Inner glow
                drawCircle(
                    color = primaryColor.copy(alpha = 0.4f),
                    radius = 8.dp.toPx(),
                    center = point
                )
                // Main point
                drawCircle(
                    color = Color.White,
                    radius = 6.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = primaryColor,
                    radius = 4.dp.toPx(),
                    center = point
                )
            }
        }

        // Enhanced X-axis labels
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(top = chartHeight + 12.dp)
        ) {
            dailySales.forEach { (day, amount) ->
                ModernDayLabel(
                    day = day,
                    amount = amount,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ModernDayLabel(day: java.time.LocalDate, amount: Double, modifier: Modifier = Modifier) {
    val isToday = day == java.time.LocalDate.now()
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val todayBackground = primaryColor.copy(alpha = 0.1f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        // Date container with today highlight
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = if (isToday) todayBackground else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                // Day of week
                Text(
                    text = day.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = if (isToday) primaryColor else surfaceVariant.copy(alpha = 0.7f),
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                )

                // Date
                Text(
                    text = day.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isToday) primaryColor else surfaceVariant,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.SemiBold
                )
            }
        }

        // Amount with smooth fade-in animation
        AnimatedVisibility(
            visible = amount > 0,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = "$${String.format("%.0f", amount)}",
                style = MaterialTheme.typography.labelSmall,
                color = surfaceVariant.copy(alpha = 0.9f),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun ModernSummaryStat(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val surfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, surfaceVariant.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = surfaceVariant.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getLast7Days(): List<java.time.LocalDate> {
    val today = java.time.LocalDate.now()
    return (6 downTo 0).map { today.minusDays(it.toLong()) }
}