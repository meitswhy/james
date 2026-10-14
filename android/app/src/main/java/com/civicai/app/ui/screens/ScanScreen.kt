package com.civicai.app.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.civicai.app.data.local.LocalDiagnosticsLogger
import com.civicai.app.data.ocr.BitmapUtils
import com.civicai.app.data.ocr.NoticeOcrPipeline
import com.civicai.app.data.ocr.NoticeOcrPipelineResult
import com.civicai.app.data.parser.DocumentParser
import com.civicai.app.data.translation.OfflineTranslator
import com.civicai.app.ui.theme.LocalCivicColors
import com.civicai.app.ui.theme.civicColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ScanUiState {
    object Idle : ScanUiState()
    data class Loading(val message: String) : ScanUiState()
    data class OcrSuccess(
        val cleanedText: String,
        val isLowConfidence: Boolean
    ) : ScanUiState()
    data class ModelNeedsDownload(
        val targetLanguage: String,
        val langCode: String,
        val approxSizeMb: Int,
        val textToTranslate: String
    ) : ScanUiState()
    data class Success(val result: NoticeOcrPipelineResult) : ScanUiState()
    data class Error(val reason: String, val canRetry: Boolean = true) : ScanUiState()
}

@Composable
fun ScanScreen(
    onSpeakText: (String) -> Unit,
    onLightClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val pipeline = remember { NoticeOcrPipeline(context) }
    val documentParser = remember { DocumentParser(context) }
    val civicColors = MaterialTheme.civicColors

    var selectedLanguage by remember { mutableStateOf("Hindi (हिन्दी)") }
    var uiState by remember { mutableStateOf<ScanUiState>(ScanUiState.Idle) }
    var editableText by remember { mutableStateOf("") }
    var activeJargonDefinition by remember { mutableStateOf<Pair<String, String>?>(null) }
    var isSplitView by remember { mutableStateOf(false) }

    fun executeTranslation(textToTranslate: String, targetLang: String) {
        coroutineScope.launch {
            if (targetLang == "English") {
                uiState = ScanUiState.Loading("Preparing document...")
                val result = pipeline.processNoticeText(textToTranslate, targetLang)
                uiState = if (result.isSuccess) ScanUiState.Success(result) else ScanUiState.Error(result.errorMessage ?: "Failed to process text.")
                return@launch
            }

            val isDownloaded = pipeline.translator.isModelDownloaded(targetLang)
            if (!isDownloaded) {
                val langCode = OfflineTranslator.getLanguageCode(targetLang)
                uiState = ScanUiState.ModelNeedsDownload(targetLang, langCode, 30, textToTranslate)
            } else {
                uiState = ScanUiState.Loading("Translating on-device into $targetLang...")
                val result = pipeline.processNoticeText(textToTranslate, targetLang)
                uiState = if (result.isSuccess) ScanUiState.Success(result) else ScanUiState.Error(result.errorMessage ?: "Translation failed.")
            }
        }
    }

    fun downloadAndTranslate(targetLang: String, textToTranslate: String) {
        coroutineScope.launch {
            uiState = ScanUiState.Loading("Downloading on-device Neural MT pack for $targetLang (~30 MB)...")
            val success = pipeline.translator.downloadModel(targetLang)
            if (success) {
                uiState = ScanUiState.Loading("Translating notice on-device...")
                val result = pipeline.processNoticeText(textToTranslate, targetLang)
                uiState = if (result.isSuccess) ScanUiState.Success(result) else ScanUiState.Error(result.errorMessage ?: "Translation failed.")
            } else {
                uiState = ScanUiState.Error("Failed to download language pack for $targetLang. Check network connection for model download.")
            }
        }
    }

    fun processImageUri(uri: Uri) {
        coroutineScope.launch {
            uiState = ScanUiState.Loading("Safely downscaling photo & initializing 12-Stage OCR...")
            try {
                val bitmap = BitmapUtils.decodeAndScaleUri(context, uri)
                if (bitmap == null) {
                    uiState = ScanUiState.Error("Failed to decode photo. The file may be corrupt or inaccessible.")
                    return@launch
                }

                uiState = ScanUiState.Loading("Running Devanagari & Latin ML Kit Text Recognition...")
                val ocrResult = pipeline.extractTextFromImage(bitmap)

                if (!ocrResult.isSuccess) {
                    uiState = ScanUiState.Error(ocrResult.errorMessage ?: "OCR process returned no readable text.")
                } else {
                    editableText = ocrResult.cleanedText
                    uiState = ScanUiState.OcrSuccess(ocrResult.cleanedText, ocrResult.isLowConfidence)
                }
            } catch (e: Exception) {
                LocalDiagnosticsLogger.e("ScanScreen", "Image process exception: ${e.message}")
                uiState = ScanUiState.Error("Unexpected Error: ${e.localizedMessage ?: "Scanning failed."}")
            }
        }
    }

    fun processDocUri(uri: Uri) {
        coroutineScope.launch {
            uiState = ScanUiState.Loading("Parsing document text content...")
            try {
                val parsed = documentParser.parseDocument(uri, "notice_doc", "Scanned Notice")
                val fullText = parsed.chunks.joinToString("\n\n") { c -> c.content }
                if (fullText.isBlank()) {
                    uiState = ScanUiState.Error("Uploaded document contains no readable text content.")
                    return@launch
                }
                val cleanedText = pipeline.cleanOcrText(fullText)
                editableText = cleanedText
                uiState = ScanUiState.OcrSuccess(cleanedText, false)
            } catch (e: Exception) {
                LocalDiagnosticsLogger.e("ScanScreen", "Doc process exception: ${e.message}")
                uiState = ScanUiState.Error("Document Error: ${e.localizedMessage ?: "Failed to read document."}")
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { processImageUri(it) }
    }

    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { processDocUri(it) }
    }

    val sharedScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(civicColors.bg)
            .padding(16.dp)
            .verticalScroll(sharedScrollState)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Notice Scanner & OCR",
                    style = MaterialTheme.typography.displayMedium,
                    color = civicColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "12-Stage OCR • Offline Indic Translation • Fraud Meter",
                    style = MaterialTheme.typography.labelSmall,
                    color = civicColors.accent
                )
            }

            if (uiState is ScanUiState.Success) {
                IconButton(onClick = { isSplitView = !isSplitView }) {
                    Icon(
                        imageVector = if (isSplitView) Icons.Default.ViewAgenda else Icons.Default.VerticalSplit,
                        contentDescription = "Toggle Split View",
                        tint = civicColors.accent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Target Language Selector
        Text(
            text = "Select Translation Target Language:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = civicColors.accent
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(OfflineTranslator.SUPPORTED_LANGUAGES) { lang ->
                val isSelected = selectedLanguage == lang
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) civicColors.accent else civicColors.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.hairline),
                    modifier = Modifier.clickable {
                        onLightClick()
                        selectedLanguage = lang
                    }
                ) {
                    Text(
                        text = lang,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) civicColors.bg else civicColors.textPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Viewfinder / Action Dock
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = civicColors.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(civicColors.surfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    val strokeColor = civicColors.accent
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                        drawRect(
                            color = strokeColor,
                            topLeft = Offset(24.dp.toPx(), 16.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(size.width - 48.dp.toPx(), size.height - 32.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx(), pathEffect = pathEffect)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = null,
                            tint = civicColors.accent,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Align Notice Paper Inside Corners",
                            style = MaterialTheme.typography.labelSmall,
                            color = civicColors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onLightClick()
                            try {
                                imagePickerLauncher.launch("image/*")
                            } catch (e: Exception) {
                                LocalDiagnosticsLogger.e("ScanScreen", "Image picker launch error: ${e.message}")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = civicColors.accent, contentColor = civicColors.bg)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan Photo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onLightClick()
                            try {
                                docPickerLauncher.launch(arrayOf("application/pdf", "text/*", "*/*"))
                            } catch (e: Exception) {
                                LocalDiagnosticsLogger.e("ScanScreen", "Doc picker launch error: ${e.message}")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = civicColors.surfaceElevated, contentColor = civicColors.textPrimary)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload Document", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // UI State Handling
        when (val state = uiState) {
            is ScanUiState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Take a clear photo or upload a municipal PDF notice to start instant on-device OCR and translation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = civicColors.textSecondary
                    )
                }
            }

            is ScanUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = civicColors.accent)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = civicColors.accent
                        )
                    }
                }
            }

            is ScanUiState.OcrSuccess -> {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = civicColors.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.hairline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Extracted OCR Text (Editable)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = civicColors.accent
                        )
                        Text(
                            text = "Review and correct any misread words before translating:",
                            style = MaterialTheme.typography.bodySmall,
                            color = civicColors.textSecondary
                        )

                        if (state.isLowConfidence) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Low OCR Confidence: Image text may contain garbled words. Please review before translating.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = civicColors.textPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = editableText,
                            onValueChange = { editableText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 140.dp, max = 280.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = civicColors.textPrimary),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = civicColors.accent,
                                unfocusedBorderColor = civicColors.hairline
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                onLightClick()
                                executeTranslation(editableText, selectedLanguage)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = civicColors.accent, contentColor = civicColors.bg)
                        ) {
                            Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Translate Notice into $selectedLanguage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            is ScanUiState.ModelNeedsDownload -> {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = civicColors.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.accent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = civicColors.accent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "On-Device Neural Model Download Required",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = civicColors.textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "To translate offline into ${state.targetLanguage}, a high-precision ML Kit Neural Machine Translation model (~${state.approxSizeMb} MB) needs to be cached on your device.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = civicColors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                onLightClick()
                                downloadAndTranslate(state.targetLanguage, state.textToTranslate)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = civicColors.accent, contentColor = civicColors.bg)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Download ${state.targetLanguage} Model (${state.approxSizeMb} MB)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            is ScanUiState.Error -> {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "OCR & Translation Error",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = state.reason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = civicColors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    onLightClick()
                                    imagePickerLauncher.launch("image/*")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retake Photo", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            if (editableText.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        onLightClick()
                                        executeTranslation(editableText, selectedLanguage)
                                    }
                                ) {
                                    Text("Retry Translation", color = civicColors.textPrimary)
                                }
                            }
                        }
                    }
                }
            }

            is ScanUiState.Success -> {
                val res = state.result

                // Fraud Risk Meter
                AnimatedFraudRiskMeter(
                    hasExecuted = res.fraudCheck.hasExecuted,
                    riskLevel = res.fraudCheck.riskLevel,
                    reasons = res.fraudCheck.reasons
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Copy / Share / Speak Actions Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = civicColors.accent.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.accent)
                    ) {
                        Text(
                            text = "Translated on-device via ${res.translationEngine}",
                            style = MaterialTheme.typography.labelSmall,
                            color = civicColors.accent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Row {
                        IconButton(onClick = {
                            onLightClick()
                            clipboardManager.setText(AnnotatedString(res.translatedText))
                        }) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = civicColors.accent)
                        }

                        IconButton(onClick = {
                            onLightClick()
                            onSpeakText(res.translatedText)
                        }) {
                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Read Aloud", tint = civicColors.accent)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Split View / Single View
                if (isSplitView) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = civicColors.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.hairline),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Original OCR Text", style = MaterialTheme.typography.labelLarge, color = civicColors.accent, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = res.originalText, style = MaterialTheme.typography.bodyMedium, color = civicColors.textPrimary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = civicColors.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.hairline),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Translated (${res.targetLanguage})", style = MaterialTheme.typography.labelLarge, color = civicColors.accent, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = res.translatedText, style = MaterialTheme.typography.bodyMedium, color = civicColors.textPrimary)
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = civicColors.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.hairline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Translated Notice (${res.targetLanguage})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = civicColors.accent
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = res.translatedText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = civicColors.textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Jargon Tap-to-Explain Chips
                if (res.jargonGlossary.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = civicColors.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.hairline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Legal & Municipal Jargon Explainer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = civicColors.accent
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(res.jargonGlossary) { jargon ->
                                    FilterChip(
                                        selected = activeJargonDefinition?.first == jargon.term,
                                        onClick = {
                                            onLightClick()
                                            activeJargonDefinition = jargon.term to jargon.plainMeaning
                                        },
                                        label = { Text(jargon.term) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = civicColors.accent,
                                            selectedLabelColor = civicColors.bg
                                        )
                                    )
                                }
                            }

                            activeJargonDefinition?.let { (term, definition) ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = civicColors.surfaceElevated,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.accent)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = term, style = MaterialTheme.typography.titleMedium, color = civicColors.accent, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = definition, style = MaterialTheme.typography.bodyMedium, color = civicColors.textPrimary)
                                    }
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
fun AnimatedFraudRiskMeter(
    hasExecuted: Boolean,
    riskLevel: String,
    reasons: List<String>
) {
    val civicColors = LocalCivicColors.current

    val (badgeColor, riskText) = if (!hasExecuted) {
        Color(0xFF9CA3AF) to "Not checked"
    } else {
        when (riskLevel.uppercase()) {
            "LOW RISK", "LOW" -> Color(0xFF10B981) to "Low Risk"
            "MEDIUM RISK", "MEDIUM" -> Color(0xFFF59E0B) to "Medium Risk"
            "HIGH RISK", "HIGH" -> Color(0xFFEF4444) to "High Risk"
            else -> Color(0xFF10B981) to "Low Risk"
        }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = civicColors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, civicColors.hairline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Notice Verification & Risk Assessment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = civicColors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor),
                    modifier = Modifier.defaultMinSize(minWidth = 72.dp)
                ) {
                    Text(
                        text = riskText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        softWrap = false,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            val displayReasons = if (!hasExecuted) listOf("Fraud & scam check was not performed on this notice.") else reasons
            if (displayReasons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                displayReasons.forEach { reason ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text("• ", color = badgeColor, fontWeight = FontWeight.Bold)
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = civicColors.textSecondary
                        )
                    }
                }
            }
        }
    }
}


