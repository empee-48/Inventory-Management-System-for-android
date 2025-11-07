package com.example.inventory.screens.composable.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.inventory.screens.formatRoles
import com.example.inventory.service.TokenManager

data class UserDetailItem(val label: String, val value: String, val icon: ImageVector)

@Composable
fun UserProfileSection(
    user: com.example.inventory.data.UserResponseDto?,
    isLoading: Boolean,
    tokenManager: TokenManager,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // Modern header with subtle background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            if (isLoading) {
                LoadingUserProfile()
            } else {
                UserProfileContent(
                    user = user,
                    tokenManager = tokenManager,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Elegant details section
        UserDetailsGrid(user = user)
    }
}

@Composable
fun LoadingUserProfile() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                strokeWidth = 2.5.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Loading profile...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun UserProfileContent(
    user: com.example.inventory.data.UserResponseDto?,
    tokenManager: TokenManager
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // Modern profile avatar with gradient border
        Box(
            modifier = Modifier
                .size(80.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor,
                                primaryColor.copy(alpha = 0.7f)
                            )
                        ),
                        radius = size.width / 2 + 2.dp.toPx()
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = user?.username ?: "Guest User",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            RoleBadge(tokenManager = tokenManager)
        }
    }
}

@Composable
fun RoleBadge(tokenManager: TokenManager) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = when (tokenManager.getUserRole()) {
            "admin" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
        },
        modifier = Modifier
            .height(32.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when (tokenManager.getUserRole()) {
                            "admin" -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.secondary
                        }
                    )
            )
            Text(
                text = tokenManager.getUserRole().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = when (tokenManager.getUserRole()) {
                    "admin" -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.secondary
                }
            )
        }
    }
}

@Composable
fun UserDetailsGrid(user: com.example.inventory.data.UserResponseDto?) {
    val userDetails = listOf(
        UserDetailItem("User ID", user?.id?.toString() ?: "—", Icons.Default.Fingerprint),
        UserDetailItem("Username", user?.username ?: "—", Icons.Default.Person),
        UserDetailItem("Roles", formatRoles(user?.roles), Icons.Default.Badge)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        userDetails.forEachIndexed { index, detail ->
            UserDetailRow(detail, isLast = index == userDetails.lastIndex)
        }
    }
}

@Composable
fun UserDetailRow(detail: UserDetailItem, isLast: Boolean = false) {
    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            // Elegant icon background
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = detail.icon,
                    contentDescription = detail.label,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = detail.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = detail.value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (!isLast) {
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                thickness = 1.dp,
                modifier = Modifier.padding(start = 68.dp)
            )
        }
    }
}