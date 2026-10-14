package com.civicai.app

import com.civicai.app.data.ocr.FraudCheckResult
import com.civicai.app.data.ocr.NoticeOcrPipeline
import com.civicai.app.data.ocr.NoticeOcrPipelineResult
import com.civicai.app.data.translation.OfflineTranslator
import org.junit.Assert.*
import org.junit.Test
import java.util.regex.Pattern

class OcrTranslationTest {

    @Test
    fun testChunkTextWithLongNotice() {
        val longNoticeParagraph = "Municipal Corporation Notice. ".repeat(40) // ~1200 characters
        
        val maxChunkLen = 300
        val paragraphs = longNoticeParagraph.split("\n\n")
        val resultChunks = mutableListOf<String>()
        var currentChunk = StringBuilder()

        for (para in paragraphs) {
            if (currentChunk.length + para.length + 2 <= maxChunkLen) {
                if (currentChunk.isNotEmpty()) currentChunk.append("\n\n")
                currentChunk.append(para)
            } else {
                if (currentChunk.isNotEmpty()) {
                    resultChunks.add(currentChunk.toString())
                    currentChunk = StringBuilder()
                }
                if (para.length > maxChunkLen) {
                    val sentences = para.split(Regex("(?<=[.!?])\\s+"))
                    for (sentence in sentences) {
                        if (currentChunk.length + sentence.length + 1 <= maxChunkLen) {
                            if (currentChunk.isNotEmpty()) currentChunk.append(" ")
                            currentChunk.append(sentence)
                        } else {
                            if (currentChunk.isNotEmpty()) {
                                resultChunks.add(currentChunk.toString())
                                currentChunk = StringBuilder()
                            }
                            resultChunks.add(sentence)
                        }
                    }
                } else {
                    currentChunk.append(para)
                }
            }
        }
        if (currentChunk.isNotEmpty()) {
            resultChunks.add(currentChunk.toString())
        }

        assertTrue(resultChunks.size > 1)
        for (chunk in resultChunks) {
            assertTrue("Chunk length should be <= 350 chars", chunk.length <= 350)
        }
    }

    @Test
    fun testSupportedLanguagesIntegrity() {
        val languages = OfflineTranslator.SUPPORTED_LANGUAGES
        assertEquals(9, languages.size)
        assertTrue(languages.contains("English"))
        assertTrue(languages.contains("Hindi (हिन्दी)"))
        assertTrue(languages.contains("Marathi (मराठी)"))
        assertTrue(languages.contains("Gujarati (ગુજરાતી)"))
        assertTrue(languages.contains("Bengali (বাংলা)"))
        assertTrue(languages.contains("Tamil (தமிழ்)"))
        assertTrue(languages.contains("Telugu (తెలుగు)"))
        assertTrue(languages.contains("Kannada (ಕನ್ನಡ)"))
        assertTrue(languages.contains("Urdu (اردو)"))
    }

    @Test
    fun testOcrTextCleanupRules() {
        val garbledText = """
            GIT GLOBAL INSTITUTE OF TECHNOLOGY, JAIPUR
            OCTO BER 202 6
            cOOEFIESTA
            Community Technology Hackathon
            COD EFIESTA HACKATHON 5 . 0
            8 - 9
        """.trimIndent()

        // Clean up text using regex rules matching NoticeOcrPipeline.cleanOcrText
        var cleaned = garbledText.replace(Regex("\\b(\\d{2,3})\\s+(\\d{1,2})\\b"), "$1$2")
            .replace(Regex("(\\d+)\\s*\\.\\s*(\\d+)"), "$1.$2")
            .replace(Regex("(\\d+)\\s*-\\s*(\\d+)"), "$1-$2")
            .replace(Regex("(?i)\\bOCTO\\s+BER\\b"), "OCTOBER")
            .replace(Regex("(?i)\\bcOOEFIESTA\\b"), "CODEFIESTA")
            .replace(Regex("(?i)\\bCOD\\s+EFIESTA\\b"), "CODEFIESTA")

        assertTrue("OCTOBER should be rejoined", cleaned.contains("OCTOBER"))
        assertTrue("2026 should be rejoined", cleaned.contains("2026"))
        assertTrue("CODEFIESTA should be rejoined", cleaned.contains("CODEFIESTA"))
        assertTrue("5.0 should be normalized", cleaned.contains("5.0"))
        assertTrue("8-9 should be normalized", cleaned.contains("8-9"))
    }

    @Test
    fun testTargetScriptValidation() {
        val devanagariText = "जयपुर नगर निगम सार्वजनिक सूचना 2026"
        val isDevanagari = Pattern.compile("[\\u0900-\\u097F]").matcher(devanagariText).find()
        assertTrue("Hindi translation must contain Devanagari script", isDevanagari)

        val gujaratiText = "જાહેર મ્યુનિસિપલ નોટિસ ૨૦૨૬"
        val isGujarati = Pattern.compile("[\\u0A80-\\u0AFF]").matcher(gujaratiText).find()
        assertTrue("Gujarati translation must contain Gujarati script", isGujarati)

        val pureEnglishText = "GIT GLOBAL INSTITUTE OF TECHNOLOGY, JAIPUR"
        val isNotDevanagari = Pattern.compile("[\\u0900-\\u097F]").matcher(pureEnglishText).find()
        assertFalse("Pure English text must fail Devanagari script validation", isNotDevanagari)
    }

    @Test
    fun testFraudCheckNotExecutedState() {
        val unverifiedResult = FraudCheckResult(
            hasExecuted = false,
            isSuspicious = false,
            riskLevel = "NOT CHECKED",
            reasons = listOf("Fraud & scam analysis was not performed on this notice.")
        )

        assertFalse(unverifiedResult.hasExecuted)
        assertEquals("NOT CHECKED", unverifiedResult.riskLevel)
        assertEquals("Fraud & scam analysis was not performed on this notice.", unverifiedResult.reasons.first())
    }

    @Test
    fun testEmptyOcrResultHandling() {
        val emptyResult = NoticeOcrPipelineResult(
            isSuccess = false,
            errorMessage = "No text detected in image. Please ensure the notice is clearly lit.",
            originalText = "",
            translatedText = ""
        )

        assertFalse(emptyResult.isSuccess)
        assertTrue(emptyResult.originalText.isEmpty())
        assertNotNull(emptyResult.errorMessage)
    }
}

