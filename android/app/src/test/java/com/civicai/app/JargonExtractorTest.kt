package com.civicai.app

import com.civicai.app.data.jargon.JargonGlossary
import org.junit.Assert.*
import org.junit.Test

class JargonExtractorTest {

    @Test
    fun testJargonExtractionKnownTerms() {
        val sampleText = "Notice of demand: Pay property tax arrears of ₹4,500 by May 31st to claim 10% rebate. Mutation certificate and Khata extract must be produced."
        val matches = JargonGlossary.findMatches(sampleText)

        assertTrue(matches.isNotEmpty())
        val matchedTerms = matches.map { it.term }

        assertTrue(matchedTerms.contains("Demand Notice"))
        assertTrue(matchedTerms.contains("Arrears"))
        assertTrue(matchedTerms.contains("Rebate"))
        assertTrue(matchedTerms.contains("Mutation"))
        assertTrue(matchedTerms.contains("Khata"))
    }

    @Test
    fun testJargonExtractionBuildingPermits() {
        val sampleText = "The proposed building has an FAR of 1.5 with a mandatory front setback of 10 feet. Sanction plan and Occupancy Certificate (OC) required."
        val matches = JargonGlossary.findMatches(sampleText)

        val matchedTerms = matches.map { it.term }
        assertTrue(matchedTerms.contains("FAR"))
        assertTrue(matchedTerms.contains("Setback"))
        assertTrue(matchedTerms.contains("Sanction Plan"))
        assertTrue(matchedTerms.contains("Occupancy Certificate"))
    }

    @Test
    fun testJargonVettedDefinitionsAndExamples() {
        val entry = JargonGlossary.ENTRIES.first { it.term == "Khata" }
        assertEquals("Land Records", entry.category)
        assertTrue(entry.plainDefinition.isNotEmpty())
        assertTrue(entry.example.contains("Khata"))
    }
}
