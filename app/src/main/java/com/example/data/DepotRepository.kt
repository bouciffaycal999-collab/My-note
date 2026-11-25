package com.example.data

import kotlinx.coroutines.flow.Flow

class DepotRepository(private val dao: DepotDao) {

    fun observePendingDeliveryNotes(): Flow<List<DeliveryNoteWithArticles>> =
        dao.observePendingDeliveryNotes()

    fun observePendingRestockNotes(): Flow<List<RestockNoteEntity>> =
        dao.observePendingRestockNotes()

    fun observeJournalEntries(): Flow<List<JournalEntryEntity>> =
        dao.observeJournalEntries()

    suspend fun createDeliveryNote(
        clientName: String,
        clientPhone: String,
        clientAddress: String,
        waitingSinceMs: Long,
        urgent: Boolean,
        articleTexts: List<String>
    ): Long {
        val clean = articleTexts.map { it.trim() }.filter { it.isNotEmpty() }.take(50)
        require(clean.isNotEmpty()) { "Ajoutez au moins un article à livrer." }
        return dao.createDeliveryNoteWithArticles(
            clientName = clientName,
            clientPhone = clientPhone,
            clientAddress = clientAddress,
            waitingSinceMs = waitingSinceMs,
            urgent = urgent,
            articleTexts = clean
        )
    }

    suspend fun updateDeliveryNote(
        noteId: Long,
        clientName: String,
        clientPhone: String,
        clientAddress: String,
        waitingSinceMs: Long,
        urgent: Boolean,
        articles: List<DeliveryArticleEntity>
    ) {
        val clean = articles.filter { it.text.isNotBlank() }.take(50)
        require(clean.isNotEmpty()) { "La note doit contenir au moins un article." }
        dao.replaceDeliveryNoteWithArticles(
            noteId = noteId,
            clientName = clientName,
            clientPhone = clientPhone,
            clientAddress = clientAddress,
            waitingSinceMs = waitingSinceMs,
            urgent = urgent,
            articles = clean
        )
    }

    suspend fun addArticleToNote(noteId: Long, articleText: String) {
        val clean = articleText.trim()
        require(clean.isNotEmpty()) { "Indiquez le nom de l'article." }
        dao.appendArticleToNote(noteId = noteId, articleText = clean)
    }

    suspend fun updateWaitingInfo(noteId: Long, waitingSinceMs: Long, urgent: Boolean) {
        dao.updateNoteWaitingInfo(
            noteId = noteId,
            waitingSinceMs = waitingSinceMs,
            urgent = urgent,
            updatedAtMs = System.currentTimeMillis()
        )
    }

    suspend fun deleteDeliveryNote(noteId: Long) {
        dao.deleteDeliveryNoteById(noteId)
    }

    suspend fun markArticleDelivered(
        note: DeliveryNoteEntity,
        article: DeliveryArticleEntity
    ): UndoAction.UndoDeliveryArticle = dao.markArticleDeliveredAndLog(note, article)

    suspend fun undoArticleDelivered(undo: UndoAction.UndoDeliveryArticle) {
        dao.undoArticleDelivered(undo)
    }

    suspend fun createRestockNote(
        articleName: String,
        quantityNote: String,
        urgent: Boolean
    ): Long {
        val cleanName = articleName.trim()
        require(cleanName.isNotEmpty()) { "Indiquez le nom de l'article à commander." }
        val now = System.currentTimeMillis()
        return dao.insertRestockNote(
            RestockNoteEntity(
                articleName = cleanName,
                quantityNote = quantityNote.trim(),
                urgent = urgent,
                purchased = false,
                createdAtMs = now,
                updatedAtMs = now
            )
        )
    }

    suspend fun updateRestockNote(
        noteId: Long,
        articleName: String,
        quantityNote: String,
        urgent: Boolean
    ) {
        val cleanName = articleName.trim()
        require(cleanName.isNotEmpty()) { "Indiquez le nom de l'article." }
        dao.updateRestockFields(
            id = noteId,
            articleName = cleanName,
            quantityNote = quantityNote.trim(),
            urgent = urgent,
            updatedAtMs = System.currentTimeMillis()
        )
    }

    suspend fun deleteRestockNote(noteId: Long) {
        dao.deleteRestockNoteById(noteId)
    }

    suspend fun markRestockPurchased(note: RestockNoteEntity): UndoAction.UndoRestockPurchase =
        dao.markRestockPurchasedAndLog(note)

    suspend fun undoRestockPurchased(undo: UndoAction.UndoRestockPurchase) {
        dao.undoRestockPurchased(undo)
    }

    suspend fun deleteJournalEntry(entryId: Long) {
        dao.deleteJournalEntryById(entryId)
    }
}
