package com.civicai.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.civicai.app.ui.theme.civicColors

data class OnboardingPageData(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val subtitle: String,
    val detail: String
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    var currentPage by remember { mutableIntStateOf(0) }
    val pages = remember {
        listOf(
            OnboardingPageData(
                icon = Icons.Default.Shield,
                title = "100% On-Device Privacy",
                subtitle = "Your files never leave this device",
                detail = "CivicAI processes documents entirely inside your phone's memory. Zero telemetry, zero cloud tracking, zero remote servers."
            ),
            OnboardingPageData(
                icon = Icons.Default.Lock,
                title = "Offline Intelligence",
                subtitle = "Works seamlessly in Airplane Mode",
                detail = "OCR, Indic language translation, document indexing, and grounded search run locally on device without requiring an active internet connection."
            ),
            OnboardingPageData(
                icon = Icons.Default.MenuBook,
                title = "Grounded Answers & Citations",
                subtitle = "Strict factual verification",
                detail = "Every response is strictly cited from your own uploaded documents with exact page and paragraph references. No hallucinations or synthetic placeholders."
            )
        )
    }

    val civicColors = MaterialTheme.civicColors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(civicColors.bg)
            .padding(24.dp)
    ) {
        // Top Skip Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onFinish) {
                Text(
                    text = "Skip",
                    color = civicColors.textSecondary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Center Content Slider
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val page = pages[currentPage]

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(civicColors.accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = civicColors.accent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = page.title,
                style = MaterialTheme.typography.displayMedium,
                color = civicColors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = page.subtitle,
                style = MaterialTheme.typography.titleLarge,
                color = civicColors.accent,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = page.detail,
                style = MaterialTheme.typography.bodyLarge,
                color = civicColors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Bottom Navigation Bar with Dots & Action Button
        Column(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            // Indicator Dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                pages.indices.forEach { index ->
                    val active = index == currentPage
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .height(6.dp)
                            .width(if (active) 24.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (active) civicColors.accent else civicColors.textSecondary.copy(alpha = 0.3f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (currentPage < pages.size - 1) {
                        currentPage++
                    } else {
                        onFinish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = civicColors.accent,
                    contentColor = civicColors.bg
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (currentPage == pages.size - 1) "Get Started" else "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (currentPage == pages.size - 1) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
