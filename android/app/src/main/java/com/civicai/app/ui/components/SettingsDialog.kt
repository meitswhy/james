package com.civicai.app.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.civicai.app.data.local.*
import com.civicai.app.ui.theme.CivicColors
import com.civicai.app.ui.theme.civicColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsModal(
    isOpen: Boolean,
    settings: UserSettings,
    preferencesRepo: UserPreferencesRepository,
    ramMb: Long,
    totalChunks: Int,
    totalDocs: Int,
    onOpenDiagnostics: () -> Unit,
    onReindexAll: () -> Unit,
    onClearIndex: () -> Unit,
    onDeleteAllData: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val civicColors = MaterialTheme.civicColors

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let {
            val success = BackupManager.exportBackup(context, it)
            val msg = if (success) "Backup exported successfully" else "Backup export failed"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val success = BackupManager.importBackup(context, it)
            val msg = if (success) "Backup restored successfully" else "Backup restore failed"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            color = civicColors.bg,
            border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.hairline)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CivicAI Settings",
                            style = MaterialTheme.typography.titleLarge,
                            color = civicColors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Quiet Luxury • 100% On-Device",
                            style = MaterialTheme.typography.labelSmall,
                            color = civicColors.accent
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Settings",
                            tint = civicColors.textPrimary
                        )
                    }
                }

                HorizontalDivider(color = civicColors.hairline)

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // --- SECTION 1: APPEARANCE ---
                    item {
                        SettingsSectionHeader("Appearance & Customization", Icons.Default.Palette)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Theme Preset Picker
                        Text(
                            text = "Theme Preset",
                            style = MaterialTheme.typography.titleMedium,
                            color = civicColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ThemePreset.values()) { preset ->
                                val selected = settings.themePreset == preset
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        coroutineScope.launch { preferencesRepo.updateThemePreset(preset) }
                                    },
                                    label = { Text(preset.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = civicColors.accent,
                                        selectedLabelColor = civicColors.bg
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Curated Accent Palette Picker
                        Text(
                            text = "Accent Color",
                            style = MaterialTheme.typography.titleMedium,
                            color = civicColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(CivicColors.CuratedAccents) { (hex, name) ->
                                val accentColor = Color(android.graphics.Color.parseColor(hex))
                                val isSelected = settings.accentColorHex.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(accentColor)
                                        .clickable {
                                            coroutineScope.launch { preferencesRepo.updateAccentColorHex(hex) }
                                        }
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) civicColors.textPrimary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dark/Light/System Toggles
                        SwitchSettingRow(
                            title = "Follow System Dark Theme",
                            subtitle = "Automatically align with system theme mode",
                            checked = settings.followSystemTheme,
                            onCheckedChange = { coroutineScope.launch { preferencesRepo.updateFollowSystemTheme(it) } }
                        )

                        if (!settings.followSystemTheme) {
                            SwitchSettingRow(
                                title = "Dark Theme",
                                subtitle = "Force dark interface",
                                checked = settings.isDarkMode,
                                onCheckedChange = { coroutineScope.launch { preferencesRepo.updateIsDarkMode(it) } }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Font Scale Slider
                        Text(
                            text = "Font Size Scale (${String.format("%.2f", settings.fontSizeScale)}x)",
                            style = MaterialTheme.typography.titleMedium,
                            color = civicColors.textPrimary
                        )
                        Slider(
                            value = settings.fontSizeScale,
                            onValueChange = { coroutineScope.launch { preferencesRepo.updateFontSizeScale(it) } },
                            valueRange = 0.85f..1.30f,
                            steps = 9
                        )

                        SwitchSettingRow(
                            title = "Reduce Motion",
                            subtitle = "Disable spring animations for low-end devices",
                            checked = settings.reduceMotion,
                            onCheckedChange = { coroutineScope.launch { preferencesRepo.updateReduceMotion(it) } }
                        )

                        SwitchSettingRow(
                            title = "Haptic Feedback",
                            subtitle = "Vibrate on button taps, swipes and completions",
                            checked = settings.hapticsEnabled,
                            onCheckedChange = { coroutineScope.launch { preferencesRepo.updateHapticsEnabled(it) } }
                        )
                    }

                    // --- SECTION 2: AI & ANSWERS ---
                    item {
                        SettingsSectionHeader("AI & Grounded Answers", Icons.Default.Psychology)
                        Spacer(modifier = Modifier.height(12.dp))

                        SwitchSettingRow(
                            title = "Strict Grounding",
                            subtitle = "Refuse queries when information is absent from indexed documents",
                            checked = settings.strictGrounding,
                            onCheckedChange = { coroutineScope.launch { preferencesRepo.updateStrictGrounding(it) } }
                        )

                        SwitchSettingRow(
                            title = "Always Show Citations",
                            subtitle = "Display explicit page & paragraph source chips",
                            checked = settings.alwaysShowCitations,
                            onCheckedChange = { coroutineScope.launch { preferencesRepo.updateAlwaysShowCitations(it) } }
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Answer Detail Level",
                            style = MaterialTheme.typography.titleMedium,
                            color = civicColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AnswerLength.values().forEach { len ->
                                FilterChip(
                                    selected = settings.answerLength == len,
                                    onClick = { coroutineScope.launch { preferencesRepo.updateAnswerLength(len) } },
                                    label = { Text(len.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = civicColors.accent,
                                        selectedLabelColor = civicColors.bg
                                    )
                                )
                            }
                        }
                    }

                    // --- SECTION 3: DOCUMENTS & INDEX ---
                    item {
                        SettingsSectionHeader("Document Storage & Index", Icons.Default.FolderSpecial)
                        Spacer(modifier = Modifier.height(12.dp))

                        SwitchSettingRow(
                            title = "Auto-Index on Upload",
                            subtitle = "Automatically run FTS5 text indexing when a document is added",
                            checked = settings.autoIndexOnUpload,
                            onCheckedChange = { coroutineScope.launch { preferencesRepo.updateAutoIndexOnUpload(it) } }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onReindexAll,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Re-Index All", color = civicColors.textPrimary)
                            }
                            OutlinedButton(
                                onClick = onClearIndex,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Clear Index", color = civicColors.textPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = onDeleteAllData,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete All Data", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // --- SECTION 4: WEB SEARCH & PRIVACY ---
                    item {
                        SettingsSectionHeader("Privacy & Security", Icons.Default.Security)
                        Spacer(modifier = Modifier.height(12.dp))

                        SwitchSettingRow(
                            title = "Enable /s Web Search",
                            subtitle = "Internet is ONLY used when you explicitly type /s in search",
                            checked = settings.webSearchEnabled,
                            onCheckedChange = { coroutineScope.launch { preferencesRepo.updateWebSearchEnabled(it) } }
                        )

                        SwitchSettingRow(
                            title = "Biometric App Lock",
                            subtitle = "Require fingerprint/face auth upon opening CivicAI",
                            checked = settings.biometricLockEnabled,
                            onCheckedChange = { coroutineScope.launch { preferencesRepo.updateBiometricLockEnabled(it) } }
                        )

                        SwitchSettingRow(
                            title = "Hide Content in Recents",
                            subtitle = "Prevent screenshot leaks in Android app switcher",
                            checked = settings.hideInRecents,
                            onCheckedChange = { coroutineScope.launch { preferencesRepo.updateHideInRecents(it) } }
                        )
                    }

                    // --- SECTION 5: BACKUP & DIAGNOSTICS ---
                    item {
                        SettingsSectionHeader("System & Backup", Icons.Default.Storage)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { exportLauncher.launch("civicai_backup.zip") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = civicColors.accent)
                            ) {
                                Text("Export Backup", color = civicColors.bg, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { importLauncher.launch(arrayOf("application/zip", "application/octet-stream")) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Import Backup", color = civicColors.textPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = onOpenDiagnostics,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = civicColors.accent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open System Diagnostics & Logs", color = civicColors.textPrimary)
                        }
                    }

                    // --- SECTION 6: ABOUT ---
                    item {
                        SettingsSectionHeader("About CivicAI", Icons.Default.Info)
                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = civicColors.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "CivicAI v1.0.0 Native Android",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = civicColors.textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Built with Kotlin 2.0, Jetpack Compose, CameraX, Room SQLite FTS5, ML Kit OCR & Indic Translation.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = civicColors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Licenses: Apache 2.0, MIT. Zero telemetry collected.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = civicColors.accent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val civicColors = MaterialTheme.civicColors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = civicColors.accent, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = civicColors.textPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SwitchSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val civicColors = MaterialTheme.civicColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = civicColors.textPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = civicColors.textSecondary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = civicColors.bg,
                checkedTrackColor = civicColors.accent
            )
        )
    }
}
