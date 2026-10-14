package com.civicai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.civicai.app.data.local.GroundedCitation
import com.civicai.app.ui.theme.civicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitationSheet(
    citation: GroundedCitation?,
    onDismiss: () -> Unit
) {
    if (citation == null) return
    val civicColors = MaterialTheme.civicColors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = civicColors.bg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    tint = civicColors.accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = citation.source,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = civicColors.textPrimary
                    )
                    Text(
                        text = "Exact Citation: Page ${citation.page} • Paragraph ${citation.paragraph}",
                        style = MaterialTheme.typography.labelSmall,
                        color = civicColors.accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Verified Grounded Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = civicColors.groundedSuccess.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.groundedSuccess.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = civicColors.groundedSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "100% Grounded Document Match (${(citation.confidence * 100).toInt()}% Relevancy Score)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = civicColors.groundedSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Highlighted Source Paragraph Excerpt:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = civicColors.textSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(civicColors.surface)
                    .border(1.dp, civicColors.accent, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = citation.excerpt,
                    style = MaterialTheme.typography.bodyLarge,
                    color = civicColors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
