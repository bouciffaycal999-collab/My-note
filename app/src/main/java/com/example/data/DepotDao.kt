package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DepotDao {

    // --- Delivery Notes (multi-article) ---

    @Transaction
    @Query(
        """
        SELECT * FROM delivery_notes
        WHERE completed = 0
        ORDER BY urgent DESC, waitingSinceMs ASC, createdAtMs DESC
        """
    )
    fun observePendingDeliveryNotes(): Flow<List<DeliveryNoteWithArticles>>

    @Transaction
    @Query("SELECT * FROM delivery_notes WHERE id = :noteId")
    suspend fun getDeliveryNoteWithArticles(noteId: Long): DeliveryNoteWithArticles?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveryNote(note: DeliveryNoteEntity): Long

    @Update
    suspend fun updateDeliveryNote(note: DeliveryNoteEntity)

    @Query("DELETE FROM delivery_notes WHERE id = :noteId")
    suspend fun deleteDeliveryNoteById(noteId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveryArticles(articles: List<DeliveryArticleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveryArticle(article: DeliveryArticleEntity): Long

    @Query("DELETE FROM delivery_articles WHERE noteId = :noteId")
    suspend fun deleteArticlesForNote(noteId: Long)

    @Query("UPDATE delivery_articles SET delivered = :delivered, deliveredAtMs = :deliveredAtMs WHERE id = :articleId")
    suspend fun updateArticleDelivered(articleId: Long, delivered: Boolean, deliveredAtMs: Long?)

    @Query("SELECT COUNT(*) FROM delivery_articles WHERE noteId = :noteId AND delivered = 0")
    suspend fun countPendingArticlesForNote(noteId: Long): Int

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM delivery_articles WHERE noteId = :noteId")
    suspend fun getMaxArticleSortOrder(noteId: Long): Int

    @Query("UPDATE delivery_notes SET completed = :completed, updatedAtMs = :updatedAtMs WHERE id = :noteId")
    suspend fun updateNoteCompleted(noteId: Long, completed: Boolean, updatedAtMs: Long)

    @Query("UPDATE delivery_notes SET waitingSinceMs = :waitingSinceMs, urgent = :urgent, updatedAtMs = :updatedAtMs WHERE id = :noteId")
    suspend fun updateNoteWaitingInfo(noteId: Long, waitingSinceMs: Long, urgent: Boolean, updatedAtMs: Long)

    @Transaction
    suspend fun createDeliveryNoteWithArticles(
        clientName: String,
        clientPhone: String,
        clientAddress: String,
        waitingSinceMs: Long,
        urgent: Boolean,
        articleTexts: List<String>
    ): Long {
        val now = System.currentTimeMillis()
        val noteId = insertDeliveryNote(
            DeliveryNoteEntity(
                clientName = clientName.trim(),
                clientPhone = clientPhone.trim(),
                clientAddress = clientAddress.trim(),
                waitingSinceMs = waitingSinceMs,
                urgent = urgent,
                completed = false,
                createdAtMs = now,
                updatedAtMs = now
            )
        )
        val entities = articleTexts.mapIndexed { idx, text ->
            DeliveryArticleEntity(
                noteId = noteId,
                text = text.trim(),
                delivered = false,
                deliveredAtMs = null,
                sortOrder = idx
            )
        }
        insertDeliveryArticles(entities)
        return noteId
    }

    @Transaction
    suspend fun replaceDeliveryNoteWithArticles(
        noteId: Long,
        clientName: String,
        clientPhone: String,
        clientAddress: String,
        waitingSinceMs: Long,
        urgent: Boolean,
        articles: List<DeliveryArticleEntity>
    ) {
        val existing = getDeliveryNoteWithArticles(noteId)?.note ?: return
        val now = System.currentTimeMillis()
        val allDelivered = articles.isNotEmpty() && articles.all { it.delivered }
        updateDeliveryNote(
            existing.copy(
                clientName = clientName.trim(),
                clientPhone = clientPhone.trim(),
                clientAddress = clientAddress.trim(),
                waitingSinceMs = waitingSinceMs,
                urgent = urgent,
                completed = allDelivered,
                updatedAtMs = now
            )
        )
        deleteArticlesForNote(noteId)
        insertDeliveryArticles(
            articles.mapIndexed { idx, article ->
                article.copy(
                    id = 0L,
                    noteId = noteId,
                    text = article.text.trim(),
                    sortOrder = idx
                )
            }
        )
    }

    @Transaction
    suspend fun appendArticleToNote(noteId: Long, articleText: String) {
        val now = System.currentTimeMillis()
        val nextOrder = getMaxArticleSortOrder(noteId) + 1
        insertDeliveryArticle(
            DeliveryArticleEntity(
                noteId = noteId,
                text = articleText.trim(),
                delivered = false,
                deliveredAtMs = null,
                sortOrder = nextOrder
            )
        )
        updateNoteCompleted(noteId = noteId, completed = false, updatedAtMs = now)
    }

    @Transaction
    suspend fun markArticleDeliveredAndLog(
        note: DeliveryNoteEntity,
        article: DeliveryArticleEntity
    ): UndoAction.UndoDeliveryArticle {
        val now = System.currentTimeMillis()
        updateArticleDelivered(articleId = article.id, delivered = true, deliveredAtMs = now)
        val remaining = countPendingArticlesForNote(note.id)
        val nowCompleted = remaining == 0
        updateNoteCompleted(noteId = note.id, completed = nowCompleted, updatedAtMs = now)

        val journalId = insertJournalEntry(
            JournalEntryEntity(
                entryType = JournalEntryType.DELIVERY.name,
                articleText = article.text,
                quantityNote = "",
                clientName = note.clientName,
                clientPhone = note.clientPhone,
                clientAddress = note.clientAddress,
                sourceNoteId = note.id,
                sourceArticleId = article.id,
                dayKey = formatDayKey(now),
                createdAtMs = now
            )
        )
        return UndoAction.UndoDeliveryArticle(
            noteId = note.id,
            articleId = article.id,
            previousNoteCompleted = note.completed,
            journalEntryId = journalId
        )
    }

    @Transaction
    suspend fun undoArticleDelivered(undo: UndoAction.UndoDeliveryArticle) {
        val now = System.currentTimeMillis()
        updateArticleDelivered(articleId = undo.articleId, delivered = false, deliveredAtMs = null)
        updateNoteCompleted(noteId = undo.noteId, completed = undo.previousNoteCompleted, updatedAtMs = now)
        deleteJournalEntryById(undo.journalEntryId)
    }

    // --- Restock Notes ---

    @Query(
        """
        SELECT * FROM restock_notes
        WHERE purchased = 0
        ORDER BY urgent DESC, createdAtMs DESC
        """
    )
    fun observePendingRestockNotes(): Flow<List<RestockNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestockNote(note: RestockNoteEntity): Long

    @Query(
        """
        UPDATE restock_notes
        SET articleName = :articleName, quantityNote = :quantityNote, urgent = :urgent, updatedAtMs = :updatedAtMs
        WHERE id = :id
        """
    )
    suspend fun updateRestockFields(
        id: Long,
        articleName: String,
        quantityNote: String,
        urgent: Boolean,
        updatedAtMs: Long
    )

    @Query("UPDATE restock_notes SET purchased = :purchased, updatedAtMs = :updatedAtMs WHERE id = :id")
    suspend fun updateRestockPurchased(id: Long, purchased: Boolean, updatedAtMs: Long)

    @Query("DELETE FROM restock_notes WHERE id = :id")
    suspend fun deleteRestockNoteById(id: Long)

    @Transaction
    suspend fun markRestockPurchasedAndLog(note: RestockNoteEntity): UndoAction.UndoRestockPurchase {
        val now = System.currentTimeMillis()
        updateRestockPurchased(id = note.id, purchased = true, updatedAtMs = now)
        val journalId = insertJournalEntry(
            JournalEntryEntity(
                entryType = JournalEntryType.PURCHASE.name,
                articleText = note.articleName,
                quantityNote = note.quantityNote,
                clientName = "",
                clientPhone = "",
                clientAddress = "",
                sourceNoteId = note.id,
                sourceArticleId = 0L,
                dayKey = formatDayKey(now),
                createdAtMs = now
            )
        )
        return UndoAction.UndoRestockPurchase(
            restockId = note.id,
            journalEntryId = journalId
        )
    }

    @Transaction
    suspend fun undoRestockPurchased(undo: UndoAction.UndoRestockPurchase) {
        val now = System.currentTimeMillis()
        updateRestockPurchased(id = undo.restockId, purchased = false, updatedAtMs = now)
        deleteJournalEntryById(undo.journalEntryId)
    }

    // --- Journal Entries ---

    @Query("SELECT * FROM journal_entries ORDER BY createdAtMs DESC")
    fun observeJournalEntries(): Flow<List<JournalEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntryEntity): Long

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteJournalEntryById(id: Long)
}
