package com.civicai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.civicai.app.data.command.CommandRegistry
import com.civicai.app.data.command.SlashCommand
import com.civicai.app.ui.theme.civicColors

@Composable
fun CommandPaletteModal(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onExecuteCommand: (SlashCommand) -> Unit,
    onLightClick: () -> Unit = {}
) {
    if (!isOpen) return

    var searchQuery by remember { mutableStateOf("") }
    val filteredCommands = remember(searchQuery) {
        CommandRegistry.filterCommands(searchQuery)
    }
    val civicColors = MaterialTheme.civicColors

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .clip(RoundedCornerShape(20.dp)),
            color = civicColors.bg,
            border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.hairline)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Header Search Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Commands",
                        tint = civicColors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Type command (e.g. /s, /policy, /scan)...", style = MaterialTheme.typography.bodyMedium, color = civicColors.textSecondary) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = civicColors.accent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = civicColors.textPrimary,
                            unfocusedTextColor = civicColors.textPrimary
                        )
                    )
                    IconButton(onClick = {
                        onLightClick()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = civicColors.textPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = civicColors.hairline)

                // Command List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCommands) { cmd ->
                        CommandItemRow(command = cmd, onClick = {
                            onLightClick()
                            onExecuteCommand(cmd)
                            onDismiss()
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun CommandItemRow(command: SlashCommand, onClick: () -> Unit) {
    val civicColors = MaterialTheme.civicColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(civicColors.surface)
            .border(1.dp, civicColors.hairline, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Keyboard-style Chip
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = civicColors.accent.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.accent.copy(alpha = 0.3f))
        ) {
            Text(
                text = command.trigger,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = civicColors.accent,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = command.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = civicColors.textPrimary
            )
            Text(
                text = command.description,
                style = MaterialTheme.typography.bodyMedium,
                color = civicColors.textSecondary
            )
        }
    }
}
