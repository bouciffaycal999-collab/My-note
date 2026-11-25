package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class JournalEntryType {
    DELIVERY,
    PURCHASE;

    companion object {
        fun fromRaw(raw: String?): JournalEntryType {
            return entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: DELIVERY
        }
    }
}

@Keep
@IgnoreExtraProperties
data class DeliveryLineItem(
    val lineId: String = "",
    val text: String = "",
    val delivered: Boolean = false,
    val deliveredAtMs: Long = 0L
) {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "lineId" to lineId.ifBlank { UUID.randomUUID().toString().take(12) },
        "text" to text.trim().take(200),
        "delivered" to delivered,
        "deliveredAtMs" to deliveredAtMs
    )
}

@Keep
@IgnoreExtraProperties
data class DeliveryNote(
    @DocumentId val id: String = "",
    val userId: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val clientAddress: String = "",
    val items: List<DeliveryLineItem> = emptyList(),
    val completed: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val deliveredCount: Int
        get() = items.count { it.delivered }

    val pendingCount: Int
        get() = items.count { !it.delivered }

    val displayClientTitle: String
        get() = when {
            clientName.isNotBlank() -> clientName.trim()
            clientPhone.isNotBlank() -> clientPhone.trim()
            clientAddress.isNotBlank() -> clientAddress.trim()
            else -> "Client sans nom"
        }
}

@Keep
@IgnoreExtraProperties
data class RestockNote(
    @DocumentId val id: String = "",
    val userId: String = "",
    val articleName: String = "",
    val quantityNote: String = "",
    val urgent: Boolean = false,
    val purchased: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class JournalEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val entryType: String = JournalEntryType.DELIVERY.name,
    val articleText: String = "",
    val quantityNote: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val clientAddress: String = "",
    val sourceNoteId: String = "",
    val sourceLineId: String = "",
    val dayKey: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    val type: JournalEntryType
        get() = JournalEntryType.fromRaw(entryType)
}

sealed interface UndoAction {
    data class UndoDeliveryLine(
        val noteId: String,
        val previousItems: List<DeliveryLineItem>,
        val previousCompleted: Boolean,
        val journalEntryId: String
    ) : UndoAction

    data class UndoRestockPurchase(
        val noteId: String,
        val journalEntryId: String
    ) : UndoAction
}

data class FeedbackMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isError: Boolean = false,
    val undoAction: UndoAction? = null
)

data class JournalDayGroup(
    val dayKey: String,
    val headerLabel: String,
    val entries: List<JournalEntry>
)

/**
 * Combines an article label, optional quantity, and optional unit into a crisp note line
 * such as "60/60 king mat 18 cart" or "Motif limouzine bleu 12 ps".
 */
fun buildArticleLineText(article: String, quantity: String, unit: String): String {
    val cleanArticle = article.trim().replace(Regex("\\s+"), " ")
    if (cleanArticle.isBlank()) return ""
    val cleanQty = quantity.trim()
    val cleanUnit = unit.trim()
    return buildString {
        append(cleanArticle)
        if (cleanQty.isNotEmpty()) {
            append(' ')
            append(cleanQty)
            if (cleanUnit.isNotEmpty()) {
                append(' ')
                append(cleanUnit)
            }
        }
    }.take(200)
}

/**
 * Splits a multi-line free-text notepad block into clean checkable DeliveryLineItems.
 */
fun parseFreeTextArticleLines(rawText: String): List<DeliveryLineItem> {
    return rawText
        .lineSequence()
        .map { line -> line.trim().removePrefix("-").removePrefix("•").removePrefix("*").trim() }
        .filter { it.isNotBlank() }
        .take(50)
        .map { clean ->
            DeliveryLineItem(
                lineId = UUID.randomUUID().toString().replace("-", "").take(12),
                text = clean.take(200),
                delivered = false,
                deliveredAtMs = 0L
            )
        }
        .toList()
}

fun formatDayKey(date: Date = Date()): String {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return fmt.format(date)
}

fun formatTimeHm(timestamp: Timestamp?): String {
    val date = timestamp?.toDate() ?: return "--:--"
    val fmt = SimpleDateFormat("HH:mm", Locale.FRANCE)
    return fmt.format(date)
}

fun formatShortDate(timestamp: Timestamp?): String {
    val date = timestamp?.toDate() ?: return "À l'instant"
    val todayKey = formatDayKey(Date())
    val itemKey = formatDayKey(date)
    return if (itemKey == todayKey) {
        "Aujourd'hui • ${formatTimeHm(timestamp)}"
    } else {
        val fmt = SimpleDateFormat("dd MMM • HH:mm", Locale.FRANCE)
        fmt.format(date)
    }
}

fun formatDayHeader(
    dayKey: String,
    todayKey: String = formatDayKey(Date()),
    yesterdayKey: String = formatDayKey(
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
    )
): String {
    return when (dayKey) {
        todayKey -> "Aujourd'hui"
        yesterdayKey -> "Hier"
        else -> {
            val parsed = runCatching {
                SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dayKey)
            }.getOrNull()
            if (parsed != null) {
                SimpleDateFormat("EEEE d MMM yyyy", Locale.FRANCE)
                    .format(parsed)
                    .replaceFirstChar { it.titlecase(Locale.FRANCE) }
            } else {
                dayKey.ifBlank { "Autre date" }
            }
        }
    }
}

fun groupJournalByDay(
    entries: List<JournalEntry>,
    todayKey: String = formatDayKey(Date()),
    yesterdayKey: String = formatDayKey(
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
    )
): List<JournalDayGroup> {
    val sorted = entries.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
    val grouped = LinkedHashMap<String, MutableList<JournalEntry>>()
    for (entry in sorted) {
        val key = entry.dayKey.ifBlank {
            entry.createdAt?.toDate()?.let { formatDayKey(it) } ?: todayKey
        }
        grouped.getOrPut(key) { mutableListOf() }.add(entry)
    }
    return grouped.entries
        .sortedByDescending { it.key }
        .map { (key, list) ->
            JournalDayGroup(
                dayKey = key,
                headerLabel = formatDayHeader(key, todayKey, yesterdayKey),
                entries = list
            )
        }
}
