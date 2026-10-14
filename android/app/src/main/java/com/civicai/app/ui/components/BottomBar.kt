package com.civicai.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.civicai.app.ui.theme.civicColors

sealed class NavTab(val route: String, val title: String, val icon: ImageVector) {
    object Chat : NavTab("chat", "Chat", Icons.AutoMirrored.Filled.Comment)
    object Scanner : NavTab("scanner", "Scanner", Icons.Default.CameraAlt)
    object Policies : NavTab("policies", "Policies", Icons.Default.MenuBook)
    object History : NavTab("history", "History", Icons.Default.History)
}

@Composable
fun CivicBottomBar(
    currentRoute: String,
    onTabSelected: (NavTab) -> Unit,
    onLightClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        NavTab.Chat,
        NavTab.Scanner,
        NavTab.Policies,
        NavTab.History
    )

    val civicColors = MaterialTheme.civicColors

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = civicColors.card,
        border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(60.dp)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = currentRoute == tab.route
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateColorAsState(
                    targetValue = if (selected) civicColors.accent else civicColors.textSecondary,
                    animationSpec = tween(200),
                    label = "tabColor"
                )

                val pressScale = if (isPressed) 0.96f else 1.0f

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) civicColors.accent.copy(alpha = 0.15f) else Color.Transparent,
                    border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, civicColors.accent.copy(alpha = 0.35f)) else null,
                    modifier = Modifier
                        .scale(pressScale)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            onLightClick()
                            onTabSelected(tab)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = scale,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = scale,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
