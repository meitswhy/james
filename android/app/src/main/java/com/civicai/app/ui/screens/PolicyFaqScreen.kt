package com.civicai.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.civicai.app.data.local.ChunkEntity
import com.civicai.app.data.local.DocumentEntity
import com.civicai.app.data.local.GroundedCitation
import com.civicai.app.ui.theme.civicColors

@Composable
fun PolicyFaqScreen(
    chunks: List<ChunkEntity>,
    documents: List<DocumentEntity> = emptyList(),
    onOpenDocumentManager: () -> Unit,
    onOpenCitation: (GroundedCitation) -> Unit,
    onAskInChat: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var expandedChunkId by remember { mutableStateOf<String?>(chunks.firstOrNull()?.id) }
    val civicColors = MaterialTheme.civicColors

    val categories = remember(chunks) {
        (listOf("All") + chunks.map { it.category }.distinct()).distinct()
    }

    val filteredChunks = remember(searchQuery, selectedCategory, chunks) {
        chunks.filter { chunk ->
            val matchCat = selectedCategory == "All" || chunk.category == selectedCategory
            val matchQuery = searchQuery.isBlank() || chunk.content.contains(searchQuery, ignoreCase = true) || chunk.documentTitle.contains(searchQuery, ignoreCase = true)
            matchCat && matchQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(civicColors.bg)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Directory Intro Banner Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = civicColors.card,
            border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = civicColors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Policy FAQ Directory",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = civicColors.textPrimary,
                            fontSize = 16.sp
                        )
                    }

                    // "+" Document Manager Sheet Action Button
                    Surface(
                        shape = CircleShape,
                        color = civicColors.box,
                        border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.border),
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onOpenDocumentManager)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Document",
                                tint = civicColors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Verified municipal regulations with exact page citations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = civicColors.textSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search water bills, property tax, building permits...", style = MaterialTheme.typography.bodySmall, color = civicColors.textSecondary, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = civicColors.textSecondary, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = civicColors.accent,
                        unfocusedBorderColor = civicColors.border,
                        focusedTextColor = civicColors.textPrimary,
                        unfocusedTextColor = civicColors.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Dynamic Filter Chips Row
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (isSelected) civicColors.accent else civicColors.input,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) civicColors.accent else civicColors.border),
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else civicColors.textSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // FAQ Cards Accordion List / Empty State
        if (filteredChunks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = civicColors.accent,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (documents.isEmpty()) "No indexed documents found.\nTap '+' above to add your first document." else "No matching policy passages found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = civicColors.textSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onOpenDocumentManager,
                        colors = ButtonDefaults.buttonColors(containerColor = civicColors.accent, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Document", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredChunks) { chunk ->
                    val isExpanded = expandedChunkId == chunk.id
                    AccordionPolicyCard(
                        chunk = chunk,
                        isExpanded = isExpanded,
                        onToggleExpand = { expandedChunkId = if (isExpanded) null else chunk.id },
                        onInspectCitation = {
                            onOpenCitation(
                                GroundedCitation(
                                    source = chunk.documentTitle,
                                    page = chunk.pageNumber,
                                    paragraph = chunk.paragraphNumber,
                                    confidence = 0.95f,
                                    excerpt = chunk.content
                                )
                            )
                        },
                        onAskInChat = { question -> onAskInChat(question) }
                    )
                }
            }
        }
    }
}

@Composable
fun AccordionPolicyCard(
    chunk: ChunkEntity,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onInspectCitation: () -> Unit,
    onAskInChat: (String) -> Unit
) {
    val civicColors = MaterialTheme.civicColors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale = if (isPressed) 0.98f else 1.0f

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = civicColors.card,
        border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.border),
        modifier = Modifier
            .fillMaxWidth()
            .scale(pressScale)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Accordion Header Line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onToggleExpand
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "What regulations apply to ${chunk.category.lowercase()} in ${chunk.documentTitle}?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = civicColors.textPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = civicColors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle Row (Category • Document Title p.N)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = chunk.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = civicColors.accent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    text = " • ",
                    style = MaterialTheme.typography.labelSmall,
                    color = civicColors.textSecondary
                )
                Text(
                    text = "${chunk.documentTitle} (p.${chunk.pageNumber})",
                    style = MaterialTheme.typography.labelSmall,
                    color = civicColors.textSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))

                // Main Answer Paragraph
                Text(
                    text = chunk.content.replace("\n", " ").trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = civicColors.textPrimary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Verbatim Official Excerpt Callout Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = civicColors.box,
                    border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.accent.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = civicColors.emerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Verbatim Official Excerpt (Clause)",
                                style = MaterialTheme.typography.labelSmall,
                                color = civicColors.emerald,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "“${chunk.content.take(180).replace("\n", " ")}...”",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = civicColors.textSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card Footer Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onInspectCitation) {
                        Text(
                            text = "Inspect Citation (p.${chunk.pageNumber})",
                            style = MaterialTheme.typography.labelSmall,
                            color = civicColors.textSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { onAskInChat("Explain ${chunk.documentTitle} page ${chunk.pageNumber}") },
                        colors = ButtonDefaults.buttonColors(containerColor = civicColors.accent, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ask in Chat", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
