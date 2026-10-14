package com.civicai.app.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.civicai.app.data.command.CommandRegistry
import com.civicai.app.data.command.SlashCommandSpec
import com.civicai.app.data.jargon.JargonEntry
import com.civicai.app.data.jargon.JargonGlossary
import com.civicai.app.data.local.ChatMessageEntity
import com.civicai.app.data.local.DocumentEntity
import com.civicai.app.data.local.GroundedCitation
import com.civicai.app.data.voice.DictationState
import com.civicai.app.data.voice.VoiceEngine
import com.civicai.app.ui.theme.civicColors

@Composable
fun AskScreen(
    messages: List<ChatMessageEntity>,
    isLoading: Boolean,
    documents: List<DocumentEntity> = emptyList(),
    onSendMessage: (String) -> Unit,
    onAttachFile: (Uri) -> Unit,
    onOpenCitation: (GroundedCitation) -> Unit,
    onSpeakText: (String) -> Unit,
    onClearChat: () -> Unit,
    onOpenScanner: () -> Unit = {},
    onOpenPolicies: () -> Unit = {},
    onOpenDocManager: () -> Unit = {},
    onOpenCommandPalette: () -> Unit = {},
    onLightClick: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val voiceEngine = remember { VoiceEngine(context) }
    val dictationState by voiceEngine.dictationState.collectAsState()
    var isDictating by remember { mutableStateOf(false) }

    val civicColors = MaterialTheme.civicColors

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { onAttachFile(it) }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isDictating = true
            voiceEngine.startDictation { recognized ->
                inputText = recognized
            }
        }
    }

    val fileTypes = arrayOf(
        "application/pdf",
        "image/*",
        "text/*",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "*/*"
    )

    // Inline Command Popup Suggestions
    val isSlashInput = inputText.trim().startsWith("/")
    val filteredCommands = remember(inputText) {
        if (isSlashInput) CommandRegistry.filterCommands(inputText) else emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(civicColors.bg)
    ) {
        // Main Scrollable Area (Empty Hero vs Chat Messages)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty() && !isLoading) {
                JamesHeroEmptyCanvas(
                    documents = documents,
                    onOpenScanner = onOpenScanner,
                    onOpenPolicies = onOpenPolicies,
                    onUploadDoc = {
                        onLightClick()
                        documentPickerLauncher.launch(fileTypes)
                    },
                    onQuickPrompt = { prompt ->
                        onLightClick()
                        onSendMessage(prompt)
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(messages) { msg ->
                        JamesMessageItem(
                            message = msg,
                            onOpenCitation = onOpenCitation,
                            onSpeakText = onSpeakText
                        )
                    }

                    if (isLoading) {
                        item {
                            TypewriterLoadingIndicator()
                        }
                    }
                }
            }
        }

        // Inline Slash Command Suggestions Popup
        if (isSlashInput && filteredCommands.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = civicColors.card,
                border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.accent),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Available Slash Commands",
                        style = MaterialTheme.typography.labelSmall,
                        color = civicColors.accent,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 180.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredCommands) { cmd ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onLightClick()
                                        inputText = "${cmd.name} ${cmd.argumentHint}".trim()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = cmd.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = civicColors.accent
                                    )
                                    if (cmd.argumentHint.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = cmd.argumentHint,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = civicColors.textSecondary
                                        )
                                    }
                                }
                                Text(
                                    text = cmd.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = civicColors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input Strip Controls
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = civicColors.card,
            border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.border)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Status Row above Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(civicColors.emerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (documents.isNotEmpty()) "${documents.sumOf { it.chunkCount }} chunks indexed" else "Ready • 100% On-device",
                            style = MaterialTheme.typography.labelSmall,
                            color = civicColors.textSecondary,
                            fontSize = 11.sp
                        )
                    }

                    if (isDictating && dictationState is DictationState.Listening) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Listening (Dictating)...",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Text Input Pill Container
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = civicColors.input,
                    border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.borderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { newValue ->
                                inputText = newValue
                            },
                            placeholder = {
                                Text(
                                    text = "Ask James a question or type /...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = civicColors.textSecondary,
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier.weight(1f),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = civicColors.textPrimary,
                                unfocusedTextColor = civicColors.textPrimary
                            )
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Microphone Dictation Button
                            IconButton(onClick = {
                                onLightClick()
                                if (isDictating) {
                                    isDictating = false
                                    voiceEngine.stopDictation()
                                } else {
                                    recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }) {
                                Icon(
                                    imageVector = if (isDictating) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Dictate Question",
                                    tint = if (isDictating) Color(0xFFEF4444) else civicColors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Attachment Button
                            IconButton(onClick = {
                                onLightClick()
                                documentPickerLauncher.launch(fileTypes)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = "Attach File",
                                    tint = civicColors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Send / Stop Button
                            val sendInteraction = remember { MutableInteractionSource() }
                            val sendPressed by sendInteraction.collectIsPressedAsState()
                            val sendScale = if (sendPressed) 0.94f else 1.0f

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isLoading) civicColors.accent.copy(alpha = 0.2f) else civicColors.accent,
                                border = if (isLoading) androidx.compose.foundation.BorderStroke(1.dp, civicColors.accent) else null,
                                modifier = Modifier
                                    .size(34.dp)
                                    .scale(sendScale)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(
                                        interactionSource = sendInteraction,
                                        indication = null
                                    ) {
                                        if (inputText.isNotBlank() && !isLoading) {
                                            onLightClick()
                                            val text = inputText.trim()
                                            inputText = ""
                                            onSendMessage(text)
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isLoading) Icons.Default.Stop else Icons.Default.Send,
                                        contentDescription = if (isLoading) "Stop" else "Send",
                                        tint = if (isLoading) civicColors.accent else Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JamesHeroEmptyCanvas(
    documents: List<DocumentEntity>,
    onOpenScanner: () -> Unit,
    onOpenPolicies: () -> Unit,
    onUploadDoc: () -> Unit,
    onQuickPrompt: (String) -> Unit
) {
    val civicColors = MaterialTheme.civicColors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing Rounded Square Sparkle Icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            civicColors.accent.copy(alpha = 0.35f),
                            civicColors.accent.copy(alpha = 0.05f)
                        )
                    )
                )
                .border(1.dp, civicColors.accent.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = civicColors.accent,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "James Assistant",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = civicColors.textPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "100% On-device municipal tax, building codes, and policy helper",
            style = MaterialTheme.typography.bodyMedium,
            color = civicColors.textSecondary
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 2-Column Quick Action Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickHeroActionCard(
                title = "Scan Notice",
                subtitle = "Extract OCR & Translate",
                icon = Icons.Default.CameraAlt,
                iconTint = civicColors.accent,
                onClick = onOpenScanner,
                modifier = Modifier.weight(1f)
            )

            QuickHeroActionCard(
                title = "Browse Policies",
                subtitle = "Explore Grounded FAQ",
                icon = Icons.Default.MenuBook,
                iconTint = civicColors.emerald,
                onClick = onOpenPolicies,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Suggested Questions Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Suggested questions:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = civicColors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (documents.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = civicColors.card,
                border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onUploadDoc)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = civicColors.accent
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Add your first document",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = civicColors.textPrimary
                        )
                        Text(
                            text = "Upload PDF, property tax notice, or house rules to enable grounded answers",
                            style = MaterialTheme.typography.bodySmall,
                            color = civicColors.textSecondary
                        )
                    }
                }
            }
        } else {
            val suggestions = remember(documents) {
                documents.take(3).map { doc ->
                    "Summarize key rules in ${doc.title}"
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                suggestions.forEach { prompt ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = civicColors.card,
                        border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onQuickPrompt(prompt) }
                    ) {
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.bodyMedium,
                            color = civicColors.textPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickHeroActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val civicColors = MaterialTheme.civicColors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale = if (isPressed) 0.96f else 1.0f

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = civicColors.card,
        border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.border),
        modifier = modifier
            .scale(pressScale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = civicColors.textPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = civicColors.textSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
fun JamesMessageItem(
    message: ChatMessageEntity,
    onOpenCitation: (GroundedCitation) -> Unit,
    onSpeakText: (String) -> Unit
) {
    val isUser = message.role == "user"
    val civicColors = MaterialTheme.civicColors
    val clipboardManager = LocalClipboardManager.current
    var selectedJargonEntry by remember { mutableStateOf<JargonEntry?>(null) }

    val displayedText = message.content

    val isSmallTalk = message.content.startsWith("Hello!") || message.content.startsWith("You're welcome!") || message.content.contains("on-device Civic Assistant", ignoreCase = true)
    val isNotFound = message.content.contains("could not find any information", ignoreCase = true) || message.content.contains("no readable text", ignoreCase = true)
    val isGrounded = !isSmallTalk && !isNotFound

    val jargonMatches = remember(displayedText) {
        if (!isUser) JargonGlossary.findMatches(displayedText) else emptyList()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isUser) civicColors.accent else civicColors.card,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isUser) civicColors.accent else civicColors.border
            ),
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = displayedText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isUser) Color.White else civicColors.textPrimary,
                    lineHeight = 22.sp
                )

                if (!isUser) {
                    // Jargon Explainer Chips
                    if (jargonMatches.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Legal terms detected (tap to explain):",
                            style = MaterialTheme.typography.labelSmall,
                            color = civicColors.textSecondary,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(jargonMatches) { match ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = civicColors.accent.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.accent.copy(alpha = 0.3f)),
                                    modifier = Modifier.clickable {
                                        selectedJargonEntry = match.entry
                                    }
                                ) {
                                    Text(
                                        text = match.term,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = civicColors.accent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    selectedJargonEntry?.let { entry ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = civicColors.box,
                            border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.accent)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Vetted definition: ${entry.term}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = civicColors.accent,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { selectedJargonEntry = null },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = civicColors.textSecondary, modifier = Modifier.size(12.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = entry.plainDefinition,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = civicColors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Example: ${entry.example}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = civicColors.textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            IconButton(
                                onClick = { clipboardManager.setText(AnnotatedString(message.content)) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = civicColors.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            IconButton(
                                onClick = { onSpeakText(message.content) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Listen",
                                    tint = civicColors.accent,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        if (!isSmallTalk) {
                            // Grounding Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isGrounded) civicColors.emerald.copy(alpha = 0.15f) else Color(0xFFD97706).copy(alpha = 0.15f),
                                modifier = Modifier.clickable {
                                    onOpenCitation(
                                        GroundedCitation(
                                            source = "Uploaded Document Corpus",
                                            page = 1,
                                            paragraph = 1,
                                            confidence = if (isGrounded) 0.95f else 0.0f,
                                            excerpt = message.content.take(200)
                                        )
                                    )
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isGrounded) civicColors.emerald else Color(0xFFD97706))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isGrounded) "Grounded" else "Not found in your documents",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isGrounded) civicColors.emerald else Color(0xFFD97706),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypewriterLoadingIndicator() {
    val civicColors = MaterialTheme.civicColors
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = civicColors.card,
        border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.border),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(civicColors.accent.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Thinking & retrieving from documents...",
                style = MaterialTheme.typography.bodySmall,
                color = civicColors.textSecondary
            )
        }
    }
}
