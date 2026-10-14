package com.civicai.app

import org.junit.Assert.*
import org.junit.Test

class VoiceEngineTest {

    private fun cleanTextForSpeech(text: String): String {
        return text
            .replace(Regex("\\[Doc[^\\]]*\\]"), "")
            .replace(Regex("https?://\\S+"), "")
            .replace(Regex("[#*_`~]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    @Test
    fun testCleanTextForSpeechStripsMarkdownAndCitations() {
        val rawText = "### Property Tax Notice\n**Rebate** is 10% [Doc Municipal Act 2026, p.4]. Visit https://civic.gov.in for details."
        val cleaned = cleanTextForSpeech(rawText)

        assertFalse("Markdown headers removed", cleaned.contains("###"))
        assertFalse("Markdown bold removed", cleaned.contains("**"))
        assertFalse("Citation chips removed", cleaned.contains("[Doc"))
        assertFalse("URLs removed", cleaned.contains("https://"))
        assertTrue("Plain text preserved", cleaned.contains("Property Tax Notice"))
        assertTrue("Rebate preserved", cleaned.contains("Rebate is 10%"))
    }
}
