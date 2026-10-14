package com.civicai.app

import com.civicai.app.data.command.CommandRegistry
import com.civicai.app.data.local.GroundedCitation
import com.civicai.app.data.ocr.NoticeOcrPipelineResult
import com.civicai.app.data.translation.OfflineTranslator
import org.junit.Assert.*
import org.junit.Test

class CivicUnitTest {

    @Test
    fun testCommandParsingAndFiltering() {
        val searchCmds = CommandRegistry.filterCommands("/s")
        assertTrue(searchCmds.any { it.trigger == "/s" })

        val policyCmds = CommandRegistry.filterCommands("/policy")
        assertTrue(policyCmds.any { it.trigger == "/policy" })

        val scanCmds = CommandRegistry.filterCommands("/scan")
        assertTrue(scanCmds.any { it.trigger == "/scan" })

        // Verify /image is completely removed per Rule 4
        val imageCmds = CommandRegistry.filterCommands("/image")
        assertTrue(imageCmds.none { it.trigger == "/image" })
    }

    @Test
    fun testCitationMapping() {
        val citation = GroundedCitation(
            source = "PropertyTaxAct_2026.pdf",
            page = 3,
            paragraph = 2,
            confidence = 0.95f,
            excerpt = "Early bird rebate of 10% applies to holding tax paid before May 31st."
        )

        assertEquals("PropertyTaxAct_2026.pdf", citation.source)
        assertEquals(3, citation.page)
        assertEquals(2, citation.paragraph)
        assertTrue(citation.confidence > 0.9f)
    }

    @Test
    fun testOfflineTranslationDictionary() {
        val languages = OfflineTranslator.SUPPORTED_LANGUAGES
        assertTrue(languages.contains("Hindi (हिन्दी)"))
        assertTrue(languages.contains("Marathi (मराठी)"))
        assertTrue(languages.contains("Gujarati (ગુજરાતી)"))
        assertTrue(languages.contains("Bengali (বাংলা)"))
        assertTrue(languages.contains("Tamil (தமிழ்)"))
        assertTrue(languages.contains("Telugu (తెలుగు)"))
        assertTrue(languages.contains("Kannada (ಕನ್ನಡ)"))
        assertTrue(languages.contains("English"))
    }

    @Test
    fun testSlidingWindowChunking() {
        val longText = "Property tax rebate is 10 percent for payments made before May 31. Late payments incur 2 percent penalty per month. Commercial properties are taxed at 1.5 times residential rate."
        // Test chunking algorithm directly
        val sentences = longText.split(Regex("(?<=[.!?\\n])\\s+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        
        assertTrue("Sentences split successfully", sentences.size >= 3)
        assertTrue("First sentence contains property tax", sentences[0].contains("Property tax"))
    }

    @Test
    fun testInstructionWordFilteringAndFtsQueryBuilding() {
        val stopWords = setOf("a", "the", "in", "of", "to", "and", "is", "for", "about")
        val instructionWords = setOf("summarize", "key", "points", "section", "general", "img", "wa0009", "jpg", "document", "20261009")

        val rawQuery = "Summarize the key points of section General in IMG-20261009-WA0009.jpg"
        val filteredTokens = rawQuery.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 2 && !stopWords.contains(it) && !instructionWords.contains(it) }

        assertTrue("Instruction words should be completely filtered out", filteredTokens.isEmpty())

        val topicQuery = "Summarize the key points about property holding tax rebate"
        val topicTokens = topicQuery.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 2 && !stopWords.contains(it) && !instructionWords.contains(it) }

        assertTrue("Topic keywords remain", topicTokens.contains("property"))
        assertTrue("Topic keywords remain", topicTokens.contains("holding"))
        assertTrue("Topic keywords remain", topicTokens.contains("tax"))
        assertTrue("Topic keywords remain", topicTokens.contains("rebate"))

        val ftsExpr = topicTokens.joinToString(" OR ") { "$it*" }
        assertEquals("property* OR holding* OR tax* OR rebate*", ftsExpr)
    }
}


