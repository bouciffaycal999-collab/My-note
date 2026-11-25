package com.example.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Entity(tableName = "delivery_notes")
data class DeliveryNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val clientName: String = "",
    val clientPhone: String = "",
    val clientAddress: String = "",
    val waitingSinceMs: Long = System.currentTimeMillis(),
    val urgent: Boolean = false,
    val completed: Boolean = false,
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "delivery_articles",
    foreignKeys = [
        ForeignKey(
            entity = DeliveryNoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["noteId"])]
)
data class DeliveryArticleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val noteId: Long = 0L,
    val text: String = "",
    val delivered: Boolean = false,
    val deliveredAtMs: Long? = null,
    val sortOrder: Int = 0
)

data class DeliveryNoteWithArticles(
    @Embedded val note: DeliveryNoteEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val articles: List<DeliveryArticleEntity>
) {
    val orderedArticles: List<DeliveryArticleEntity>
        get() = articles.sortedWith(compareBy<DeliveryArticleEntity> { it.delivered }.thenBy { it.sortOrder }.thenBy { it.id })

    val pendingArticles: List<DeliveryArticleEntity>
        get() = articles.filter { !it.delivered }.sortedWith(compareBy<DeliveryArticleEntity> { it.sortOrder }.thenBy { it.id })

    val deliveredArticles: List<DeliveryArticleEntity>
        get() = articles.filter { it.delivered }.sortedWith(compareBy<DeliveryArticleEntity> { it.sortOrder }.thenBy { it.id })

    val pendingCount: Int get() = articles.count { !it.delivered }
    val deliveredCount: Int get() = articles.count { it.delivered }
    val totalCount: Int get() = articles.size

    val displayClientTitle: String
        get() = when {
            note.clientName.isNotBlank() -> note.clientName.trim()
            note.clientPhone.isNotBlank() -> note.clientPhone.trim()
            note.clientAddress.isNotBlank() -> note.clientAddress.trim()
            else -> "Client sans nom"
        }

    fun waitingDays(nowMs: Long = System.currentTimeMillis()): Int =
        calculateWaitingDays(note.waitingSinceMs, nowMs)
}

@Entity(tableName = "restock_notes")
data class RestockNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val articleName: String = "",
    val quantityNote: String = "",
    val urgent: Boolean = false,
    val purchased: Boolean = false,
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
)

enum class JournalEntryType {
    DELIVERY,
    PURCHASE;

    companion object {
        fun fromRaw(raw: String?): JournalEntryType =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: DELIVERY
    }
}

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val entryType: String = JournalEntryType.DELIVERY.name,
    val articleText: String = "",
    val quantityNote: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val clientAddress: String = "",
    val sourceNoteId: Long = 0L,
    val sourceArticleId: Long = 0L,
    val dayKey: String = "",
    val createdAtMs: Long = System.currentTimeMillis()
) {
    val type: JournalEntryType get() = JournalEntryType.fromRaw(entryType)
}

data class JournalDayGroup(
    val dayKey: String,
    val label: String,
    val entries: List<JournalEntryEntity>
)

sealed interface UndoAction {
    data class UndoDeliveryArticle(
        val noteId: Long,
        val articleId: Long,
        val previousNoteCompleted: Boolean,
        val journalEntryId: Long
    ) : UndoAction

    data class UndoRestockPurchase(
        val restockId: Long,
        val journalEntryId: Long
    ) : UndoAction
}

/**
 * Calculates whole calendar days elapsed since [waitingSinceMs] relative to [nowMs].
 * Returns 0 if waiting started today (or in the future).
 */
fun calculateWaitingDays(waitingSinceMs: Long, nowMs: Long = System.currentTimeMillis()): Int {
    val startCal = Calendar.getInstance().apply {
        timeInMillis = waitingSinceMs
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val nowCal = Calendar.getInstance().apply {
        timeInMillis = nowMs
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val diffMs = nowCal.timeInMillis - startCal.timeInMillis
    if (diffMs <= 0L) return 0
    return ((diffMs + 43_200_000L) / 86_400_000L).toInt().coerceAtLeast(0)
}

fun formatShortDate(timestampMs: Long, locale: Locale = Locale.FRENCH): String {
    return SimpleDateFormat("dd MMM", locale).format(Date(timestampMs))
}

fun formatFullDate(timestampMs: Long, locale: Locale = Locale.FRENCH): String {
    return SimpleDateFormat("dd MMM yyyy", locale).format(Date(timestampMs))
}

fun formatWaitingBadgeText(waitingSinceMs: Long, nowMs: Long = System.currentTimeMillis()): String {
    val days = calculateWaitingDays(waitingSinceMs, nowMs)
    val shortDate = formatShortDate(waitingSinceMs)
    return when (days) {
        0 -> "Attend · Aujourd'hui ($shortDate)"
        1 -> "Attend depuis $shortDate · 1 jour"
        else -> "Attend depuis $shortDate · $days jours"
    }
}

fun formatDayKey(timestampMs: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(timestampMs))
}

fun formatTimeHm(timestampMs: Long): String {
    return SimpleDateFormat("HH:mm", Locale.FRENCH).format(Date(timestampMs))
}

fun formatDayHeader(
    dayKey: String,
    todayKey: String,
    yesterdayKey: String,
    locale: Locale = Locale.FRENCH
): String {
    if (dayKey == todayKey) return "Aujourd'hui"
    if (dayKey == yesterdayKey) return "Hier"
    return try {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dayKey)
        if (parsed != null) {
            SimpleDateFormat("EEEE d MMMM yyyy", locale)
                .format(parsed)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
        } else {
            dayKey
        }
    } catch (_: Exception) {
        dayKey
    }
}

fun groupJournalByDay(
    entries: List<JournalEntryEntity>,
    nowMs: Long = System.currentTimeMillis()
): List<JournalDayGroup> {
    if (entries.isEmpty()) return emptyList()
    val todayKey = formatDayKey(nowMs)
    val yesterdayCal = Calendar.getInstance().apply {
        timeInMillis = nowMs
        add(Calendar.DAY_OF_YEAR, -1)
    }
    val yesterdayKey = formatDayKey(yesterdayCal.timeInMillis)

    return entries
        .groupBy { entry -> entry.dayKey.ifBlank { formatDayKey(entry.createdAtMs) } }
        .entries
        .sortedByDescending { it.key }
        .map { (dayKey, dayEntries) ->
            JournalDayGroup(
                dayKey = dayKey,
                label = formatDayHeader(dayKey, todayKey, yesterdayKey),
                entries = dayEntries.sortedByDescending { it.createdAtMs }
            )
        }
}

fun buildArticleLineText(name: String, qty: String, unit: String): String {
    val cleanName = name.trim()
    val cleanQty = qty.trim()
    val cleanUnit = unit.trim()
    if (cleanName.isEmpty()) return ""
    return buildString {
        append(cleanName)
        if (cleanQty.isNotEmpty()) {
            append(" ")
            append(cleanQty)
            if (cleanUnit.isNotEmpty()) {
                append(" ")
                append(cleanUnit)
            }
        }
    }.trim()
}

fun parseFreeTextArticleLines(raw: String): List<String> {
    return raw
        .lines()
        .map { line -> line.trim().removePrefix("-").removePrefix("•").removePrefix("*").trim() }
        .filter { it.isNotBlank() }
        .take(50)
}
