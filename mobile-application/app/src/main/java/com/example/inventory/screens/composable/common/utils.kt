package com.example.inventory.screens.composable.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LoadingComponent(
    modifier: Modifier = Modifier,
    message: String = "Loading...",
    size: Dp = 120.dp,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    showMessage: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated Geometric Loader
        GeometricLoader(
            size = size,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor
        )

        if (showMessage) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun GeometricLoader(
    size: Dp = 120.dp,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Rotation animation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Pulsing scale animation
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Dot opacity animation (staggered)
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                0.3f at 0
                1f at 500
                0.3f at 1000
                0.3f at 2000
            }
        )
    )

    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                0.3f at 0
                0.3f at 500
                1f at 1000
                0.3f at 1500
                0.3f at 2000
            }
        )
    )

    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                0.3f at 0
                0.3f at 1000
                1f at 1500
                0.3f at 2000
            }
        )
    )

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2, size.toPx() / 2)
            val radius = size.toPx() * 0.35f
            val dotRadius = size.toPx() * 0.08f

            // Draw rotating hexagon
            rotate(rotation) {
                drawPolygon(
                    center = center,
                    radius = radius * scale,
                    sides = 6,
                    color = primaryColor.copy(alpha = 0.1f),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Draw animated dots at hexagon vertices
            val angles = listOf(0f, 60f, 120f, 180f, 240f, 300f)
            val alphas = listOf(dot1Alpha, dot2Alpha, dot3Alpha, dot1Alpha, dot2Alpha, dot3Alpha)

            angles.forEachIndexed { index, angle ->
                val rad = Math.toRadians(angle.toDouble()).toFloat()
                val x = center.x + radius * cos(rad)
                val y = center.y + radius * sin(rad)
                val dotAlpha = alphas[index]

                drawCircle(
                    color = primaryColor.copy(alpha = dotAlpha),
                    center = Offset(x, y),
                    radius = dotRadius * scale
                )
            }

            // Draw center dot
            drawCircle(
                color = primaryColor,
                center = center,
                radius = dotRadius * 0.8f
            )
        }
    }
}

@Composable
fun LoadingComponentSmall(
    modifier: Modifier = Modifier,
    message: String = "Loading...",
    size: Dp = 40.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        PulsingDotLoader(size = size, color = color)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun PulsingDotLoader(
    size: Dp = 40.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition()

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            drawCircle(
                color = color.copy(alpha = alpha),
                center = Offset(size.toPx() / 2, size.toPx() / 2),
                radius = (size.toPx() / 4) * scale
            )
        }
    }
}

@Composable
fun ShimmerLoadingItem(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val shimmerBackgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val shimmerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            // Base background - simple rectangle without rounded corners
            drawRect(
                color = shimmerBackgroundColor
            )

            // Shimmer effect
            val shimmerWidth = size.width / 3
            drawRect(
                color = shimmerColor,
                topLeft = Offset(translateAnim - shimmerWidth, 0f),
                size = Size(shimmerWidth, size.height)
            )
        }
    }
}

@Composable
fun ShimmerCardLoading(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        ShimmerLoadingItem(modifier = Modifier.height(100.dp))
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerLoadingItem(modifier = Modifier.height(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        ShimmerLoadingItem(modifier = Modifier.height(20.dp))
    }
}

// Extension function for drawing polygons - this is NOT a composable function
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPolygon(
    center: Offset,
    radius: Float,
    sides: Int,
    color: Color,
    strokeWidth: Float = 2f
) {
    val angleStep = 360f / sides

    for (i in 0 until sides) {
        val startAngle = i * angleStep
        val endAngle = (i + 1) * angleStep

        val startRad = Math.toRadians(startAngle.toDouble()).toFloat()
        val endRad = Math.toRadians(endAngle.toDouble()).toFloat()

        val startX = center.x + radius * cos(startRad)
        val startY = center.y + radius * sin(startRad)
        val endX = center.x + radius * cos(endRad)
        val endY = center.y + radius * sin(endRad)

        drawLine(
            color = color,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = strokeWidth
        )
    }
}
