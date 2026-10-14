package com.civicai.app

import com.civicai.app.data.command.CommandRegistry
import org.junit.Assert.*
import org.junit.Test

class CommandRegistryTest {

    @Test
    fun testCommandParsingStandard() {
        val parsed = CommandRegistry.parseCommand("/s property tax rebate")
        assertNotNull(parsed)
        assertEquals("/s", parsed?.first?.name)
        assertEquals("property tax rebate", parsed?.second)
    }

    @Test
    fun testCommandParsingCaseInsensitiveAndLeadingSpaces() {
        val parsed = CommandRegistry.parseCommand("   /S   PROPERTY TAX   ")
        assertNotNull(parsed)
        assertEquals("/s", parsed?.first?.name)
        assertEquals("PROPERTY TAX", parsed?.second)
    }

    @Test
    fun testCommandParsingAliases() {
        val parsedWeb = CommandRegistry.parseCommand("/web water tariff")
        assertNotNull(parsedWeb)
        assertEquals("/s", parsedWeb?.first?.name)

        val parsedSearch = CommandRegistry.parseCommand("/search sanitation rules")
        assertNotNull(parsedSearch)
        assertEquals("/s", parsedSearch?.first?.name)
    }

    @Test
    fun testCommandParsingHindiText() {
        val parsed = CommandRegistry.parseCommand("/s संपत्ति कर छूट नियम 2026")
        assertNotNull(parsed)
        assertEquals("/s", parsed?.first?.name)
        assertEquals("संपत्ति कर छूट नियम 2026", parsed?.second)
    }

    @Test
    fun testCommandParsingQuotesAndSpecialChars() {
        val parsed = CommandRegistry.parseCommand("/jargon \"ad valorem\" tax")
        assertNotNull(parsed)
        assertEquals("/jargon", parsed?.first?.name)
        assertEquals("\"ad valorem\" tax", parsed?.second)
    }

    @Test
    fun testUnknownCommandHandling() {
        val parsed = CommandRegistry.parseCommand("/image sunset over city")
        assertNull("Image command should be rejected as unknown/deleted", parsed)

        val parsedFoo = CommandRegistry.parseCommand("/foo bar")
        assertNull(parsedFoo)
    }

    @Test
    fun testHelpMessageFormatting() {
        val help = CommandRegistry.buildHelpMessage("Commands List:")
        assertTrue(help.contains("/s <query>"))
        assertTrue(help.contains("/docs"))
        assertTrue(help.contains("/summarize"))
        assertTrue(help.contains("/translate"))
        assertTrue(help.contains("/clear"))
        assertTrue(help.contains("/jargon"))
        assertTrue(help.contains("/help"))
    }
}
