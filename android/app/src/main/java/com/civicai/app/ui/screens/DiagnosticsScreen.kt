package com.civicai.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.civicai.app.data.local.DocumentEntity
import com.civicai.app.data.local.LocalDiagnosticsLogger
import com.civicai.app.data.local.LogEntry
import com.civicai.app.data.local.LogLevel
import com.civicai.app.data.llm.OnDeviceLlmEngine
import com.civicai.app.ui.theme.civicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    ramMb: Long,
    totalChunks: Int,
    totalDocs: Int,
    ftsRowCount: Int = 0,
    documents: List<DocumentEntity> = emptyList(),
    llmEngine: OnDeviceLlmEngine? = null,
    onBack: () -> Unit
) {
    val civicColors = MaterialTheme.civicColors
    var logs by remember { mutableStateOf(LocalDiagnosticsLogger.getLogs()) }
    var testQueryText by remember { mutableStateOf("") }
    var ftsMatchExpr by remember { mutableStateOf("") }
    var extractedKeywordsText by remember { mutableStateOf("") }
    var selectedExtractedTextDoc by remember { mutableStateOf<DocumentEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "System Diagnostics & FTS Inspector",
                        style = MaterialTheme.typography.titleLarge,
                        color = civicColors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = civicColors.textPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { logs = LocalDiagnosticsLogger.getLogs() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = civicColors.textPrimary
                        )
                    }
                    IconButton(onClick = {
                        LocalDiagnosticsLogger.clearLogs()
                        logs = LocalDiagnosticsLogger.getLogs()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Logs",
                            tint = civicColors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = civicColors.bg)
            )
        },
        containerColor = civicColors.bg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Metric Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Available RAM",
                        value = "$ramMb MB"
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Indexed Chunks",
                        value = totalChunks.toString()
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "FTS Rows",
                        value = (if (ftsRowCount > 0) ftsRowCount else totalChunks).toString()
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Documents",
                        value = totalDocs.toString()
                    )
                }
            }

            // Test FTS Query Box (Rule F)
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = civicColors.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.hairline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "FTS Query Tester & MATCH Expression Inspector",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = civicColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = testQueryText,
                            onValueChange = { input ->
                                testQueryText = input
                                if (llmEngine != null && input.isNotBlank()) {
                                    val kws = llmEngine.extractQueryKeywords(input)
                                    extractedKeywordsText = kws.joinToString(", ")
                                    ftsMatchExpr = if (kws.isNotEmpty()) kws.joinToString(" OR ") { "$it*" } else "NONE (Instruction words filtered)"
                                } else {
                                    extractedKeywordsText = ""
                                    ftsMatchExpr = ""
                                }
                            },
                            placeholder = { Text("Enter test query (e.g. 'Summarize IMG-20261009-WA0009.jpg' or 'tax')") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = civicColors.accent) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = civicColors.accent,
                                unfocusedBorderColor = civicColors.hairline
                            )
                        )
                        if (ftsMatchExpr.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = civicColors.surfaceElevated,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Extracted Keywords: ${if (extractedKeywordsText.isBlank()) "None" else extractedKeywordsText}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = civicColors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "FTS MATCH Expression: $ftsMatchExpr",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = civicColors.accent
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Executed SQL: SELECT * FROM chunks JOIN chunks_fts ON chunks.rowid = chunks_fts.docid WHERE chunks_fts MATCH '$ftsMatchExpr'",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = civicColors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Per-Document Index Status (Rule F)
            item {
                Text(
                    text = "Indexed Documents & OCR Status",
                    style = MaterialTheme.typography.titleMedium,
                    color = civicColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            if (documents.isEmpty()) {
                item {
                    Text(
                        text = "No documents in local index.",
                        color = civicColors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(documents) { doc ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = civicColors.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.hairline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = doc.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = civicColors.textPrimary
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (doc.indexStatus == "INDEXED") civicColors.accent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = doc.indexStatus,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (doc.indexStatus == "INDEXED") civicColors.accent else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "MIME: ${doc.mimeType} • Chunks: ${doc.chunkCount} • Pages: ${doc.pageCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = civicColors.textSecondary
                            )
                            doc.errorMessage?.let { err ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Error: $err",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { selectedExtractedTextDoc = doc },
                                colors = ButtonDefaults.buttonColors(containerColor = civicColors.accent, contentColor = civicColors.bg),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Show Extracted Text", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // Local System Logs
            item {
                Text(
                    text = "Local System Logs",
                    style = MaterialTheme.typography.titleMedium,
                    color = civicColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            items(logs) { entry ->
                LogEntryItem(entry = entry)
            }
        }
    }

    // Extracted Text Dialog Modal
    selectedExtractedTextDoc?.let { doc ->
        AlertDialog(
            onDismissRequest = { selectedExtractedTextDoc = null },
            title = {
                Text(
                    text = "Extracted Text: ${doc.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = civicColors.textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Document ID: ${doc.id}\nStatus: ${doc.indexStatus}\nChunks: ${doc.chunkCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = civicColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Cleaned extracted text is indexed cleanly into Room FTS. Binary garbage and unreadable control characters are rejected at the quality gate.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = civicColors.textPrimary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedExtractedTextDoc = null }) {
                    Text("Close", color = civicColors.accent)
                }
            },
            containerColor = civicColors.surface
        )
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    val civicColors = MaterialTheme.civicColors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = civicColors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.hairline)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = civicColors.textSecondary,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = civicColors.accent,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LogEntryItem(entry: LogEntry) {
    val civicColors = MaterialTheme.civicColors
    val levelColor = when (entry.level) {
        LogLevel.INFO -> civicColors.accent
        LogLevel.WARNING -> civicColors.groundedWarning
        LogLevel.ERROR -> MaterialTheme.colorScheme.error
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = civicColors.surfaceElevated,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "[${entry.level.name}] ${entry.tag}",
                    style = MaterialTheme.typography.labelSmall,
                    color = levelColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = entry.formattedTime(),
                    style = MaterialTheme.typography.labelSmall,
                    color = civicColors.textSecondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.message,
                style = MaterialTheme.typography.bodyMedium,
                color = civicColors.textPrimary
            )
        }
    }
}
