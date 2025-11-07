package com.example.inventory.screens.composable.mainscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModernTopAppBar(
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Settings icon
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Tune,
                contentDescription = "Settings",
                tint = Color(0xFF444444),
                modifier = Modifier.size(26.dp)
            )
        }

        Text(
            text = "Inventory Pro",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.W900,
                fontSize = 28.sp
            ),
            color = Color(0xFF444444)
        )

        IconButton(
            onClick = onLogoutClick,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ExitToApp,
                contentDescription = "Logout",
                tint = Color(0xFF444444),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}