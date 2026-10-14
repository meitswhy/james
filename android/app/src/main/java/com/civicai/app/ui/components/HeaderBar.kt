package com.civicai.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.civicai.app.data.local.UserSettings
import com.civicai.app.ui.theme.civicColors

enum class EngineStatus {
    READY, INDEXING, ERROR
}

@Composable
fun CivicTopHeaderBar(
    settings: UserSettings,
    engineStatus: EngineStatus = EngineStatus.READY,
    totalTokens: Int = 0,
    maxContextTokens: Int = 8000,
    isSpeakerOn: Boolean = false,
    onToggleTheme: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onLightClick: () -> Unit = {}
) {
    val civicColors = MaterialTheme.civicColors

    // Pulsing animation for INDEXING state
    val infiniteTransition = rememberInfiniteTransition(label = "statusPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val statusColor = when (engineStatus) {
        EngineStatus.READY -> civicColors.emerald
        EngineStatus.INDEXING -> Color(0xFFF59E0B).copy(alpha = pulseAlpha)
        EngineStatus.ERROR -> Color(0xFFEF4444)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = civicColors.card,
        border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Status Indicator & James Wordmark
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    onLightClick()
                    onOpenDiagnostics()
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "James",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = civicColors.textPrimary,
                    fontSize = 18.sp
                )
            }

            // Right: Utility Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Token Meter Badge (Only if real tokens > 0)
                if (totalTokens > 0) {
                    val tokenText = if (totalTokens >= 1000) String.format("%.1fk", totalTokens / 1000.0) else totalTokens.toString()
                    val maxText = if (maxContextTokens >= 1000) "${maxContextTokens / 1000}k" else maxContextTokens.toString()
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = civicColors.box,
                        border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.border)
                    ) {
                        Text(
                            text = "$tokenText / $maxText",
                            style = MaterialTheme.typography.labelSmall,
                            color = civicColors.textSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Theme Moon Toggle Button (32dp Square)
                HeaderSquareButton(
                    icon = if (civicColors.isDark) Icons.Default.Bedtime else Icons.Default.WbSunny,
                    contentDescription = "Toggle Theme",
                    isActive = false,
                    onClick = {
                        onLightClick()
                        onToggleTheme()
                    }
                )

                // Speaker Auto Read-Aloud Button (32dp Square)
                HeaderSquareButton(
                    icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = "Speaker Mode",
                    isActive = isSpeakerOn,
                    activeColor = Color(0xFF2563EB),
                    onClick = {
                        onLightClick()
                        onToggleSpeaker()
                    }
                )

                // Settings Cog Button (32dp Square)
                HeaderSquareButton(
                    icon = Icons.Default.Settings,
                    contentDescription = "Settings",
                    isActive = false,
                    onClick = {
                        onLightClick()
                        onOpenSettings()
                    }
                )
            }
        }
    }
}

@Composable
fun HeaderSquareButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isActive: Boolean = false,
    activeColor: Color = Color(0xFF2563EB),
    onClick: () -> Unit
) {
    val civicColors = MaterialTheme.civicColors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale = if (isPressed) 0.94f else 1.0f

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) activeColor.copy(alpha = 0.2f) else civicColors.box,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) activeColor.copy(alpha = 0.4f) else civicColors.border
        ),
        modifier = Modifier
            .size(32.dp)
            .scale(pressScale)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isActive) activeColor else civicColors.textSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
