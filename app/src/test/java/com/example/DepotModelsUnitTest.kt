package com.example

import com.example.data.DeliveryArticleEntity
import com.example.data.DeliveryNoteEntity
import com.example.data.DeliveryNoteWithArticles
import com.example.data.JournalEntryEntity
import com.example.data.JournalEntryType
import com.example.data.buildArticleLineText
import com.example.data.calculateWaitingDays
import com.example.data.formatWaitingBadgeText
import com.example.data.groupJournalByDay
import com.example.data.parseFreeTextArticleLines
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DepotModelsUnitTest {

    @Test
    fun buildArticleLineText_formatsArticleWithQuantityAndUnit() {
        assertEquals("60/60 king mat 18 cart", buildArticleLineText("60/60 king mat", "18", "cart"))
        assertEquals("Motif limouzine bleu 12 ps", buildArticleLineText("Motif limouzine bleu", "12", "ps"))
        assertEquals("Colle blanche", buildArticleLineText("Colle blanche", "", "sac"))
    }

    @Test
    fun parseFreeTextArticleLines_splitsMultiArticleDepotNotes() {
        val raw = """
            - 60/60 king mat 18 cart
            • Motif limouzine bleu 12 ps
            
            Plinthe 8/60 40 ml
        """.trimIndent()
        val parsed = parseFreeTextArticleLines(raw)
        assertEquals(
            listOf("60/60 king mat 18 cart", "Motif limouzine bleu 12 ps", "Plinthe 8/60 40 ml"),
            parsed
        )
    }

    @Test
    fun calculateWaitingDays_countsExactCalendarDaysWaiting() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 25, 14, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val threeDaysAgo = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, -3)
        }.timeInMillis

        assertEquals(0, calculateWaitingDays(now, now))
        assertEquals(3, calculateWaitingDays(threeDaysAgo, now))
        assertTrue(formatWaitingBadgeText(threeDaysAgo, now).contains("3 jours"))
    }

    @Test
    fun deliveryNoteWithArticles_tracksMultiplePendingAndDeliveredItems() {
        val note = DeliveryNoteWithArticles(
            note = DeliveryNoteEntity(id = 1L, clientName = "Karim"),
            articles = listOf(
                DeliveryArticleEntity(id = 10L, noteId = 1L, text = "60/60 king mat 18 cart", delivered = false, sortOrder = 0),
                DeliveryArticleEntity(id = 11L, noteId = 1L, text = "Motif limouzine bleu 12 ps", delivered = true, sortOrder = 1),
                DeliveryArticleEntity(id = 12L, noteId = 1L, text = "Plinthe 8/60 40 ml", delivered = false, sortOrder = 2)
            )
        )
        assertEquals(2, note.pendingCount)
        assertEquals(1, note.deliveredCount)
        assertEquals(3, note.totalCount)
        assertEquals("Karim", note.displayClientTitle)
    }

    @Test
    fun groupJournalByDay_ordersNewestFirstWithFrenchLabels() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 25, 15, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val entries = listOf(
            JournalEntryEntity(id = 1L, entryType = "DELIVERY", articleText = "A", dayKey = "2026-11-24", createdAtMs = now - 86_400_000L),
            JournalEntryEntity(id = 2L, entryType = "PURCHASE", articleText = "B", dayKey = "2026-11-25", createdAtMs = now)
        )
        val groups = groupJournalByDay(entries, now)
        assertEquals(2, groups.size)
        assertEquals("Aujourd'hui", groups[0].label)
        assertEquals("Hier", groups[1].label)
        assertEquals(JournalEntryType.PURCHASE, groups[0].entries.first().type)
    }
}
