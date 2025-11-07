package com.example.inventory.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventory.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class CarouselSlide(
    val title: String,
    val description: String,
    val imageRes: Int,
    val backgroundColor: Color,
    val accentColor: Color
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GetStartedScreen(
    onGetStarted: () -> Unit
) {
    val carouselSlides = remember {
        listOf(
            CarouselSlide(
                title = "Smart Inventory\nManagement",
                description = "Track your products in real-time with our advanced inventory system",
                imageRes = R.drawable.ic_launcher_foreground, // Replace with actual image
                backgroundColor = Color(0xFF6366F1),
                accentColor = Color(0xFF4F46E5)
            ),
            CarouselSlide(
                title = "Wireless Earpods\nCollection",
                description = "Manage your audio products with seamless inventory control",
                imageRes = R.drawable.ic_launcher_foreground,
                backgroundColor = Color(0xFF06B6D4),
                accentColor = Color(0xFF0891B2)
            ),
            CarouselSlide(
                title = "Fast Charging\nSolutions",
                description = "Keep track of charging accessories and power banks",
                imageRes = R.drawable.ic_launcher_foreground,
                backgroundColor = Color(0xFF10B981),
                accentColor = Color(0xFF059669)
            ),
            CarouselSlide(
                title = "Mobile Accessories\nGalaxy",
                description = "Organize cases, cables, and mobile accessories effortlessly",
                imageRes = R.drawable.ic_launcher_foreground,
                backgroundColor = Color(0xFFF59E0B),
                accentColor = Color(0xFFD97706)
            ),
            CarouselSlide(
                title = "Tech Gadgets\nHub",
                description = "Complete inventory solution for all your tech products",
                imageRes = R.drawable.ic_launcher_foreground,
                backgroundColor = Color(0xFF3182BD), // Your brand color
                accentColor = Color(0xFF2B6CB0)
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { carouselSlides.size })
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll carousel
    LaunchedEffect(pagerState.currentPage) {
        while (true) {
            delay(4000) // 4 seconds per slide
            val nextPage = (pagerState.currentPage + 1) % carouselSlides.size
            coroutineScope.launch {
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Carousel Background with Gradient
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val slide = carouselSlides[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                slide.backgroundColor,
                                slide.accentColor
                            )
                        )
                    )
            ) {
                // Product Image Placeholder (Replace with actual images)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Product showcase - you'll replace these with actual product images
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Product visualization - you can replace this with actual images
                        when (page) {
                            0 -> ProductVisualization("📱", "Inventory System")
                            1 -> ProductVisualization("🎧", "Wireless Earpods")
                            2 -> ProductVisualization("⚡", "Fast Chargers")
                            3 -> ProductVisualization("📱", "Mobile Accessories")
                            4 -> ProductVisualization("💻", "Tech Gadgets")
                        }
                    }
                }

                // Content Overlay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 160.dp, start = 32.dp, end = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = slide.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 40.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = slide.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }
        }

        // Page Indicators
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(carouselSlides.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                val width = if (pagerState.currentPage == iteration) 24.dp else 8.dp

                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(color)
                        .animateContentSize()
                )
            }
        }

        // Get Started Button at Bottom (replaced Skip button)
        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 32.dp, end = 32.dp)
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF3182BD)
            ),
            shape = RoundedCornerShape(8.dp), // Small border radius
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Get Started",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ProductVisualization(emoji: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = emoji,
            fontSize = 64.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}