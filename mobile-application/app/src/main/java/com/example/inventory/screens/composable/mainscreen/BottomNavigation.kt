package com.example.inventory.screens.composable.mainscreen

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventory.screens.MainScreenTab

@Composable
public fun ModernBottomNavigation(
    currentTab: MainScreenTab,
    onTabSelected: (MainScreenTab) -> Unit
) {
    val darkerSkyBlue = Color(0xFF4A90D6) // Darker sky blue for better contrast

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 4.dp,
        modifier = Modifier.height(72.dp)
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    if (currentTab == MainScreenTab.DASHBOARD) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                    "Dashboard",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    "Dashboard",
                    fontSize = 12.sp,
                    fontWeight = if (currentTab == MainScreenTab.DASHBOARD) FontWeight.Bold else FontWeight.Medium
                )
            },
            selected = currentTab == MainScreenTab.DASHBOARD,
            onClick = { onTabSelected(MainScreenTab.DASHBOARD) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = darkerSkyBlue,
                selectedTextColor = darkerSkyBlue,
                unselectedIconColor = Color(0xFF666666),
                unselectedTextColor = Color(0xFF666666),
                indicatorColor = darkerSkyBlue.copy(alpha = 0.1f)
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    if (currentTab == MainScreenTab.PRODUCTS) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
                    "Products",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    "Products",
                    fontSize = 12.sp,
                    fontWeight = if (currentTab == MainScreenTab.PRODUCTS) FontWeight.Bold else FontWeight.Medium
                )
            },
            selected = currentTab == MainScreenTab.PRODUCTS,
            onClick = { onTabSelected(MainScreenTab.PRODUCTS) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = darkerSkyBlue,
                selectedTextColor = darkerSkyBlue,
                unselectedIconColor = Color(0xFF666666),
                unselectedTextColor = Color(0xFF666666),
                indicatorColor = darkerSkyBlue.copy(alpha = 0.1f)
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    if (currentTab == MainScreenTab.ORDERS) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                    "Orders",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    "Orders",
                    fontSize = 12.sp,
                    fontWeight = if (currentTab == MainScreenTab.ORDERS) FontWeight.Bold else FontWeight.Medium
                )
            },
            selected = currentTab == MainScreenTab.ORDERS,
            onClick = { onTabSelected(MainScreenTab.ORDERS) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = darkerSkyBlue,
                selectedTextColor = darkerSkyBlue,
                unselectedIconColor = Color(0xFF666666),
                unselectedTextColor = Color(0xFF666666),
                indicatorColor = darkerSkyBlue.copy(alpha = 0.1f)
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    if (currentTab == MainScreenTab.SALES) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                    "Sales",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    "Sales",
                    fontSize = 12.sp,
                    fontWeight = if (currentTab == MainScreenTab.SALES) FontWeight.Bold else FontWeight.Medium
                )
            },
            selected = currentTab == MainScreenTab.SALES,
            onClick = { onTabSelected(MainScreenTab.SALES) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = darkerSkyBlue,
                selectedTextColor = darkerSkyBlue,
                unselectedIconColor = Color(0xFF666666),
                unselectedTextColor = Color(0xFF666666),
                indicatorColor = darkerSkyBlue.copy(alpha = 0.1f)
            )
        )
    }
}