package com.civicai.app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.civicai.app.data.local.DocumentEntity
import com.civicai.app.ui.theme.civicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentManagerSheet(
    isOpen: Boolean,
    documents: List<DocumentEntity>,
    onUploadDocument: (Uri, String) -> Unit,
    onDeleteDocument: (String) -> Unit,
    onRenameDocument: (String, String) -> Unit = { _, _ -> },
    onReindexDocument: (DocumentEntity) -> Unit = {},
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val civicColors = MaterialTheme.civicColors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val title = it.lastPathSegment?.substringAfterLast('/') ?: "Document_${System.currentTimeMillis()}"
            onUploadDocument(it, title)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = civicColors.card,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = civicColors.accent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Document Manager",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = civicColors.textPrimary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = civicColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dropzone Area
            DropzoneCard(
                onPickFile = { filePickerLauncher.launch("*/*") }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // List Title
            Text(
                text = "Indexed Documents (${documents.size})",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = civicColors.textPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (documents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = civicColors.textSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No documents uploaded yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = civicColors.textSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(documents, key = { it.id }) { doc ->
                        DocumentRowItem(
                            doc = doc,
                            onDelete = { onDeleteDocument(doc.id) },
                            onRename = { newName -> onRenameDocument(doc.id, newName) },
                            onReindex = { onReindexDocument(doc) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DropzoneCard(
    onPickFile: () -> Unit
) {
    val civicColors = MaterialTheme.civicColors
    val strokeColor = civicColors.accent.copy(alpha = 0.4f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(civicColors.box)
            .drawWithContent {
                drawContent()
                drawRoundRect(
                    color = strokeColor,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                    )
                )
            }
            .clickable(onClick = onPickFile)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(civicColors.accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = civicColors.accent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Tap or drop documents here",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = civicColors.textPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Supported: PDF, TXT, MD, Images (OCR)",
                style = MaterialTheme.typography.labelSmall,
                color = civicColors.textSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun DocumentRowItem(
    doc: DocumentEntity,
    onDelete: () -> Unit,
    onRename: (String) -> Unit,
    onReindex: () -> Unit
) {
    val civicColors = MaterialTheme.civicColors
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = civicColors.card,
        border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(civicColors.accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = civicColors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = civicColors.textPrimary,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Real status indicator
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (doc.chunkCount > 0) civicColors.emerald else Color(0xFFF59E0B))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (doc.chunkCount > 0) "${doc.chunkCount} chunks • Ready" else "Indexing...",
                            style = MaterialTheme.typography.labelSmall,
                            color = civicColors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = civicColors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Re-index") },
                        onClick = {
                            showMenu = false
                            onReindex()
                        },
                        leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }
        }
    }
}
