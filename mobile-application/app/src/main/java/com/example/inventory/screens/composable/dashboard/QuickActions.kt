package com.example.inventory.screens.composable.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun QuickActionsSection(
    isAdmin: Boolean,
    onActionClick: (String) -> Unit
) {
    Column {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val userActions = listOf(
            ActionItem("Add Product", Icons.Default.Add, MaterialTheme.colorScheme.primary),
            ActionItem("View Orders", Icons.Default.ShoppingCart, MaterialTheme.colorScheme.secondary),
            ActionItem("Analytics", Icons.Default.Analytics, Color(0xFF8B5CF6)),
            ActionItem("Inventory", Icons.Default.ListAlt, Color(0xFFF59E0B))
        )

        val adminActions = listOf(
            ActionItem("Manage Users", Icons.Default.People, Color(0xFFEC4899)),
            ActionItem("Settings", Icons.Default.Settings, Color(0xFF6B7280))
        )

        ActionsGrid(actions = userActions, onActionClick = onActionClick)

        if (isAdmin) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Admin Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            ActionsGrid(actions = adminActions, onActionClick = onActionClick)
        }
    }
}