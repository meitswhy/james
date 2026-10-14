package com.civicai.app.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.civicai.app.data.search.DuckDuckGoSearchProvider
import com.civicai.app.data.search.WebSearchResultItem
import com.civicai.app.ui.theme.civicColors
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    initialQuery: String = "",
    onLightClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val searchProvider = remember { DuckDuckGoSearchProvider(context) }
    val civicColors = MaterialTheme.civicColors

    var searchQuery by remember { mutableStateOf(initialQuery) }
    var isLoading by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<WebSearchResultItem>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasInternet by remember { mutableStateOf(true) }

    fun runSearch(query: String) {
        if (query.isBlank()) return
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            val resp = searchProvider.searchWeb(query)
            hasInternet = resp.hasInternet
            results = resp.results
            errorMessage = resp.errorMessage
            isLoading = false
        }
    }

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank()) {
            runSearch(initialQuery)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(civicColors.bg)
            .padding(16.dp)
    ) {
        // Top Title Header
        Text(
            text = "Live Web Search (/s)",
            style = MaterialTheme.typography.displayMedium,
            color = civicColors.textPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Keyless real-time web search (DuckDuckGo & Wikipedia REST API)",
            style = MaterialTheme.typography.labelSmall,
            color = civicColors.accent
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Input Bar Dock
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Type query e.g. /s property tax portal...", style = MaterialTheme.typography.bodyMedium, color = civicColors.textSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = civicColors.accent) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = civicColors.accent,
                    unfocusedBorderColor = civicColors.hairline,
                    focusedTextColor = civicColors.textPrimary,
                    unfocusedTextColor = civicColors.textPrimary
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    onLightClick()
                    runSearch(searchQuery)
                },
                colors = ButtonDefaults.buttonColors(containerColor = civicColors.accent, contentColor = civicColors.bg),
                modifier = Modifier.height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Search", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = civicColors.accent)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Fetching keyless live search results...", style = MaterialTheme.typography.bodyMedium, color = civicColors.accent)
                }
            }
        } else if (!hasInternet || errorMessage != null) {
            // Designed Offline State
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Device Offline",
                        style = MaterialTheme.typography.displayMedium,
                        color = civicColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = errorMessage ?: "Web search requires an active internet connection. All document OCR, translation, and RAG features remain fully operational offline.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = civicColors.textSecondary
                    )
                }
            }
        } else if (results.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Type /s <query> to perform live web search.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = civicColors.textSecondary
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // On-Device Summary Header Block
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = civicColors.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.accent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = civicColors.accent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("On-Device Web Synthesis", style = MaterialTheme.typography.titleMedium, color = civicColors.accent, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Extracted ${results.size} live web references for '$searchQuery'. Tapping any card opens the domain in your browser.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = civicColors.textPrimary
                            )
                        }
                    }
                }

                items(results) { item ->
                    WebResultSourceCard(item = item, onClick = {
                        onLightClick()
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                        context.startActivity(intent)
                    })
                }
            }
        }
    }
}

@Composable
fun WebResultSourceCard(item: WebSearchResultItem, onClick: () -> Unit) {
    val civicColors = MaterialTheme.civicColors
    val domainLabel = try {
        val uri = Uri.parse(item.url)
        uri.host?.removePrefix("www.") ?: "web"
    } catch (e: Exception) {
        "source"
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = civicColors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.hairline),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favicon-free domain chip
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = civicColors.accent.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = domainLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = civicColors.accent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = civicColors.accent, modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = civicColors.textPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.snippet,
                style = MaterialTheme.typography.bodyMedium,
                color = civicColors.textSecondary,
                lineHeight = 19.sp
            )
        }
    }
}
