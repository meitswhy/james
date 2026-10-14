package com.civicai.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.civicai.app.data.command.SlashCommand
import com.civicai.app.data.llm.OnDeviceLlmEngine
import com.civicai.app.data.local.*
import com.civicai.app.data.voice.VoiceEngine
import com.civicai.app.ui.components.*
import com.civicai.app.ui.screens.*
import com.civicai.app.ui.theme.CivicAITheme
import com.civicai.app.ui.theme.HapticFeedbackHelper
import com.civicai.app.ui.theme.civicColors
import com.civicai.app.worker.DocumentIndexingWorker
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : FragmentActivity() {

    private lateinit var db: AppDatabase
    private lateinit var llmEngine: OnDeviceLlmEngine
    private lateinit var voiceEngine: VoiceEngine
    private lateinit var preferencesRepo: UserPreferencesRepository
    private lateinit var haptics: HapticFeedbackHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = AppDatabase.getDatabase(this)
        llmEngine = OnDeviceLlmEngine(this)
        voiceEngine = VoiceEngine(this)
        preferencesRepo = UserPreferencesRepository(this)
        haptics = HapticFeedbackHelper(this) { true }

        LocalDiagnosticsLogger.i("MainActivity", "CivicAI main activity launched successfully.")

        setContent {
            val userSettings by preferencesRepo.userSettingsFlow.collectAsState(initial = UserSettings())

            // Dynamic Window FLAG_SECURE for hide in recents
            LaunchedEffect(userSettings.hideInRecents) {
                if (userSettings.hideInRecents) {
                    window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            CivicAITheme(settings = userSettings) {
                val civicColors = MaterialTheme.civicColors

                // Sync System Bars with Theme Colors
                SideEffect {
                    window.statusBarColor = civicColors.bg.toArgb()
                    window.navigationBarColor = civicColors.card.toArgb()
                }

                val coroutineScope = rememberCoroutineScope()
                if (!userSettings.onboardingCompleted) {
                    OnboardingScreen(
                        onFinish = {
                            coroutineScope.launch {
                                preferencesRepo.setOnboardingCompleted(true)
                            }
                        }
                    )
                } else {
                    MainAppContent(userSettings = userSettings)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainAppContent(userSettings: UserSettings) {
        var currentTab by remember { mutableStateOf<NavTab>(NavTab.Chat) }
        var isCommandPaletteOpen by remember { mutableStateOf(false) }
        var isSettingsOpen by remember { mutableStateOf(false) }
        var isDiagnosticsOpen by remember { mutableStateOf(false) }
        var isDocManagerOpen by remember { mutableStateOf(false) }
        var activeCitation by remember { mutableStateOf<GroundedCitation?>(null) }
        var initialSearchQuery by remember { mutableStateOf("") }
        var isSpeakerOn by remember { mutableStateOf(false) }

        val coroutineScope = rememberCoroutineScope()

        val docDao = db.documentDao()
        val chunkDao = db.chunkDao()
        val chatDao = db.chatDao()

        val documents by docDao.getAllDocumentsFlow().collectAsState(initial = emptyList())
        val sessions by chatDao.getAllSessionsFlow().collectAsState(initial = emptyList())

        var activeSessionId by remember { mutableStateOf("sess_" + System.currentTimeMillis()) }
        val messages by chatDao.getMessagesForSessionFlow(activeSessionId).collectAsState(initial = emptyList())
        var isGenerating by remember { mutableStateOf(false) }

        val totalChunks by chunkDao.getTotalChunkCountFlow().collectAsState(initial = 0)

        val civicColors = MaterialTheme.civicColors

        if (isDiagnosticsOpen) {
            DiagnosticsScreen(
                ramMb = llmEngine.getAvailableRamMb(),
                totalChunks = totalChunks,
                totalDocs = documents.size,
                ftsRowCount = totalChunks,
                documents = documents,
                llmEngine = llmEngine,
                onBack = { isDiagnosticsOpen = false }
            )
        } else {
            Scaffold(
                topBar = {
                    CivicTopHeaderBar(
                        settings = userSettings,
                        engineStatus = if (isGenerating) EngineStatus.INDEXING else EngineStatus.READY,
                        totalTokens = if (messages.isNotEmpty()) messages.sumOf { it.tokenCount } else 0,
                        maxContextTokens = 8000,
                        isSpeakerOn = isSpeakerOn,
                        onToggleTheme = {
                            coroutineScope.launch {
                                preferencesRepo.updateIsDarkMode(!userSettings.isDarkMode)
                            }
                        },
                        onToggleSpeaker = {
                            isSpeakerOn = !isSpeakerOn
                            if (!isSpeakerOn) voiceEngine.stopSpeaking()
                        },
                        onOpenSettings = {
                            haptics.lightClick()
                            isSettingsOpen = true
                        },
                        onOpenDiagnostics = {
                            haptics.lightClick()
                            isDiagnosticsOpen = true
                        },
                        onLightClick = { haptics.lightClick() }
                    )
                },
                bottomBar = {
                    CivicBottomBar(
                        currentRoute = currentTab.route,
                        onTabSelected = { currentTab = it },
                        onLightClick = { haptics.lightClick() }
                    )
                },
                containerColor = civicColors.bg
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (currentTab) {
                        NavTab.Chat -> AskScreen(
                            messages = messages,
                            isLoading = isGenerating,
                            documents = documents,
                            onSendMessage = { text ->
                                coroutineScope.launch {
                                    val trimmed = text.trim()
                                    if (trimmed.startsWith("/")) {
                                        val userMsg = ChatMessageEntity(
                                            id = UUID.randomUUID().toString(),
                                            sessionId = activeSessionId,
                                            role = "user",
                                            content = trimmed,
                                            timestamp = System.currentTimeMillis()
                                        )
                                        chatDao.insertMessage(userMsg)

                                        chatDao.insertSession(
                                            ChatSessionEntity(
                                                id = activeSessionId,
                                                title = trimmed.take(30),
                                                createdAt = System.currentTimeMillis(),
                                                lastUpdated = System.currentTimeMillis(),
                                                messageCount = messages.size + 1
                                            )
                                        )

                                        when (val cmdRes = com.civicai.app.data.command.CommandRegistry.executeCommand(
                                            context = this@MainActivity,
                                            input = trimmed,
                                            documents = documents,
                                            onClearChat = {
                                                coroutineScope.launch {
                                                    chatDao.deleteMessagesForSession(activeSessionId)
                                                }
                                            }
                                        )) {
                                            is com.civicai.app.data.command.CommandExecutionResult.Success -> {
                                                val assistantMsg = ChatMessageEntity(
                                                    id = UUID.randomUUID().toString(),
                                                    sessionId = activeSessionId,
                                                    role = "assistant",
                                                    content = cmdRes.message,
                                                    timestamp = System.currentTimeMillis()
                                                )
                                                chatDao.insertMessage(assistantMsg)
                                                if (isSpeakerOn) voiceEngine.speak(cmdRes.message)
                                            }
                                            is com.civicai.app.data.command.CommandExecutionResult.Error -> {
                                                val assistantMsg = ChatMessageEntity(
                                                    id = UUID.randomUUID().toString(),
                                                    sessionId = activeSessionId,
                                                    role = "assistant",
                                                    content = cmdRes.reason,
                                                    timestamp = System.currentTimeMillis()
                                                )
                                                chatDao.insertMessage(assistantMsg)
                                            }
                                            is com.civicai.app.data.command.CommandExecutionResult.ClearChat -> {
                                                // Cleared
                                            }
                                        }
                                    } else {
                                        isGenerating = true

                                        val userMsg = ChatMessageEntity(
                                            id = UUID.randomUUID().toString(),
                                            sessionId = activeSessionId,
                                            role = "user",
                                            content = text,
                                            timestamp = System.currentTimeMillis()
                                        )
                                        chatDao.insertMessage(userMsg)

                                        chatDao.insertSession(
                                            ChatSessionEntity(
                                                id = activeSessionId,
                                                title = text.take(30),
                                                createdAt = System.currentTimeMillis(),
                                                lastUpdated = System.currentTimeMillis(),
                                                messageCount = messages.size + 1
                                            )
                                        )

                                        val groundedResponse = llmEngine.generateGroundedAnswer(text)

                                        val assistantMsg = ChatMessageEntity(
                                            id = UUID.randomUUID().toString(),
                                            sessionId = activeSessionId,
                                            role = "assistant",
                                            content = groundedResponse.reply,
                                            timestamp = System.currentTimeMillis(),
                                            tokenCount = groundedResponse.promptTokens + groundedResponse.completionTokens
                                        )
                                        chatDao.insertMessage(assistantMsg)

                                        if (isSpeakerOn && groundedResponse.reply.isNotBlank()) {
                                            voiceEngine.speak(groundedResponse.reply)
                                        }

                                        isGenerating = false
                                        haptics.successPattern()
                                    }
                                }
                            },
                            onAttachFile = { uri ->
                                val title = uri.lastPathSegment?.substringAfterLast('/') ?: "Attachment"
                                val docId = UUID.randomUUID().toString()
                                DocumentIndexingWorker.enqueueIndexing(this@MainActivity, docId, uri, title)
                                haptics.mediumClick()
                            },
                            onOpenCitation = { activeCitation = it },
                            onSpeakText = { text -> voiceEngine.speak(text) },
                            onClearChat = {
                                coroutineScope.launch {
                                    chatDao.deleteMessagesForSession(activeSessionId)
                                }
                            },
                            onOpenCommandPalette = { isCommandPaletteOpen = true },
                            onOpenDocManager = { isDocManagerOpen = true },
                            onOpenScanner = { currentTab = NavTab.Scanner },
                            onOpenPolicies = { currentTab = NavTab.Policies },
                            onLightClick = { haptics.lightClick() }
                        )

                        NavTab.Scanner -> ScanScreen(
                            onSpeakText = { text -> voiceEngine.speak(text) },
                            onLightClick = { haptics.lightClick() }
                        )

                        NavTab.History -> {
                            LibraryScreen(
                                sessions = sessions,
                                onNewChat = {
                                    activeSessionId = "sess_" + System.currentTimeMillis()
                                    currentTab = NavTab.Chat
                                },
                                onDeleteSession = { id ->
                                    coroutineScope.launch {
                                        chatDao.deleteSession(id)
                                        chatDao.deleteMessagesForSession(id)
                                    }
                                    haptics.heavyClick()
                                },
                                onSelectSession = { id ->
                                    activeSessionId = id
                                    currentTab = NavTab.Chat
                                },
                                onLightClick = { haptics.lightClick() }
                            )
                        }

                        NavTab.Policies -> {
                            var allChunks by remember { mutableStateOf<List<ChunkEntity>>(emptyList()) }
                            LaunchedEffect(Unit) {
                                allChunks = chunkDao.getAllChunksLimit(50)
                            }
                            PolicyFaqScreen(
                                chunks = allChunks,
                                documents = documents,
                                onOpenDocumentManager = { isDocManagerOpen = true },
                                onOpenCitation = { activeCitation = it },
                                onAskInChat = { question ->
                                    currentTab = NavTab.Chat
                                    coroutineScope.launch {
                                        isGenerating = true
                                        val userMsg = ChatMessageEntity(
                                            id = UUID.randomUUID().toString(),
                                            sessionId = activeSessionId,
                                            role = "user",
                                            content = question,
                                            timestamp = System.currentTimeMillis()
                                        )
                                        chatDao.insertMessage(userMsg)

                                        val groundedResponse = llmEngine.generateGroundedAnswer(question)

                                        val assistantMsg = ChatMessageEntity(
                                            id = UUID.randomUUID().toString(),
                                            sessionId = activeSessionId,
                                            role = "assistant",
                                            content = groundedResponse.reply,
                                            timestamp = System.currentTimeMillis(),
                                            tokenCount = groundedResponse.promptTokens + groundedResponse.completionTokens
                                        )
                                        chatDao.insertMessage(assistantMsg)
                                        isGenerating = false
                                    }
                                }
                            )
                        }
                    }

                    // Command Palette Modal Dialog
                    CommandPaletteModal(
                        isOpen = isCommandPaletteOpen,
                        onDismiss = { isCommandPaletteOpen = false },
                        onExecuteCommand = { cmd ->
                            when (cmd.trigger) {
                                "/s" -> currentTab = NavTab.Chat
                                "/clear" -> coroutineScope.launch { chatDao.deleteMessagesForSession(activeSessionId) }
                                "/policy" -> currentTab = NavTab.Policies
                                "/scan" -> currentTab = NavTab.Scanner
                                "/history" -> currentTab = NavTab.History
                                "/settings" -> isSettingsOpen = true
                                "/status" -> isDiagnosticsOpen = true
                                "/help" -> isCommandPaletteOpen = true
                            }
                        },
                        onLightClick = { haptics.lightClick() }
                    )

                    // Grounded Citation Sheet
                    CitationSheet(
                        citation = activeCitation,
                        onDismiss = { activeCitation = null }
                    )

                    // Document Manager Sheet
                    DocumentManagerSheet(
                        isOpen = isDocManagerOpen,
                        documents = documents,
                        onUploadDocument = { uri, title ->
                            val docId = UUID.randomUUID().toString()
                            DocumentIndexingWorker.enqueueIndexing(this@MainActivity, docId, uri, title)
                        },
                        onDeleteDocument = { id ->
                            coroutineScope.launch {
                                docDao.deleteDocument(id)
                                docDao.deleteChunksForDocument(id)
                            }
                        },
                        onDismiss = { isDocManagerOpen = false }
                    )

                    // Settings & System Status Modal
                    SettingsModal(
                        isOpen = isSettingsOpen,
                        settings = userSettings,
                        preferencesRepo = preferencesRepo,
                        ramMb = llmEngine.getAvailableRamMb(),
                        totalChunks = totalChunks,
                        totalDocs = documents.size,
                        onOpenDiagnostics = {
                            isSettingsOpen = false
                            isDiagnosticsOpen = true
                        },
                        onReindexAll = {
                            coroutineScope.launch {
                                LocalDiagnosticsLogger.i("MainActivity", "Re-indexing all documents requested")
                            }
                        },
                        onClearIndex = {
                            coroutineScope.launch {
                                chunkDao.deleteAllChunks()
                                LocalDiagnosticsLogger.w("MainActivity", "All indexed chunks cleared")
                            }
                        },
                        onDeleteAllData = {
                            coroutineScope.launch {
                                docDao.deleteAllDocuments()
                                chunkDao.deleteAllChunks()
                                chatDao.deleteAllMessages()
                                chatDao.deleteAllSessions()
                                LocalDiagnosticsLogger.e("MainActivity", "All user data deleted")
                            }
                        },
                        onDismiss = { isSettingsOpen = false }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceEngine.shutdown()
    }
}
