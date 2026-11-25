package com.example

import com.example.data.DeliveryNote
import com.example.data.JournalEntry
import com.example.data.JournalEntryType
import com.example.data.buildArticleLineText
import com.example.data.formatDayHeader
import com.example.data.groupJournalByDay
import com.example.data.parseFreeTextArticleLines
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DepotModelsUnitTest {

    @Test
    fun `buildArticleLineText formats article with quantity and unit cleanly`() {
        assertEquals("60/60 king mat 18 cart", buildArticleLineText("60/60 king mat", "18", "cart"))
        assertEquals("Motif limouzine bleu 12 ps", buildArticleLineText("Motif limouzine bleu", "12", "ps"))
        assertEquals("60/60 king mat 18", buildArticleLineText("60/60 king mat", "18", ""))
        assertEquals("Colle grise 25kg", buildArticleLineText("  Colle   grise 25kg ", "", "cart"))
        assertEquals("", buildArticleLineText("   ", "18", "cart"))
    }

    @Test
    fun `parseFreeTextArticleLines turns multi-line depot note into checkable items`() {
        val raw = """
            - 60/60 king mat 18 cart
            • Motif limouzine bleu 12 ps

            * Plinthe 8/60 20 ps
        """.trimIndent()
        val parsed = parseFreeTextArticleLines(raw)
        assertEquals(3, parsed.size)
        assertEquals("60/60 king mat 18 cart", parsed[0].text)
        assertEquals("Motif limouzine bleu 12 ps", parsed[1].text)
        assertEquals("Plinthe 8/60 20 ps", parsed[2].text)
        assertTrue(parsed.all { !it.delivered && it.lineId.isNotBlank() })
    }

    @Test
    fun `DeliveryNote displayClientTitle gracefully handles optional blank metadata`() {
        assertEquals("Karim", DeliveryNote(clientName = "Karim", clientPhone = "0550112233").displayClientTitle)
        assertEquals("0550112233", DeliveryNote(clientPhone = "0550112233").displayClientTitle)
        assertEquals("Oran Est", DeliveryNote(clientAddress = "Oran Est").displayClientTitle)
        assertEquals("Client sans nom", DeliveryNote().displayClientTitle)
    }

    @Test
    fun `groupJournalByDay groups entries newest day first with Aujourd'hui and Hier labels`() {
        val entries = listOf(
            JournalEntry(id = "e1", entryType = "DELIVERY", articleText = "60/60 king mat 18 cart", dayKey = "2026-11-25", createdAt = Timestamp(1000, 0)),
            JournalEntry(id = "e2", entryType = "PURCHASE", articleText = "Colle grise", dayKey = "2026-11-24", createdAt = Timestamp(500, 0)),
            JournalEntry(id = "e3", entryType = "DELIVERY", articleText = "Motif limouzine bleu 12 ps", dayKey = "2026-11-25", createdAt = Timestamp(2000, 0))
        )
        val groups = groupJournalByDay(entries, todayKey = "2026-11-25", yesterdayKey = "2026-11-24")
        assertEquals(2, groups.size)
        assertEquals("Aujourd'hui", groups[0].headerLabel)
        assertEquals(listOf("e3", "e1"), groups[0].entries.map { it.id })
        assertEquals("Hier", groups[1].headerLabel)
        assertEquals(listOf("e2"), groups[1].entries.map { it.id })
        assertEquals("Aujourd'hui", formatDayHeader("2026-11-25", "2026-11-25", "2026-11-24"))
    }

    @Test
    fun `JournalEntryType fromRaw handles unknown values safely without crashing`() {
        assertEquals(JournalEntryType.DELIVERY, JournalEntryType.fromRaw("DELIVERY"))
        assertEquals(JournalEntryType.PURCHASE, JournalEntryType.fromRaw("purchase"))
        assertEquals(JournalEntryType.DELIVERY, JournalEntryType.fromRaw("UNKNOWN_FUTURE_TYPE"))
        assertFalse(JournalEntryType.fromRaw(null) == JournalEntryType.PURCHASE)
    }
}
