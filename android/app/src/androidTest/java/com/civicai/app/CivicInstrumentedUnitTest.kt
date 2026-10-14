package com.civicai.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.civicai.app.data.llm.OnDeviceLlmEngine
import com.civicai.app.data.local.AppDatabase
import com.civicai.app.data.local.ChunkEntity
import com.civicai.app.data.ocr.NoticeOcrPipeline
import com.civicai.app.data.search.DuckDuckGoSearchProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class CivicInstrumentedUnitTest {

    @Test
    fun testDocumentIndexingAndGroundedAnswerFlow() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = AppDatabase.getDatabase(appContext)
        val chunkDao = db.chunkDao()

        // 1. Index sample document chunk
        val sampleChunk = ChunkEntity(
            id = UUID.randomUUID().toString(),
            documentId = "doc_test_1",
            documentTitle = "Municipal_Property_Tax_Rules.pdf",
            pageNumber = 2,
            paragraphNumber = 1,
            content = "Property tax early-bird rebate of 10% is applicable for payments submitted before June 30.",
            category = "Property Tax"
        )
        chunkDao.insertChunks(listOf(sampleChunk))

        // 2. Ask grounded question
        val llmEngine = OnDeviceLlmEngine(appContext)
        val response = llmEngine.generateGroundedAnswer("How do I get 10% early bird rebate on property tax?")

        // 3. Verify grounded response and citation mapping
        assertTrue(response.isGrounded)
        assertTrue(response.citations.isNotEmpty())
        assertEquals("Municipal_Property_Tax_Rules.pdf", response.citations[0].source)
        assertEquals(2, response.citations[0].page)
        assertEquals(1, response.citations[0].paragraph)
    }

    @Test
    fun testGroundingRefusalForUnindexedQuery() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = AppDatabase.getDatabase(appContext)
        val chunkDao = db.chunkDao()

        val llmEngine = OnDeviceLlmEngine(appContext)
        val response = llmEngine.generateGroundedAnswer("What is the rocket propulsion formula for interplanetary flight?")

        // Verify strictly grounded refusal
        assertFalse(response.isGrounded)
        assertTrue(response.reply.contains("could not find any information") || response.reply.contains("No documents uploaded"))
    }

    @Test
    fun testNoticeOcrAndTranslationPipeline() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val pipeline = NoticeOcrPipeline(appContext)

        val noticeText = """
            MUNICIPAL CORPORATION DEMAND NOTICE
            Ref: MUN/TAX/2026/9941
            Date: 05/10/2026
            Property Tax Arrears of Rs 4,500 due. Please pay before 31st October to avoid late penalty fee.
            GSTIN: 27AAACM1234F1Z5
        """.trimIndent()

        val result = pipeline.processNoticeText(noticeText, "Hindi (हिन्दी)")

        assertNotNull(result)
        assertEquals("Hindi (हिन्दी)", result.targetLanguage)
        assertTrue(result.threeActionBullets.size == 3)
        assertFalse(result.fraudCheck.isSuspicious)
        assertTrue(result.protectedEntities.any { it.label == "GSTIN" })
    }

    @Test
    fun testSearchProviderOfflineErrorHandling() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val searchProvider = DuckDuckGoSearchProvider(appContext)

        val response = searchProvider.searchWeb("test civic query")
        assertNotNull(response)
    }
}
